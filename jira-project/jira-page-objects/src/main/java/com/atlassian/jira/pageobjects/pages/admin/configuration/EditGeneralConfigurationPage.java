package com.atlassian.jira.pageobjects.pages.admin.configuration;

import com.atlassian.jira.testkit.client.model.JiraMode;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.by.ByDataAttribute;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

import java.util.List;

import static com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage.PROPERTY_CONTACT_ADMIN_FORM;
import static com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage.PROPERTY_DISABLE_INLINE_EDIT;
import static com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage.PROPERTY_ID;
import static com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage.PROPERTY_JIRA_MODE;
import static com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage.ROW_TAG;

/**
 * Edit General configuration properties.
 *
 * @since v5.1
 */
public class EditGeneralConfigurationPage extends AbstractJiraPage
{
    private final static String URI = "/secure/admin/EditApplicationProperties.jspa";

    static final String FIELD_VALUE_CLASS = "fieldValueArea";

    @ElementBy(id = "edit_property")
    protected PageElement submitButton;

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.cssSelector("form[action='EditApplicationProperties.jspa']")).timed().isPresent();
    }

    public String getUrl()
    {
        return URI;
    }

    public ViewGeneralConfigurationPage save()
    {
        return submit(ViewGeneralConfigurationPage.class);
    }

    public <P> P submit(Class<P> page, Object... args)
    {
        submitButton.click();
        return pageBinder.bind(page, args);
    }

    public JiraMode getJiraMode()
    {
        return JiraMode.fromValue(getSelect(PROPERTY_JIRA_MODE).getSelected().value());
    }

    public EditGeneralConfigurationPage setJiraMode(JiraMode mode)
    {
        getSelect(PROPERTY_JIRA_MODE).select(Options.value(mode.optionValue()));
        return this;
    }

    public boolean isContactAdminFormOn()
    {
        return getBooleanProperty(PROPERTY_CONTACT_ADMIN_FORM);
    }

    public EditGeneralConfigurationPage setContactAdminFormOn(boolean isOn)
    {
        setBooleanProperty(PROPERTY_CONTACT_ADMIN_FORM, isOn);
        return this;
    }

    public void setSelectProperty(String propertyId, String optionValue)
    {
        getSelect(propertyId).select(Options.value(optionValue));
    }

    public void setBooleanProperty(String propertyId, boolean value)
    {
        final BooleanValues values = getBooleanRadios(propertyId);
        if (values.currentValue() != value)
        {
            values.set(value);
        }
    }

    public boolean getBooleanProperty(String propertyId)
    {
        return getBooleanRadios(propertyId).currentValue();
    }

    protected SelectElement getSelect(String propertyId)
    {
        return findValueCell(propertyId).find(By.tagName("select"), SelectElement.class);
    }

    protected BooleanValues getBooleanRadios(String propertyId)
    {
        return new BooleanValues(findValueCell(propertyId));
    }

    public boolean isInlineEditPresent()
    {
        return findPropertyRow(PROPERTY_DISABLE_INLINE_EDIT).isPresent();
    }

    protected PageElement findValueCell(String propertyId)
    {
        return findPropertyRow(propertyId).find(By.className(FIELD_VALUE_CLASS));
    }

    protected PageElement findPropertyRow(String propertyId)
    {
        return elementFinder.find(ByDataAttribute.byTagAndData(ROW_TAG, PROPERTY_ID, propertyId));
    }

    private static final class BooleanValues
    {
        final PageElement on;
        final PageElement off;

        BooleanValues(PageElement valueCell)
        {
            final List<PageElement> radios = valueCell.findAll(By.className("radio"));
            this.on = Iterables.find(radios, PageElements.hasValue("true"));
            this.off = Iterables.find(radios, PageElements.hasValue("false"));
        }

        boolean currentValue()
        {
            return on.isSelected();
        }

        void set(boolean newValue)
        {
            if (newValue)
            {
                on.select();
            }
            else
            {
                off.select();
            }
        }

    }

}
