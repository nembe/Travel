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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class WhitelistFileWatchTest {

    WhitelistFileWatch fileWatch;

    File importDir;
    WhitelistFileWatchListener listener;

    @Before
    public void setupTestDirectory() throws IOException {
        importDir = Files.createTempDir();
        listener = mock(WhitelistFileWatchListener.class);

        fileWatch = new WhitelistFileWatch(importDir.getAbsolutePath(), 1);
        fileWatch.addListeners(Arrays.asList(listener));
    }

    @After
    public void cleanup() {
        fileWatch.stopWatching();
    }

    @Test
    public void notifies_listeners_of_created_files() throws Exception {
        fileWatch.startWatching();

        addFileToImportDir("new.csv");

        // this is only required for the Mac because the JVM there doesnt natively support watches
        // so instead relies internally on some really slow polling
        // TODO find a reliable alternative to this sleep
        Thread.sleep(10000);

        verify(listener).fileCreated(argThat(isExpectedPathTo("new.csv")));
    }

    @Test
    public void is_resilient_to_errors() throws Exception {
        fileWatch.startWatching();

        doThrow(RuntimeException.class).when(listener).fileCreated(any());

        addFileToImportDir("test1.csv");
        addFileToImportDir("test2.csv");

        // TODO find a reliable alternative to this sleep
        Thread.sleep(10000);

        verify(listener, times(2)).fileCreated(any());
    }

    @Test
    public void picks_up_files_already_present_in_input_dir() throws Exception {
        addFileToImportDir("test1.csv");
        addFileToImportDir("test2.csv");

        fileWatch.startWatching();

        // TODO find a reliable alternative to this sleep
        Thread.sleep(1000);

        verify(listener).fileCreated(argThat(isExpectedPathTo("test1.csv")));
        verify(listener).fileCreated(argThat(isExpectedPathTo("test2.csv")));
        verifyNoMoreInteractions(listener);
    }

    private void addFileToImportDir(String filename) throws IOException {
        File newFile = new File(importDir, filename);
        Files.append("something", newFile, Charset.forName("UTF-8"));
    }

    private ArgumentMatcher<Path> isExpectedPathTo(String fileName) {
        return new ArgumentMatcher<Path>() {
            @Override
            public boolean matches(Object o) {
                Path path = (Path) o;
                return path.equals(importDir.toPath().resolve(fileName));
            }
        };
    }
}
