package com.atlassian.jira.webwork.parameters;

import webwork.util.editor.PropertyEditorException;
import webwork.util.editor.PropertyMessage;

/**
 * A implementation of {@link ParameterConverter} for {@link Integer} objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class IntegerConverter extends AbstractParameterConverter
{
    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        return convert(parameterValues[0], paramType);
    }

    Integer convert(final String parameterValue, final Class paramType)
    {
        checkPrimitiveInput(parameterValue, paramType, PropertyMessage.EMPTY_INTEGER);

        if (!paramType.isPrimitive() && isEmpty(parameterValue))
        {
            return null;
        }
        return convertTo(parameterValue, paramType);
    }

    private Integer convertTo(String parameterValue, final Class paramType)
    {
        try
        {
            return Integer.valueOf(parameterValue);
        }
        catch (NumberFormatException e)
        {
            throw new PropertyEditorException(paramType.isPrimitive() ? PropertyMessage.BAD_INTEGER : PropertyMessage.BAD_INTEGEROBJ, parameterValue);
        }
    }
}
