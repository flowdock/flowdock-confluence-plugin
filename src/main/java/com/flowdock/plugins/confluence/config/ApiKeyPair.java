package com.flowdock.plugins.confluence.config;

import com.atlassian.confluence.spaces.Space;

public class ApiKeyPair {
	private Space space;
	private String spaceKey; // when Space is not available, use this as an ID
	private String apiKey; // flowdock API token
	
	public ApiKeyPair() {
	}
	
	public ApiKeyPair(Space space, String key) {
		this.setSpace(space);
		this.setApiKey(key);
	}
	
	public ApiKeyPair(String spaceKey, String apiKey) {
		this.spaceKey = spaceKey;
		this.setApiKey(apiKey);
	}

	public void setSpace(Space space) {
		this.space = space;
		this.spaceKey = space.getKey();
	}

	public Space getSpace() {
		return space;
	}

	public void setApiKey(String key) {
		if (key == null || key == "") {
			this.apiKey = null;
		} else {
			this.apiKey = key;
		}
	}

	public String getApiKey() {
		return apiKey;
	}
	
	public String getSpaceKey() {
		return this.spaceKey;
	}
}
