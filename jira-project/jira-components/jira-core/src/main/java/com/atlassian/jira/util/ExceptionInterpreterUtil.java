package com.atlassian.jira.util;

import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;

/**
 * This util will feed an exception through the exception interpreter chain of
 * responsibility.
 */
public class ExceptionInterpreterUtil
{
    // Oracle data size is the only current known one, and has been for 5 years
    private static final ExceptionInterpreter interpreter = new OracleDataSizeExceptionInterpreter();

    /**
     * This method is used to register a new URL interpreter into the chain.
     *
     * Is this used by anything anymore?  Plugins maybe?
     *
     * @param val is the implementation of ExceptionInterpreter to add to the
     *            chain.
     */
    public static void addInterpreter(ExceptionInterpreter val)
    {
        if (val == null)
        {
            throw new IllegalArgumentException("Cannot add a null exception interpreter");
        }
        interpreter.append(val);
    }

    /**
     * Will pass the exception message through the registered interpreters returing a
     * message string, which can be html if one of the interpreters can handle the exception message.
     *
     * @param extendedSystemInfoUtils is the system information at the time of the exception
     * @param exceptionMessage        is the exceptions message
     * @return is the message to be displayed for the given exception message, null if not understood
     */
    public static String execute(ExtendedSystemInfoUtils extendedSystemInfoUtils, String exceptionMessage)
    {
        String interpretedExceptionMessage = null;
        try
        {
            interpretedExceptionMessage = interpreter.invoke(extendedSystemInfoUtils, exceptionMessage);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return interpretedExceptionMessage;
    }

}
