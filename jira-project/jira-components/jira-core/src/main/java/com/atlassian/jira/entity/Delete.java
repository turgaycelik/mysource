package com.atlassian.jira.entity;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.Validate;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create a Delete SQL statement to be executed by {@link EntityEngine#delete(Delete.DeleteWhereContext)}.
 *
 * @since v5.0
 *
 * @see EntityEngine#delete(Delete.DeleteWhereContext)
 */
public class Delete
{
    public static DeleteFromContext from(String entityName)
    {
        return new DeleteFromContext(entityName);
    }

    public static DeleteFromContext from(EntityFactory entityFactory)
    {
        return new DeleteFromContext(entityFactory.getEntityName());
    }

    public static abstract class WhereClauseAwareDeleteContext
    {
        public abstract DeleteWhereContext whereEqual(String fieldName, String value);
        public abstract DeleteWhereContext whereEqual(String fieldName, Long value);
        public abstract DeleteWhereContext whereCondition(EntityCondition condition);

        public final DeleteWhereContext whereLike(String fieldName, String pattern)
        {
            return whereCondition(new EntityExpr(fieldName, EntityOperator.LIKE, pattern));
        }

        public final DeleteWhereContext andEqual(String fieldName, String value)
        {
            return whereEqual(fieldName, value);
        }

        public final DeleteWhereContext andEqual(String fieldName, Long value)
        {
            return whereEqual(fieldName, value);
        }

        public final DeleteWhereContext andLike(String fieldName, String pattern)
        {
            return whereLike(fieldName, pattern);
        }

        public final DeleteWhereContext andCondition(EntityCondition condition)
        {
            return whereCondition(condition);
        }
    }

    public static class DeleteFromContext extends WhereClauseAwareDeleteContext
    {
        private final String entityName;

        private DeleteFromContext(String entityName)
        {
            this.entityName = entityName;
        }

        public DeleteWhereContext all()
        {
            return new DeleteWhereContext(entityName, new FieldMap());
        }

        public DeleteWhereContext byAnd(FieldMap fieldMap)
        {
            return (fieldMap != null) ? new DeleteWhereContext(entityName, fieldMap) : all();
        }

        public DeleteWhereContext whereIdEquals(Long id)
        {
            return new DeleteWhereContext(entityName, new FieldMap("id", id));
        }

        @Override
        public DeleteWhereContext whereEqual(String fieldName, String value)
        {
            return new DeleteWhereContext(entityName, new FieldMap(fieldName, value));
        }

        @Override
        public DeleteWhereContext whereEqual(String fieldName, Long value)
        {
            return new DeleteWhereContext(entityName, new FieldMap(fieldName, value));
        }

        @Override
        public DeleteWhereContext whereCondition(EntityCondition condition)
        {
            return all().whereCondition(condition);
        }
    }

    public static class DeleteWhereContext extends WhereClauseAwareDeleteContext
    {
        private final String entityName;
        private final FieldMap fieldMap;
        private final List<EntityCondition> conditions = new ArrayList<EntityCondition>();

        private DeleteWhereContext(String entityName, FieldMap fieldMap)
        {
            Validate.notBlank(entityName);
            Validate.notNull(fieldMap);
            this.entityName = entityName;
            this.fieldMap = fieldMap;
        }

        @Override
        public DeleteWhereContext whereEqual(String fieldName, String value)
        {
            fieldMap.add(fieldName, value);
            return this;
        }

        @Override
        public DeleteWhereContext whereEqual(String fieldName, Long value)
        {
            fieldMap.add(fieldName, value);
            return this;
        }

        public DeleteWhereContext whereCondition(EntityCondition condition)
        {
            conditions.add(condition);
            return this;
        }

        String getEntityName()
        {
            return entityName;
        }

        FieldMap getFieldMap()
        {
            return fieldMap;
        }

        List<EntityCondition> getConditions()
        {
            return conditions;
        }

        public int execute(EntityEngine entityEngine)
        {
            return entityEngine.delete(this);
        }

        public int execute(OfBizDelegator ofBizDelegator)
        {
            if (conditions.isEmpty())
            {
                return ofBizDelegator.removeByAnd(getEntityName(), getFieldMap());
            }
            EntityCondition condition;
            if (conditions.size() == 1)
            {
                condition = conditions.get(0);
            }
            else
            {
                condition = new EntityConditionList(conditions, EntityOperator.AND);
            }
            if (!fieldMap.isEmpty())
            {
                condition = new EntityConditionList(
                        ImmutableList.of(new EntityFieldMap(fieldMap, EntityOperator.AND), condition),
                        EntityOperator.AND);
            }
            return ofBizDelegator.removeByCondition(entityName, condition);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final DeleteWhereContext that = (DeleteWhereContext) o;

            return conditions.equals(that.conditions) && entityName.equals(that.entityName) && fieldMap.equals(that.fieldMap);
        }

        @Override
        public int hashCode()
        {
            return entityName.hashCode();
        }

        @Override
        public String toString()
        {
            return String.format("[Entity = %s, Map = %s, condition = %s]", entityName, fieldMap, conditions);
        }
    }
}
