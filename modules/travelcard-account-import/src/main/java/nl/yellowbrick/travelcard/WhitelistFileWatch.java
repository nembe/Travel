package nl.yellowbrick.travelcard;

import com.google.common.collect.Queues;
import com.sun.nio.file.SensitivityWatchEventModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

@Component
public class WhitelistFileWatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistFileWatch.class);

    private final Path importDirPath;
    private final int importDelay;
    private final List<WhitelistFileWatchListener> listeners;
    private final ExecutorService executorService;

    private WatchService watchService;
    private boolean running;

    @Autowired
    public WhitelistFileWatch(@Value("${tc.import.dir}") String importDir,
                              @Value("${tc.import.delay}") int importDelay) throws IOException {
        this.importDirPath = Paths.get(importDir).toAbsolutePath();
        this.importDelay = importDelay;
        this.listeners = new ArrayList<>();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Autowired
    public void addListeners(List<WhitelistFileWatchListener> listeners) {
        this.listeners.addAll(listeners);
    }

    @PostConstruct
    public void startWatching() throws IOException {
        running = true;

        final Queue<Path> fileQueue = Queues.newArrayDeque();
        filesInImportDir().forEach(fileQueue::add);

        watchService = FileSystems.getDefault().newWatchService();
        importDirPath.register(watchService, new WatchEvent.Kind[] { ENTRY_CREATE }, SensitivityWatchEventModifier.HIGH);

        executorService.execute(() -> {
            while(running) {
                WatchKey watchKey = null;

                try {
                    while(fileQueue.peek() != null) {
                        notifyListeners(fileQueue.poll());
                    }

                    watchKey = watchService.poll(importDelay, TimeUnit.MILLISECONDS);

                    if(watchKey != null) {
                        watchKey.pollEvents().forEach(event -> {
                            Path fileName = ((WatchEvent<Path>) event).context();
                            fileQueue.add(importDirPath.resolve(fileName));
                        });
                    }
                } catch (InterruptedException e) {
                    LOGGER.warn("failed to gracefully shutdown", e);
                } catch (ClosedWatchServiceException e) {
                    LOGGER.debug("tried to access a closed WatchService", e);
                } catch (Exception e) {
                    LOGGER.error("got unhandled exception", e);
                } finally {
                    if(watchKey != null)
                        watchKey.reset();
                }
            }
        });
    }

    @PreDestroy
    public void stopWatching() {
        running = false;

        try {
            watchService.close();
        } catch (IOException e) {
            LOGGER.warn("failed to gracefully shutdown WatchService", e);
        }
    }

    private Stream<Path> filesInImportDir() {
        try {
            return Files.list(importDirPath).filter(path -> !Files.isDirectory(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read import dir " + importDirPath.toString(), e);
        }
    }

    private void notifyListeners(Path path) {
        listeners.forEach((listener) -> listener.fileCreated(path));
    }
}
