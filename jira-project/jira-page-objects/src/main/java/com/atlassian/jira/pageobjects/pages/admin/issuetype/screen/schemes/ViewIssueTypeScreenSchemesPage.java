package com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.configure.ConfigureIssueTypeScreenSchemePage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openqa.selenium.By;

import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.junit.Assert.assertTrue;

/**
 * Represents the &quot;view issue type screen schemes page&quot; available from within the JIRA Administration UI.
 *
 * @since v5.0.2
 */
public class ViewIssueTypeScreenSchemesPage extends AbstractJiraPage
{
    private static final String CONFIGURE_LINK_CSS_SELECTOR =
            "tbody tr[data-issue-type-screen-scheme-name='%s'] .operations-list [data-operation=configure]";

    @ElementBy (id="add-issue-type-screen-scheme")
    private PageElement addIssueTypeScreenSchemeButton;

    @ElementBy(id="issue-type-screen-schemes-table")
    private PageElement issueTypeScreenSchemesTable;

    @Override
    public TimedCondition isAt()
    {
        return addIssueTypeScreenSchemeButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewIssueTypeScreenSchemes.jspa";
    }

    public AddIssueTypeScreenSchemeDialog openAddIssueTypeScreenSchemeDialog()
    {
        addIssueTypeScreenSchemeButton.click();
        return pageBinder.bind(AddIssueTypeScreenSchemeDialog.class);
    }

    public ConfigureIssueTypeScreenSchemePage configure(final String issueTypeScreenSchemeName)
    {
        final PageElement configureLink = issueTypeScreenSchemesTable.find(By.cssSelector(format(CONFIGURE_LINK_CSS_SELECTOR, issueTypeScreenSchemeName)));
        assertTrue("Attempted to configure an issue type screen scheme that is not present on the page", configureLink.isPresent());

        final String issueTypeScreenSchemeId = removeStart(configureLink.getAttribute("id"), "configure_issuetypescreenscheme_");

        configureLink.click();
        return pageBinder.bind(ConfigureIssueTypeScreenSchemePage.class, issueTypeScreenSchemeId);
    }


    public Iterable<IssueTypeScreenSchemeItem> getIssueTypeScreenSchemes()
    {
        final List<PageElement> fieldConfigurationSchemeRows =
                issueTypeScreenSchemesTable.findAll(By.cssSelector("tbody tr"));

        return copyOf(transform(fieldConfigurationSchemeRows, new Function<PageElement, IssueTypeScreenSchemeItem>()
        {
            @Override
            public IssueTypeScreenSchemeItem apply(final PageElement pageElement)
            {
                final String screenSchemeName = pageElement.
                        find(By.cssSelector("[data-scheme-field=name]")).getText();

                String screenSchemeDescription = "";
                if (pageElement.find(By.className("description")).isPresent())
                {
                    screenSchemeDescription = pageElement.find(By.className("description")).getText();
                }
                return new IssueTypeScreenSchemeItem(screenSchemeName, screenSchemeDescription);
            }
        }));
    }


    /**
     * Represents the data in an issue type screen scheme displayed in the {@link ViewIssueTypeScreenSchemesPage}
     *
     * @since 5.0.1
     */
    public static class IssueTypeScreenSchemeItem
    {
        private final String name;

        private final String description;

        public IssueTypeScreenSchemeItem(final String name, final String description)
        {
            this.name = name;
            this.description = description;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof IssueTypeScreenSchemeItem)) { return false; }

            final IssueTypeScreenSchemeItem rhs = (IssueTypeScreenSchemeItem) obj;

            return new EqualsBuilder().
                    append(getName(), rhs.getName()).
                    append(getDescription(), rhs.getDescription()).
                    isEquals();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("name", name).
                    append("description", description).
                    toString();
        }
    }
}
