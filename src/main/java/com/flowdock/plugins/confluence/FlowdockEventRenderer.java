package com.flowdock.plugins.confluence;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.confluence.json.json.JsonString;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.content.render.xhtml.DeviceTypeAwareRenderer;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.event.events.content.Edited;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.event.events.content.comment.CommentCreateEvent;
import com.atlassian.confluence.event.events.content.comment.CommentUpdateEvent;
import com.atlassian.confluence.event.events.content.comment.CommentRemoveEvent;

import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageRemoveEvent;
import com.atlassian.confluence.event.events.content.page.PageRestoreEvent;
import com.atlassian.confluence.event.events.content.page.PageTrashedEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.event.events.types.Created;
import com.atlassian.confluence.event.events.types.Updated;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostCreateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostRemoveEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostRestoreEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostUpdateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostTrashedEvent;
import com.atlassian.confluence.json.json.JsonArray;
import com.atlassian.confluence.json.json.JsonObject;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.User;
import com.opensymphony.util.TextUtils;

/**
 * This class tries to figure out all relevant information from an event.
 * 
 * It's inspired by Atlassian's own PageNotificationsListener and
 * AbstractNotificationsListener. However, overriding those classes turned out
 * to be a mess, so everything's re-implemented in this class.
 * 
 * @author mutru, Sampo Verkasalo
 *
 */
public class FlowdockEventRenderer {
	public JsonObject renderEvent(ContentEvent event) {
		if (skipEvent(event) || eventMap.get(event.getClass()) == null)
			return null;
		else
			return renderEventJson(event, eventMap.get(event.getClass()));
	}

	private static final Map<Class<? extends ContentEvent>, String> eventMap;

	static {
		eventMap = new HashMap<Class<? extends ContentEvent>, String>();
		eventMap.put(BlogPostCreateEvent.class, "blog_created");
		eventMap.put(BlogPostRemoveEvent.class, "blog_removed");
		eventMap.put(BlogPostRestoreEvent.class, "blog_restored");
		eventMap.put(BlogPostTrashedEvent.class, "blog_trashed");
		eventMap.put(BlogPostUpdateEvent.class, "blog_updated");
		eventMap.put(CommentRemoveEvent.class, "comment_removed");
		eventMap.put(CommentCreateEvent.class, "comment_created");
		eventMap.put(CommentUpdateEvent.class, "comment_updated");
		eventMap.put(PageCreateEvent.class, "page_created");
		eventMap.put(PageRemoveEvent.class, "page_removed");
		eventMap.put(PageRestoreEvent.class, "page_restored");
		eventMap.put(PageTrashedEvent.class, "page_trashed");
		eventMap.put(PageUpdateEvent.class, "page_updated");
	}

	private JsonObject renderEventJson(ContentEvent event, String eventName) {
		JsonObject result = new JsonObject();
		result.setProperty("object", renderContentJson(event.getContent()));
		result.setProperty("event", eventMap.get(event.getClass()));
		result.setProperty("base_url", getBaseUrl());
		User user = this.findEventUser(event);
		if (user != null)
			result.setProperty("user", renderUserData(user));
		return result;
	}

	private JsonObject renderContentJson(ContentEntityObject content) {
		if (content instanceof Page)
			return renderPageData((Page) content);
		else if (content instanceof BlogPost)
			return renderBlogData((BlogPost) content);
		else if (content instanceof Comment)
			return renderCommentData((Comment) content);
		else
			throw new RuntimeException("Unknown event content");
	}

	private JsonObject renderSpaceData(Space space) {
		JsonObject result = new JsonObject();
		result.setProperty("name", space.getName());
		result.setProperty("url", getBaseUrl() + space.getUrlPath());
		return result;
	}

	private JsonObject renderBlogData(BlogPost blog) {
		JsonObject result = renderObjectData(blog);
		result.setProperty("space", renderSpaceData(blog.getSpace()));
		return result;
	}

	private JsonObject renderCommentData(Comment comment) {
		JsonObject result = renderObjectData(comment);
		if (comment.getParent() != null)
			result.setProperty("parent", renderObjectData(comment.getParent()));
		result.setProperty("container", renderContentJson(comment.getContainer()));
		return result;
	}

	private JsonObject renderPageData(Page page) {
		JsonObject result = renderObjectData(page);
		JsonArray ancestors = new JsonArray();
		for (Page ancestor : page.getAncestors() ) {
			JsonObject ancestorJson = new JsonObject();
			ancestorJson.setProperty("title", ancestor.getTitle());
			ancestorJson.setProperty("url", getBaseUrl() + ancestor.getUrlPath());
			ancestors.add(ancestorJson);
		}
		result.setProperty("space", renderSpaceData(page.getSpace()));
		result.setProperty("ancestors", ancestors);
		return result;
	}

	private JsonObject renderObjectData(ContentEntityObject object) {
		JsonObject result = new JsonObject();
		result.setProperty("title", object.getTitle());
		result.setProperty("content", object.getBodyAsStringWithoutMarkup());
		result.setProperty("html_content", renderAsHtml(object));
		result.setProperty("url", getBaseUrl() + object.getUrlPath());
		result.setProperty("id", String.valueOf(object.getId()));
		result.setProperty("type", object.getType());
		JsonArray arr = new JsonArray();
		for (Label label : object.getLabels())
			arr.add(new JsonString(label.getDisplayTitle()));
		result.setProperty("labels", arr);
		return result;
	}

	private JsonObject renderUserData(User user) {
		JsonObject result = new JsonObject();
		result.setProperty("email", user.getEmail());
		result.setProperty("name", user.getFullName());
		return result;
	}

	private String renderAsHtml(ContentEntityObject object) {
		final DeviceTypeAwareRenderer renderer = (DeviceTypeAwareRenderer) ContainerManager
				.getComponent("viewRenderer");
		ConversionContext conversionContext = new DefaultConversionContext(object.toPageContext());
		return renderer.render(object.getEntity(), conversionContext);
	}

	private String getBaseUrl() {
		String baseUrl = GeneralUtil.getGlobalSettings().getBaseUrl();
		if (TextUtils.stringSet(baseUrl) && baseUrl.endsWith("/"))
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		return baseUrl;
	}

	private boolean skipEvent(ContentEvent event) {
		if (event instanceof Edited && ((Edited) event).isMinorEdit())
			return true;
		return false;
	}

	private User findEventUser(ContentEvent event) {
		if (event instanceof Created) {
			return event.getContent().getCreator();
		} else if (event instanceof Updated) {
			return event.getContent().getLastModifier();
		} else if (event instanceof com.atlassian.confluence.event.events.types.UserDriven) {
			return ((com.atlassian.confluence.event.events.types.UserDriven) event).getOriginatingUser();
		} else if (event instanceof com.atlassian.confluence.event.events.content.page.async.types.UserDriven) {
			UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
			UserKey userKey = ((com.atlassian.confluence.event.events.content.page.async.types.UserDriven) event)
					.getOriginatingUserKey();
			return userAccessor.getExistingUserByKey(userKey);
		} else {
			return null;
		}
	}
}
