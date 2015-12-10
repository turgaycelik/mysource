package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Creates OfBiz EntityCondition objects from Crowd search SearchRestriction objects for Users.
 *
 * @since 0.1
 */
class UserEntityConditionFactory extends EntityConditionFactory
{
    // Note: We build raw SQL.  These really are the table and column names, not entity and field names!
    private final String attributeTableName;

    UserEntityConditionFactory(OfBizDelegator ofBizDelegator)
    {
        final ModelEntity modelEntity = ofBizDelegator.getDelegatorInterface().getModelEntity(UserAttributeEntity.ENTITY);
        attributeTableName = modelEntity.getTableName(ofBizDelegator.getDelegatorInterface().getEntityHelperName(UserAttributeEntity.ENTITY));
    }

    @Override
    String getEntityTableIdColumnName()
    {
        return "id";
    }

    @Override
    String getAttributeTableName()
    {
        return attributeTableName;
    }

    @Override
    String getAttributeIdColumnName()
    {
        return "user_id";
    }

    @Override
    boolean isCoreProperty(final Property<?> property)
    {
        return UserEntity.isSystemField(property.getPropertyName());
    }

    @Override
    String getLowerFieldName(final Property<?> property)
    {
        return UserEntity.getLowercaseFieldNameFor(property.getPropertyName());
    }
}
