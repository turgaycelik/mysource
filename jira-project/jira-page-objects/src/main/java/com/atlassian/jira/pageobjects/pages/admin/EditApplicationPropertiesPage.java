package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Conditions.and;

public class EditApplicationPropertiesPage extends AbstractJiraPage
{
    @ElementBy (className = "jiraform")
    private PageElement table;

    @ElementBy (name = "Update", within = "table")
    private PageElement submitButton;

    @ElementBy (name = "title", within = "table")
    private PageElement applicationTitleField;

    @ElementBy (name = "baseURL", within = "table")
    private PageElement baseUrlField;

    @ElementBy (name = "emailFromHeaderFormat", within = "table")
    private PageElement emailFromHeaderFormat;

    @ElementBy (name = "mode")
    private PageElement jiraMode;

    @ElementBy (id = "useGzipOn")
    private PageElement gzipOnToggle;

    @ElementBy (id = "useGzipOff")
    private PageElement gzipOffToggle;

    @ElementBy (id = "allowUnassignedOff")
    private PageElement allowUnassignedOffToggle;

    @ElementBy (id = "allowUnassignedOn")
    private PageElement allowUnassignedOnToggle;

    @Override
    public String getUrl()
    {
        return "/secure/admin/jira/EditApplicationProperties!default.jspa";
    }

    /**
     * Timed condition checking if we're at given page.
     *
     * @return timed condition checking, if the test is at given page
     */
    @Override
    public TimedCondition isAt()
    {
        return and(table.timed().isPresent(), submitButton.timed().isPresent());
    }

    public String getApplicationTitle()
    {
        return applicationTitleField.getValue();
    }

    public EditApplicationPropertiesPage setApplicationTitle(final String value)
    {
        applicationTitleField.clear().type(value);
        return this;
    }

    public boolean isApplicationTitleFieldEnabled()
    {
        return applicationTitleField.isEnabled();
    }

    public String getBaseUrl()
    {
        return baseUrlField.getValue();
    }

    public String getEmailFromHeaderFormat()
    {
        return emailFromHeaderFormat.getValue();
    }

    public EditApplicationPropertiesPage setBaseUrl(final String value)
    {
        baseUrlField.clear().type(value);
        return this;
    }

    public EditApplicationPropertiesPage setEmailFromHeaderFormat(final String value)
    {
        emailFromHeaderFormat.clear().type(value);
        return this;
    }

    public EditApplicationPropertiesPage setTitle(final String value)
    {
        applicationTitleField.clear().type(value);
        return this;
    }

    public boolean isBaseUrlFieldEnabled()
    {
        return baseUrlField.isEnabled();
    }

    public Boolean isGzipCompressionEnabled()
    {
        return isRadioOptionEnabled(gzipOnToggle, gzipOffToggle);
    }

    public EditApplicationPropertiesPage setGzipCompression(final boolean to)
    {
        return setRadioOption(to, gzipOnToggle, gzipOffToggle);
    }

    public EditApplicationPropertiesPage setAllowUnassigned(final boolean to)
    {
        return setRadioOption(to, allowUnassignedOnToggle, allowUnassignedOffToggle);
    }

    public Boolean isGzipCompressionFieldEnabled()
    {
        return isRadioFieldEnabled(gzipOnToggle, gzipOffToggle);
    }

    public void submit()
    {
        submitButton.click();
    }

    public ViewGeneralConfigurationPage submitAndBind()
    {
        submit();
        return pageBinder.bind(ViewGeneralConfigurationPage.class);
    }

    private boolean isRadioOptionEnabled(final PageElement enablingElement, final PageElement disablingElement)
    {
        if (enablingElement.isSelected() ^ disablingElement.isSelected())
        {
            return enablingElement.isSelected() && !disablingElement.isSelected();
        }
        throw new IllegalStateException(enablingElement.getAttribute("name") + " option is neither on or off.");
    }

    private Boolean isRadioFieldEnabled(final PageElement enablingElement, final PageElement disablingElement)
    {
        final boolean whether = enablingElement.isEnabled();
        if (whether == disablingElement.isEnabled())
        {
            return whether;
        }
        throw new IllegalStateException(enablingElement.getAttribute("name") + " option has only one option enabled.");
    }

    private EditApplicationPropertiesPage setRadioOption(final boolean to, final PageElement enablingElement,
            final PageElement disablingElement)
    {
        final PageElement toggle = to ? enablingElement : disablingElement;
        toggle.select();
        return this;
    }

    public Boolean isPublicSignupEnabled()
    {
        return "public".equals(jiraMode.getValue().trim());
    }

    public EditApplicationPropertiesPage setPublicSignup(final boolean toState)
    {
        if (toState)
        {
            // select public
            jiraMode.find(By.cssSelector("option[value=public]")).select();
        }
        else
        {
            // select private
            jiraMode.find(By.cssSelector("option[value=private]")).select();
        }
        return this;
    }
}
