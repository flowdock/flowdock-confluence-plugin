package com.flowdock.plugins.confluence.config;

import java.util.List;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;

public class ConfigureFlowdockAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = -5732284806136026378L;
	private SpaceManager spaceManager = null;
	private List<Space> spaces;
	
	public String input() {
		this.updateSpacesList();
		return INPUT;
	}
	
	public String save() {
		this.updateSpacesList();
		addActionMessage(getText("successfully.saved.api.keys"));
		return SUCCESS;
	}
	
	@Override
	public String getActionName(String fullClassName) {
		return "Configure Flowdock integration";
	}

	@Override
	public boolean isPermitted() {
		return true; // TODO PermissionManager.hasPermission(User,Permission,Object)
	}
	
	public void setSpaceManager(SpaceManager manager) {
		this.spaceManager = manager;
	}
	
	public List<Space> getSpaces() {
		return this.spaces;
	}
	
	private void updateSpacesList() {
		this.spaces = this.spaceManager.getAllSpaces();
	}
}
