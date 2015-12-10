package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;

import java.util.List;

/**
 * @since v4.4
 */
public class EditIssueSecurityScheme extends AbstractJiraPage
{
    @ElementBy (id = "issue-security-table")
    private PageElement issueSecurityTable;

    @ElementBy (id = "schemeId")
    private PageElement schemeIdElement;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;
    
    @ElementBy (cssSelector = "tbody>tr", within = "issueSecurityTable")
    protected Iterable<PageElement> securityLevels;

    private final long schemeId;

    public EditIssueSecurityScheme(long schemeId)
    {
        this.schemeId = schemeId;
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/EditIssueSecurities!default.jspa?schemeId=" + schemeId;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(issueSecurityTable.timed().isPresent(),
                Conditions.forMatcher(schemeIdElement.timed().getValue(), Matchers.equalTo(String.valueOf(schemeId))));
    }

    public Iterable<SecurityLevelRow> getSecurityLevelRows()
    {
        return Iterables.transform(securityLevels, PageElements.bind(pageBinder, SecurityLevelRow.class));
    }
    
    public SecurityLevelRow getSecurityLevelRowByName(final String name)
    {
        Iterable<SecurityLevelRow> allRows = Iterables.transform(securityLevels, PageElements.bind(pageBinder, SecurityLevelRow.class));
        return Iterables.find(allRows, new Predicate<SecurityLevelRow>()
        {
            @Override
            public boolean apply(SecurityLevelRow row)
            {
                return row.getLevelName().equals(name);
            }
        }, null);
    }

    public static final class SecurityLevelRow
    {
        private final String eventName;
        private final List<String> deleteLinkIds;

        public SecurityLevelRow(final PageElement webElement)
        {
            final List<PageElement> cols = webElement.findAll(By.tagName("td"));
            final PageElement name = cols.get(0);
            final PageElement permissions = cols.get(1);

            this.deleteLinkIds = Lists.transform(permissions.findAll(By.linkText("Delete")), new Function<PageElement, String>()
            {
                @Override
                public String apply(PageElement element)
                {
                    return element.getAttribute("id");
                }
            });
            this.eventName = name.find(By.tagName("b")).getText().trim();
        }

        public String getLevelName()
        {
            return eventName;
        }

        public List<String> getDeleteLinks()
        {
            return deleteLinkIds;
        }
        
    }

    public long getSchemeId()
    {
        return schemeId;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

}
