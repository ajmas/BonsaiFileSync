package osj.filesync;

import java.util.ArrayList;
import java.util.List;

/**
 * General parameters for the file synchronisation. It specifies the local
 * configuration, the remote configuration, and the paths to include
 * and exclude during the sync. The paths are relative to the 'sync base'.
 *  
 * @author ajmas
 *
 */
public class FileSyncConfiguration {

    private EndpointConfiguration remoteConfiguration;
    private EndpointConfiguration localConfiguration;

    private List<String> pathsToInclude = new ArrayList<String>();
    private List<String> pathsToExclude = new ArrayList<String>();

    public EndpointConfiguration getRemoteConfiguration() {
        return remoteConfiguration;
    }

    public void setRemoteConfiguration(EndpointConfiguration remoteConfiguration) {
        this.remoteConfiguration = remoteConfiguration;
    }

    public EndpointConfiguration getLocalConfiguration() {
        return localConfiguration;
    }

    public void setLocalConfiguration(EndpointConfiguration localConfiguration) {
        this.localConfiguration = localConfiguration;
    }

    public List<String> getPathsToInclude() {
        return pathsToInclude;
    }

    public void setPathsToInclude(List<String> pathsToInclude) {
        this.pathsToInclude = pathsToInclude;
    }

    public List<String> getPathsToExclude() {
        return pathsToExclude;
    }

    public void setPathsToExclude(List<String> pathsToExclude) {
        this.pathsToExclude = pathsToExclude;
    }


    public PathFilter getPathFilter() {
        return new FSPathFilter ();
    }

    class FSPathFilter implements PathFilter {

        public boolean accept(String path) {

            if ( pathsToExclude != null && pathsToExclude.size() > 0 ) {
                for (String excludePath : pathsToExclude) {
                    if (path.startsWith(excludePath)) {
                        return false;
                    }
                }
            }


            if ( pathsToInclude != null && pathsToExclude.size() > 0 ) {
                for (String includePath : pathsToInclude) {
                    if (path.startsWith(includePath)) {
                        return true;
                    }
                }

                return false;
            }

            return true;
        }

    }
}
