package osj.filesync;

import java.util.HashMap;
import java.util.Map;


public class FileSyncFactory {

    Map<String,Class<FileSynchroniser>> fileSynchroniserMap = new HashMap<String,Class<FileSynchroniser>>();

    public FileSynchroniser createSynchroniser(String localProtocol, String remoteProtocol) {

        Class<FileSynchroniser> fileSynchroniserClass = fileSynchroniserMap.get(localProtocol + ":" + remoteProtocol);
        if ( fileSynchroniserClass != null ) {
            try {
                return fileSynchroniserClass.newInstance();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return null;
    }

}
