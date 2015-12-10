package com.atlassian.jira.webwork.parameters;

/**
 * A implementation of {@link ParameterConverter} for {@link Character}
 * objects
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class CharacterConverter extends AbstractParameterConverter
{
    public Object convertParameter(final String[] parameterValues, final Class paramType)
            throws IllegalArgumentException
    {
        return convert(parameterValues[0], paramType);
    }

    Character convert(final String parameterValue, final Class paramType)
    {
        checkPrimitiveInput(parameterValue, paramType, null);

        if (!paramType.isPrimitive() && isEmpty(parameterValue))
        {
            return null;
        }
        return new Character(parameterValue.charAt(0));
    }
}