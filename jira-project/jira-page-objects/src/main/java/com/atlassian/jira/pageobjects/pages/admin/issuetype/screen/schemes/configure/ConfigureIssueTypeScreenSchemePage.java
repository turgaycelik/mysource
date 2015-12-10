package com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.configure;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openqa.selenium.By;

import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;
import static java.lang.String.valueOf;

/**
 * Represents the &quot;configure issue type screen scheme&quot; page available from the
 * the {@link com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.ViewIssueTypeScreenSchemesPage}
 * in the administration section.
 *
 * @since v5.0.2
 */
public class ConfigureIssueTypeScreenSchemePage extends AbstractJiraPage
{
    private static final String ATTRIBUTE_SCHEME_ID = "data-id";

    @ElementBy(cssSelector="h2 [data-scheme-field='name']")
    private PageElement name;

    @ElementBy(id="add-issue-type-screen-scheme-configuration-association")
    private PageElement addIssueTypeToScreenSchemeButton;

    @ElementBy(id= "issue-type-table")
    private PageElement issueTypeToScreenSchemeAssociationsTable;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private long issueTypeScreenSchemeId;

    public ConfigureIssueTypeScreenSchemePage(final long issueTypeScreenSchemeId)
    {
        this.issueTypeScreenSchemeId = issueTypeScreenSchemeId;
    }

    public ConfigureIssueTypeScreenSchemePage(final String issueTypeScreenSchemeId)
    {
        this(Long.parseLong(issueTypeScreenSchemeId));
    }

    public ConfigureIssueTypeScreenSchemePage()
    {
        this(-1);
    }

    @Override
    public TimedCondition isAt()
    {
        TimedCondition condition = name.timed().isPresent();
        if (issueTypeScreenSchemeId >= 0)
        {
            condition = Conditions.and(condition,
                    name.timed().hasAttribute(ATTRIBUTE_SCHEME_ID, valueOf(issueTypeScreenSchemeId)));
        }
        return condition;
    }

    @Override
    public String getUrl()
    {
        if (issueTypeScreenSchemeId < 0)
        {
            throw new IllegalStateException("issueTypeScreenSchemeId not specified in the constructor.");
        }
        return format("/secure/admin/ConfigureIssueTypeScreenScheme.jspa?id=%s", issueTypeScreenSchemeId);
    }

    public AddIssueTypeToScreenSchemeAssociationDialog openAssociateIssueTypeToScreenSchemeDialog()
    {
        addIssueTypeToScreenSchemeButton.click();
        return pageBinder.bind(AddIssueTypeToScreenSchemeAssociationDialog.class);
    }

    /**
     * Gets the name of the screen scheme that will be used by default for all unmapped issue types.
     *
     * @return A String containing the name of the screen scheme that will be used by default for all
     * unmapped issue types.
     */
    public String getDefaultScreenScheme()
    {
        PageElement defaulIssueTypeToScreenAssociation =
                issueTypeToScreenSchemeAssociationsTable.find(By.cssSelector("tbody tr[data-default-association='true']"));

        return defaulIssueTypeToScreenAssociation.find(By.cssSelector("[data-scheme-field=screen-scheme]")).getText();
    }

    public Iterable<IssueTypeToScreenSchemeAssociationItem> getIssueTypeToScreenSchemeAssociations()
    {
        List<PageElement> issueTypeToFieldConfigurationAssociationsRows =
                issueTypeToScreenSchemeAssociationsTable.findAll(By.cssSelector("tbody tr"));

        return copyOf(transform(issueTypeToFieldConfigurationAssociationsRows, new Function<PageElement, IssueTypeToScreenSchemeAssociationItem>()
        {
            @Override
            public IssueTypeToScreenSchemeAssociationItem apply(final PageElement pageElement)
            {
                final String issueTypeName =
                        pageElement.find(By.cssSelector("[data-scheme-field=issue-type]")).getText();

                final String fieldConfigurationName =
                        pageElement.find(By.cssSelector("[data-scheme-field=screen-scheme]")).getText();

                return new IssueTypeToScreenSchemeAssociationItem(issueTypeName, fieldConfigurationName);
            }
        }));

    }

    public boolean isAddingAnIssueTypeToScreenSchemeAssociationDisabled()
    {
        return ("true").equals(addIssueTypeToScreenSchemeButton.getAttribute("aria-disabled"))
                && addIssueTypeToScreenSchemeButton.getAttribute("href") == null;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

    public String getName()
    {
        return name.getText();
    }

    public static class IssueTypeToScreenSchemeAssociationItem
    {
        private final String issueType;

        private final String screenScheme;

        public IssueTypeToScreenSchemeAssociationItem(final String issueType, final String screenScheme)
        {
            this.issueType = issueType;
            this.screenScheme = screenScheme;
        }

        public String getScreenScheme()
        {
            return screenScheme;
        }

        public String getIssueType()
        {
            return issueType;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof IssueTypeToScreenSchemeAssociationItem)) { return false; }

            final IssueTypeToScreenSchemeAssociationItem rhs = (IssueTypeToScreenSchemeAssociationItem) obj;

            return new EqualsBuilder().
                    append(getIssueType(), rhs.getIssueType()).
                    append(getScreenScheme(), rhs.getScreenScheme()).
                    isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().
                    append(getIssueType()).
                    append(getScreenScheme()).
                    toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("issueType", issueType).
                    append("fieldConfiguration", screenScheme).
                    toString();
        }
    }
}
