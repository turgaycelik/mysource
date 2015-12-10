package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.util.text.TextKit;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.HashMap;

/**
 * @since v4.2
 */
public class FieldScreenAssociationsImpl extends AbstractFuncTestUtil implements FieldScreenAssociations
{
    public FieldScreenAssociationsImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    public void removeFieldFromScreen(final String screen)
    {
        tester.setWorkingForm("jiraform");
        initCheckboxState("associatedScreens");
        tester.uncheckCheckbox("associatedScreens", getCheckboxValueForScreen(screen));
        tester.submit("Update");
    }

    public void addFieldToScreen(final String screen)
    {
        tester.setWorkingForm("jiraform");
        initCheckboxState("associatedScreens");
        tester.checkCheckbox("associatedScreens", getCheckboxValueForScreen(screen));
        tester.submit("Update");
    }

    /**
     * Initialises a checkbox control so that it knows which options are checked. By default, HttpUnit has trouble with
     * checkboxes which have mulitple values.
     *
     * @param name the name of the checkbox
     */
    private void initCheckboxState(final String name)
    {
        final XPathLocator checkboxLocator = new XPathLocator(tester, String.format("//input[@type='checkbox' and @name='%s']", name));
        final Node nodes[] = checkboxLocator.getNodes();
        if (nodes == null || nodes.length == 0)
        {
            throw new AssertionError("Could not locate checkboxes with name '" + name + "'");
        }

        final Map<String, Boolean> checkboxState = new HashMap<String, Boolean>();
        for (Node node : nodes)
        {
            final String value = node.getAttributes().getNamedItem("value").getNodeValue();
            boolean checked = false;
            final Node checkedAttribute = node.getAttributes().getNamedItem("checked");
            if (checkedAttribute != null)
            {
                checked = "checked".equalsIgnoreCase(checkedAttribute.getNodeValue());
            }
            checkboxState.put(value, checked);
        }

        tester.uncheckCheckbox(name);
        for (String value : checkboxState.keySet())
        {
            if (checkboxState.get(value))
            {
                tester.checkCheckbox(name, value);
            }
        }
    }

    /**
     * Looks up which row of the screens table a screen name appears in, finds the checkbox on that row and then returns
     * that checkbox's value.
     *
     * @param screen the name of the screen
     * @return the value of the checkbox (which represents the screen)
     */
    private String getCheckboxValueForScreen(final String screen)
    {
        final WebTable screenAssociations = tester.getDialog().getWebTableBySummaryOrId("screenAssociations");
        for (int row = 1; row < screenAssociations.getRowCount(); row++)
        {
            if (TextKit.equalsCollapseWhiteSpace(screen, screenAssociations.getCellAsText(row, 0)))
            {
                XPathLocator valueLocator = new XPathLocator(tester, String.format("//table[@id='screenAssociations']//tr[%d]/td[3]/input[@type='checkbox']/@value", row));
                final Node nodes[] = valueLocator.getNodes();
                if (nodes == null || nodes.length == 0 || nodes.length > 1)
                {
                    throw new AssertionError("Could not locate checkbox value");
                }

                return nodes[0].getNodeValue();
            }
        }

        throw new AssertionError("Could not find entry for Screen '" + screen + "'.");
    }
}
