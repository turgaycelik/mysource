package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Form;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.functest.framework.util.url.URLUtil;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * @since v4.0
 */
public class CustomFieldsImpl extends AbstractFuncTestUtil implements CustomFields
{
    private final Navigation navigation;
    private final Form form;

    public CustomFieldsImpl(WebTester tester, JIRAEnvironmentData environmentData, Navigation navigation, final Form form)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
        this.form = form;
    }

    public String addCustomField(final String fieldType, final String fieldName)
    {
        return addCustomField(fieldType, fieldName, new String[] {}, new String[] {});
    }

    public String addCustomField(final String fieldType, final String fieldName, final String[] issueTypes, final String[] projects)
    {
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("add_custom_fields");
        tester.setFormElement("fieldType", fieldType);
        tester.submit("nextBtn");

        tester.setFormElement("fieldName", fieldName);
        if (issueTypes.length == 0)
        {
            form.selectOption("issuetypes", "Any issue type");
        }
        else
        {
            form.selectOptionsByValue("issuetypes", issueTypes);
        }

        if (projects.length == 0)
        {
            tester.checkCheckbox("global", "true");
        }
        else
        {
            tester.checkCheckbox("global", "false");
        }
        form.selectOptionsByValue("projects", projects);
        tester.submit("nextBtn");

        final String customFieldId = getQueryParamValueFromResponse("fieldId");

        tester.checkCheckbox("associatedScreens", "1");
        tester.submit("Update");

        return customFieldId;
    }

    public String setCustomFieldSearcher(final String customFieldId, String searcherKey)
    {
        // null indicates we want to remove the searcher
        if (searcherKey == null)
        {
            searcherKey = "-1";
        }

        tester.gotoPage("/secure/admin/EditCustomField!default.jspa?id=" + customFieldId);
        tester.setWorkingForm("jiraform");

        String oldSearcher = tester.getDialog().getForm().getParameterValue("searcher");
        tester.setFormElement("searcher", searcherKey);
        tester.submit("Update");

        return oldSearcher;
    }

    public String renameCustomField(final String numericCustomFieldId, final String newCustomFieldName)
    {
        tester.gotoPage("/secure/admin/EditCustomField!default.jspa?id=" + numericCustomFieldId);
        tester.setWorkingForm("jiraform");

        String oldName = tester.getDialog().getForm().getParameterValue("name");
        tester.setFormElement("name", newCustomFieldName);
        tester.submit("Update");

        return oldName;
    }

    public String addConfigurationSchemeContext(final String customFieldId, final String label, final String[] issueTypeIds, final String[] projectIds)
    {
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("config_customfield_" + customFieldId);
        tester.clickLinkWithText("Add new context");

        updateConfigurationSchemeContext(label, issueTypeIds, projectIds, true);
        return getSchemeIdForLabel(customFieldId, label);
    }

    private String getSchemeIdForLabel(final String numericCustomFieldId, final String label)
    {
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("config_customfield_" + numericCustomFieldId);
        final XPathLocator locator = new XPathLocator(tester, "//td[@class='jiraformheader']/h3[text() = '" + label + "']");
        if (!locator.exists())
        {
            Assert.fail("Could not find label '" + label + "' for custom field '" + numericCustomFieldId + "'");
            return null;
        }
        final Element table = DomKit.getFirstParentByTag((Element) locator.getNodes()[0], "table");
        final String schemeTableId = table.getAttribute("id");
        // id is of form 'configschemeXXXXXXX'
        return schemeTableId.substring(12);
    }

    public void editConfigurationSchemeContextById(final String customFieldId, final String fieldConfigSchemeId, final String label, final String[] issueTypeIds, final String[] projectIds)
    {
        tester.gotoPage("/secure/admin/ManageConfigurationScheme!default.jspa?fieldConfigSchemeId=" + fieldConfigSchemeId + "&customFieldId=" + customFieldId);

        updateConfigurationSchemeContext(label, issueTypeIds, projectIds, false);
    }

    public void editConfigurationSchemeContextByLabel(final String numericCustomFieldId, final String label, final String newLabel, final String[] issueTypeIds, final String[] projectIds)
    {
        editConfigurationSchemeContextById(numericCustomFieldId, getSchemeIdForLabel(numericCustomFieldId, label), newLabel, issueTypeIds, projectIds);
    }

    private void updateConfigurationSchemeContext(final String label, final String[] issueTypeIds, final String[] projectIds, final boolean isAdd)
    {
        tester.setWorkingForm("jiraform");

        if (label != null)
        {
            tester.setFormElement("name", label);
        }

        if (issueTypeIds != null)
        {
            if (issueTypeIds.length == 0)
            {
                form.selectOption("issuetypes", "Any issue type");
            }
            else
            {
                form.selectOptionsByValue("issuetypes", issueTypeIds);
            }
        }

        if (projectIds != null)
        {
            final XPathLocator locator = new XPathLocator(tester, "//input[@id='global_true']");
            if (locator.exists())
            {
                if (projectIds.length == 0)
                {
                    tester.checkCheckbox("global", "true");
                }
                else
                {
                    tester.checkCheckbox("global", "false");
                }
            }
            form.selectOptionsByValue("projects", projectIds);
        }

        if (isAdd)
        {
            tester.submit("Add");
        }
        else
        {
            tester.submit("Modify");
        }
    }

    public void removeGlobalContext(final String customFieldId)
    {
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("config_customfield_" + customFieldId);

        XPathLocator locator = new XPathLocator(tester, "//a[@title = 'Delete Scheme']");
        if (locator.getNodes().length == 0)
        {
            throw new IllegalArgumentException("Could not find any links with the title 'Delete Scheme'.");
        }

        String deleteHref = null;
        for (Node node : locator.getNodes())
        {
            final Element table = DomKit.getFirstParentByTag((Element) node, "table");
            if (table != null)
            {
                XPathLocator globLoc = new XPathLocator(table, ".//dd[text() = 'Global (all issues)']");
                if (globLoc.getNodes().length > 0)
                {
                    deleteHref = node.getAttributes().getNamedItem("href").getNodeValue();
                    break;
                }
            }
        }

        if (deleteHref != null)
        {
            tester.gotoPage("/secure/admin/" + deleteHref);
        }
    }

    public void removeConfigurationSchemeContextById(final String numericCustomFieldId, final String fieldConfigSchemeId)
    {
        tester.gotoPage("secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + numericCustomFieldId);
        tester.clickLink("delete_" + fieldConfigSchemeId);
    }

    public void removeConfigurationSchemeContextByLabel(final String numericCustomFieldId, final String fieldConfigSchemeLabel)
    {
        removeConfigurationSchemeContextById(numericCustomFieldId, getSchemeIdForLabel(numericCustomFieldId, fieldConfigSchemeLabel));
    }

    public void removeCustomField(final String customFieldId)
    {
        final String deleteLink = "del_" + customFieldId;
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink(deleteLink);
        tester.submit("Delete");
        tester.assertLinkNotPresent(deleteLink);
    }

    public void addOptions(final String numericCustomFieldId, final String... options)
    {
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + numericCustomFieldId);
        tester.clickLinkWithText("Edit Options");
        for (String option : options)
        {
            tester.setWorkingForm("jiraform");
            tester.setFormElement("addValue", option);
            tester.submit();
        }
    }

    public void setDefaultValue(String numericCustomFieldId, String defValue)
    {
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + numericCustomFieldId);
        navigation.clickLinkWithExactText("Edit Default Value");
        tester.setWorkingForm("jiraform");
        tester.setFormElement("customfield_" + numericCustomFieldId, defValue);
        tester.submit();
    }

    public void removeOptions(final String numericCustomFieldId, final String... options)
    {
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + numericCustomFieldId);
        tester.clickLinkWithText("Edit Options");
        for (String option : options)
        {
            tester.clickLink("del_" + option);
            tester.submit("Delete");
        }
    }

    public void disableOptions(final String numericCustomFieldId, final String... options)
    {
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + numericCustomFieldId);
        tester.clickLinkWithText("Edit Options");
        for (String option : options)
        {
            tester.clickLink("disable_" + option);
        }
    }

    public void enableOptions(final String numericCustomFieldId, final String... options)
    {
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + numericCustomFieldId);
        tester.clickLinkWithText("Edit Options");
        for (String option : options)
        {
            tester.clickLink("enable_" + option);
        }
    }

    public void editOptionValue(String numericCustomFieldId, String option, String newValue)
    {
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + numericCustomFieldId);
        tester.clickLinkWithText("Edit Options");
        tester.clickLink("edit_" + option);
        tester.setFormElement("value", newValue);
        tester.submit("Update");
    }

    private String getQueryParamValueFromResponse(String paramName)
    {
        final URL url = tester.getDialog().getResponse().getURL();
        return URLUtil.getQueryParamValueFromUrl(url, paramName);
    }
}
