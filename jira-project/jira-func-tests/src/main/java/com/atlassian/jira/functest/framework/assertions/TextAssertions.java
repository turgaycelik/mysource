package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.locator.Locator;

/**
 * Contains a number of methods that do "extended" functional test assertions involving text.
 *
 * @see Assertions
 * @since v3.13
 */
public interface TextAssertions
{
    /**
     * Asserts that 'expectedText' can be found in the current page.
     *
     * @param expectedText the expected text
     */
    public void assertTextPresent(final String expectedText);

    /**
     * Asserts that 'expectedText' can be found in the current page in a "HTML Encoded" form.
     * <p>
     *     The encoding expects <, >, ", ', and & to be encoded to appropriate HTML encoding.
     * </p>
     *
     * @param expectedText the expected text
     */
    void assertTextPresentHtmlEncoded(String expectedText);

    /**
     * Asserts that the given text cannot be found in the current page.
     *
     * @param text the expected text
     */
    public void assertTextNotPresent(final String text);

    /**
     * Asserts that 'expectedText' can be found in 'srcText'.
     *
     * @param srcText      the text to search in
     * @param expectedText the expected text
     */
    public void assertTextPresent(String srcText, String expectedText);

    /**
     * Synonym for {@link TextAssertions#assertTextPresent(String,String)} that calls getText() on the provided
     * locator.
     *
     * @param locator      the source of the text
     * @param expectedText the expected text
     */
    public void assertTextPresent(Locator locator, String expectedText);

    /**
     * Asserts that 'expectedText' can be found in the current page exactly 'numOccurences' times.
     *
     * @param expectedText the expected text
     * @param numOccurences the number of times the expected text should occur; must be greater than 0.
     */
    public void assertTextPresentNumOccurences(String expectedText, int numOccurences);

    /**
     * Asserts that 'expectedText' can be found in 'srcText' exactly 'numOccurences' times.
     *
     * @param srcText      the text to search in
     * @param expectedText the expected text
     * @param numOccurences the number of times the expected text should occur; must be greater than 0.
     */
    public void assertTextPresentNumOccurences(String srcText, String expectedText, int numOccurences);

    /**
     * Synonym for {@link TextAssertions#assertTextPresentNumOccurences(String,String,int)} that calls getText() on the provided
     * locator.
     *
     * @param locator      the source of the text
     * @param expectedText the expected text
     * @param numOccurences the number of times the expected text should occur; must be greater than 0.
     */
    public void assertTextPresentNumOccurences(Locator locator, String expectedText, int numOccurences);

    /**
     * This asserets that the regular expression pattern 'regexPattern' has at least one match somewhere inside 'srcText'
     *
     * @param srcText      the text source to do the matching in
     * @param regexPattern the regex pattern
     */
    public void assertRegexMatch(String srcText, String regexPattern);

    /**
     * This asserets that the regular expression pattern 'regexPattern' has NO match somewhere inside 'srcText'
     *
     * @param srcText      the text source to do the matching in
     * @param regexPattern the regex pattern
     */
    public void assertRegexNoMatch(String srcText, String regexPattern);

    /**
     * Synonym for {@link TextAssertions#assertRegexMatch(String,String)} that calls getText() on the provided
     * locator.
     *
     * @param locator      the source of the teext
     * @param regexPattern the regex pattern
     */
    public void assertRegexMatch(Locator locator, String regexPattern);

    /**
     * Synonym for {@link TextAssertions#assertRegexNoMatch(String,String)} that calls getText() on the provided
     * locator.
     *
     * @param locator      the source of the teext
     * @param regexPattern the regex pattern
     */
    public void assertRegexNoMatch(Locator locator, String regexPattern);

    /**
     * Asserts that 'expectedText' can NOT be found in 'srcText'.
     *
     * @param srcText      the text to search in
     * @param expectedText the expected text
     */
    public void assertTextNotPresent(String srcText, String expectedText);

    /**
     * Synonym for {@link TextAssertions#assertTextNotPresent(String,String)} that calls getText() on the provided
     * locator.
     *
     * @param locator      the source of the text
     * @param expectedText the expected text
     */
    public void assertTextNotPresent(Locator locator, String expectedText);

    /**
     * Asserts that the text sequence 'expectedTextSequence' can be found in 'srcText'
     *
     * @param srcText              the text to search in
     * @param expectedTextSequence the expected text sequence
     */
    public void assertTextSequence(String srcText, String expectedTextSequence[]);

    /**
     * Asserts that the text sequence 'expectedTextSequence' can be found in 'srcText'
     *
     * @param srcText              the text to search in
     * @param expected1 the first expected text in the sequence
     * @param expected2 the rest of expected texts in the sequence
     */
    public void assertTextSequence(String srcText, String expected1, String... expected2);

    /**
     * Synonym for {@link TextAssertions#assertTextSequence(String,String[])} that calls getText() on the provided
     * locator.
     *
     * @param locator              the source of the text
     * @param expectedTextSequence the expected text sequence
     */
    public void assertTextSequence(Locator locator, String expectedTextSequence[]);

    /**
     * Synonym for {@link TextAssertions#assertTextSequence(String,String[])} that calls getText() on the provided
     * locator.
     *
     * @param locator              the source of the text
     * @param expected1 the first expected text in the sequence
     * @param expected2 the rest of expected texts in the sequence
     */
    public void assertTextSequence(Locator locator, String expected1, String... expected2);

    /**
     * Asserts that at least one of the provided text strings is present in the locator. Will throw an
     * {@link junit.framework.AssertionFailedError} if none are present.
     *
     * @param locator the source of the text
     * @param option1 the expected text
     * @param options the expected texts
     */
    public void assertAtLeastOneTextPresent(Locator locator, String option1, String... options);

}
