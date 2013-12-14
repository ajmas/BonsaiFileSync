package osj.filesync;

import java.io.IOException;
import java.util.List;
import java.util.Set;


public interface FileSynchroniser {

    enum SyncDirection {
        TO_REMOTE,
        TO_LOCAL,
        TWO_WAY
    }

    public Set<SyncDirection> getSupportSyncDirections();

    public void syncFiles(FileSyncConfiguration configuration, SyncDirection syncDirection, List<String> relativePaths, boolean force, PathFilter pathFilter) throws FileSyncException, IOException;

}
