package nl.yellowbrick.travelcard.bootstrap;

import com.google.common.io.Files;
import nl.yellowbrick.travelcard.WhitelistFileWatch;
import nl.yellowbrick.travelcard.WhitelistFileWatchListener;
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
        File newFile = new File(importDir, "new.csv");
        Files.append("something", newFile, Charset.forName("UTF-8"));

        // this is only required for the Mac because the JVM there doesnt natively support watches
        // so instead relies internally on some really slow polling
        // TODO find a reliable alternative to this sleep
        Thread.sleep(10000);

        ArgumentMatcher<Path> isExpectedPath = new ArgumentMatcher<Path>() {
            @Override
            public boolean matches(Object o) {
                Path path = (Path) o;
                return path.toString().equals("new.csv");
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
}
