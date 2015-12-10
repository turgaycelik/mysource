package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.LocatorEntry;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

import static junit.framework.Assert.fail;

/**
 * And implementation of {@link JiraFormAssertions}.
 *
 * @since v3.13
 */
public class JiraFormAssertionsImpl extends AbstractFuncTestUtil implements JiraFormAssertions
{
    private static final String AUI_MESSAGE_ERROR_CSS = ".aui-message.error";
    private static final String AUI_MESSAGE_WARNING_CSS = ".aui-message.warning";
    private static final String AUI_MESSAGE_INFO_CSS = ".aui-message.info";

    private final TextAssertions textAssertions;

    public JiraFormAssertionsImpl(TextAssertions textAssertions, WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
        this.textAssertions = textAssertions;
    }

    public void assertFieldErrMsg(final String expectedText)
    {
        Locator error = createFieldErrorMessageLocator();
        textAssertions.assertTextPresent(error, expectedText);
    }

    public void assertAuiFieldErrMsg(final String expectedText)
    {
        Locator error = createAuiFieldErrorMessageLocator();
        textAssertions.assertTextPresent(error, expectedText);
    }

    public void assertFormErrMsg(final String expectedText)
    {
        Locator error = createFormErrorMessageLocator();
        textAssertions.assertTextPresent(error, expectedText);
    }

    public void assertFormErrMsgContainsLink(String linkExactText)
    {
        final Locator linksInErrorMsg = locators.css(AUI_MESSAGE_ERROR_CSS + " a");
        if (!nodeWithTextExists(linkExactText, linksInErrorMsg))
        {
            fail("Link with text '" + linkExactText + "' not found in any error message");
        }
    }

    private boolean nodeWithTextExists(String linkExactText, Locator linksInErrorMsg)
    {
        for (LocatorEntry link : linksInErrorMsg.allMatches())
        {
            if (linkExactText.equals(link.getText()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void assertNoFieldErrMsg(final String notExpectedText)
    {
        textAssertions.assertTextNotPresent(createFieldErrorMessageLocator(), notExpectedText);
    }

    @Override
    public void assertNoFormErrMsg(final String notExpectedText)
    {
        textAssertions.assertTextNotPresent(createFormErrorMessageLocator(), notExpectedText);
    }

    @Override
    public void assertNoErrorsPresent()
    {
        Locator errorLocator = createFieldErrorMessageLocator();
        Assert.assertNull("Expected no errors on the page, but there was a field with an error.", errorLocator.getNode());

        errorLocator = createFormErrorMessageLocator();
        Assert.assertNull("Expected no errors on the page, but the page had a global error.", errorLocator.getNode());
    }

    @Override
    public void assertSelectElementHasOptionSelected(final String selectElementName, final String optionName)
    {
        final String actual = tester.getDialog().getSelectedOption(selectElementName);
        Assert.assertEquals(
                "Expected option selected '" + optionName + "' was not selected in form element '" + selectElementName + "'. Actual selected option was '" + actual + "'.",
                optionName, actual);
    }

    @Override
    public void assertFormNotificationMsg(final String expectedText)
    {
        Locator notification = createFormNotificationMessageLocator();
        textAssertions.assertTextPresent(notification, expectedText);
    }

    @Override
    public void assertNoFormNotificationMsg(final String notExpectedText)
    {
        textAssertions.assertTextNotPresent(createFormNotificationMessageLocator(), notExpectedText);
    }

    public void assertFormNotificationMsgContainsLink(String linkExactText)
    {
        final Locator linksInErrorMsg = locators.css(AUI_MESSAGE_INFO_CSS + " a");
        if (!nodeWithTextExists(linkExactText, linksInErrorMsg))
        {
            fail("Link with text '" + linkExactText + "' not found in any notification message");
        }
    }

    @Override
    public void assertFormSuccessMsg(String expectedText)
    {
        Locator notification = createFormSuccessMessageLocator();
        textAssertions.assertTextPresent(notification, expectedText);
    }

    @Override
    public void assertFormWarningMessage(String messageText)
    {
        textAssertions.assertTextPresent(createFormWarningMessageLocator(), messageText);
    }

    @Override
    public void assertNoFormWarningMessage(final String notExpectedText)
    {
        textAssertions.assertTextNotPresent(createFormWarningMessageLocator(), notExpectedText);
    }

    public void assertFormWarningMessageContainsLink(String linkExactText)
    {
        final Locator linksInMsg = locators.css(AUI_MESSAGE_WARNING_CSS + " a");
        if (!nodeWithTextExists(linkExactText, linksInMsg))
        {
            fail("Link with text '" + linkExactText + "' not found in any warning message");
        }
    }

    private Locator createFieldErrorMessageLocator()
    {
        return new XPathLocator(tester, "//span[@class='errMsg']");
    }

    private Locator createAuiFieldErrorMessageLocator()
    {
        return new XPathLocator(tester, "//form[@class='aui']//div[@class='field-group']/div[@class='error']");
    }

    private Locator createFormErrorMessageLocator()
    {
        return locators.css(AUI_MESSAGE_ERROR_CSS);
    }

    private Locator createFormNotificationMessageLocator()
    {
        return locators.css(AUI_MESSAGE_INFO_CSS);
    }

    private Locator createFormSuccessMessageLocator()
    {
        return locators.css(".aui-message.success");
    }

    private Locator createFormWarningMessageLocator()
    {
        return locators.css(AUI_MESSAGE_WARNING_CSS);
    }
}