package com.flowdock.plugins.confluence;

import java.util.HashMap;
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
import com.atlassian.user.User;

/**
 * This class tries to figure out all relevant information from an event.
 * 
 * It's inspired by Atlassian's own PageNotificationsListener and
 * AbstractNotificationsListener. However, overriding those classes
 * turned out to be a mess, so everything's re-implemented in this class.
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
			result.put("diff", getDiff((PageUpdateEvent)event));
		} else {
			throw new RuntimeException("Unknown page event type.");
		}
		
		return result;
	}
	
	private String getDiff(PageUpdateEvent event) {
		StaticHtmlChangeChunkRenderer renderer = StaticHtmlChangeChunkRenderer.INSTANCE;
		ContentEntityObject originalContent = event.getOriginalPage();
		ContentEntityObject content = event.getPage();
		StringBuffer output = new StringBuffer();
		
		try {
			ConfluenceDiff diff = new ConfluenceDiff(originalContent, content, true);
			
			for (ChangeChunk chunk : diff.getChunks()) {
				// example chunk:
				// <tr><td class="diff-added-lines" style="background-color: #dfd;"> <br>THIS IS SO AWESOME <br></td></tr>
				String chunkText = renderer.getFormattedText(chunk);
				output.append(chunkText);
			}
			
		} catch (DifferentiationFailedException e) {
			// There's nothing we can do about it - diff failed.
		}
		
		return output.toString();
	}
}
