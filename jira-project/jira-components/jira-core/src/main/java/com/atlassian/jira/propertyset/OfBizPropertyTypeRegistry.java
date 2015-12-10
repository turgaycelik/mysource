package com.atlassian.jira.propertyset;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertyImplementationException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.ofbiz.DataPropertyHandler;
import com.opensymphony.module.propertyset.ofbiz.DatePropertyHandler;
import com.opensymphony.module.propertyset.ofbiz.DecimalPropertyHandler;
import com.opensymphony.module.propertyset.ofbiz.NumberPropertyHandler;
import com.opensymphony.module.propertyset.ofbiz.PropertyHandler;
import com.opensymphony.module.propertyset.ofbiz.StringPropertyHandler;
import com.opensymphony.util.Data;

import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_DATA;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_DATE;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_DECIMAL;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_NUMBER;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_STRING;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_TEXT;

/**
 * Maps the int property value types as defined in {@link PropertySet} to the appropriate
 * handling information for them.  The property handlers have been enhanced such that those
 * whose {@link PropertyHandler#processGet(int, Object) processGet} methods return mutable
 * data (specifically for the {@code DATE} and {@code DATA} types) return a safe copy of the
 * value instead of the original so that they will be safe to use inside of the
 * {@link CachingOfBizPropertySet} implementation.
 *
 * @since v6.2
 */
class OfBizPropertyTypeRegistry
{
    private static final Map<Integer,TypeMapper> TYPE_MAPPER;
    static
    {
        final StringPropertyHandler stringPropertyHandler = new StringPropertyHandler();

        final TypeMapper propString = new TypeMapper(PROPERTY_STRING, stringPropertyHandler);
        final TypeMapper propText = new TypeMapper(PROPERTY_TEXT, stringPropertyHandler);
        final TypeMapper propDate = new TypeMapper(PROPERTY_DATE, new SafeDatePropertyHandler());
        final TypeMapper propData = new TypeMapper(PROPERTY_DATA, new SafeDataPropertyHandler());
        final TypeMapper propNumber = new TypeMapper(PROPERTY_NUMBER, new NumberPropertyHandler());
        final TypeMapper propDecimal = new TypeMapper(PROPERTY_DECIMAL, new DecimalPropertyHandler());

        TYPE_MAPPER = ImmutableMap.<Integer,TypeMapper>builder()
                .put(PropertySet.BOOLEAN, propNumber)
                .put(PropertySet.INT, propNumber)
                .put(PropertySet.LONG, propNumber)
                .put(PropertySet.DOUBLE, propDecimal)
                .put(PropertySet.STRING, propString)
                .put(PropertySet.TEXT, propText)
                .put(PropertySet.DATE, propDate)
                .put(PropertySet.OBJECT, propData)
                .put(PropertySet.XML, propData)
                .put(PropertySet.DATA, propData)
                .put(PropertySet.PROPERTIES, propData)
                .build();
    }

    @Nonnull
    static TypeMapper mapper(final int type)
    {
        return mapper(Integer.valueOf(type));
    }

    @Nonnull
    static TypeMapper mapper(final Integer type)
    {
        final TypeMapper mapper = TYPE_MAPPER.get(type);
        if (mapper == null)
        {
            throw new PropertyImplementationException("Invalid property type: " + type);
        }
        return mapper;
    }



    static class SafeDatePropertyHandler extends DatePropertyHandler
    {
        @Override
        public Object processGet(final int type, final Object input)
        {
            final Object result = super.processGet(type, input);
            return result instanceof Date ? ((Date)result).clone() : result;
        }
    }

    static class SafeDataPropertyHandler extends DataPropertyHandler
    {
        @Override
        public Object processGet(final int type, final Object input)
        {
            final Object result = super.processGet(type, input);
            return result instanceof Data ? new Data(((Data)result).getBytes().clone()) : result;
        }
    }

    static class TypeMapper
    {
        private final PropertyHandler handler;
        private final String entityName;

        public TypeMapper(String entityName, PropertyHandler handler)
        {
            this.entityName = entityName;
            this.handler = handler;
        }

        public String getEntityName()
        {
            return entityName;
        }

        public PropertyHandler getHandler()
        {
            return handler;
        }

        public boolean hasSameEntityName(@Nonnull TypeMapper otherMapper)
        {
            return entityName.equals(otherMapper.entityName);
        }
    }
}

