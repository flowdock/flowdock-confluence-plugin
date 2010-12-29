package com.flowdock.plugins.confluence.config;

public class ApiKeyPair {
	private String space;
	private String apiKey;
	
	public ApiKeyPair() {
	}
	
	public ApiKeyPair(String space, String key) {
		this.setSpace(space);
		this.setApiKey(key);
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public String getSpace() {
		return space;
	}

	public void setApiKey(String key) {
		this.apiKey = key;
	}

	public String getApiKey() {
		return apiKey;
	}
}
