package com.flowdock.plugins.confluence.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;

public class FlowdockConfigurationManager {
	public static final String FLOWDOCK_API_KEY = "ext.flowdock.api.key";
	public static final String FLOWDOCK_API_KEYS = "ext.flowdock.api.keys";
	
	private BandanaManager bandanaManager;
	
	public FlowdockConfigurationManager() {
	}
	
	public List<ApiKeyPair> getFlowdockApiKeys() {
		String propsString = (String)this.bandanaManager.getValue(
				new ConfluenceBandanaContext(),
				FLOWDOCK_API_KEYS);
		Properties props = new Properties();
		InputStream in = new ByteArrayInputStream(propsString.getBytes());
		List<ApiKeyPair> result = new ArrayList<ApiKeyPair>();
		
		try {
			props.load(in);
		} catch (IOException ioe) {} // seems unlikely to happen
		
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			result.add(new ApiKeyPair((String)entry.getKey(), (String)entry.getValue()));
		}
		
		return result;
	}
	
	public void setFlowdockApiKeys(List<ApiKeyPair> apiKeys) {
		Properties props = new Properties();
		
		for (ApiKeyPair pair : apiKeys) {
			props.put(pair.getSpace(), pair.getApiKey());
		}
		
		OutputStream out = new ByteArrayOutputStream();
		try {
			props.store(out, null);
		} catch (IOException ioe) {} // seems unlikely to happen
		
		this.bandanaManager.setValue(new ConfluenceBandanaContext(),
				FLOWDOCK_API_KEYS, out.toString());
	}
	
	// Bean configuration
	
	public void setBandanaManager(BandanaManager manager) {
		this.bandanaManager = manager;
	}
}
