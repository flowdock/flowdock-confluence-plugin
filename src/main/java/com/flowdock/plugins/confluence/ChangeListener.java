package com.flowdock.plugins.confluence;

import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageTrashedEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;

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
	
	public ChangeListener(EventPublisher eventPublisher) {
		this.eventRenderer = new FlowdockEventRenderer();
		eventPublisher.register(this);
	}
	
	@EventListener
	public void pageUpdateEvent(PageUpdateEvent event) {
		FlowdockConnection.sendApiMessage(this.eventRenderer.renderEvent(event));
	}
	
	@EventListener
	public void pageCreateEvent(PageCreateEvent event) {
		FlowdockConnection.sendApiMessage(this.eventRenderer.renderEvent(event));
	}
	
	@EventListener
	public void pageTrashedEvent(PageTrashedEvent event) {
		FlowdockConnection.sendApiMessage(this.eventRenderer.renderEvent(event));
	}
}
