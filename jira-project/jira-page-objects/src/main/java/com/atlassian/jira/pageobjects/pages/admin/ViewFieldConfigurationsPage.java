package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
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

/**
 * Represents the &quot;view field configurations page&quot; available from within the JIRA Administration UI.
 *
 * @since v5.0.1
 */
public class ViewFieldConfigurationsPage extends AbstractJiraPage
{
    @ElementBy(id="add-field-configuration")
    private PageElement addConfigurationButton;

    @ElementBy(id="field-configurations-table")
    private PageElement fieldConfigurationsTable;

    @Override
    public TimedCondition isAt()
    {
        return addConfigurationButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewFieldLayouts.jspa";
    }

    public AddFieldConfigurationDialog openAddFieldConfigurationDialog()
    {
        addConfigurationButton.click();
        return pageBinder.bind(AddFieldConfigurationDialog.class);
    }

    /**
     * Retrieves the current field configurations.
     *
     * @return an Iterable containing the current field configurations.
     */
    public Iterable<FieldConfigurationItem> getFieldConfigurations()
    {
        List<PageElement> fieldConfigurationRows = fieldConfigurationsTable.findAll(By.cssSelector("tbody tr"));

        return copyOf(transform(fieldConfigurationRows, new Function<PageElement, FieldConfigurationItem>()
        {
            @Override
            public FieldConfigurationItem apply(final PageElement pageElement)
            {
                final String fieldConfigName = pageElement.find(By.cssSelector("[data-scheme-field='name']")).getText();

                String fieldDescriptionName = "";
                if (pageElement.find(By.cssSelector("[data-scheme-field='description']")).isPresent())
                {
                    fieldDescriptionName = pageElement.find(By.cssSelector("[data-scheme-field='description']")).getText();
                }
                return new FieldConfigurationItem(fieldConfigName, fieldDescriptionName);
            }
        }));
    }

    /**
     * Represents the data for a field configuration in the {@link ViewFieldConfigurationsPage}
     *
     * @since 5.0.1
     */
    public static class FieldConfigurationItem
    {
        private final String name;
        
        private final String description;

        public FieldConfigurationItem(final String name, final String description)
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

            if (!(obj instanceof FieldConfigurationItem)) { return false; }

            final FieldConfigurationItem rhs = (FieldConfigurationItem) obj;

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
