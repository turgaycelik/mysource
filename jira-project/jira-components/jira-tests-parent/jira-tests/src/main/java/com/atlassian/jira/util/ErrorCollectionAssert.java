package com.atlassian.jira.util;

import junit.framework.Assert;

/**
 * Provides methods to help unit tests make assertions about ErrorCollection objects.
 * @since v3.13
 */
public class ErrorCollectionAssert
{
    /**
     * Asserts that the given ErrorCollection contains only the given error message.
     *
     * @param errorCollection ErrorCollection under test.
     * @param expectedErrorMessage The expected error message.
     */
    public static void assert1ErrorMessage(final ErrorCollection errorCollection, final String expectedErrorMessage)
    {
        Assert.assertEquals("Expected exactly one message in the given ErrorCollection.",
                1, errorCollection.getErrorMessages().size());
        Assert.assertEquals(expectedErrorMessage, errorCollection.getErrorMessages().iterator().next());
        Assert.assertTrue("Expected only a single global error, but also found a field-specific error.", errorCollection.getErrors().isEmpty());
    }

    /**
     * Asserts that the given ErrorCollection contains only the given field-specific error.
     *
     * @param errorCollection ErrorCollection under test.
     * @param fieldName The field name that we expected the error for.
     * @param expectedErrorMessage The expected error message.
     */
    public static void assert1FieldError(final ErrorCollection errorCollection, final String fieldName, final String expectedErrorMessage)
    {
        Assert.assertEquals("Expected exactly one field-specific message in the given ErrorCollection.",
                1, errorCollection.getErrors().size());
        Assert.assertEquals(expectedErrorMessage, errorCollection.getErrors().get(fieldName));
        Assert.assertTrue("Expected only a field-specific error, but also found a global error.", errorCollection.getErrorMessages().isEmpty());
    }

    /**
     * Asserts that the given ErrorCollection contains the given field-specific error.
     *
     * @param errorCollection ErrorCollection under test.
     * @param fieldName The field name that we expected the error for.
     * @param expectedErrorMessage The expected error message.
     */
    public static void assertFieldError(final ErrorCollection errorCollection, final String fieldName, final String expectedErrorMessage)
    {
        Assert.assertEquals(expectedErrorMessage, errorCollection.getErrors().get(fieldName));
    }

    /**
     * Asserts that the given ErrorCollection has no errors.
     *
     * @param errorCollection ErrorCollection under test.
     */
    public static void assertNoErrors(final ErrorCollection errorCollection)
    {
        Assert.assertFalse(errorCollection.hasAnyErrors());
    }
}
