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
import com.atlassian.confluence.util.GeneralUtil;
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
		if (skipEvent(event)) {
			return null;
		}
		
		HashMap<String, String> result = new HashMap<String, String>();
		
		// Space
		result.put("space_name", event.getPage().getSpace().getName());
		result.put("space_path", event.getPage().getSpace().getUrlPath());
		
		// Page
		result.put("page_title", event.getPage().getTitle());
		result.put("page_path", event.getPage().getUrlPath());
		result.put("page_url", GeneralUtil.getPageUrl(event.getPage()));
		
		result.put("version_comment", event.getPage().getRenderedVersionComment());
		
		if (event instanceof PageCreateEvent) {
			result.put("event", "create");
			
			User user = findEventUser((PageCreateEvent)event);
			result.put("user_email", user.getEmail());
			result.put("user_name", user.getFullName());
			
			String content = event.getPage().getContent();
			result.put("content", content);
			result.put("content_summary", GeneralUtil.makeSummary(content).toString());
		} else if (event instanceof PageTrashedEvent) {
			result.put("event", "trashed");
			
			User user = ((PageTrashedEvent)event).getOriginatingUser();
			result.put("user_email", user.getEmail());
			result.put("user_name", user.getFullName());
		} else if (event instanceof PageUpdateEvent) {
			result.put("event", "update");
			result.put("diff", getDiff((PageUpdateEvent)event));
			
			User user = findEventUser((PageUpdateEvent)event);
			result.put("user_email", user.getEmail());
			result.put("user_name", user.getFullName());
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
	
	private boolean skipEvent(PageEvent event) {
		if (event instanceof PageUpdateEvent && ((PageUpdateEvent)event).isMinorEdit())
			return true;
		
		return false;
	}
	
	private User findEventUser(PageCreateEvent event) {
		return event.getPage().getUserAccessor().getUser(event.getPage().getCreatorName());
	}
	
	private User findEventUser(PageUpdateEvent event) {
		return event.getPage().getUserAccessor().getUser(event.getNew().getLastModifierName());
	}
}
