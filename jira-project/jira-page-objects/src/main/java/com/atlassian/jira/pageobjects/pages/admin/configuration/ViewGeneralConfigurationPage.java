package com.atlassian.jira.pageobjects.pages.admin.configuration;

import java.util.List;

import org.openqa.selenium.By;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.by.ByDataAttribute;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * View General configuration properties
 * 
 * @since v5.1
 */
public class ViewGeneralConfigurationPage extends AbstractJiraPage
{
    private final static String URI = "/secure/admin/ViewApplicationProperties.jspa";

    private static final String STATUS_ON = "status-active";
    private static final String STATUS_OFF = "status-inactive";

    static final String ROW_TAG = "tr";
    static final String PROPERTY_ID = "property-id";

    static final String PROPERTY_JIRA_MODE = "jira-mode";
    static final String PROPERTY_CONTACT_ADMIN_FORM = "contact-admin-form";
    static final String PROPERTY_DISABLE_INLINE_EDIT = "disableInlineEdit";

    @ElementBy(id = "edit-app-properties")
    private PageElement editButton;

    @ElementBy(id = "options_table")
    private PageElement optionsTable;

    @ElementBy(tagName = "tr", within = "optionsTable")
    private Iterable<PageElement> optionRows;

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("options_table")).timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    public EditGeneralConfigurationPage edit()
    {
        editButton.click();
        return pageBinder.bind(EditGeneralConfigurationPage.class);
    }

    public boolean isContactAdminFormOn()
    {
        return isBooleanPropertyEnabled(PROPERTY_CONTACT_ADMIN_FORM);
    }

    public boolean isBooleanPropertyEnabled(final String propertyId)
    {
        return findPropertyCell(propertyId).hasClass(STATUS_ON);
    }

    public boolean isInlineEditPresent()
    {
        return findPropertyCell(PROPERTY_DISABLE_INLINE_EDIT).isPresent();
    }

    public PageElement findPropertyCell(final String propertyId)
    {
        return elementFinder.find(ByDataAttribute.byTagAndData(ROW_TAG, PROPERTY_ID, propertyId));
    }

    public Iterable<OptionRow> getOptions()
    {
        return Iterables.transform(optionRows, PageElements.bind(pageBinder, OptionRow.class));
    }

    public Optional<OptionRow> getOption(final String name)
    {
        return Optional.fromNullable(Iterables.find(getOptions(), new Predicate<OptionRow>()
        {
            @Override
            public boolean apply(final OptionRow input)
            {
                return input.getName().equalsIgnoreCase(name);
            }
        }, null));
    }

    public static class OptionRow
    {
        private final String name;
        private final String value;

        public OptionRow(final PageElement element)
        {
            final List<PageElement> cells = element.findAll(By.tagName("td"));
            name = cells.get(0).getText();
            value = cells.get(1).getText();
        }

        public String getName()
        {
            return name;
        }

        public String getValue()
        {
            return value;
        }
    }
}
