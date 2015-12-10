package com.atlassian.jira.crowd.embedded.ofbiz;

import org.ofbiz.core.entity.EntityConditionParam;
import org.ofbiz.core.entity.EntityWhereString;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;

import java.util.List;

/**
 * This class builds an Entity Condition "where string" that supports parameter markers. 
 *
 * @since 0.1
 */
public class EntityAttributeCondition extends EntityWhereString
{

    private final String propertyName;
    private final String value;
    public EntityAttributeCondition(final String whereString, final String propertyName, final String value) {
        super(whereString);
        this.propertyName = propertyName;
        this.value = value;
    }

    @Override
    public String makeWhereString(final ModelEntity modelEntity, final List entityConditionParams)
    {
        /* Build model fields to be used in the entity condition. */
        ModelField mfName = new ModelField();
        mfName.setName("name");
        mfName.setColName("name");
        mfName.setType("long-varchar");
        entityConditionParams.add(new EntityConditionParam(mfName, propertyName));

        ModelField mfValue = new ModelField();
        mfValue.setName("value");
        mfValue.setColName("value");
        mfValue.setType("long-varchar");
        entityConditionParams.add(new EntityConditionParam(mfValue, value));

        return super.makeWhereString(modelEntity, entityConditionParams);
    }
}
