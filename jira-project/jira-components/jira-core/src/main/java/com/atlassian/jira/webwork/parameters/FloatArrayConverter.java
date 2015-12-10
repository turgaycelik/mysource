package com.atlassian.jira.webwork.parameters;

/**
 * A implementation of {@link com.atlassian.jira.webwork.parameters.ParameterConverter} for {@link Float[]}
 * objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class FloatArrayConverter extends AbstractParameterConverter
{
    private final FloatConverter converter = new FloatConverter();

    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        final Float[] convertedValues = new Float[parameterValues.length];
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

    private Object convertToPrimitive(final Float[] convertedValues)
    {
        float[] primtiveArr = new float[convertedValues.length];
        for (int i = 0; i < convertedValues.length; i++)
        {
            primtiveArr[i] = convertedValues[i].floatValue();
        }
        return primtiveArr;
    }
}
