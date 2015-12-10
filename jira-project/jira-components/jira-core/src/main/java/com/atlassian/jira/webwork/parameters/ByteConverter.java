package com.atlassian.jira.webwork.parameters;

import webwork.util.editor.PropertyEditorException;
import webwork.util.editor.PropertyMessage;

/**
 * A implementation of {@link com.atlassian.jira.webwork.parameters.ParameterConverter} for {@link Byte} objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class ByteConverter extends AbstractParameterConverter
{
    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        return convert(parameterValues[0], paramType);
    }

    Byte convert(final String parameterValue, final Class paramType)
    {
        checkPrimitiveInput(parameterValue, paramType, PropertyMessage.EMPTY_BYTE);

        if (!paramType.isPrimitive() && isEmpty(parameterValue))
        {
            return null;
        }
        return convertTo(parameterValue, paramType);
    }

    private Byte convertTo(String parameterValue, final Class paramType)
    {
        try
        {
            return Byte.valueOf(parameterValue);
        }
        catch (NumberFormatException e)
        {
            throw new PropertyEditorException(paramType.isPrimitive() ? PropertyMessage.BAD_BYTE : PropertyMessage.BAD_BYTEOBJ, parameterValue);
        }
    }
}
