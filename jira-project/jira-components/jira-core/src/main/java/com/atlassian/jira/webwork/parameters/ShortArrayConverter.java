package com.atlassian.jira.webwork.parameters;

/**
 * A implementation of {@link ParameterConverter} for {@link Short[]} objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class ShortArrayConverter extends AbstractParameterConverter
{
    private final ShortConverter converter = new ShortConverter();

    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        final Short[] convertedValues = new Short[parameterValues.length];
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

    private Object convertToPrimitive(final Short[] convertedValues)
    {
        short[] primtiveArr = new short[convertedValues.length];
        for (int i = 0; i < convertedValues.length; i++)
        {
            primtiveArr[i] = convertedValues[i].shortValue();
        }
        return primtiveArr;
    }
}
