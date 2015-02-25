package nl.yellowbrick.travelcard;

import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class WhitelistFileWatchTest {

    WhitelistFileWatch fileWatch;

    File importDir;
    File existingFile;
    WhitelistFileWatchListener listener;

    @Before
    public void setupTestDirectory() throws IOException {
        importDir = Files.createTempDir();
        listener = mock(WhitelistFileWatchListener.class);

        existingFile = new File(importDir, "existing.csv");
        Files.append("I was already here", existingFile, Charset.forName("UTF-8"));

        fileWatch = new WhitelistFileWatch(importDir.getAbsolutePath(), 1);
        fileWatch.addListeners(Arrays.asList(listener));
        fileWatch.startWatching();
    }

    @After
    public void cleanup() {
        fileWatch.stopWatching();
    }

    @Test
    public void notifies_listeners_of_created_files() throws IOException, InterruptedException {
        addFileToImportDir("new.csv");

        // this is only required for the Mac because the JVM there doesnt natively support watches
        // so instead relies internally on some really slow polling
        // TODO find a reliable alternative to this sleep
        Thread.sleep(10000);

        ArgumentMatcher<Path> isExpectedPath = new ArgumentMatcher<Path>() {
            @Override
            public boolean matches(Object o) {
                Path path = (Path) o;
                return path.equals(importDir.toPath().resolve("new.csv"));
            }
        };

        verify(listener).fileCreated(argThat(isExpectedPath));
    }

    @Test
    public void does_not_notify_listeners_of_updates_or_deletions() throws InterruptedException {
        final boolean deleted = existingFile.delete();

        assertThat(deleted, is(true));

        // TODO find a reliable alternative to this sleep
        Thread.sleep(10000);

        verifyZeroInteractions(listener);
    }

    @Test
    public void is_resilient_to_errors() throws Exception {
        doThrow(RuntimeException.class).when(listener).fileCreated(any());

        addFileToImportDir("test1.csv");
        addFileToImportDir("test2.csv");

        // TODO find a reliable alternative to this sleep
        Thread.sleep(10000);

        verify(listener, times(2)).fileCreated(any());
    }

    private void addFileToImportDir(String filename) throws IOException {
        File newFile = new File(importDir, filename);
        Files.append("something", newFile, Charset.forName("UTF-8"));
    }
}
