package com.atlassian.jira.pageobjects.pages.admin.roles;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Represents the group role actor action page.
 *
 * @since v5.2
 */
public class GroupRoleActorActionPage extends AbstractActorSelectionPage
{
    private static final String URI = "/secure/project/GroupRoleActorAction.jspa";
    private static final String PICKER_ID = "groupNames";

    @ElementBy (id = "groupNames_container")
    protected PageElement pickerContainer;

    @ElementBy (id = "watcher-list")
    protected PageElement groupsTable;
    
    @ElementBy (tagName = "a", within = "groupsTable")
    protected Iterable<PageElement> groupLinks;
    
    private final String projectRoleId;

    public GroupRoleActorActionPage(String projectRoleId)
    {
        this.projectRoleId = projectRoleId;
    }

    public Iterable<String> getGroupLinkIds()
    {
        return 
        Iterables.transform(groupLinks, new Function<PageElement, String>()
        {
            @Override
            public String apply(PageElement link)
            {
                return link.getAttribute("id");
            }
            
        });
    }

    @Override
    protected String pickerId()
    {
        return PICKER_ID;
    }

    @Override
    public String getUrl()
    {
        return URI + "?projectRoleId=" + projectRoleId;
    }

    @Override
    public TimedCondition isAt()
    {
        return pickerContainer.timed().isPresent();
    }
}
