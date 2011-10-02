package com.flowdock.plugins.confluence.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;

public class FlowdockConfigurationManager {
	public static final String FLOWDOCK_API_KEYS = "ext.flowdock.api.keys";
	
	private BandanaManager bandanaManager;
	private SpaceManager spaceManager;
	
	public FlowdockConfigurationManager() {
	}
	
	/**
	 * Returns a Flowdock ApiKeyPair for each Space in the system.
	 * 
	 * {@link ApiKeyPair#getApiKey()} might be null, if it hasn't been
	 * configured for the given space..
	 * 
	 * @return
	 */
	public List<ApiKeyPair> getFlowdockApiKeys() {
		List<ApiKeyPair> result = new ArrayList<ApiKeyPair>();
		Properties props = this.readApiKeyPropertiesObj();
		
		List<Space> spaces = this.spaceManager.getAllSpaces();
		for (Space space : spaces) {	
			String apiKey = props.getProperty(space.getKey());
			result.add(new ApiKeyPair(space, apiKey));
		}
		
		return result;
	}
	
	/**
	 * Save listed ApiKeys.
	 * 
	 * @param apiKeys
	 */
	public void setFlowdockApiKeys(List<ApiKeyPair> apiKeys) {
		Properties props = new Properties();
		
		for (ApiKeyPair pair : apiKeys) {
			props.put(pair.getSpaceKey(), pair.getApiKey());
		}
		
		OutputStream out = new ByteArrayOutputStream();
		try {
			props.store(out, null);
		} catch (IOException ioe) {} // seems unlikely to happen
		
		this.bandanaManager.setValue(new ConfluenceBandanaContext(),
			FLOWDOCK_API_KEYS, out.toString());
	}
	
	public String getApiKeyForSpace(Space space) {
		List<ApiKeyPair> pairs = this.getFlowdockApiKeys();
		for (ApiKeyPair pair : pairs) {
			if (pair.getSpace() == space) {
				return pair.getApiKey();
			}
		}
		
		return null;
	}
	
	// Bean configuration
	
	public void setBandanaManager(BandanaManager manager) {
		this.bandanaManager = manager;
	}
	
	public void setSpaceManager(SpaceManager manager) {
		this.spaceManager = manager;
	}
	
	// Helpers
	
	private Properties readApiKeyPropertiesObj() {
		ConfluenceBandanaContext context = new ConfluenceBandanaContext();
		String propsString = (String)this.bandanaManager.getValue(context, FLOWDOCK_API_KEYS);
		if (propsString == null) propsString = ""; // initially it doesn't exist
		
		Properties props = new Properties();
		InputStream in = new ByteArrayInputStream(propsString.getBytes());
		
		try {
			props.load(in);
		} catch (IOException ioe) {} // seems unlikely to happen
		
		return props;
	}
}
