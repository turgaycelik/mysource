package com.atlassian.jira.webwork.parameters;

/**
 * A implementation of {@link com.atlassian.jira.webwork.parameters.ParameterConverter} for {@link Character[]} objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class CharacterArrayConverter extends AbstractParameterConverter
{
    private final CharacterConverter converter = new CharacterConverter();

    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        final Character[] convertedValues = new Character[parameterValues.length];
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

    private Object convertToPrimitive(final Character[] convertedValues)
    {
        char[] primtiveArr = new char[convertedValues.length];
        for (int i = 0; i < convertedValues.length; i++)
        {
            primtiveArr[i] = convertedValues[i].charValue();
        }
        return primtiveArr;
    }
}