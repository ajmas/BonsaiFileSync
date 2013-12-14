package osj.filesync;

import org.junit.Test;

public class SimpleLocalFileSynchroniserTest {

	@Test
	public void basicToRemoteFileCopyTest() throws Exception {
        SimpleLocalFileSynchroniser fileSync = new SimpleLocalFileSynchroniser();


        FileSyncConfiguration configuration = new FileSyncConfiguration();
        configuration.setLocalConfiguration(new EndpointConfiguration("file:///tmp/sourceFolder"));
        configuration.setRemoteConfiguration(new EndpointConfiguration("file:///tmp/destinationFolder"));

        fileSync.syncFiles(configuration, FileSynchroniser.SyncDirection.TO_REMOTE, null, true, null);

	}
}
