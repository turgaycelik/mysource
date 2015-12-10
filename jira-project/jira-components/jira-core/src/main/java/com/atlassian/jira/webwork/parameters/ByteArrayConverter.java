package com.atlassian.jira.webwork.parameters;

/**
 * A implementation of {@link ParameterConverter} for {@link Byte[]} objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class ByteArrayConverter extends AbstractParameterConverter
{
    private final ByteConverter converter = new ByteConverter();

    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        final Byte[] convertedValues = new Byte[parameterValues.length];
        for (int i = 0; i < parameterValues.length; i++)
        {
            convertedValues[i] = converter.convert(parameterValues[i], paramType.getComponentType());
        }
        if (paramType.getComponentType().isPrimitive())
        {
            return convertToPrimitive(convertedValues);
        }
        else
        {
            return convertedValues;
        }
    }

    private Object convertToPrimitive(final Byte[] convertedValues)
    {
        byte[] primtiveArr = new byte[convertedValues.length];
        for (int i = 0; i < convertedValues.length; i++)
        {
            primtiveArr[i] = convertedValues[i].byteValue();
        }
        return primtiveArr;
    }
}
