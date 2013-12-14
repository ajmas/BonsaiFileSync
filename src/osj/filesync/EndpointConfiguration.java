package osj.filesync;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * An end point where files are either found in
 * the local system or the remote system.
 * 
 * @author ajmas
 */
public class EndpointConfiguration {

    URI endpointAddress;
    Properties properties = new Properties();

    public EndpointConfiguration(String urlStr) throws URISyntaxException {
        this.endpointAddress = new URI(urlStr);
    }

    public EndpointConfiguration(URI url) {
        this.endpointAddress = url;
    }

    public URI getURI() {
        return this.endpointAddress;
    }

    public Properties getConfigurationProperties() {
    	return properties;
    }
}
