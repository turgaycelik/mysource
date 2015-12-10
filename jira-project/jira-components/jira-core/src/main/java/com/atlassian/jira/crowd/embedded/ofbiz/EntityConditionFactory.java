package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;
import com.atlassian.crowd.search.query.entity.restriction.NullRestriction;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;

import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * Creates OfBiz EntityCondition objects from Crowd search SearchRestriction objects.
 *
 * Each OfBiz entity that requires searching needs to have a specific subclass of this to provide the
 * required table and column information for that entity.
 *
 * @since 0.1
 */
abstract class EntityConditionFactory
{
    /**
     * Returns the name of the table storing the entities attribute data.
     * @return the name of the table storing the entities attribute data.
     */
    abstract String getEntityTableIdColumnName();

    /**
     * Returns the column name of the entity id in the entity table.
     * @return the column name of the entity id in the entity table.
     */
    abstract String getAttributeTableName();

    /**
     * Returns the column name of the entity id in the attribute table.
     * @return column name of the entity id in the attribute table.
     */
    abstract String getAttributeIdColumnName();

    /**
     * Return the name of the lower case sibling field for this property.
     * @param property Property
     * @return lower class field name
     */
    abstract String getLowerFieldName(final Property<?> property);

    /**
     * Return true if this property is a first class attribute base entity table.
     * @param property Property
     * @return true if a first class attribute
     */
    abstract boolean isCoreProperty(final Property<?> property);

    /**
     * Returns an OfBiz Entity Condition for the search restriction.
     * This will return null for a null or NullRestriction.  Callers must handle a null return and
     * not mindlessly pass it to OfBiz.
     * @param restriction Restriction to be interpreted.
     * @return An OfBiz Entity Condition for the search restriction.
     */
    EntityCondition getEntityConditionFor(final SearchRestriction restriction)
    {
        // Switch on the type of SearchRestriction
        if ((restriction == null) || (restriction instanceof NullRestriction))
        {
            return null;
        }
        if (restriction instanceof BooleanRestriction)
        {
            return parseBooleanRestriction((BooleanRestriction) restriction);
        }
        if (restriction instanceof PropertyRestriction)
        {
            return parsePropertyRestriction((PropertyRestriction<?>) restriction);
        }

        // We should not get here
        throw new UnsupportedOperationException("Unknown SearchRestriction type " + restriction.getClass());
    }

    private EntityCondition parsePropertyRestriction(final PropertyRestriction<?> propertyRestriction)
    {
        if (isCoreProperty(propertyRestriction.getProperty()))
        {
            return parseCorePropertyRestriction(propertyRestriction);
        }
        else
        {
            return parseAttributePropertyRestriction(propertyRestriction);
        }
    }

    /**
     * Create a EntityCondition for a Core Entity property.
     * @param restriction Property Restriction
     * @return Ofbiz EntityCondition
     */
    private EntityCondition parseCorePropertyRestriction(final PropertyRestriction<?> restriction)
    {
        switch (restriction.getMatchMode())
        {
            case EXACTLY_MATCHES:
                return makeCaseInsensitiveEntityCondition(restriction, EntityOperator.EQUALS, restriction.getValue());
            case GREATER_THAN:
                return makeCaseInsensitiveEntityCondition(restriction, EntityOperator.GREATER_THAN, restriction.getValue());
            case LESS_THAN:
                return makeCaseInsensitiveEntityCondition(restriction, EntityOperator.LESS_THAN, restriction.getValue());
            case CONTAINS:
                return makeCaseInsensitiveEntityCondition(restriction, EntityOperator.LIKE, "%" + restriction.getValue() + "%");
            case STARTS_WITH:
                return makeCaseInsensitiveEntityCondition(restriction, EntityOperator.LIKE, restriction.getValue() + "%");
            case NULL:
                return makeCaseInsensitiveEntityCondition(restriction, EntityOperator.EQUALS, null);
        }
        throw new UnsupportedOperationException("Unknown Match Mode: " + restriction.getMatchMode());
    }

    /**
     * Parse the restriction for crowd user attributes.
     * @param restriction Property Restriction
     * @return Ofbiz EntityCondition
     */
    private EntityCondition parseAttributePropertyRestriction(final PropertyRestriction<?> restriction)
    {
        switch (restriction.getMatchMode())
        {
            case EXACTLY_MATCHES:
                return makeCaseInsensitiveAttributeCondition(restriction, " = ", restriction.getValue());
            case GREATER_THAN:
                return makeCaseInsensitiveAttributeCondition(restriction, " > ", restriction.getValue());
            case LESS_THAN:
                return makeCaseInsensitiveAttributeCondition(restriction, " < ", restriction.getValue());
            case CONTAINS:
                return makeCaseInsensitiveAttributeCondition(restriction, " LIKE ", "%" + restriction.getValue() + "%");
            case STARTS_WITH:
                return makeCaseInsensitiveAttributeCondition(restriction, " LIKE ", restriction.getValue() + "%");
            case NULL:
                return makeNullAttributeCondition(restriction);
        }
        throw new UnsupportedOperationException("Unknown Match Mode: " + restriction.getMatchMode());
    }

    /**
     * Create a EntityCondition for a Core Entity property that does lower case matching for strings..
     * @param restriction PropertyRestriction
     * @param operator Operator
     * @param value  Test value
     * @return Ofbiz EntityCondition
     */
    private EntityCondition makeCaseInsensitiveEntityCondition(final PropertyRestriction<?> restriction, final EntityOperator operator, final Object value)
    {
        final Property<?> property = restriction.getProperty();
        if (property.getPropertyType().equals(String.class))
        {
            // Get the lowercase sibling name
            final String lowerName = getLowerFieldName(property);
            if (lowerName != null)
            {
                final String strValue = value == null ? null : toLowerCase((String) value);
                return new EntityExpr(lowerName, operator, strValue);
            }
            return new EntityExpr(property.getPropertyName(), operator, value);
        }
        if (property.getPropertyType().equals(Boolean.class))
        {
            // We use Integer to store a boolean value, so we need to translate
            final Integer intValue = BooleanUtils.toIntegerObject((Boolean) value);
            return new EntityExpr(property.getPropertyName(), operator, intValue);
        }
        if (property.getPropertyType().equals(Date.class))
        {
            // Convert the java.util.Date to java.sql.Timestamp for OfBiz
            final Timestamp timestampValue = new Timestamp(((Date) value).getTime());
            return new EntityExpr(property.getPropertyName(), operator, timestampValue);
        }
        throw new IllegalArgumentException("Unrecognised PropertyType '" + property.getPropertyType().getName() + "'.");
    }

    /**
     * Create a EntityCondition for an attribute property that does lower case matching for strings.
     * Note: All attributes are stored as strings and support lower case searching.
     * @param restriction PropertyRestriction
     * @param operator Operator
     * @param value  Test value
     * @return Ofbiz EntityCondition
     */
    private EntityCondition makeCaseInsensitiveAttributeCondition(final PropertyRestriction<?> restriction, final String operator, final Object value)
    {
        final Property<?> property = restriction.getProperty();
        final String strValue = value == null ? null : toLowerCase(value.toString());

        final StringBuilder builder = new StringBuilder(getEntityTableIdColumnName());
        builder.append(" IN (SELECT ").append(getAttributeIdColumnName());
        builder.append(" FROM ").append(getAttributeTableName());
        builder.append(" WHERE attribute_name = ? AND lower_attribute_value");
        builder.append(operator);
        builder.append(" ? )");

        return new EntityAttributeCondition(builder.toString(), property.getPropertyName(), strValue);
    }


    /**
     * Create a EntityCondition for an attribute property that does lower case matching for strings.
     * Note: All attributes are stored as strings and support lower case searching.
     * @param restriction PropertyRestriction
     * @return Ofbiz EntityCondition
     */
    private EntityCondition makeNullAttributeCondition(final PropertyRestriction<?> restriction)
    {
        final Property<?> property = restriction.getProperty();

        final StringBuilder builder = new StringBuilder(getEntityTableIdColumnName());
        builder.append(" NOT IN (SELECT ").append(getAttributeIdColumnName());
        builder.append(" FROM ").append(getAttributeTableName());
        builder.append(" WHERE attribute_name = ? )");

        return new NullEntityAttributeCondition(builder.toString(), property.getPropertyName());
    }

    private EntityCondition parseBooleanRestriction(final BooleanRestriction booleanRestriction)
    {
        final List<EntityCondition> entityConditions = new ArrayList<EntityCondition>();
        for (final SearchRestriction searchRestriction : booleanRestriction.getRestrictions())
        {
            final EntityCondition condition = getEntityConditionFor(searchRestriction);
            // Check for null restriction (meaning "match all")
            if (condition == null)
            {
                if (booleanRestriction.getBooleanLogic() == BooleanRestriction.BooleanLogic.OR)
                {
                    // this means there is no restriction at all on this list - return the null Condition
                    return null;
                }
                // IF it is AND then we just don't add this restriction to the list as it has no effect.
            }
            else
            {
                entityConditions.add(condition);
            }
        }
        return new EntityConditionList(entityConditions, getOperator(booleanRestriction));
    }

    private EntityOperator getOperator(final BooleanRestriction booleanRestriction) throws UnsupportedOperationException
    {
        switch (booleanRestriction.getBooleanLogic())
        {
            case AND:
                return EntityOperator.AND;
            case OR:
                return EntityOperator.OR;
        }
        throw new UnsupportedOperationException("Unknown Boolean Logic: " + booleanRestriction.getBooleanLogic());
    }
}
