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
 * Represents the &quot;view screens page&quot; available from within the JIRA Administration UI.
 *
 * @since v5.0.1
 */
public class ViewFieldScreensPage extends AbstractJiraPage
{
    @ElementBy(id="add-field-screen")
    private PageElement addScreenButton;

    @ElementBy(id="field-screens-table")
    private PageElement fieldScreensTable;

    @Override
    public TimedCondition isAt()
    {
        return addScreenButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewFieldScreens.jspa";
    }

    public AddFieldScreenDialog openAddFieldScreenDialog()
    {
        addScreenButton.click();
        return pageBinder.bind(AddFieldScreenDialog.class);
    }

    /**
     * Retrieves the current field configurations.
     *
     * @return an Iterable containing the current field configurations.
     */
    public Iterable<FieldScreenItem> getFieldScreens()
    {
        List<PageElement> fieldScreenRows = fieldScreensTable.findAll(By.cssSelector("tbody tr"));

        return copyOf(transform(fieldScreenRows, new Function<PageElement, FieldScreenItem>()
        {
            @Override
            public FieldScreenItem apply(final PageElement pageElement)
            {
                final String fieldConfigName = pageElement.find(By.className("field-screen-name")).getText();

                String fieldDescriptionName = "";
                if (pageElement.find(By.className("field-screen-description")).isPresent())
                {
                    fieldDescriptionName = pageElement.find(By.className("field-screen-description")).getText();
                }
                return new FieldScreenItem(fieldConfigName, fieldDescriptionName);
            }
        }));
    }

    /**
     * Represents the data for a field screen in the {@link ViewFieldScreensPage}
     *
     * @since 5.0.1
     */
    public static class FieldScreenItem
    {
        private final String name;
        
        private final String description;

        public FieldScreenItem(final String name, final String description)
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

            if (!(obj instanceof FieldScreenItem)) { return false; }

            final FieldScreenItem rhs = (FieldScreenItem) obj;

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
