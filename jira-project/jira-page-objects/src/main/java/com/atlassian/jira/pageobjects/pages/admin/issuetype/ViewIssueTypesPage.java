package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.pageobjects.elements.AvatarId;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import java.util.List;

import javax.validation.constraints.NotNull;

import static junit.framework.Assert.assertTrue;

/**
 * Page object that can be used to drive the view issue types page.
 *
 * @since v5.0.1
 */
public class ViewIssueTypesPage extends AbstractJiraPage
{
    @ElementBy(id = "issue-types-table")
    private PageElement issueTypesTable;

    @ElementBy(id = "add-issue-type")
    private PageElement issueTypesButton;

    @Override
    public TimedCondition isAt()
    {
        return issueTypesTable.timed().isPresent();
    }

    public AddIssueTypeDialog addIssueType()
    {
        issueTypesButton.click();
        return pageBinder.bind(AddIssueTypeDialog.class);
    }
    
    public <T> T addIssueTypeAndBind(Class<T> page, Object...args)
    {
        issueTypesButton.click();
        return pageBinder.bind(page, args);
    }

    public List<IssueType> getIssueTypes()
    {
        List<IssueType> types = Lists.newArrayList();
        final List<PageElement> rows = issueTypesTable.findAll(By.cssSelector("tbody tr"));
        for (PageElement row : rows)
        {
            final List<PageElement> all = row.findAll(By.tagName("td"));
            assertTrue("Not enough columns in the table.", all.size() >= 2);

            final PageElement nameElement = all.get(0);
            final PageElement img = nameElement.find(By.tagName("img"));

            String name = StringUtils.trimToNull(nameElement.find(By.cssSelector("[data-issue-type-field=name]")).getText());
            String iconUrl = img == null ? null : img.getAttribute("src");
            String description = StringUtils.trimToNull(all.get(0).find(By.cssSelector("div.description")).getText());
            boolean subtask = all.get(1).hasAttribute("data-type", "subtask");

            types.add(new IssueType(name, description, subtask, AvatarId.fromImageLink(iconUrl)));
        }
        return types;
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewIssueTypes.jspa";
    }
    
    public static class IssueType
    {
        private static final long DEFAULT_ISSUETYPE_AVATAR_ID = 10240;
        private static final long DEFAULT_ISSUETYPE_SUBTASK_AVATAR_ID = 10256;
        private final String name;
        private final String description;
        private final boolean subtask;
        private final AvatarId avatarId;

        public IssueType(String name, String description, boolean subtask)
        {
            this(
                name,
                description,
                subtask,
                subtask ?
                        getDefaultIssueTypeSubtaskAvatarId() :
                        getDefaultIssueTypeAvatarId() );
        }

        @Deprecated
        public IssueType(String name, String description, boolean subtask, String iconUrl) {
            this.name = name;
            this.description = description;
            this.subtask = subtask;
            this.avatarId = AvatarId.fromImageLink(iconUrl);
        }

        public IssueType(String name, String description, boolean subtask, @NotNull AvatarId avatarId)
        {
            assert avatarId!=null;

            this.name = name;
            this.description = description;
            this.subtask = subtask;
            this.avatarId = avatarId;
        }

        public static AvatarId getDefaultIssueTypeAvatarId() {
            return AvatarId.fromId(DEFAULT_ISSUETYPE_AVATAR_ID);
        }

        public static AvatarId getDefaultIssueTypeSubtaskAvatarId() {
            return AvatarId.fromId(DEFAULT_ISSUETYPE_SUBTASK_AVATAR_ID);
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public boolean isSubtask()
        {
            return subtask;
        }

        public AvatarId getAvatarId()
        {
            return avatarId;
        }

        public String getIconUrl() {
            return avatarId.asRelativeLinkForDefaultSize(Avatar.Type.ISSUETYPE);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            IssueType issueType = (IssueType) o;

            if (subtask != issueType.subtask) { return false; }
            if (description != null ? !description.equals(issueType.description) : issueType.description != null)
            {
                return false;
            }
            if (!avatarId.equals(issueType.avatarId)) { return false; }
            if (name != null ? !name.equals(issueType.name) : issueType.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (subtask ? 1 : 0);
            result = 31 * result + avatarId.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
