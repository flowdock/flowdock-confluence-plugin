package com.flowdock.plugins.confluence.config;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.xwork.RequireSecurityToken;

public class ConfigureFlowdockAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = -5732284806136026379L;
	
	// Bean configured
	private FlowdockConfigurationManager flowdockConfigurationManager;
	
	// For templates
	private List<ApiKeyPair> apiKeyPairs;
	
	// POST data
	private String[] spaceKeys;
	private String[] apiKeys;
	
	public String input() {
		this.updateTemplateData();
		return INPUT;
	}
	
	@RequireSecurityToken(true)
	public String save() {
		this.updateTemplateData();
		addActionMessage(getText("successfully.saved.api.keys"));
		
		List<ApiKeyPair> pairs = this.parseApiKeyPairs();
		this.flowdockConfigurationManager.setFlowdockApiKeys(pairs);
		
		return SUCCESS;
	}
	
	@Override
	public String getActionName(String fullClassName) {
		return getText("action.name");
	}

	@Override
	public boolean isPermitted() {
		return true; // TODO PermissionManager.hasPermission(User,Permission,Object)
	}
	
	public void setFlowdockConfigurationManager(FlowdockConfigurationManager manager) {
		this.flowdockConfigurationManager = manager;
	}
	
	public List<ApiKeyPair> getApiKeyPairs() {
		return this.apiKeyPairs;
	}
	
	public void setSpaceKeys(String[] keys) {
		this.spaceKeys = keys;
	}
	
	public void setApiKeys(String[] keys) {
		this.apiKeys = keys;
	}
	
	private void updateTemplateData() {
		this.updateApiKeyPairs();
	}
	
	private void updateApiKeyPairs() {
		this.apiKeyPairs = this.flowdockConfigurationManager.getFlowdockApiKeys();
	}
	
	// Uses HTTP POST data to generate a list.
	private List<ApiKeyPair> parseApiKeyPairs() {
		List<ApiKeyPair> result = new ArrayList<ApiKeyPair>();
		if (this.spaceKeys == null || this.apiKeys == null || this.spaceKeys.length != this.apiKeys.length) {
			return result;
		}

		for (int i=0; i<this.spaceKeys.length; i++) {
			if (apiKeys[i] != null && apiKeys[i] != "") {
				result.add(new ApiKeyPair(spaceKeys[i], apiKeys[i]));
			}
		}

		return result;
	}
}
