package com.atlassian.jira.util;

import org.hamcrest.Matchers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Defines assertions on the MessageSet type that can be used to make Unit tests more concise and readable.
 *
 * @since v3.13
 */
public class MessageSetAssert
{
    /**
     * Asserts that there is exactly one Error message in the given MessageSet, and that it is as expected.
     * This assertion will fail if there are any warnings present.
     *
     * @param messageSet MessageSet to test.
     * @param expectedErrorMessage The expected error message.
     */
    public static void assert1ErrorNoWarnings(final MessageSet messageSet, final String expectedErrorMessage)
    {
        assert1Error(messageSet, expectedErrorMessage);
        assertNoWarnings(messageSet);
    }

    /**
     * Asserts that there is exactly one Error message in the given MessageSet, and that it is as expected.
     * This assertion does not care if there are any warnings present.
     *
     * @param messageSet MessageSet to test.
     * @param expectedErrorMessage The expected error message.
     */
    public static void assert1Error(final MessageSet messageSet, final String expectedErrorMessage)
    {
        assertThat("getErrorMessages", messageSet.getErrorMessages(), contains(expectedErrorMessage));
        assertThat("hasAnyErrors", messageSet.hasAnyErrors(), is(true));
        assertThat("hasAnyMessages", messageSet.hasAnyMessages(), is(true));
    }

    /**
     * Asserts that there are exactly the specified warning messages in the given MessageSet.
     * This assertion does not care if there are any errors present or what order the warnings are in.
     *
     * @param messageSet MessageSet to test.
     * @param expectedWarningMessages The expected warning messages (order is not significant)
     */
    public static void assertWarningMessages(final MessageSet messageSet, final String... expectedWarningMessages)
    {
        assertThat("getWarningMessages", messageSet.getWarningMessages(), containsInAnyOrder(expectedWarningMessages));
        assertThat("hasAnyWarnings", messageSet.hasAnyWarnings(), is(true));
        assertThat("hasAnyMessages", messageSet.hasAnyMessages(), is(true));
    }

    /**
     * Asserts that there are exactly the specified error messages in the given MessageSet.
     * This assertion does not care if there are any warnings present or what order the errors are in.
     *
     * @param messageSet MessageSet to test.
     * @param expectedErrorMessages The expected error messages (order is not significant)
     */
    public static void assertErrorMessages(final MessageSet messageSet, final String... expectedErrorMessages)
    {
        assertThat("getErrorMessages", messageSet.getErrorMessages(), containsInAnyOrder(expectedErrorMessages));
        assertThat("hasAnyErrors", messageSet.hasAnyErrors(), is(true));
        assertThat("hasAnyMessages", messageSet.hasAnyMessages(), is(true));
    }

    /**
     * Asserts that there is exactly one Warning message in the given MessageSet, and that it is as expected.
     * This assertion will fail if there are any errors present.
     *
     * @param messageSet MessageSet to test.
     * @param expectedWarningMessage The expected warning message.
     */
    public static void assert1WarningNoErrors(final MessageSet messageSet, final String expectedWarningMessage)
    {
        assertNoErrors(messageSet);
        assert1Warning(messageSet, expectedWarningMessage);
    }

    /**
     * Asserts that there is exactly one Warning message in the given MessageSet, and that it is as expected.
     * This assertion will fail if there are any errors present.
     *
     * @param messageSet MessageSet to test.
     * @param expectedWarningMessage The expected warning message.
     */
    public static void assert1Warning(final MessageSet messageSet, final String expectedWarningMessage)
    {
        assertThat("getWarningMessages", messageSet.getWarningMessages(), contains(expectedWarningMessage));
        assertThat("hasAnyWarnings", messageSet.hasAnyWarnings(), is(true));
        assertThat("hasAnyMessages", messageSet.hasAnyMessages(), is(true));
    }

    /**
     * Asserts that there are no messages at all in the given MessageSet.
     * This assertion will fail if there are either errors or warnings present.
     *
     * @param messageSet MessageSet to test.
     */
    public static void assertNoMessages(final MessageSet messageSet)
    {
        assertNoErrors(messageSet);
        assertNoWarnings(messageSet);
        assertThat("hasAnyMessages", messageSet.hasAnyMessages(), is(false));
    }

    public static void assertNoWarnings(final MessageSet messageSet)
    {
        assertThat("getWarningMessages", messageSet.getWarningMessages(), Matchers.<String>empty());
        assertThat("hasAnyWarnings", messageSet.hasAnyWarnings(), is(false));
    }

    public static void assertNoErrors(final MessageSet messageSet)
    {
        assertThat("getErrorMessages", messageSet.getErrorMessages(), Matchers.<String>empty());
        assertThat("hasAnyErrors", messageSet.hasAnyErrors(), is(false));
    }
}
