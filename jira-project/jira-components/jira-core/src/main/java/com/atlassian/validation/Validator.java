package com.atlassian.validation;

/**
 * Small abstraction to enable valdiation of Strings.
 *
 * @since v4.4
 */
public interface Validator
{
    /**
     * Validate the given string to see if it passes and if not, why not.
     *
     * @param value all validation input is a String.
     * @return A result that contains access to the details of the validation outcome.
     */
    Result validate(String value);

    /**
     * Post-validation state. Enables access to the value object and failure case details like error messages.
     */
    interface Result
    {
        /**
         * Whether the validation succeeded.
         *
         * @return true only if validaiton was successful.
         */
        boolean isValid();

        /**
         * If validation failed, the reason is found here in plain text.
         *
         * @return the error message if any, or null if validation succeeded.
         */
        String getErrorMessage();

        /**
         * If validation failed, the reason is found here in html.
         *
         * @return the error message if any, or null if validation succeeded.
         */
        String getErrorMessageHtml();

        /**
         * If valid returns the value in string form.
         *
         * @return the value.
         * @throws IllegalStateException if the result is not valid.
         */
        String get() throws IllegalStateException;
    }
}
