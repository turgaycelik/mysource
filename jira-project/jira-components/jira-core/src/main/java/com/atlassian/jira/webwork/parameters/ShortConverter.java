package com.atlassian.jira.webwork.parameters;

import webwork.util.editor.PropertyEditorException;
import webwork.util.editor.PropertyMessage;

/**
 * A implementation of {@link com.atlassian.jira.webwork.parameters.ParameterConverter} for {@link Short}
 * objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class ShortConverter extends AbstractParameterConverter
{
    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        return convert(parameterValues[0], paramType);
    }

    Short convert(final String parameterValue, final Class paramType)
    {
        checkPrimitiveInput(parameterValue, paramType, PropertyMessage.EMPTY_SHORT);

        if (!paramType.isPrimitive() && isEmpty(parameterValue))
        {
            return null;
        }
        return convertTo(parameterValue, paramType);
    }

    private Short convertTo(String parameterValue, final Class paramType)
    {
        try
        {
            return Short.valueOf(parameterValue);
        }
        catch (NumberFormatException e)
        {
            throw new PropertyEditorException(paramType.isPrimitive() ? PropertyMessage.BAD_SHORT : PropertyMessage.BAD_SHORTOBJ, parameterValue);
        }
    }

}
