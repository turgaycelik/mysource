package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @since v4.0
 */
public class FieldConfigurationsImpl extends AbstractFuncTestUtil implements FieldConfigurations, FieldConfigurations.FieldConfiguration
{
    public FieldConfigurationsImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    protected Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }

    public FieldConfiguration defaultFieldConfiguration()
    {
        getNavigation().gotoAdminSection("field_configuration");
        tester.clickLink("configure-Default Field Configuration");
        return this;
    }

    public FieldConfiguration fieldConfiguration(final String fieldConfigurationName)
    {
        getNavigation().gotoAdminSection("field_configuration");
        tester.clickLink("configure-" + fieldConfigurationName);
        return this;
    }

    public void showField(final int id)
    {
        tester.clickLink("show_" + id);
    }

    public void showFields(final String name)
    {
        clickLinkForFieldWithText(name, "Show");
    }

    public void hideField(final int id)
    {
        tester.clickLink("hide_" + id);
    }

    public void hideFields(final String name)
    {
        clickLinkForFieldWithText(name, "Hide");
    }

    public void requireField(final String name)
    {
        clickLinkForFieldWithText(name, "Required");
    }

    public void optionalField(final String name)
    {
        clickLinkForFieldWithText(name, "Optional");
    }

    public String getRenderer(final String fieldName)
    {
        clickLinkForFieldWithText(fieldName, "Renderers");
        return tester.getDialog().getSelectedOption("selectedRendererType");
    }

    public void setRenderer(final String fieldName, final String rendererName)
    {
        clickLinkForFieldWithText(fieldName, "Renderers");
        tester.selectOption("selectedRendererType", rendererName);
        tester.submit();

        // if we get the "Are you really sure you want to do this" screen, we have to submit again
        try
        {
            if (tester.getDialog().getResponse().getFormWithName("jiraform") != null)
            {
                tester.setWorkingForm("jiraform");
                tester.submit();
            }
        }
        catch (SAXException e)
        {
            // ignore and return
        }
    }

    public FieldScreenAssociations getScreens(final String name)
    {
        clickLinkForFieldWithText(name, "Screens");
        return new FieldScreenAssociationsImpl(tester, environmentData);
    }

    private void clickLinkForFieldWithText(final String name, final String urlText)
    {
        //Find all the links in the 'field_table' that have a title with our field name and whose content is the operation we are looking at.
        final XPathLocator locator = new XPathLocator(tester, String.format("//table[@id = 'field_table']//a[contains(@title, \"'%s'\") and contains(text(), '%s')]/@id", name, urlText));
        final Node nodes[] = locator.getNodes();
        if (nodes == null || nodes.length == 0)
        {
            throw new AssertionError(String.format("Unable to %s field '%s': Could not find the '%s' link.", urlText, name, urlText));
        }

        for (Node node : nodes)
        {
            final String id = node.getNodeValue();
            if (StringUtils.isNotBlank(id))
            {
                tester.clickLink(id);
            }
        }
    }
}
