package osj.filesync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * File synchroniser using an external rsync command.
 * 
 * @author ajmas
 *
 */
public class RsyncFileSynchroniser implements FileSynchroniser {

    String rsyncBinaryPath = "/usr/bin/rsync";

    public Set<SyncDirection> getSupportSyncDirections() {
        // TODO Auto-generated method stub
        return null;
    }

    public void syncFiles(FileSyncConfiguration configuration, SyncDirection syncDirection,
            List<String> relativePaths, boolean force, PathFilter pathFilter) throws FileSyncException {

    	OutputStreamLogger outStreamLogger = null;
    	OutputStreamLogger errStreamLogger = null;
    	try {
	        File rsyncBinaryFile = new File(rsyncBinaryPath);
	        if (!rsyncBinaryFile.exists()) {
	            throw new RuntimeException("rsync binary was not located on the file system");
	        }
	
	        String local  = null;
	        String remote = null;
	        
	        URI localURI = configuration.getLocalConfiguration().getURI();
	        
	        URI remoteURI = configuration.getRemoteConfiguration().getURI();
	        
	        if ("file".equalsIgnoreCase(localURI.getScheme())) {
	            local = localURI.getPath();
	        }
	        
	        if (remoteURI.getScheme() == null) {
	            // TODO should be forcing the rsync protocol and then going through this based on config?
	            remote = remoteURI.getPath();
	            int idx = remote.indexOf("/");
	            remote = remote.substring(0,idx) + ":" + remote.substring(idx);
	        } else if ("file".equalsIgnoreCase(remoteURI.getScheme())) {
	            remote = remoteURI.getPath();
	        } else {
	            remote = remoteURI.toString();
	        }
	                
	        String rsyncOptions = "-avzru";
	        String[] rsyncOptionParts = new String[0];
	        
	        Properties configurationProperties = configuration.getRemoteConfiguration().getConfigurationProperties();
	        
	        String password = configurationProperties.getProperty("password");
	                
	        if (rsyncOptions != null && rsyncOptions.trim().length() > 0) {
	            rsyncOptionParts = rsyncOptions.split(" ");
	        }
	
	        List<String> parameterList = new ArrayList<>();
	        parameterList.add(rsyncBinaryPath);
	        for (int i=0; i<rsyncOptionParts.length; i++) {
	            parameterList.add(rsyncOptionParts[i]);
	        }
	        if (password != null) {
	            File passwordFile = writePasswordToFile(password);
	            parameterList.add("--password-file=" + passwordFile.getAbsolutePath());
	        }
	        parameterList.add(local);
	        parameterList.add(remote);
	
	        System.out.println("Executing: " + parameterList);
	
	        // TODO see how we deal with host signatures
	        
	        if (syncDirection == SyncDirection.TO_REMOTE) {
	            Process process = Runtime.getRuntime().exec(parameterList.toArray(new String[0]));
	            try {
	                outStreamLogger = new OutputStreamLogger(process.getInputStream());
	                (new Thread(outStreamLogger)).start();
	                errStreamLogger = new OutputStreamLogger(process.getErrorStream());
	                (new Thread(errStreamLogger)).start();
	                process.waitFor();
	                
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	
	            int exitValue = process.exitValue();
	            if (exitValue != 0) {
	            	System.err.println( errStreamLogger.getOutput());
	            	throw new FileSyncException("Rsync command failed with an error (" + exitValue + ")", errStreamLogger.getOutput());
	            }
	        }
    	} catch (IOException ex) {
    		throw new FileSyncException(ex);
    	}
    }
    
    private File writePasswordToFile(String password) throws IOException {
        File file = File.createTempFile("rsync", ".dat");
        
        System.out.println(file);
        
        OutputStream out = new FileOutputStream(file);
        out.write(password.getBytes(Charset.forName("UTF-8")));
        out.close();
        
        file.deleteOnExit();
        
        return file;
    }
    
    
    static class OutputStreamLogger implements Runnable {
        InputStream in = null;
        StringBuilder strBuilder = new StringBuilder();
        
        OutputStreamLogger(InputStream in) {
            this.in = in;
        }
        
        public void run() {
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(in);
                char[] buffer = new char[1024];
                int len = -1;
                while ((len = reader.read(buffer)) > -1) {
                	strBuilder.append(new String(buffer, 0, len));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        public String getOutput() {
        	return strBuilder.toString();
        }
    }

}
