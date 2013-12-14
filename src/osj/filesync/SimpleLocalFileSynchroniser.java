package osj.filesync;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Simple file synchroniser for synchronising files across the
 * local file system. It decides whether the files need copying
 * based on last modified time.
 * 
 * It supports synchronising files to the remote location or the
 * local location, but not both at the same time.
 *  
 * @author ajmas
 *
 */
public class SimpleLocalFileSynchroniser implements FileSynchroniser {

    static final String FILE_PROTOCOL = "file";

    Logger  logger = Logger.getLogger(getClass());
    
    SyncDirection[] SUPPORTED_DIRECTIONS = new SyncDirection[] {
            SyncDirection.TO_LOCAL,
            SyncDirection.TO_REMOTE
    };

    public Set<SyncDirection> getSupportSyncDirections() {
        return new HashSet<FileSynchroniser.SyncDirection>(Arrays.asList(SUPPORTED_DIRECTIONS));
    }

    public void syncFiles(FileSyncConfiguration configuration, SyncDirection syncDirection,
            List<String> relativePaths, boolean force, PathFilter pathFilter) throws FileSyncException {

        URI localURI = configuration.getLocalConfiguration().getURI();
        if ( !FILE_PROTOCOL.equalsIgnoreCase(localURI.getScheme())) {
            throw new RuntimeException("Only URLs of type 'file' are supported by this synchroniser");
        }

        URI remoteURI = configuration.getRemoteConfiguration().getURI();
        if ( !FILE_PROTOCOL.equalsIgnoreCase(remoteURI.getScheme())) {
            throw new RuntimeException("Only URLs of type 'file' are supported by this synchroniser");
        }

        File localBasePath = new File(localURI.getPath());
        File remoteBasePath = new File(remoteURI.getPath());

        try {
	        if (syncDirection == SyncDirection.TO_REMOTE) {
	            visitAndCopy(localBasePath, remoteBasePath, force, configuration.getPathFilter(),  pathFilter );
	        } else if (syncDirection == SyncDirection.TO_LOCAL) {
	            visitAndCopy(remoteBasePath, localBasePath, force, configuration.getPathFilter(),  pathFilter );
	        } else {
	            throw new RuntimeException("two way sync is not implemented");
	        }
        } catch (IOException ex) {
        	throw new FileSyncException(ex);
        }
    }

    private void visitAndCopy (File sourceBasePath, File destinationBasePath, boolean force, PathFilter... pathFilters) throws FileSyncException, IOException {

        List<File> unvisitedFolders = new ArrayList<File>();
        unvisitedFolders.add(sourceBasePath);
        
        File folder = null;
        while(unvisitedFolders.size() > 0 && (folder = unvisitedFolders.remove(0)) != null ) {

        	// INFO Handle deletion of resources on the destination file system, which are no longer
        	//      source file system
            File destinationFolder = new File(destinationBasePath, getPath(sourceBasePath,folder));
            if (destinationFolder.exists()) {
            	File[] children = destinationFolder.listFiles();
            	for (File remoteFile : children) {
            		File localFile = new File(sourceBasePath, getPath(destinationBasePath, remoteFile));
            		if (!localFile.exists()) {
                    	logger.debug("Deleting " + remoteFile);
            			deleteFile(remoteFile);
            		}
            	}
            }
            
            // INFO Handle creation and update of remote resources
            File[] children = folder.listFiles();
            if (children.length > 0 ) {
                for (File localFile : children) {
                    String path = getPath(sourceBasePath, localFile);

                    // INFO if the path for the folder or file is not accepted continue to next item
                    if (!accept(path, pathFilters)) {
                        continue;
                    }

                    File destinationFile = new File(destinationBasePath,path);

                    if (localFile.isDirectory()) {
                        unvisitedFolders.add(localFile);
                        if (!destinationFile.exists()) {
                        	logger.debug("Creating remote directory " + destinationFile);
                            destinationFile.mkdir();
                        }
                    } else {
                        if (force || !destinationFile.exists() || localFile.lastModified() > destinationFile.lastModified()) {
                        	logger.debug("Copying " + localFile);
                            Files.copy(localFile.toPath(), destinationFile.toPath(), REPLACE_EXISTING, COPY_ATTRIBUTES);
                        }
                    }
                }
            }

        }

    }
    
    /**
     * Recursive file deletion operation, since we can't delete a directory until all 
     * its children have been deleted.
     * 
     * @param file
     * @return
     */
    private boolean deleteFile (File file) throws FileSyncException {
    	if (file.isDirectory()) {
    		File[] children = file.listFiles();
    		if (file.list().length > 0) {
    			for (File child : children) {
    				deleteFile(child);
    			}
    		}
    	}    	
    	
    	if (!file.delete()) {
			throw new FileSyncException("Unable to delete file " + file);
    	}
    	
    	return true;
    }

    private boolean accept (String path, PathFilter... pathFilters) {
        boolean accept = true;
        for (PathFilter pathFilter : pathFilters) {
            if (pathFilter != null) {
                accept &= pathFilter.accept(path);
            }
        }
        return accept;
    }

    private String getPath(File parentFile, File childFile) {
        String path = childFile.getAbsolutePath().substring(parentFile.getAbsolutePath().length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }


}
