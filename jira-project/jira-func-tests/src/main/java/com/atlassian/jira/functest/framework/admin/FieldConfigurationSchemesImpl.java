package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Form;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.util.url.URLUtil;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.WebLink;
import net.sourceforge.jwebunit.WebTester;

import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * @since v4.0
 */
public class FieldConfigurationSchemesImpl extends AbstractFuncTestUtil implements FieldConfigurationSchemes, FieldConfigurationSchemes.FieldConfigurationScheme
{
    private String fieldConfigurationSchemeId;
    private String fieldConfigurationSchemeName;

    public FieldConfigurationSchemesImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    protected Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }

    protected Form getForm()
    {
        return getFuncTestHelperFactory().getForm();
    }

    public String addFieldConfigurationScheme(final String name, final String description)
    {
        getNavigation().gotoAdminSection("issue_fields");
        tester.clickLink("add-field-configuration-scheme");
        tester.setFormElement("fieldLayoutSchemeName", name);
        if (description != null)
        {
            tester.setFormElement("fieldLayoutSchemeDescription", description);
        }
        tester.submit("Add");

        return trimToNull(locators.xpath("//*[@data-scheme-field='name']/@data-id").getText());
    }

    public FieldConfigurationScheme fieldConfigurationScheme(final String name)
    {
        getNavigation().gotoAdminSection("issue_fields");
        this.fieldConfigurationSchemeId = getSchemeIdForName(name);
        this.fieldConfigurationSchemeName = name;
        return this;
    }

    @Override
    public FieldConfigurationScheme goTo()
    {
        getNavigation().gotoAdminSection("issue_fields");
        getNavigation().clickLinkWithExactText(fieldConfigurationSchemeName);
        return this;
    }

    public void addAssociation(final String issueTypeId, final String fieldConfigurationName)
    {
        getNavigation().gotoAdminSection("issue_fields");
        getNavigation().clickLinkWithExactText(fieldConfigurationSchemeName);
        tester.clickLink("add-issue-type-field-configuration-association");
        tester.setWorkingForm("add-issue-type-field-configuration-association-form");
        getForm().selectOptionsByValue("issueTypeId", new String[] {issueTypeId});
        tester.selectOption("fieldConfigurationId", fieldConfigurationName);
        tester.submit("Add");
        tester.assertLinkPresent("edit_fieldlayoutschemeentity_" + issueTypeId);
    }

    public void editAssociation(final String issueTypeId, final String newFieldConfigurationName)
    {
        getNavigation().gotoAdminSection("issue_fields");
        getNavigation().clickLinkWithExactText(fieldConfigurationSchemeName);

        String editLink = "edit_fieldlayoutschemeentity";
        if (issueTypeId != null)
        {
            editLink += "_" + issueTypeId;
        }
        tester.clickLink(editLink);

        tester.setWorkingForm("jiraform");
        tester.selectOption("fieldConfigurationId", newFieldConfigurationName);
        tester.submit("Update");
    }

    public void removeAssociation(final String issueTypeId)
    {
        getNavigation().gotoAdminSection("issue_fields");
        getNavigation().clickLinkWithExactText(fieldConfigurationSchemeName);
        tester.clickLink("delete_fieldlayoutschemeentity_" + issueTypeId);
        tester.assertLinkNotPresent("delete_fieldlayoutschemeentity_" + issueTypeId);
    }

    private String getSchemeIdForName(final String fieldConfigSchemeName)
    {
        HtmlPage page = new HtmlPage(tester);
        final WebLink schemeLink = page.getLinksWithExactText(fieldConfigSchemeName)[0];
        return URLUtil.getQueryParamValueFromUrl(schemeLink.getURLString(), "id");
    }
}
