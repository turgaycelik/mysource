package com.atlassian.jira.functest.framework.assertions;

/**
 * An assertions helper for working with JIRA forms
 *
 * @since v3.13
 */
public interface JiraFormAssertions
{
    /**
     * Asserts that there is a "field" error message with the <tt>expectedText</tt>.
     *
     * @param expectedText the expected error message
     */
    void assertFieldErrMsg(String expectedText);

    /**
     * Asserts that there is a "field" error message with the <tt>expectedText</tt> in an AUI form.
     *
     * @param expectedText the expected error message
     */
    void assertAuiFieldErrMsg(String expectedText);

    /**
     * Asserts that there is a "form" error message with the <tt>expectedText</tt>.
     *
     * @param expectedText the expected error message
     */
    void assertFormErrMsg(String expectedText);

    /**
     * Assert that error message on the form contains link with given exact text.
     *
     * @param linkExactText text of the expected link
     */
    public void assertFormErrMsgContainsLink(String linkExactText);

    /**
     * Asserts that there is <b>NO</b> "field" error message with the <tt>notExpectedText</tt>.
     *
     * @param notExpectedText the expected error message
     */
    void assertNoFieldErrMsg(String notExpectedText);

    /**
     * Asserts that there is <b>NO</b> "form" error message with the <tt>notExpectedText</tt>.
     *
     * @param notExpectedText the 'not' expected error message (or part of it)
     */
    void assertNoFormErrMsg(String notExpectedText);

    /**
     * Assert that the form contains warning message with given text.
     *
     * @param messageText text of the message
     */
    void assertFormWarningMessage(String messageText);

    /**
     * Asserts that there is <b>NO</b> "form" warning message with the <tt>notExpectedText</tt>.
     *
     * @param notExpectedText the 'not' expected warning message (or part of it)
     */
    void assertNoFormWarningMessage(String notExpectedText);

    /**
     * Assert that warning message on the form contains link with given exact text.
     *
     * @param linkExactText text of the expected link
     */
    public void assertFormWarningMessageContainsLink(String linkExactText);

    /**
     * Asserts that there is no errors present.
     */
    void assertNoErrorsPresent();

    /**
     * Asserts that there is a "form" notification message with the <tt>expectedText</tt>.
     *
     * @param expectedText the expected notification message
     */
    void assertFormNotificationMsg(String expectedText);

    /**
     * Asserts that there is <b>NO</b> "form" notification message with the <tt>notExpectedText</tt>.
     *
     * @param notExpectedText the 'not' expected notification message (or part of it)
     */
    void assertNoFormNotificationMsg(String notExpectedText);

    /**
     * Assert that notification message on the form contains link with given exact text.
     *
     * @param linkExactText text of the expected link
     */
    public void assertFormNotificationMsgContainsLink(String linkExactText);

    /**
     * Asserts that there is a "form" success message with the <tt>expectedText</tt>
     * @param expectedText the expected notification message
     */
    void assertFormSuccessMsg(String expectedText);

    /**
     * Asserts that the specified Select form element has this option selected.
     *
     * @param selectElementName the name of the &lt;select&gt; element.
     * @param optionName the name of the option that should be selected (not the value of the option).
     */
    void assertSelectElementHasOptionSelected(final String selectElementName, final String optionName);
}
