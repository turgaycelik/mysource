package com.atlassian.jira.functest.framework.page;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains helper methods for when you are on the View Issue Page
 *
 * @since v6.0
 */
public class ViewIssuePage
{
    private final FuncTestHelperFactory funcTestHelperFactory;

    public ViewIssuePage(FuncTestHelperFactory funcTestHelperFactory)
    {
        this.funcTestHelperFactory = funcTestHelperFactory;
    }

    /**
     * Returns the current value of the Assignee field as appearing on this View Issue Page.
     * @return the current value of the Assignee field as appearing on this View Issue Page.
     */
    public String getAssignee()
    {
        return getFieldValue("assignee");
    }

    public String getAssigneeUsername()
    {
        return getRelUsername(getFieldNode("assignee"));
    }

    /**
     * Returns the current value of the Reporter field as appearing on this View Issue Page.
     * @return the current value of the Reporter field as appearing on this View Issue Page.
     */
    public String getReporter()
    {
        return getFieldValue("reporter");
    }

    public String getReporterUsername()
    {
        return getRelUsername(getFieldNode("reporter"));
    }

    public String getCreatorUserName()
    {
        // you should be on the change history tab to use this function - make sure you call
        // openTabWithId("changehistory-tabpanel") prior to calling this.

        String creatorAnchorId = "issuecreator_"+ getIssueId();
        String creator =  getLocatorFactory().id(creatorAnchorId).getText();
        if (creator.equals(""))
        {
            creator = getRelUsername(getLocatorFactory().css("#issuecreateddetails-"+getIssueId()).getNode());
        }
        return creator;
    }


    /**
     *
     * @param id  id of tab to open
     * @return  a new ViewIssuePage if navigation took place, else this
     */
    public ViewIssuePage openTabWithId(String id)
    {
        if (id.equals(getActiveTabId()))
        {
            return this;
        }
        else
        {
            getTester().clickLink("changehistory-tabpanel");
            return new ViewIssuePage(funcTestHelperFactory);
        }
    }

    /**
     *
     * @return the id of the currently active tab
     */
    public String getActiveTabId()
    {
        Element activeTab = (Element)getLocatorFactory().css("#issue-tabs li.active").getNode();
        return activeTab.getAttribute("id");
    }


    public String getIssueId()
    {
        String id = "";
        Node node = getLocatorFactory().id("key-val").getNode();
        if (node != null & node instanceof Element)
        {
            id = ((Element)node).getAttribute("rel");
        }
        return id;
    }

    /**
     * Returns the current value of the named issue field as appearing on this View Issue Page.
     * @return the current value of the named issue field as appearing on this View Issue Page.
     */
    public String getFieldValue(String fieldName)
    {
        String value = getLocatorFactory().id(fieldName + "-val").getText();
        return value == null ? null : value.trim();
    }

    /**
     * Returns the DOM node of the named issue field as appearing on this View Issue Page.
     * @return the DOM node of the named issue field as appearing on this View Issue Page.
     */
    public Node getFieldNode(String fieldName)
    {
        return getLocatorFactory().id(fieldName + "-val").getNode();
    }

    /**
     * Extracts a single username from a "rel" attribute somewhere under the given node.
     * @param node the node to examine, which should be the value node for a single-user field
     * @return the single username found, or {@code null} if the value was not found or had
     *      multiple values
     */
    public static String getRelUsername(Node node)
    {
        final List<String> usernames = getRelUsernames(node);
        return (usernames.size() == 1) ? usernames.get(0) : null;
    }

    /**
     * Extracts any number of usernames from the "rel" attributes somewhere under the given node
     * @param node the node to examine, which should be the value node for a single- or
     *      multiple-user field
     * @return a list of the usernames found (never {@code null})
     */
    public static List<String> getRelUsernames(Node node)
    {
        return recurse(new ArrayList<String>(), node);
    }

    private static List<String> recurse(List<String> collector, Node node)
    {
        if (node instanceof Element)
        {
            final Element element = (Element)node;
            if (element.hasAttribute("rel"))
            {
                String userName = element.getAttribute("rel").trim();
                if (StringUtils.isNotBlank(userName))
                {
                    collector.add(userName);
                }
            }

            final NodeList kids = element.getChildNodes();
            for (int i=0; i<kids.getLength(); ++i)
            {
                recurse(collector, kids.item(i));
            }
        }
        return collector;
    }

    /**
     * Returns the current value of the custom field with the given ID.
     * This is equivalent to calling getFieldValue("customfield_1234")
     *
     * @return the current value of the custom field with the given ID.
     */
    public String getCustomFieldValue(int customFieldId)
    {
        return getFieldValue("customfield_" + customFieldId);
    }

    public String getCustomFieldRelUsername(int customFieldId)
    {
        return getRelUsername(getFieldNode("customfield_" + customFieldId));
    }

    public List<String> getCustomFieldRelUsernames(int customFieldId)
    {
        return getRelUsernames(getFieldNode("customfield_" + customFieldId));
    }

    public boolean hasVoted()
    {
        final String title = getLocatorFactory().id("vote-toggle").getText();
        if (title.equals("Remove vote for this issue"))
            return true;
        if (title.equals("Vote for this issue"))
            return false;
        throw new IllegalStateException("Unknown Vote title '" + title + "'");
    }

    public int getVoteCount()
    {
        return Integer.parseInt(getLocatorFactory().id("vote-data").getText());
    }

    /**
     * Clicks on the "vote-toggle" link.
     *
     * @return the refreshed ViewIssuePage.
     */
    public ViewIssuePage toggleVote()
    {
        getTester().clickLink("vote-toggle");
        return new ViewIssuePage(funcTestHelperFactory);
    }

    public boolean canVote()
    {
        return getLocatorFactory().id("vote-toggle").exists();
    }

    public boolean isWatching()
    {
        final String title = getLocatorFactory().id("watching-toggle").getText();
        if (title.equals("Stop watching this issue"))
            return true;
        if (title.equals("Start watching this issue"))
            return false;
        throw new IllegalStateException("Unknown watch title '" + title + "'");
    }

    public int getWatcherCount()
    {
        return Integer.parseInt(getLocatorFactory().id("watcher-data").getText());
    }

    /**
     * Clicks on the "vote-toggle" link.
     *
     * @return the refreshed ViewIssuePage.
     */
    public ViewIssuePage toggleWatch()
    {
        getTester().clickLink("watching-toggle");
        return new ViewIssuePage(funcTestHelperFactory);
    }

    private LocatorFactory getLocatorFactory()
    {
        return funcTestHelperFactory.getLocator();
    }

    private WebTester getTester()
    {
        return funcTestHelperFactory.getTester();
    }

    public boolean containsEditButton()
    {
        IdLocator locator = getLocatorFactory().id("edit-issue");
        return locator.getNodes().length > 0;
    }
}
