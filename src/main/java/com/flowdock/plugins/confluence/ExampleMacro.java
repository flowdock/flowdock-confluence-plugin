package com.flowdock.plugins.confluence;

import java.util.Map;
import java.util.List;
import java.util.Iterator;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.user.User;

/**
 * This very simple macro shows you the very basic use-case of displaying *something* on the Confluence page where it is used.
 * Use this example macro to toy around, and then quickly move on to the next example - this macro doesn't
 * really show you all the fun stuff you can do with Confluence.
 */
public class ExampleMacro extends BaseMacro
{

    // We just have to define the variables and the setters, then Spring injects the correct objects for us to use. Simple and efficient.
    // You just need to know *what* you want to inject and use.

    private final PageManager pageManager;
    private final SpaceManager spaceManager;

    public ExampleMacro(PageManager pageManager, SpaceManager spaceManager)
    {
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
    }

    public boolean isInline()
    {
        return false;
    }

    public boolean hasBody()
    {
        return false;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    /**
     * This method returns XHTML to be displayed on the page that uses this macro
     * we just do random stuff here, trying to show how you can access the most basic
     * managers and model objects. No emphasis is put on beauty of code nor on
     * doing actually useful things :-)
     */
    public String execute(Map params, String body, RenderContext renderContext)
            throws MacroException
    {

        // in this most simple example, we build the result in memory, appending HTML code to it at will.
        // this is something you absolutely don't want to do once you start writing plugins for real. Refer
        // to the next example for better ways to render content.
        StringBuffer result = new StringBuffer();

        // get the currently logged in user and display his name
        User user = AuthenticatedUserThreadLocal.getUser();
        if (user != null)
        {
            String greeting = "Hello " + user.getFullName() + "<br><br>";
            result.append(greeting);
        }

        //get the pages added in the last 55 days to the DS space ("Demo Space"), and display them
        List list = pageManager.getRecentlyAddedPages(55, "DS");
        result.append("Some stats for the Demo space: <br> ");
        for (Iterator i = list.iterator(); i.hasNext();)
        {
            Page page = (Page) i.next();
            int numberOfChildren = page.getChildren().size();
            String pageWithChildren = "Page " + page.getTitle() + " has " + numberOfChildren + " children <br> ";
            result.append(pageWithChildren);
        }

        // and show the number of all spaces in this installation.
        String spaces = "<br>Altogether, this installation has " + spaceManager.getAllSpaces().size() + " spaces. <br>";
        result.append(spaces);

        // this concludes our little demo. Now you should understand the basics of code injection use in Confluence, and how
        // to get a really simple macro running.

        return result.toString();
    }

}