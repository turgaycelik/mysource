package com.atlassian.jira.webwork.parameters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps a list of all known {@link com.atlassian.jira.webwork.parameters.ParameterConverter}s
 * <p/>
 * If a type is not in this list, then JIRA cannot accept that type as web input.
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class KnownParameterConverters
{
    private final static Map<Class, ParameterConverter> PROPERTY_CONVERTERS;

    static
    {
        Map<Class,ParameterConverter> map = new HashMap<Class, ParameterConverter>();

        map.put(Boolean[].class, new BooleanArrayConverter());
        map.put(boolean[].class, new BooleanArrayConverter());
        map.put(Boolean.class, new BooleanConverter());
        map.put(Boolean.TYPE, new BooleanConverter());

        map.put(Byte[].class, new ByteArrayConverter());
        map.put(byte[].class, new ByteArrayConverter());
        map.put(Byte.class, new ByteConverter());
        map.put(Byte.TYPE, new ByteConverter());

        map.put(Character[].class, new CharacterArrayConverter());
        map.put(char[].class, new CharacterArrayConverter());
        map.put(Character.class, new CharacterConverter());
        map.put(Character.TYPE, new CharacterConverter());

        map.put(Double[].class, new DoubleArrayConverter());
        map.put(double[].class, new DoubleArrayConverter());
        map.put(Double.class, new DoubleConverter());
        map.put(Double.TYPE, new DoubleConverter());

        map.put(Float[].class, new FloatArrayConverter());
        map.put(float[].class, new FloatArrayConverter());
        map.put(Float.class, new FloatConverter());
        map.put(Float.TYPE, new FloatConverter());

        map.put(Integer[].class, new IntegerArrayConverter());
        map.put(int[].class, new IntegerArrayConverter());
        map.put(Integer.class, new IntegerConverter());
        map.put(Integer.TYPE, new IntegerConverter());

        map.put(Long[].class, new LongArrayConverter());
        map.put(long[].class, new LongArrayConverter());
        map.put(Long.class, new LongConverter());
        map.put(Long.TYPE, new LongConverter());

        map.put(Short[].class, new ShortArrayConverter());
        map.put(short[].class, new ShortArrayConverter());
        map.put(Short.class, new ShortConverter());
        map.put(Short.TYPE, new ShortConverter());

        PROPERTY_CONVERTERS = Collections.unmodifiableMap(map);
    }

    /**
     * Called this to find a {@link ParameterConverter} for a given parameter type class
     *
     * @param parameterType the parameter type
     * @return a ParameterConverter if one can be found or null
     */
    public static ParameterConverter getConverter(Class parameterType)
    {
        return PROPERTY_CONVERTERS.get(parameterType);
    }
}
