package com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.configure;

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
 * Represents the &quot;configure field configuration scheme&quot; page available from the
 * the {@link com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.ViewFieldConfigurationSchemesPage}
 * in the administration section.
 *
 * @since v5.0.1
 */
public class ConfigureFieldConfigurationSchemePage extends AbstractJiraPage
{
    private static final String DATA_ID = "data-id";

    private final long fieldConfigurationSchemeId;

    @ElementBy(id="add-issue-type-field-configuration-association")
    private PageElement addIssueTypeToFieldConfigurationButton;

    @ElementBy(id= "scheme_entries")
    private PageElement issueTypeToFieldConfigurationAssociationsTable;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;
    
    @ElementBy (cssSelector = "h2 [data-scheme-field='name']")
    private PageElement name;

    public ConfigureFieldConfigurationSchemePage(final String fieldConfigurationSchemeId)
    {
        this(Long.parseLong(fieldConfigurationSchemeId));
    }
    
    public ConfigureFieldConfigurationSchemePage(final long fieldConfigurationSchemeId)
    {
        this.fieldConfigurationSchemeId = fieldConfigurationSchemeId;
    }

    public ConfigureFieldConfigurationSchemePage()
    {
        this(-1);
    }

    @Override
    public TimedCondition isAt()
    {
        TimedCondition present = issueTypeToFieldConfigurationAssociationsTable.timed().isPresent();
        if (fieldConfigurationSchemeId >= 0)
        {
            present = Conditions.and(present, 
                    name.timed().hasAttribute(DATA_ID, valueOf(fieldConfigurationSchemeId)));
        }
        return present;
    }

    public boolean isAddingAnIssueTypeToFieldConfigurationAssociationDisabled()
    {
        return "true".equals(addIssueTypeToFieldConfigurationButton.getAttribute("aria-disabled"))
                && addIssueTypeToFieldConfigurationButton.getAttribute("href") == null;
    }

    public AddIssueTypeToFieldConfigurationDialog openAddIssueTypeToFieldConfigurationDialog()
    {
        addIssueTypeToFieldConfigurationButton.click();
        return pageBinder.bind(AddIssueTypeToFieldConfigurationDialog.class);
    }

    /**
     * Retrieves the current field configurations.
     *
     * @return an Iterable containing the current field configurations.
     */
    public Iterable<IssueTypeToFieldConfigurationAssociationItem> getIssueTypeToFieldConfigurationAssociations()
    {
        List<PageElement> issueTypeToFieldConfigurationAssociationsRows =
                issueTypeToFieldConfigurationAssociationsTable.findAll(By.cssSelector("tbody tr"));

        return copyOf(transform(issueTypeToFieldConfigurationAssociationsRows, new Function<PageElement, IssueTypeToFieldConfigurationAssociationItem>()
        {
            @Override
            public IssueTypeToFieldConfigurationAssociationItem apply(final PageElement pageElement)
            {
                final String issueTypeName =
                        pageElement.find(By.cssSelector("[data-scheme-field=issue-type]")).getText();

                final String fieldConfigurationName =
                        pageElement.find(By.cssSelector("[data-scheme-field=field-configuration]")).getText();

                return new IssueTypeToFieldConfigurationAssociationItem(issueTypeName, fieldConfigurationName);
            }
        }));
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
    
    public String getName()
    {
        return name.getText();
    }

    @Override
    public String getUrl()
    {
        if (fieldConfigurationSchemeId < 0)
        {
            throw new IllegalStateException("fieldConfigurationSchemeId not specified.");
        }
        return format("/secure/admin/ConfigureFieldLayoutScheme!default.jspa?id=%s", fieldConfigurationSchemeId);
    }

    public long getSchemeId()
    {
        return Long.parseLong(name.getAttribute(DATA_ID));
    }
    
    public static class IssueTypeToFieldConfigurationAssociationItem
    {
        private final String issueType;

        private final String fieldConfiguration;

        public IssueTypeToFieldConfigurationAssociationItem(final String issueType, final String fieldConfiguration)
        {
            this.issueType = issueType;
            this.fieldConfiguration = fieldConfiguration;
        }

        public String getFieldConfiguration()
        {
            return fieldConfiguration;
        }

        public String getIssueType()
        {
            return issueType;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof IssueTypeToFieldConfigurationAssociationItem)) { return false; }

            final IssueTypeToFieldConfigurationAssociationItem rhs = (IssueTypeToFieldConfigurationAssociationItem) obj;

            return new EqualsBuilder().
                    append(getIssueType(), rhs.getIssueType()).
                    append(getFieldConfiguration(), rhs.getFieldConfiguration()).
                    isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().
                    append(getIssueType()).
                    append(getFieldConfiguration()).
                    toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("issueType", issueType).
                    append("fieldConfiguration", fieldConfiguration).
                    toString();
        }
    }
}
