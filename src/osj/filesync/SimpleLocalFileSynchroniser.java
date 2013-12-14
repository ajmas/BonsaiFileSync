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

    SyncDirection[] SUPPORTED_DIRECTIONS = new SyncDirection[] {
            SyncDirection.TO_LOCAL,
            SyncDirection.TO_REMOTE
    };

    public Set<SyncDirection> getSupportSyncDirections() {
        return new HashSet<FileSynchroniser.SyncDirection>(Arrays.asList(SUPPORTED_DIRECTIONS));
    }

    public void syncFiles(FileSyncConfiguration configuration, SyncDirection syncDirection,
            List<String> relativePaths, boolean force, PathFilter pathFilter) throws IOException {

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

        if (syncDirection == SyncDirection.TO_REMOTE) {
            visitAndCopy(localBasePath, remoteBasePath, force, configuration.getPathFilter(),  pathFilter );
        } else if (syncDirection == SyncDirection.TO_LOCAL) {
            visitAndCopy(remoteBasePath, localBasePath, force, configuration.getPathFilter(),  pathFilter );
        } else {
            throw new RuntimeException("two way sync is not implemented");
        }

    }

    private void visitAndCopy (File localBasePath, File remoteBasePath, boolean force, PathFilter... pathFilters) throws IOException {

        List<File> unvisitedFolders = new ArrayList<File>();
        unvisitedFolders.add(localBasePath);

        File folder = null;
        while(unvisitedFolders.size() > 0 && (folder = unvisitedFolders.remove(0)) != null ) {
            File[] children = folder.listFiles();
            if (children.length > 0 ) {
                for (File localFile : children) {
                    String path = getPath(localBasePath, localFile);

                    // if the path for the folder or file is not accepted continue to next item
                    if (!accept(path, pathFilters)) {
                        continue;
                    }

                    File remoteFile = new File(remoteBasePath,path);

                    if (localFile.isDirectory()) {
                        unvisitedFolders.add(localFile);
                        if (!remoteFile.exists()) {
                            remoteFile.mkdir();
                        }
                    } else {
                        if (force || !remoteFile.exists() || localFile.lastModified() > remoteFile.lastModified()) {
                            Files.copy(localFile.toPath(), remoteFile.toPath(), REPLACE_EXISTING, COPY_ATTRIBUTES);
                        }
                    }
                }
            }

        }

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
