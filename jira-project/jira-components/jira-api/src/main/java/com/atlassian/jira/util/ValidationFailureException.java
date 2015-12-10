package com.atlassian.jira.util;

/**
 * A validation exception is essentially a "throwable" error collection.
 *
 * @see ErrorCollection
 * @since v4.3
 */
public class ValidationFailureException extends RuntimeException
{
    /**
     * A collection of validation errors.
     */
    private final ErrorCollection errors;

    /**
     * Constructs a new ValidationFailureException with a single error message.
     *
     * @param message a String containing an error message
     */
    public ValidationFailureException(String message)
    {
        this(message, null, null);
    }

    /**
     * Constructs a new ValidationFailureException with a single error message and a cause.
     *
     * @param message a String containing an error message
     * @param throwable the Throwable that caused the validation failure
     */
    public ValidationFailureException(String message, Throwable throwable)
    {
        this(message, null, throwable);
    }

    /**
     * Constructs a new ValidationFailureException initialised with a collection of errors.
     *
     * @param errors an ErrorCollection containing collection of validation errors
     */
    public ValidationFailureException(ErrorCollection errors)
    {
        this(null, errors, null);
    }

    /**
     * Constructs a new ValidationFailureException with an error message, a collection of errors, and a cause.
     *
     * @param message a String containing an error message
     * @param errors an ErrorCollection containing collection of validation errors
     * @param throwable the Throwable that caused the validation failure
     */
    public ValidationFailureException(String message, ErrorCollection errors, Throwable throwable)
    {
        super(message, throwable);

        SimpleErrorCollection errorsCopy = new SimpleErrorCollection();
        if (message != null) { errorsCopy.addErrorMessage(message); }
        if (errors != null) { errorsCopy.addErrorCollection(errors); }
        this.errors = errorsCopy;
    }

    /**
     * Returns the validation errors.
     *
     * @return an ErrorCollection
     */
    public ErrorCollection errors()
    {
        return errors;
    }
}
