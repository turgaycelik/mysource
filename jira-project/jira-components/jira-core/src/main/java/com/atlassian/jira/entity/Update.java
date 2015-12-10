package com.atlassian.jira.entity;

import java.sql.Timestamp;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

/**
 * This class is a fluent builder to create an Update SQL statement to be executed by Entity Engine.
 * <p>
 * You can run this like:
 * <pre>
 *     Update.into("Project").set("counter", counter).whereEqual("key", projectKey).execute(ofBizDelegator);
 * </pre>
 * or:
 * <pre>
 *     entityEngine.execute(
 *         Update.into("Project").set("counter", counter).whereEqual("key", projectKey)
 *     );
 * </pre>
 * @since v5.0
 *
 * @see EntityEngine#execute(Update.WhereContext)
 */
public class Update
{
    public static IntoContext into(String entityName)
    {
        return new IntoContext(entityName);
    }

    public static IntoContext into(EntityFactory entityFactory)
    {
        return new IntoContext(entityFactory.getEntityName());
    }

    public static class IntoContext
    {
        private final String entityName;

        private IntoContext(String entityName)
        {
            this.entityName = entityName;
        }

        public SetContext set(String fieldName, Long value)
        {
            return new SetContext(entityName, FieldMap.build(fieldName, value));
        }

        public SetContext set(String fieldName, String value)
        {
            return new SetContext(entityName, FieldMap.build(fieldName, value));
        }

        public SetContext set(String fieldName, Timestamp value)
        {
            return new SetContext(entityName, FieldMap.build(fieldName, value));
        }
    }

    public static class SetContext
    {
        private final String entityName;
        private final FieldMap updateFields;

        private SetContext(String entityName, FieldMap updateFields)
        {
            this.entityName = entityName;
            this.updateFields = updateFields;
        }

        public SetContext set(String fieldName, Long value)
        {
            updateFields.add(fieldName, value);
            return this;
        }

        public SetContext set(String fieldName, String value)
        {
            updateFields.add(fieldName, value);
            return this;
        }

        public SetContext set(String fieldName, Timestamp value)
        {
            updateFields.add(fieldName, value);
            return this;
        }

        public WhereContext all()
        {
            return new WhereContext(entityName, updateFields, new FieldMap());
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        public WhereContext whereIdEquals(Long id)
        {
            return new WhereContext(entityName, updateFields, new FieldMap("id", id));
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        public WhereContext whereEqual(String fieldName, String value)
        {
            return new WhereContext(entityName, updateFields, new FieldMap(fieldName, value));
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        public WhereContext whereEqual(String fieldName, Long value)
        {
            return new WhereContext(entityName, updateFields, new FieldMap(fieldName, value));
        }
    }

    public static class WhereContext
    {
        private final String entityName;
        private final FieldMap updateFields;
        private final FieldMap whereClause;

        private WhereContext(String entityName, FieldMap updateFields, FieldMap whereClause)
        {
            this.entityName = entityName;
            this.updateFields = updateFields;
            this.whereClause = whereClause;
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        public WhereContext andEqual(String fieldName, String value)
        {
            whereClause.add(fieldName, value);
            return this;
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        public WhereContext andEqual(String fieldName, Long value)
        {
            whereClause.add(fieldName, value);
            return this;
        }

        public int execute(EntityEngine entityEngine)
        {
            return entityEngine.execute(this);
        }

        public int execute(OfBizDelegator ofBizDelegator)
        {
            return ofBizDelegator.bulkUpdateByAnd(entityName, updateFields, whereClause);
        }
    }
}
