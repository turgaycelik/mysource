package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * @since v5.0.1
 */
public class ManageIssueTypeSchemePage extends AbstractJiraPage
{
    @ElementBy(id = "issuetypeschemes")
    private PageElement table;

    @ElementBy(id = "issuetype-scheme-add")
    private PageElement addLink;

    @Override
    public TimedCondition isAt()
    {
        return table.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ManageIssueTypeSchemes!default.jspa";
    }

    public IssueTypeSchemeRow getDefaultScheme()
    {
        for (IssueTypeSchemeRow schemeRow : getIssueTypeSchemes())
        {
            if (schemeRow.isDefault())
            {
                return schemeRow;
            }
        }
        return null;
    }
    
    public IssueTypeSchemeRow getSchemeForName(String searchName)
    {
        for (IssueTypeSchemeRow schemeRow : getIssueTypeSchemes())
        {
            if (StringUtils.equals(schemeRow.getName(), searchName))
            {
                return schemeRow;
            }
        }
        return null;
    }
    
    public List<IssueTypeSchemeRow> getIssueTypeSchemes()
    {
        List<IssueTypeSchemeRow> types = Lists.newArrayList();
        final List<PageElement> rows = table.findAll(By.cssSelector("tbody tr"));
        for (PageElement row : rows)
        {
            types.add(pageBinder.bind(IssueTypeSchemeRow.class, row));
        }
        return types;
    }

    public EditIssueTypeSchemePage createNewScheme()
    {
        addLink.click();
        return pageBinder.bind(EditIssueTypeSchemePage.class);
    }
    
    private static boolean hasElement(PageElement root, By selector)
    {
        return root.find(selector).isPresent();
    }

    public static class IssueTypeSchemeRow
    {
        @Inject
        private PageBinder binder;
        
        private final String name;
        private final String description;
        private final List<String> issueTypes;
        private final String defaultIssueType;
        private final boolean isDefault;
        private final PageElement editElement;
        private final PageElement copyElement;

        public IssueTypeSchemeRow(PageElement row)
        {
            long id = Long.parseLong(row.getAttribute("data-id"));
            
            final List<PageElement> all = row.findAll(By.tagName("td"));
            assertTrue("Expecting 4 rows in the table.", all.size() == 4);

            this.name = trimToNull(all.get(0).find(By.cssSelector("[data-scheme-field='name']")).getText());
            this.description = trimToNull(all.get(0).find(By.className("description")).getText());

            String defaultIssueType = null;
            List<String> issueTypes = Lists.newArrayList();
            for (PageElement issueTypeElement : all.get(1).findAll(By.tagName("li")))
            {
                final PageElement nameElement = issueTypeElement.find(By.className("issue-type-name"));
                final String name = trimToNull(nameElement.getText());
                boolean isDefault = hasElement(issueTypeElement, By.className("issue-type-default"));
                if (isDefault)
                {
                    if (defaultIssueType == null)
                    {
                        defaultIssueType = name;
                    }
                    else
                    {
                        assertTrue("More than one issue type signaled as default.", false);
                    }
                }
                issueTypes.add(name);
            }
            
            this.defaultIssueType = defaultIssueType;
            this.issueTypes = issueTypes;

            this.isDefault = hasElement(all.get(2), By.className("issue-type-scheme-global"));

            final PageElement operations = all.get(3);
            this.copyElement = operations.find(By.id("copy_" + id));
            this.editElement = operations.find(By.id("edit_" + id));
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public List<String> getIssueTypes()
        {
            return issueTypes;
        }

        public String getDefaultIssueType()
        {
            return defaultIssueType;
        }
        
        public boolean isDefault()
        {
            return isDefault;
        }

        public EditIssueTypeSchemePage editIssueTypeScheme()
        {
            editElement.click();
            return binder.bind(EditIssueTypeSchemePage.class);
        }

        public EditIssueTypeSchemePage copyIssueTypeScheme()
        {
            copyElement.click();
            return binder.bind(EditIssueTypeSchemePage.class);
        }
    }
    
    public static class IssueType
    {
        private final String name;
        private final boolean defaultType;

        private IssueType(String name, boolean defaultType)
        {
            this.name = name;
            this.defaultType = defaultType;
        }

        public String getName()
        {
            return name;
        }

        public boolean isDefaultType()
        {
            return defaultType;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            IssueType issueType = (IssueType) o;

            if (defaultType != issueType.defaultType) { return false; }
            if (name != null ? !name.equals(issueType.name) : issueType.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (defaultType ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
