package com.atlassian.jira.webwork.parameters;

import webwork.util.editor.PropertyEditorException;

/**
 * A abstract class for implementations of {@link com.atlassian.jira.webwork.parameters.ParameterConverter}
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
abstract class AbstractParameterConverter implements ParameterConverter
{
    /**
     * The same test that webwork1 uses for null-able Longs/Integers/Shorts/Bytes
     *
     * @param parameterValue the parameter value
     * @return true if its an empty string
     */
    protected boolean isEmpty(String parameterValue)
    {
        return parameterValue == null || parameterValue.length() == 0;
    }

    /**
     * Throws a PropertyEditorException just like webwork1 when a value is a primitive AND the string is empty
     * @param parameterValue the value in play
     * @param paramType the type of parameter
     * @param bundleKey the webwork1 bundle key to use
     */
    protected void checkPrimitiveInput(final String parameterValue, final Class paramType, final String bundleKey)
    {
        if (paramType.isPrimitive() && isEmpty(parameterValue))
        {
            throw new PropertyEditorException(bundleKey, parameterValue);
        }
    }
}
