package com.flowdock.plugins.confluence;

import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageTrashedEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.flowdock.plugins.confluence.config.FlowdockConfigurationManager;

/**
 * Listen to all kinds of events we're aware of.
 * 
 * This class needs to be in sync with FlowdockEventRenderer, who
 * turns all events into POSTable data.
 * 
 * @author mutru
 *
 */
public class ChangeListener {
	private FlowdockEventRenderer eventRenderer = null;
	private FlowdockConfigurationManager flowdockConfigurationManager = null;
	
	public ChangeListener(EventPublisher eventPublisher, FlowdockConfigurationManager manager, FlowdockEventRenderer eventRenderer) {
    this.eventRenderer = eventRenderer;
		this.setFlowdockConfigurationManager(manager);
		eventPublisher.register(this);
	}
	
	@EventListener
	public void pageUpdateEvent(PageUpdateEvent event) {
		this.sendNotification(event);
	}
	
	@EventListener
	public void pageCreateEvent(PageCreateEvent event) {
		this.sendNotification(event);
	}
	
	@EventListener
	public void pageTrashedEvent(PageTrashedEvent event) {
		this.sendNotification(event);
	}
	
	// Bean configuration
	public void setFlowdockConfigurationManager(FlowdockConfigurationManager manager) {
		this.flowdockConfigurationManager = manager;
	}
	
	private void sendNotification(PageEvent event) {
		String apiKey = this.flowdockConfigurationManager.getApiKeyForSpace(event.getPage().getSpace());
		FlowdockConnection.sendApiMessage(this.eventRenderer.renderEvent(event), apiKey);
	}
}
