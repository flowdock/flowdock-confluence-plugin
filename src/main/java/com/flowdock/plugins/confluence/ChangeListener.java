package com.flowdock.plugins.confluence;

import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.event.events.content.comment.CommentCreateEvent;
import com.atlassian.confluence.event.events.content.comment.CommentEvent;
import com.atlassian.confluence.event.events.content.comment.CommentUpdateEvent;
import com.atlassian.confluence.event.events.content.comment.CommentRemoveEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageTrashedEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.event.events.content.page.PageRestoreEvent;
import com.atlassian.confluence.event.events.content.page.PageRemoveEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostCreateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostRemoveEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostRestoreEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostUpdateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostTrashedEvent;
import com.atlassian.confluence.pages.AbstractPage;
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
 * @author mutru, Sampo Verkasalo
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
	public void pageRemoveEvent(PageRemoveEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void pageRestoreEvent(PageRestoreEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void blogPostUpdateEvent(BlogPostUpdateEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void blogPostCreateEvent(BlogPostCreateEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void blogPostTrashedEvent(BlogPostTrashedEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void blogPostRemoveEvent(BlogPostRemoveEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void blogPostRestoreEvent(BlogPostRestoreEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void commentCreateEvent(CommentCreateEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void commentUpdateEvent(CommentUpdateEvent event) {
		this.sendNotification(event);
	}

	@EventListener
	public void commentRemoveEvent(CommentRemoveEvent event) {
		this.sendNotification(event);
	}

	// Bean configuration
	public void setFlowdockConfigurationManager(FlowdockConfigurationManager manager) {
		this.flowdockConfigurationManager = manager;
	}

	private void sendNotification(final CommentEvent event) {
		if (event.getComment().getContainer() instanceof AbstractPage) {
			this.sendNotification((AbstractPage)event.getComment().getContainer(), event);
		} else {
			throw new RuntimeException("Unknown event type: " + event.getComment().getContainer().getType());
		}
	}

	private void sendNotification(final PageEvent event) {
		this.sendNotification(event.getPage(), event);
	}
	
	private void sendNotification(final BlogPostEvent event) {
		this.sendNotification(event.getBlogPost(), event);
	}

	private void sendNotification(final AbstractPage page, final ContentEvent event) {
		sendActivity(event, flowdockConfigurationManager.getApiKeyForSpace(page.getSpace()));
	}

	private void sendActivity(final ContentEvent event, final String apiKey) {
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
