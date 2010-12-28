package com.flowdock.plugins.confluence;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;

public class ChangeListener {
	public ChangeListener(EventPublisher eventPublisher) {
		eventPublisher.register(this);
	}
	
	@EventListener
	public void pageUpdateEvent(PageUpdateEvent event) {
		Map<String, String> data = eventToMessage(event);
		FlowdockConnection.sendApiMessage(data);
	}
	
	@EventListener
	public void pageCreateEvent(PageCreateEvent event) {
		Map<String, String> data = eventToMessage(event);
		FlowdockConnection.sendApiMessage(data);
	}
	
	private Map<String, String> eventToMessage(PageCreateEvent event) {
		HashMap<String, String> result = new HashMap<String, String>();
		
		result.put("title", event.getPage().getDisplayTitle());
		result.put("content", event.getContent().getContent());
		result.put("user", event.getPage().getCreatorName());
		result.put("space", event.getPage().getSpace().getName());
		
		return result;
	}
	
	private Map<String, String> eventToMessage(PageUpdateEvent event) {
		HashMap<String, String> result = new HashMap<String, String>();
		
		result.put("title", event.getPage().getDisplayTitle());
		result.put("content", event.getContent().getContent());
		result.put("user", event.getPage().getLastModifierName());
		result.put("email", event.getPage().getUserAccessor().getUser(event.getNew().getLastModifierName()).getEmail());
		result.put("space", event.getPage().getSpace().getName());
		
		return result;
	}
}
