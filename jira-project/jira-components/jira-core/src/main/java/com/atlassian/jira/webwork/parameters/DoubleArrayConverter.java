package com.atlassian.jira.webwork.parameters;

/**
 * A implementation of {@link ParameterConverter} for {@link Double[]} objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class DoubleArrayConverter extends AbstractParameterConverter
{
    private final DoubleConverter converter = new DoubleConverter();

    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        final Double[] convertedValues = new Double[parameterValues.length];
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

    private Object convertToPrimitive(final Double[] convertedValues)
    {
        double[] primtiveArr = new double[convertedValues.length];
        for (int i = 0; i < convertedValues.length; i++)
        {
            primtiveArr[i] = convertedValues[i].doubleValue();
        }
        return primtiveArr;
    }
}
