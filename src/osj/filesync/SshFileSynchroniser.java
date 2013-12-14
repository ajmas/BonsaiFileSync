package osj.filesync;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * Synchroniser class that synchronises to an ssh host
 * 
 * @author ajmas
 *
 */
public class SshFileSynchroniser implements FileSynchroniser {

	static final String FILE_PROTOCOL = "file";
	static final String SSH_PROTOCOL = "ssh";

	SyncDirection[] SUPPORTED_DIRECTIONS = new SyncDirection[] { SyncDirection.TO_REMOTE };

	public Set<SyncDirection> getSupportSyncDirections() {
		return new HashSet<FileSynchroniser.SyncDirection>(
				Arrays.asList(SUPPORTED_DIRECTIONS));
	}

	public void syncFiles(FileSyncConfiguration configuration,
			SyncDirection syncDirection, List<String> relativePaths,
			boolean force, PathFilter pathFilter) throws IOException {

		URI localURI = configuration.getLocalConfiguration().getURI();
		if (!FILE_PROTOCOL.equalsIgnoreCase(localURI.getScheme())) {
			throw new RuntimeException(
					"Only URLs of type 'file' are supported by this synchroniser, specified scheme was: "
							+ localURI.getScheme());
		}

		URI remoteURI = configuration.getRemoteConfiguration().getURI();
		if (!SSH_PROTOCOL.equalsIgnoreCase(remoteURI.getScheme())) {
			throw new RuntimeException(
					"Only remote URLs of type 'ssh' are supported by this synchroniser, specified scheme was: "
							+ remoteURI.getScheme());
		}

		if (syncDirection == SyncDirection.TO_REMOTE) {
			visitAndCopyToRemote(configuration.getLocalConfiguration(),
					configuration.getRemoteConfiguration(), force,
					configuration.getPathFilter(), pathFilter);
		} else if (syncDirection == SyncDirection.TO_LOCAL) {
			// visitAndCopy(remoteBasePath, localBasePath, force,
			// configuration.getPathFilter(), pathFilter );
		} else {
			throw new RuntimeException("two way sync is not implemented");
		}

	}

	// ref: http://stackoverflow.com/questions/199624/scp-via-java
	private void visitAndCopyToRemote(EndpointConfiguration localEndpoint,
			EndpointConfiguration remoteEndpoint, boolean force,
			PathFilter... pathFilters) throws IOException {

		File localBasePath = new File(localEndpoint.getURI().getPath());

		List<File> unvisitedFolders = new ArrayList<File>();
		unvisitedFolders.add(localBasePath);

		URI remoteURI = remoteEndpoint.getURI();

		String host = remoteURI.getHost();
		Integer port = remoteURI.getPort();
		if (port == null || port == -1) {
			port = 22;
		}

		Properties configurationProperties = remoteEndpoint
				.getConfigurationProperties();
		String userName = configurationProperties.getProperty("ssh.username");
		String password = configurationProperties.getProperty("ssh.password");

		System.out.println("host: " + host);
		System.out.println("port: " + port);
		System.out.println("userName: " + userName);
		System.out.println("password: " + password);

		Session session = null;
		Channel channel = null;
		try {
			JSch ssh = new JSch();
			JSch.setConfig("StrictHostKeyChecking", "no");
			session = ssh.getSession(userName, host, port);
			session.setPassword(password);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftp = (ChannelSftp) channel;

			File folder = null;
			while (unvisitedFolders.size() > 0
					&& (folder = unvisitedFolders.remove(0)) != null) {
				File[] children = folder.listFiles();
				if (children.length > 0) {
					for (File localFile : children) {
						String path = getPath(localBasePath, localFile);

						// if the path for the folder or file is not accepted
						// continue to next item
						if (!accept(path, pathFilters)) {
							continue;
						}

						String remotePath = remoteURI.getPath() + "/" + path;

						SftpATTRS remoteFileAttr = getRemoteFileAttr(sftp,
								remotePath);
						if (localFile.isDirectory()) {
							unvisitedFolders.add(localFile);
							if (remoteFileAttr == null) {
								System.out.println("mkdir: " + localFile.getAbsolutePath());
								sftp.mkdir(remotePath);
							}
						} else {
							if (force || remoteFileAttr == null || (localFile.lastModified() / 1000) > remoteFileAttr.getMTime()) {
								System.out.println("copy: " + localFile.getAbsolutePath() + " to " + remotePath);
								sftp.put(localFile.getAbsolutePath(), remotePath);
							}
						}
					}
				}

			}

		} catch (JSchException e) {
			System.out.println(userName);
			e.printStackTrace();

		} catch (SftpException e) {
			System.out.println(userName);
			e.printStackTrace();
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}

	}

	private SftpATTRS getRemoteFileAttr(ChannelSftp sftp, String path) {
		try {
			return sftp.lstat(path);
		} catch (SftpException e) {
			//System.err.println(e);
		}
		return null;

	}

	private boolean accept(String path, PathFilter... pathFilters) {
		boolean accept = true;
		for (PathFilter pathFilter : pathFilters) {
			if (pathFilter != null) {
				accept &= pathFilter.accept(path);
			}
		}
		return accept;
	}

	private String getPath(File parentFile, File childFile) {
		String path = childFile.getAbsolutePath().substring(
				parentFile.getAbsolutePath().length());
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}

}
