package com.atlassian.jira.webwork.parameters;

import webwork.util.editor.PropertyMessage;

/**
 * A implementation of {@link com.atlassian.jira.webwork.parameters.ParameterConverter} for {@link Boolean}
 * objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class BooleanConverter extends AbstractParameterConverter
{
    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        return convert(parameterValues[0], paramType);
    }

    Boolean convert(final String parameterValue, final Class paramType)
    {
        checkPrimitiveInput(parameterValue, paramType, PropertyMessage.EMPTY_BOOLEAN);

        if (!paramType.isPrimitive() && isEmpty(parameterValue))
        {
            return null;
        }
        return new Boolean(parameterValue);
    }
}
