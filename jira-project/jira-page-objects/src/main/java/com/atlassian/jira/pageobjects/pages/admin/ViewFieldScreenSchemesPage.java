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
 * Represents the &quot;view screens schemes page&quot; available from within the JIRA Administration UI.
 *
 * @since v5.0.1
 */
public class ViewFieldScreenSchemesPage extends AbstractJiraPage
{
    @ElementBy(id="add-field-screen-scheme")
    private PageElement addScreenSchemeButton;

    @ElementBy(id="field-screen-schemes-table")
    private PageElement fieldScreenSchemesTable;

    @Override
    public TimedCondition isAt()
    {
        return addScreenSchemeButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewFieldScreenSchemes.jspa";
    }

    public AddFieldScreenSchemeDialog openAddFieldScreenSchemeDialog()
    {
        addScreenSchemeButton.click();
        return pageBinder.bind(AddFieldScreenSchemeDialog.class);
    }

    /**
     * Retrieves the current field configurations.
     *
     * @return an Iterable containing the current field configurations.
     */
    public Iterable<FieldScreenSchemeItem> getFieldScreenSchemes()
    {
        List<PageElement> fieldScreenSchemeRows = fieldScreenSchemesTable.findAll(By.cssSelector("tbody tr"));

        return copyOf(transform(fieldScreenSchemeRows, new Function<PageElement, FieldScreenSchemeItem>()
        {
            @Override
            public FieldScreenSchemeItem apply(final PageElement pageElement)
            {
                final String fieldConfigName = pageElement.find(By.className("field-screen-scheme-name")).getText();

                String fieldDescriptionName = "";
                if (pageElement.find(By.className("field-screen-scheme-description")).isPresent())
                {
                    fieldDescriptionName = pageElement.find(By.className("field-screen-scheme-description")).getText();
                }
                return new FieldScreenSchemeItem(fieldConfigName, fieldDescriptionName);
            }
        }));
    }

    /**
     * Represents the data for a field screen scheme in the {@link com.atlassian.jira.pageobjects.pages.admin.ViewFieldScreenSchemesPage}
     *
     * @since 5.0.1
     */
    public static class FieldScreenSchemeItem
    {
        private final String name;
        
        private final String description;

        public FieldScreenSchemeItem(final String name, final String description)
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

            if (!(obj instanceof FieldScreenSchemeItem)) { return false; }

            final FieldScreenSchemeItem rhs = (FieldScreenSchemeItem) obj;

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
