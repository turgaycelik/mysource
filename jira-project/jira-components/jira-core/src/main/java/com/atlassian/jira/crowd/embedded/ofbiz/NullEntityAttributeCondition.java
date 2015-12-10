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
public class NullEntityAttributeCondition extends EntityWhereString
{
    private final String propertyName;

    public NullEntityAttributeCondition(final String whereString, final String propertyName) {
        super(whereString);
        this.propertyName = propertyName;
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

        return super.makeWhereString(modelEntity, entityConditionParams);
    }
}