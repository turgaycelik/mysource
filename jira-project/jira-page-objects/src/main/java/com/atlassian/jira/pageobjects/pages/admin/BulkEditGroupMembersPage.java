package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import java.util.List;

/**
 * Author: Geoffrey Wong
 * JIRA Administration page to Bulk Edit members of groups
 */
public class BulkEditGroupMembersPage extends AbstractJiraPage
{
    @ElementBy (id = "usersToAssignStr")
    PageElement usersToAssign;
    
    @ElementBy (name = "assign")
    PageElement assignButton;

    @ElementBy (cssSelector = ".bulk-edit-user-groups thead tr:first-child th")
    PageElement groupHeading;

    @ElementBy (cssSelector = ".aui-message.error")
    PageElement errorMessage;

    private String URI = "/secure/admin/user/BulkEditUserGroups!default.jspa";
    private String singleGroup = null;
    private List<String> multipleGroups = null;

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return usersToAssign.timed().isPresent();
    }

    public BulkEditGroupMembersPage ()
    {
        // go to the unselected bulk edit group members page
    }

    public BulkEditGroupMembersPage (String singleGroup)
    {
        URI += "?selectedGroupsStr=" + singleGroup;
        this.singleGroup = singleGroup;
    }
    
    public BulkEditGroupMembersPage (List<String> multipleGroups)
    {
        URI += "?";
        for (int i = 0; i < multipleGroups.size(); i++)
        {
            URI += "selectedGroupsStr=" + multipleGroups.get(i);
            if (i + 1 < multipleGroups.size())
            {
                URI += "&";
            }
        }
        this.multipleGroups = multipleGroups;
    }

    public BulkEditGroupMembersPage addNewMember(String newMember)
    {
        usersToAssign.clear().type(newMember);
        return this;
    }
    
    public BulkEditGroupMembersPage submitMembersToAdd()
    {
        assignButton.click();
        if (multipleGroups == null)
        {
            return pageBinder.bind(BulkEditGroupMembersPage.class, singleGroup);
        }

        return pageBinder.bind(BulkEditGroupMembersPage.class, multipleGroups);
    }

    public String newMembersText()
    {
        return usersToAssign.getText();
    }

    public String groupHeadingText()
    {
        return groupHeading.getText();
    }

    public String errorMessageText()
    {
        return errorMessage.getText();
    }

    public BulkEditGroupMembersPage addNewMembers(List<String> users)
    {
        usersToAssign.clear();
        for (String username: users)
        {
            usersToAssign.type(username);
            usersToAssign.type(", ");
        }
        return this;
    }
}
