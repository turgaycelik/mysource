package com.atlassian.jira.webwork.parameters;

/**
 * A implementation of {@link ParameterConverter} for {@link Boolean[]} objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class BooleanArrayConverter extends AbstractParameterConverter
{
    private final BooleanConverter converter = new BooleanConverter();

    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        final Boolean[] convertedValues = new Boolean[parameterValues.length];
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

    private Object convertToPrimitive(final Boolean[] convertedValues)
    {
        boolean[] primtiveArr = new boolean[convertedValues.length];
        for (int i = 0; i < convertedValues.length; i++)
        {
            primtiveArr[i] = convertedValues[i].booleanValue();
        }
        return primtiveArr;
    }
}
