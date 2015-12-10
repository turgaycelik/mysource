package com.atlassian.jira.pageobjects.pages.admin.subtask;

import com.atlassian.jira.pageobjects.elements.AvatarId;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ViewIssueTypesPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import java.util.List;

import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.junit.Assert.assertTrue;

/**
 * Page object to drive the manage subtasks page.
 *
 * @since v5.0.1
 */
public class ManageSubtasksPage extends AbstractJiraPage
{
    @ElementBy(id = "disable_subtasks")
    private PageElement disableElement;

    @ElementBy(id = "enable_subtasks")
    private PageElement enableElement;

    @ElementBy(id = "add-subtask-type")
    private PageElement addElement;
    
    @ElementBy(id = "sub-task-list")
    private  PageElement listElement;
    
    @Override
    public TimedCondition isAt()
    {
        return Conditions.or(disableElement.timed().isPresent(), enableElement.timed().isPresent());
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/subtasks/ManageSubTasks.jspa";
    }

    public boolean isSubtasksEnabled()
    {
        return disableElement.isPresent();
    }

    public AddSubtaskTypeDialog addSubtask()
    {
        assertSubtasksEnabled();
        addElement.click();
        return pageBinder.bind(AddSubtaskTypeDialog.class);
    }

    public <T> T addSubtaskAndBind(Class<T> nextPage, Object...args)
    {
        assertSubtasksEnabled();
        addElement.click();
        return pageBinder.bind(nextPage, args);
    }

    public List<Subtask> getSubtaks()
    {
        List<Subtask> tasks = Lists.newArrayList();
        List<PageElement> rows = listElement.findAll(By.cssSelector("tbody tr"));
        for (PageElement row : rows)
        {
            List<PageElement> cols = row.findAll(By.tagName("td"));
            assertTrue("Expecting at least three columns.", cols.size() >= 3);

            String name = trimToNull(cols.get(0).getText());
            String description = trimToNull(cols.get(1).getText());
            PageElement img = cols.get(2).find(By.tagName("img"));
            String iconUrl = img ==  null ? null : trimToNull(img.getAttribute("src"));
            
            tasks.add(new Subtask(name, description, AvatarId.fromImageLink(iconUrl)));
        }
        return tasks;
    }
    
    private void assertSubtasksEnabled()
    {
        assertTrue("Subtasks need to be enabled to add one.", isSubtasksEnabled());
    }
    
    public static class Subtask 
    {
        private final String name;
        private final String description;
        private final String iconUrl;
        private final AvatarId avatarId;

        @Deprecated
        public Subtask(String name, String description, String iconUrl)
        {
            this.name = name;
            this.description = description;
            this.iconUrl = iconUrl;
            this.avatarId = null;
        }

        public Subtask(String name, String description)
        {
            this.name = name;
            this.description = description;
            this.iconUrl = null;
            this.avatarId = ViewIssueTypesPage.IssueType.getDefaultIssueTypeSubtaskAvatarId();
        }

        public Subtask(final String name, final String description, final AvatarId avatarId)
        {
            this.name = name;
            this.description = description;
            this.avatarId = avatarId;
            iconUrl = null;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getIconUrl()
        {
            return iconUrl;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Subtask subtask = (Subtask) o;

            if (description != null ? !description.equals(subtask.description) : subtask.description != null)
            {
                return false;
            }
            if (iconUrl != null ? !iconUrl.equals(subtask.iconUrl) : subtask.iconUrl != null) { return false; }
            if (avatarId != null)
            {
                if (!avatarId.equals(subtask.avatarId))
                {
                    return false;
                }
            }
            else
            {
                if (name != null ? !name.equals(subtask.name) : subtask.name != null) { return false; }
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
            return result;
        }
    }
}
