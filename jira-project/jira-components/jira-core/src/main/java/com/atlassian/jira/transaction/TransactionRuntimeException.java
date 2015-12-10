package com.atlassian.jira.transaction;

/**
 * This runtime exception is thrown by the Transaction handling code.  However since its a {@link RuntimeException} is
 * doesnt need to be explcitly handed.
 * <p/>
 * This exception class therefore is a marker one so that you can write code to catch this specific exception if you so
 * choose }
 *
 * @since v4.4.1
 */
public class TransactionRuntimeException extends RuntimeException
{

    public TransactionRuntimeException()
    {
        super();
    }

    public TransactionRuntimeException(String message)
    {
        super(message);
    }

    public TransactionRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TransactionRuntimeException(Throwable cause)
    {
        super(cause);
    }
}
