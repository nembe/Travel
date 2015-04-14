package nl.yellowbrick.travelcard;

import java.nio.file.Path;
import java.util.EventListener;

public interface WhitelistFileWatchListener extends EventListener {

    void fileCreated(Path path);
}
