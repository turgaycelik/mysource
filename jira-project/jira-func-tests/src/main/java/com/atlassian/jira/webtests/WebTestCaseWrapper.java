package com.atlassian.jira.webtests;

import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.util.ProgressPageControl;
import com.meterware.httpunit.HttpUnitOptions;
import net.sourceforge.jwebunit.WebTestCase;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class pretty much wraps the WebTestCase to dump out the response whenever the WebTestFails. A definite candidate
 * for some simple AOP work :)
 */
public class WebTestCaseWrapper extends WebTestCase
{
    //---------------------------------------------------------------------------------------------------- static helper

    public static void raiseRuntimeException(Throwable e)
    {
        if (e instanceof Error)
        {
            throw (Error) e;
        }
        if (e instanceof RuntimeException)
        {
            throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
    }

    //------------------------------------------------------------------------------------------------------------ ctors

    public WebTestCaseWrapper(String name)
    {
        super(name);
    }

    public WebTestCaseWrapper()
    {
    }

    // ------------------------------------------------------------------------------------------ Wrapped Assert Methods

    /**
     * Clicks the standard JIRA form's cancel button. Note: this requires
     * {@link HttpUnitOptions#setScriptingEnabled(boolean)} to be set true BEFORE
     * the containing page is loaded. If you do this, make sure you set back in
     * a try/finally block
     */
    public void clickCancelButton()
    {
        assertTrue("Scripting must be enabled in the HttpUnit for cancel to work correctly", HttpUnitOptions.isScriptingEnabled());
        clickLink("cancelButton");
    }

    public void assertTextPresent(String text)
    {
        log("Asserting text present: " + text);
        super.assertTextPresent(text);
    }


    public void assertTextNotPresent(String text)
    {
        log("Asserting text *not* present: " + text);
        super.assertTextNotPresent(text);
    }

    public void assertTextInTable(String tableSummaryOrId, String text)
    {
        log("Asserting text present in table (" + tableSummaryOrId + ") : " + text);
        super.assertTextInTable(tableSummaryOrId, text);
    }


    public void assertElementPresent(String anID)
    {
        Locator xPathLocator = new XPathLocator(tester, "//*[@id='" + anID + "']");
        assertTrue("Could not find any elements with the id '" + anID + "'", xPathLocator.getNodes().length > 0);
    }


    public void assertElementNotPresent(String anID)
    {
        Locator xPathLocator = new XPathLocator(tester, "//*[@id='" + anID + "']");
        assertTrue("Found an element with the id '" + anID + "' - was expecting none", xPathLocator.getNodes().length == 0);
    }

    public void assertElementPresentBy(String attrName, String attrValue)
    {
        Locator xPathLocator = new XPathLocator(tester, "//*[@" + attrName + "='" + attrValue + "']");
        assertTrue("Could not find any elements with " + attrName + " = '" + attrValue + "'", xPathLocator.getNodes().length > 0);
    }

    public void assertElementNotPresentBy(String attrName, String attrValue)
    {
        Locator xPathLocator = new XPathLocator(tester, "//*[@" + attrName + "='" + attrValue + "']");
        assertTrue("Could not find any elements with " + attrName + " = '" + attrValue + "'", xPathLocator.getNodes().length == 0);
    }


    // ----------------------------------------------------------------------------------------------- RuntimeExceptions
    public void selectMultiOption(String selectName, String option)
    {
        // A bit of a hack. The only way to really select multiple options at the moment is to treat it like a checkbox
        String value = getDialog().getValueForOption(selectName, option);
        checkCheckbox(selectName, value);
    }

    /**
     * Selects an option from a multiple select field by specifying the value of the desired option.
     *
     * @param selectName the name of the select form element.
     * @param value      the value of the option to be selected.
     */
    public void selectMultiOptionByValue(String selectName, String value)
    {
        checkCheckbox(selectName, value);
    }

    /**
     * Selects an option from a select form field by specifying the visible option label (as opposed to the value)
     *
     * @param selectName the name of the select form element.
     * @param option     the string label for the option to be selected.
     */
    public void selectOption(String selectName, String option)
    {
        super.selectOption(selectName, option);
    }

    /**
     * Asserts that an option with the given option label is present on the
     * given select form element.
     *
     * @param selectName  the name of the select form element.
     * @param optionLabel the value of the option to assert on.
     */
    public void assertOptionValuePresent(String selectName, String optionLabel)
    {
        if (!getDialog().hasRadioOption(selectName, optionLabel))
        {
            fail("Option with value " + optionLabel + " not found for select name " + selectName);
        }
    }

    /**
     * Asserts that an option with the given option label is not present on the
     * given select form element.
     *
     * @param selectName the name of the select form element.
     * @param option the value of the option to assert on.
     * @deprecated Use {@link net.sourceforge.jwebunit.WebTester#assertRadioOptionValueNotPresent(String, String)}
     */
    public void assertOptionValueNotPresent(String selectName, String option)
    {
        if (getDialog().hasRadioOption(selectName, option))
        {
            fail("Option with value " + option + " found for select name " + selectName);
        }
    }

    /**
     * Submits the current form using the button with the given name.
     *
     * @param buttonName The name attribute on the submit input element.
     * @throws RuntimeException if the form or button is not present.
     */
    public void submit(String buttonName)
    {
        try
        {
            super.submit(buttonName);
        }
        catch (Throwable e)
        {
            raiseRuntimeException(e);
        }
    }

    public void waitAndReloadBulkOperationProgressPage()
    {
        waitAndReloadBulkOperationProgressPage(false);
    }

    public void waitAndReloadBulkOperationProgressPage(boolean optional)
    {
        waitAndReloadBulkOperationProgressPage(optional, tester);
    }

    public void waitAndReloadBulkOperationProgressPage(boolean optional, WebTester webTester)
    {
        if (optional && !webTester.getDialog().getResponsePageTitle().contains("Bulk Operation"))
            return;
        assertTextPresent("Bulk Operation Progress");
        ProgressPageControl.waitAndReload(webTester, "bulkoperationprogressform", "Refresh", "Acknowledge");
        log("waitAndReloadBulkOperationProgressPage");
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods

    public static void log(String msg)
    {
        FuncTestOut.log(msg);
    }

    public static void logSection(String msg)
    {
        log("");
        log(StringUtils.repeat("-", msg.length()));
        log(msg);
    }

    public static void log(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        log(sw.toString());
    }

    public static void log(String msg, Throwable t)
    {
        log(msg);
        log(t);
    }

    public void dumpResponse()
    {
        super.dumpResponse(FuncTestOut.out);
    }

    public void dumpResponse(Throwable t)
    {
        if (t != null)
        {
            t.printStackTrace(FuncTestOut.out);
        }
        super.dumpResponse(FuncTestOut.out);
    }

    public void tearDown()
    {
        tester = null;
        try
        {
            super.tearDown();
        }
        catch (Throwable t)
        {
            raiseRuntimeException(t);
        }
    }
}
