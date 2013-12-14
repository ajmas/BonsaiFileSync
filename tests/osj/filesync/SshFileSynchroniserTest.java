package osj.filesync;

import org.junit.Test;

public class SshFileSynchroniserTest {

	@Test
	public void basicToRemoteFileCopyTest() throws Exception {
        SshFileSynchroniser fileSync = new SshFileSynchroniser();


        FileSyncConfiguration configuration = new FileSyncConfiguration();
        configuration.setLocalConfiguration(new EndpointConfiguration("file:///tmp/sourceFolder"));
        
        
        EndpointConfiguration remoteConfiguration = new EndpointConfiguration("ssh://" + System.getProperty("user.name") + "@localhost/tmp/destinationFolder");
        remoteConfiguration.getConfigurationProperties().setProperty("ssh.username", System.getProperty("ssh.username"));
        remoteConfiguration.getConfigurationProperties().setProperty("ssh.password", System.getProperty("ssh.password"));
        
        configuration.setRemoteConfiguration(remoteConfiguration);

        fileSync.syncFiles(configuration, FileSynchroniser.SyncDirection.TO_REMOTE, null, false, null);

	}
	
	@Test
	public void basicToLocalFileCopyTest() throws Exception {
        SshFileSynchroniser fileSync = new SshFileSynchroniser();


        FileSyncConfiguration configuration = new FileSyncConfiguration();
        configuration.setLocalConfiguration(new EndpointConfiguration("file:///tmp/sourceFolder"));
        
        
        EndpointConfiguration remoteConfiguration = new EndpointConfiguration("ssh://" + System.getProperty("user.name") + "@localhost/tmp/destinationFolder");
        remoteConfiguration.getConfigurationProperties().setProperty("ssh.username", System.getProperty("ssh.username"));
        remoteConfiguration.getConfigurationProperties().setProperty("ssh.password", System.getProperty("ssh.password"));
        
        configuration.setRemoteConfiguration(remoteConfiguration);

        fileSync.syncFiles(configuration, FileSynchroniser.SyncDirection.TO_LOCAL, null, false, null);

	}

}
