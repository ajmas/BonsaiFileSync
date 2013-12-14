package osj.filesync;

import org.junit.Test;

public class RsyncFileSynchroniserTest {

	@Test
	public void basicToRemoteFileCopyTest() throws Exception {
		RsyncFileSynchroniser rsyncFileSync = new RsyncFileSynchroniser();

		FileSyncConfiguration configuration = new FileSyncConfiguration();
		configuration.setLocalConfiguration(new EndpointConfiguration("file:///tmp/sourceFolder1"));
		configuration.setRemoteConfiguration(new EndpointConfiguration("file:///tmp/destinationFolder2"));

		rsyncFileSync.syncFiles(configuration,
				FileSynchroniser.SyncDirection.TO_REMOTE, null, true, null);

	}

	@Test
	public void basicToRemoteFileCopyTest2() throws Exception {

		RsyncFileSynchroniser rsyncFileSync = new RsyncFileSynchroniser();

		FileSyncConfiguration configuration = new FileSyncConfiguration();
		configuration.setLocalConfiguration(new EndpointConfiguration("file:///tmp/sourceFolder"));

		configuration.setRemoteConfiguration(
				new EndpointConfiguration(System.getProperty("user.name")
						+ "@localhost/tmp/destinationFolder"));

		System.out.println(configuration.getRemoteConfiguration().getURI());
		System.out.println(configuration.getRemoteConfiguration().getURI()
				.getScheme());

		rsyncFileSync.syncFiles(configuration,
				FileSynchroniser.SyncDirection.TO_REMOTE, null, true, null);

	}
}
