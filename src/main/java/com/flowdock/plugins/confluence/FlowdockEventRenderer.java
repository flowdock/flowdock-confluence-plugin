package com.flowdock.plugins.confluence;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jrcs.diff.DifferentiationFailedException;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.diff.ChangeChunk;
import com.atlassian.confluence.diff.ConfluenceDiff;
import com.atlassian.confluence.diff.renderer.StaticHtmlChangeChunkRenderer;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageTrashedEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.mail.notification.listeners.NotificationData;
import com.atlassian.confluence.mail.notification.listeners.PageNotificationsListener;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.user.User;

/**
 * This class kindly abuses Atlassian's own mail notification listener. But instead
 * of sending emails, it stores the notification data.
 * 
 * @author mutru
 *
 */
public class FlowdockEventRenderer {
	public Map<String, String> renderEvent(PageEvent event) {
		HashMap<String, String> result = new HashMap<String, String>();
		
		// Space
		result.put("space", event.getPage().getSpace().getName());
		
		// Page
		result.put("title", event.getPage().getTitle());
		result.put("url", event.getPage().getUrlPath());
		result.put("version_comment", event.getPage().getRenderedVersionComment());
		
		if (event instanceof PageCreateEvent) {
			result.put("event", "create");
		} else if (event instanceof PageTrashedEvent) {
			result.put("event", "trashed");
			
			User user = ((PageTrashedEvent)event).getOriginatingUser();
			result.put("email", user.getEmail());
			result.put("user", user.getFullName());
		} else if (event instanceof PageUpdateEvent) {
			result.put("event", "update");
			// TODO: add diff
		} else {
			throw new RuntimeException("Unknown page event type.");
		}
		
		return result;
	}
	
	private List<String> getDiff(PageUpdateEvent event) {
		StaticHtmlChangeChunkRenderer renderer = StaticHtmlChangeChunkRenderer.INSTANCE;
		ContentEntityObject originalContent = event.getOriginalPage();
		ContentEntityObject content = event.getPage();
		LinkedList<String> formattedChunks = new LinkedList<String>();
		
		try {
			ConfluenceDiff diff = new ConfluenceDiff(originalContent, content, true);
			
			for (ChangeChunk chunk : diff.getChunks()) {
				String chunkText = renderer.getFormattedText(chunk);
				formattedChunks.add(chunkText);
			}
			
		} catch (DifferentiationFailedException e) {
			// There's nothing we can do about it - diff failed.
		}
		
		return formattedChunks;
	}
}
