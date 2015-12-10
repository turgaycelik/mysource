package com.atlassian.jira.webwork.parameters;

import webwork.util.editor.PropertyEditorException;
import webwork.util.editor.PropertyMessage;

/**
 * A implementation of {@link ParameterConverter} for {@link Float} objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class FloatConverter extends AbstractParameterConverter
{
    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        return convert(parameterValues[0], paramType);
    }

    Float convert(final String parameterValue, final Class paramType)
    {
        checkPrimitiveInput(parameterValue, paramType, PropertyMessage.EMPTY_FLOAT);

        if (!paramType.isPrimitive() && isEmpty(parameterValue))
        {
            return null;
        }
        return convertTo(parameterValue, paramType);
    }

    private Float convertTo(String parameterValue, final Class paramType)
    {
        try
        {
            return Float.valueOf(parameterValue);
        }
        catch (NumberFormatException e)
        {
            throw new PropertyEditorException(paramType.isPrimitive() ? PropertyMessage.BAD_FLOAT : PropertyMessage.BAD_FLOATOBJ, parameterValue);
        }
    }
}
