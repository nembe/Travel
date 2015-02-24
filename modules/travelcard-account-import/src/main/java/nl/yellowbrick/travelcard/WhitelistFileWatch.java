package nl.yellowbrick.travelcard;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        this.importDirPath = Paths.get(importDir);
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

        watchService = FileSystems.getDefault().newWatchService();
        importDirPath.register(watchService, new WatchEvent.Kind[] { ENTRY_CREATE }, SensitivityWatchEventModifier.HIGH);

        executorService.execute(() -> {
            while(running) {
                try {
                    WatchKey watchKey = watchService.poll(importDelay, TimeUnit.MILLISECONDS);

                    if(watchKey != null) {
                        List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                        watchEvents.forEach((event) -> notifyListeners((WatchEvent<Path>) event));
                        watchKey.reset();
                    }
                } catch (InterruptedException e) {
                    LOGGER.warn("failed to gracefully shutdown", e);
                } catch (ClosedWatchServiceException e) {
                    LOGGER.debug("tried to access a closed WatchService", e);
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

    private void notifyListeners(WatchEvent<Path> watchEvent) {
        final Path pathToCreatedFile = watchEvent.context();

        listeners.forEach((listener) -> listener.fileCreated(pathToCreatedFile));
    }
}
