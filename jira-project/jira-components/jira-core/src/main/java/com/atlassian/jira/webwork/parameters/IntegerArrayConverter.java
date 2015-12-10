package com.atlassian.jira.webwork.parameters;

/**
 * A implementation of {@link com.atlassian.jira.webwork.parameters.ParameterConverter} for {@link Integer[]}
 * objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class IntegerArrayConverter extends AbstractParameterConverter
{
    private final IntegerConverter converter = new IntegerConverter();

    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        final Integer[] convertedValues = new Integer[parameterValues.length];
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

    private Object convertToPrimitive(final Integer[] convertedValues)
    {
        int[] primtiveArr = new int[convertedValues.length];
        for (int i = 0; i < convertedValues.length; i++)
        {
            primtiveArr[i] = convertedValues[i].intValue();
        }
        return primtiveArr;
    }
}
