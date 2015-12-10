package com.atlassian.jira.webwork.parameters;

import javax.annotation.concurrent.Immutable;

/**
 * This is called to convert a value from String[] format into the specific type.  If it cant convert the value it
 * should throw an IllegalArgumentException.
 * <p/>
 * When dealing with Numbers and their primitive types, its imperative to differentiate between conversions on empty
 * strings.  Empty string be converted to a null 'Long' but not to a 'long'.
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
@Immutable
public interface ParameterConverter
{
    /**
     * This is called to perform the conversion.  The parameterValues will always be a String[] of length 1 or more.  It
     * can never be null.
     *
     * @param parameterValues a String[] that is never null and at least length 1 or more.
     * @param paramType the type of the target object
     * @return a converted object of the right type
     *
     * @throws IllegalArgumentException if the value cant be converted
     */
    Object convertParameter(String[] parameterValues, final Class paramType) throws IllegalArgumentException;
}
