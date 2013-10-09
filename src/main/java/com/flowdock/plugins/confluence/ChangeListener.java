package com.flowdock.plugins.confluence;

import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.event.events.content.comment.CommentCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageTrashedEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.pages.Page;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.flowdock.plugins.confluence.config.FlowdockConfigurationManager;
import org.springframework.beans.factory.DisposableBean;

/**
 * Listen to all kinds of events we're aware of.
 * 
 * This class needs to be in sync with FlowdockEventRenderer, who
 * turns all events into POSTable data.
 * 
 * @author mutru
 *
 */
public class ChangeListener implements DisposableBean {
	private FlowdockEventRenderer eventRenderer = null;
	private FlowdockConfigurationManager flowdockConfigurationManager = null;
	protected EventPublisher eventPublisher;
	
	public ChangeListener(EventPublisher eventPublisher, FlowdockConfigurationManager manager, FlowdockEventRenderer eventRenderer) {
		this.eventRenderer = eventRenderer;
		this.eventPublisher = eventPublisher;
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
	
	@EventListener
	public void commentCreateEvent(CommentCreateEvent event) {
		this.sendNotification(event);
	}
	
	// Bean configuration
	public void setFlowdockConfigurationManager(FlowdockConfigurationManager manager) {
		this.flowdockConfigurationManager = manager;
	}
	
	private void sendNotification(final CommentCreateEvent event) {
		if (event.getComment().getOwner().getType() == "page") {
			Page page = (Page)event.getComment().getOwner();
			this.sendNotification(page, event);
		}
	}
	
	private void sendNotification(final PageEvent event) {
		this.sendNotification(event.getPage(), event);
	}
	
	private void sendNotification(final Page page, final ContentEvent event) {
		final String apiKey = flowdockConfigurationManager.getApiKeyForSpace(page.getSpace());
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				FlowdockConnection.sendApiMessage(eventRenderer.renderEvent(event), apiKey);
			}
		});
		t.start();
	}

	// Unregister the listener if the plugin is uninstalled or disabled.
	public void destroy() throws Exception
	{
		eventPublisher.unregister(this);
	}
}
