package com.atlassian.jira.pageobjects.pages.admin.customfields;

import java.util.List;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.common.base.Function;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openqa.selenium.By;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;

/**
 * @since v6.2
 */
public class CreateCustomFieldPage extends AbstractJiraPage
{
    @ElementBy(className = "custom-field-types")
    private PageElement customFieldsTable;

    @Override
    public TimedCondition isAt()
    {
        return customFieldsTable.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/CreateCustomField!default.jspa";
    }

    public Iterable<CustomFieldItem> getAvailableCustomFields()
    {
        final List<PageElement> customfieldEntries =
                customFieldsTable.findAll(By.cssSelector("tbody tr td"));

        return copyOf(transform(customfieldEntries, new Function<PageElement, CustomFieldItem>()
        {
            @Override
            public CustomFieldItem apply(final PageElement pageElement)
            {
                final String customFieldName = pageElement.find(By.tagName("label")).getText();

                final String customFieldDescription = (pageElement.find(By.className("description")).isPresent())
                        ? pageElement.find(By.className("description")).getText()
                        : "";
                return new CustomFieldItem(customFieldName, customFieldDescription);
            }
        }));
    }

    /**
     * Represents the data for a custom field in the {@link CreateCustomFieldPage}
     *
     * @since 6.2
     */
    public static class CustomFieldItem
    {
        private final String name;

        private final String description;

        public CustomFieldItem(final String name, final String description)
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

            if (!(obj instanceof CustomFieldItem)) { return false; }

            final CustomFieldItem rhs = (CustomFieldItem) obj;

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
