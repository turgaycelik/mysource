package com.atlassian.jira.entity.property;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Function2;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.EntityConstants;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Visitor;

import com.google.common.collect.Lists;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.entity.Entity.ENTITY_PROPERTY;
import static com.atlassian.jira.entity.property.EntityProperty.ENTITY_ID;
import static com.atlassian.jira.entity.property.EntityProperty.ENTITY_NAME;
import static com.atlassian.jira.entity.property.EntityProperty.ID;
import static com.atlassian.jira.entity.property.EntityProperty.KEY;
import static com.atlassian.jira.entity.property.EntityProperty.UPDATED;
import static com.atlassian.jira.entity.property.EntityProperty.VALUE;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Low-level database API for accessing JSON entity properties.
 *
 * @since v6.1
 */
public class JsonEntityPropertyManagerImpl implements JsonEntityPropertyManager
{
    /**
     * If you any of these change these values, please update the JavaDocs for
     * {@link JsonEntityPropertyManager#put(String, Long, String, String)} and
     * {@link JsonEntityPropertyManager#getMaximumValueLength()} accordingly.
     */
    public static final int MAXIMUM_ENTITY_NAME_LENGTH = EntityConstants.LONG_VARCHAR_MAXIMUM_LENGTH;
    public static final int MAXIMUM_KEY_LENGTH = EntityConstants.LONG_VARCHAR_MAXIMUM_LENGTH;
    public static final int MAXIMUM_VALUE_LENGTH = EntityConstants.EXTREMELY_LONG_MAXIMUM_LENGTH;

    private final EntityEngine entityEngine;
    private final EventPublisher eventPublisher;

    public JsonEntityPropertyManagerImpl(EntityEngine entityEngine, EventPublisher eventPublisher)
    {
        this.entityEngine = entityEngine;
        this.eventPublisher = eventPublisher;
    }



    @Override
    public void putDryRun(@Nonnull final String entityName, @Nonnull final String key, final String json)
    {
        if (json == null)
        {
            // It's a delete, so we don't really care...
            return;
        }

        restrictLength("entityName", notBlank("entityName", entityName), MAXIMUM_ENTITY_NAME_LENGTH);
        restrictLength("key", notBlank("key", key), MAXIMUM_KEY_LENGTH);
        restrictLength("json", json, EntityConstants.EXTREMELY_LONG_MAXIMUM_LENGTH);
        validateJson(json);
    }

    @Override
    public void put(@Nonnull final String entityName, @Nonnull final Long entityId, @Nonnull final String key,
            final String json)
    {
        if (json == null)
        {
            delete(entityName, entityId, key);
            return;
        }

        putDryRun(entityName, key, json);

        // We only need the ID and UPDATED to resolve conflicts; a full get(...) could be expensive if
        // large values are stored
        final GenericValue existing = findUniqueGenericValue(
                Select.columns(ID, UPDATED).from(ENTITY_PROPERTY.getEntityName()),
                entityName, entityId, key);
        if (existing == null)
        {
            // New property
            entityEngine.createValue(ENTITY_PROPERTY, EntityPropertyImpl.forCreate(entityName, entityId, key, json));
            return;
        }

        // Updating an existing property
        Update.into(ENTITY_PROPERTY)
                .set(UPDATED, new Timestamp(System.currentTimeMillis()))
                .set(VALUE, json)
                .whereEqual(ID, existing.getLong(ID))
                .execute(entityEngine);
    }

    @Override
    public void put(ApplicationUser user, @Nonnull String entityName, @Nonnull Long entityId, @Nonnull String key, @Nullable String json, Function2<ApplicationUser, EntityProperty, ? extends EntityPropertySetEvent> eventFunction, boolean dispatchEvent){
        put(entityName, entityId, key, json);
        if(dispatchEvent && eventFunction != null)
        {
            EntityProperty property = get(entityName, entityId, key);
            if(property != null) {
                eventPublisher.publish(eventFunction.apply(user, property));
            }
        }
    }

    @Override
    public EntityProperty get(final String entityName, final Long entityId, final String key)
    {
        final GenericValue gv = findUniqueGenericValue(
                Select.from(ENTITY_PROPERTY.getEntityName()),
                notBlank("entityName", entityName),
                notNull("entityId", entityId),
                notBlank("key", key));
        return (gv != null) ? ENTITY_PROPERTY.build(gv) : null;
    }

    @Override
    public void delete(@Nonnull final String entityName, @Nonnull final Long entityId, @Nonnull final String key)
    {
        Delete.from(ENTITY_PROPERTY)
                .whereEqual(ENTITY_NAME, notBlank("entityName", entityName))
                .andEqual(ENTITY_ID, notNull("entityId", entityId))
                .andEqual(KEY, notBlank("key", key))
                .execute(entityEngine);
    }

    @Override
    public int getMaximumValueLength()
    {
        return EntityConstants.EXTREMELY_LONG_MAXIMUM_LENGTH;
    }

    @Override
    public EntityPropertyQuery<?> query()
    {
        return new JsonEntityPropertyQuery();
    }



    // Convenience methods

    @Nonnull
    @Override
    public List<String> findKeys(@Nonnull final String entityName, @Nonnull final String keyPrefix)
    {
        return query().entityName(entityName)
                .keyPrefix(keyPrefix)
                .findDistinctKeys();
    }

    @Nonnull
    @Override
    public List<String> findKeys(@Nonnull final String entityName, @Nonnull final Long entityId)
    {
        return query().entityId(entityId)
                .entityName(entityName)
                .findKeys();
    }

    @Override
    public boolean exists(@Nonnull final String entityName, @Nonnull final Long entityId, @Nonnull final String key)
    {
        return query().entityName(entityName)
                .entityId(entityId)
                .key(key)
                .count() > 0;
    }

    @Override
    public long countByEntity(@Nonnull final String entityName, @Nonnull final Long entityId)
    {
        return query().entityName(entityName)
                .entityId(entityId)
                .count();
    }

    @Override
    public long countByEntityNameAndPropertyKey(@Nonnull final String entityName, @Nonnull final String key)
    {
        return query().entityName(entityName)
                .key(key)
                .count();
    }

    @Override
    public void deleteByEntity(@Nonnull final String entityName, @Nonnull final Long entityId)
    {
        query().entityName(entityName)
                .entityId(entityId)
                .delete();
    }

    @Override
    public void deleteByEntityNameAndPropertyKey(@Nonnull final String entityName, @Nonnull final String key)
    {
        query().entityName(entityName)
                .key(key)
                .delete();
    }



    /**
     * Since we are not using a unique constraint across (entityName, entityId, propertyKey) tuples, we need
     * to have some kind of fault-tolerance mechanism for when the race condition results in multiple values
     * being stored for the same tuple.  What we'll do is take only the most recent value, deleting any old
     * ones that we accidentally pulled in with it.  Note that
     * {@link EntityPropertyQuery.ExecutableQuery#count() count} queries can return incorrect results when
     * the race condition in {@link #put(String, Long, String, String) put} fails, but it will correct itself
     * when this method is called from inside a {@code find} or {@code get} request.
     */
    private GenericValue findUniqueGenericValue(Select.WhereClauseAwareContext<GenericValue> select,
            String entityName, Long entityId, String key)
    {
        final List<GenericValue> list = select
                .whereEqual(ENTITY_NAME, entityName)
                .andEqual(ENTITY_ID, entityId)
                .andEqual(KEY, key)
                .runWith(entityEngine)
                .asList();
        if (list.isEmpty())
        {
            return null;
        }
        if (list.size() > 1)
        {
            Collections.sort(list, GenericValueCollisionResolver.INSTANCE);

            // Destroy the older duplicates
            for (int i=1; i<list.size(); ++i)
            {
                Delete.from(ENTITY_PROPERTY)
                        .whereIdEquals(list.get(i).getLong(ID))
                        .execute(entityEngine);
            }
        }
        return list.get(0);
    }

    /**
     * Filters a basic select statement for the entity name and either property key or property
     * key prefix, as requested, imposing only the {@code WHERE} clause portion.  Use this instead
     * of {@link #filteredQuery(Select.WhereClauseAwareContext, JsonEntityPropertyQuery)} when the results
     * will be counted, deleted, etc. as the ordering and limit aren't needed.
     *
     * @param select the basic select statement context
     * @param query the query parameters to be applied
     * @param <T> the expected result entity type
     * @return and ongoing select query context queryed by the appropriate fields
     */
    private <T> Select.WhereClauseAwareContext<T> filteredWhere(
            Select.WhereClauseAwareContext<T> select,
            JsonEntityPropertyQuery query)
    {
        if (query.entityName != null)
        {
            select = select.whereEqual(ENTITY_NAME, query.entityName);
        }
        if (query.entityId != null)
        {
            select = select.whereEqual(ENTITY_ID, query.entityId);
        }
        if (query.key != null)
        {
            select = select.whereEqual(KEY, query.key);
        }
        else if (query.keyPrefix != null)
        {
            select = select.whereLike(KEY, query.keyPrefix + '%');
        }
        return select;
    }

    /**
     * Filters a basic select statement for the entity name and either property key or property
     * key prefix, as requested.  Also imposes ordering by entityName, entityId, and key as well
     * as any query limits.
     *
     * @param select the basic select statement context
     * @param query the query parameters to be applied
     * @param <T> the expected result entity type
     * @return a select query context execution context ready to be resolved with
     *          {@code .singleValue()}, {@code .asList()}, etc.
     */
    <T> SelectQuery.ExecutionContext<T> filteredQuery(
            Select.WhereClauseAwareContext<T> select,
            JsonEntityPropertyQuery query)
    {
        return filteredWhere(select, query)
                .orderBy(ENTITY_NAME, ENTITY_ID, KEY)
                .limit(query.offset, query.maxResults)
                .runWith(entityEngine);
    }

    /**
     * Verifies that the provided string is well-formed JSON data.
     *
     * @param json the alleged JSON input
     * @throws IllegalArgumentException if the provided data is malformed
     */
    private void validateJson(final String json)
    {
        JsonFactory jsonFactory = new JsonFactory();
        try
        {
            JsonParser jp = jsonFactory.createJsonParser(new StringReader(json));
            //noinspection StatementWithEmptyBody

            // lightweight version of code from Jackson2StaxReader, which is not provided by Jira
            // this code is required, because Json reader from Jackson does not validate all corner cases
            JsonToken jtok = jp.nextToken();
            int level = 0;
            while (jtok != null)
            {
                switch(jtok)
                {
                    case START_OBJECT:
                    case START_ARRAY:
                        level++;
                        jtok = jp.nextToken();
                        break;
                    case END_ARRAY:
                    case END_OBJECT:
                        level--;
                        jtok = jp.nextToken();
                        if(level == 0 && jtok != null)
                        {
                            switch(jtok)
                            {
                                case VALUE_STRING:
                                    throw new IOException("Unexpected string");
                                case VALUE_NUMBER_FLOAT:
                                case VALUE_NUMBER_INT:
                                    throw new IOException("Unexpected number");
                                default:
                                    throw new IOException("Unexpected character " + jtok.asString().charAt(0));
                            }
                        }
                        break;
                    default:
                        jtok = jp.nextToken();
                        break;
                }
            }
        }
        catch (IOException ioe)
        {
            throw new InvalidJsonPropertyException(ioe);
        }
    }



    class DuplicateResolver implements EntityListConsumer<EntityProperty,List<EntityProperty>>
    {
        private Map<Key,EntityProperty> results = new LinkedHashMap<Key,EntityProperty>();

        @Override
        public void consume(final EntityProperty entity)
        {
            final Key key = new Key(entity);
            final EntityProperty first = results.get(key);
            if (first == null)
            {
                results.put(key, entity);
                return;
            }

            if (EntityPropertyCollisionResolver.INSTANCE.compare(first, entity) > 0)
            {
                // If the one we already have is older than this one...
                // Explicit remove to make sure the new one shows up in the right order
                results.remove(key);
                results.put(key, entity);
                Delete.from(ENTITY_PROPERTY).whereIdEquals(first.getId()).execute(entityEngine);
            }
            else
            {
                // The one we already have is newer than this one...
                Delete.from(ENTITY_PROPERTY).whereIdEquals(entity.getId()).execute(entityEngine);
            }
        }

        @Override
        public List<EntityProperty> result()
        {
            return Lists.newArrayList(results.values());
        }
    }

    static class Key
    {
        final EntityProperty property;
        final int hash;

        Key(EntityProperty property)
        {
            this.property = property;
            int hash = property.getEntityId().hashCode();
            hash = 29 * hash + property.getEntityName().hashCode();
            hash = 83 * hash + property.getKey().hashCode();
            this.hash = hash;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (!(o instanceof Key))
            {
                return false;
            }

            final Key other = (Key)o;
            return hash == other.hash
                    && property.getEntityId().equals(other.property.getEntityId())
                    && property.getEntityName().equals(other.property.getEntityName())
                    && property.getKey().equals(other.property.getKey());
        }

        @Override
        public int hashCode()
        {
            return hash;
        }
    }

    /**
     * Imposes ORDER BY updated DESC, id DESC on GenericValue results.
     */
    static class GenericValueCollisionResolver implements Comparator<GenericValue>
    {
        static final GenericValueCollisionResolver INSTANCE = new GenericValueCollisionResolver();

        @Override
        public int compare(final GenericValue o1, final GenericValue o2)
        {
            // Note: intentionally reversed
            int cmp = o2.getTimestamp(UPDATED).compareTo(o1.getTimestamp(UPDATED));
            return (cmp != 0) ? cmp : o2.getLong(ID).compareTo(o1.getLong(ID));
        }
    }

    /**
     * Imposes ORDER BY updated DESC, id DESC on EntityProperty results.
     */
    static class EntityPropertyCollisionResolver implements Comparator<EntityProperty>
    {
        static final EntityPropertyCollisionResolver INSTANCE = new EntityPropertyCollisionResolver();

        @Override
        public int compare(final EntityProperty o1, final EntityProperty o2)
        {
            // Note: intentionally reversed
            int cmp = o2.getUpdated().compareTo(o1.getUpdated());
            return (cmp != 0) ? cmp : o2.getId().compareTo(o2.getId());
        }
    }

    static void restrictLength(String field, String value, int maximumLength)
    {
        if (value.length() > maximumLength)
        {
            throw new FieldTooLongJsonPropertyException(field, value.length(), maximumLength);
        }
    }


    class JsonEntityPropertyQuery implements EntityPropertyQuery<JsonEntityPropertyQuery>, Cloneable
    {
        String entityName;
        Long entityId;
        String key;
        String keyPrefix;
        int offset;
        int maxResults;

        @Override
        public ExecutableQuery entityName(@Nonnull final String entityName)
        {
            this.entityName = notBlank("entityName", entityName);
            return new ExecutableQuery();
        }

        @Override
        public ExecutableQuery key(@Nonnull final String key)
        {
            this.key = notBlank("key", key);
            if (keyPrefix != null)
            {
                throw new IllegalStateException("You cannot search by both 'key' and 'keyPrefix'");
            }
            return new ExecutableQuery();
        }

        @Override
        public JsonEntityPropertyQuery entityId(@Nonnull final Long entityId)
        {
            this.entityId = notNull("entityId", entityId);
            return this;
        }

        @Override
        public JsonEntityPropertyQuery keyPrefix(@Nonnull final String keyPrefix)
        {
            this.keyPrefix = notBlank("keyPrefix", keyPrefix);
            if (key != null)
            {
                throw new IllegalStateException("You cannot search by both 'key' and 'keyPrefix'");
            }
            return this;
        }

        @Override
        public JsonEntityPropertyQuery offset(final int offset)
        {
            this.offset = offset;
            return this;
        }

        @Override
        public JsonEntityPropertyQuery maxResults(final int maxResults)
        {
            this.maxResults = maxResults;
            return this;
        }

        @Override
        public String toString()
        {
            return "JsonEntityPropertyQuery[" +
                    "entityName=" + entityName +
                    ",entityId=" + entityId +
                    ",key=" + key +
                    ",keyPrefix=" + keyPrefix +
                    ",offset=" + offset +
                    ",maxResults=" + maxResults +
                    ']';
        }

        class ExecutableQuery implements EntityPropertyQuery.ExecutableQuery
        {
            // The query methods delegate back to the manager to do the dirty work
            @Nonnull
            @Override
            public List<String> findDistinctKeys()
            {
                return filteredQuery(Select.distinctString(KEY)
                        .from(ENTITY_PROPERTY), JsonEntityPropertyQuery.this)
                        .asList();
            }

            @Nonnull
            @Override
            public List<String> findKeys()
            {
                return filteredQuery(Select.stringColumn(KEY)
                        .from(ENTITY_PROPERTY), JsonEntityPropertyQuery.this)
                        .asList();
            }

            @Nonnull
            @Override
            public List<EntityProperty> find()
            {
                return filteredQuery(Select.from(ENTITY_PROPERTY), JsonEntityPropertyQuery.this)
                        .consumeWith(new DuplicateResolver());
            }

            @Override
            public void find(@Nonnull final Visitor<EntityProperty> visitor)
            {
                for (EntityProperty property : find())
                {
                    visitor.visit(property);
                }
            }

            @Override
            public long count()
            {
                return filteredWhere(Select.from(ENTITY_PROPERTY), JsonEntityPropertyQuery.this)
                        .runWith(entityEngine)
                        .count();
            }

            @Override
            public void delete()
            {
                final FieldMap fieldMap = new FieldMap();
                if (entityName != null)
                {
                    fieldMap.add(ENTITY_NAME, entityName);
                }
                if (entityId != null)
                {
                    fieldMap.add(ENTITY_ID, entityId);
                }
                if (key != null)
                {
                    fieldMap.add(KEY, key);
                }

                Delete.DeleteWhereContext delete = Delete.from(ENTITY_PROPERTY).byAnd(fieldMap);
                if (keyPrefix != null)
                {
                    delete = delete.whereLike(KEY, keyPrefix);
                }
                delete.execute(entityEngine);
            }



            // The builder methods all delegate so that any sanity checking can be inherited.
            @Override
            public ExecutableQuery entityName(@Nonnull final String entityName)
            {
                JsonEntityPropertyQuery.this.entityName(entityName);
                return this;
            }

            @Override
            public ExecutableQuery key(@Nonnull final String key)
            {
                JsonEntityPropertyQuery.this.key(key);
                return this;
            }

            @Override
            public ExecutableQuery entityId(@Nonnull final Long entityId)
            {
                JsonEntityPropertyQuery.this.entityId(entityId);
                return this;
            }

            @Override
            public ExecutableQuery keyPrefix(@Nonnull final String keyPrefix)
            {
                JsonEntityPropertyQuery.this.keyPrefix(keyPrefix);
                return this;
            }

            @Override
            public ExecutableQuery offset(final int offset)
            {
                JsonEntityPropertyQuery.this.offset(offset);
                return this;
            }

            @Override
            public ExecutableQuery maxResults(final int maxResults)
            {
                JsonEntityPropertyQuery.this.maxResults(maxResults);
                return this;
            }

            @Override
            public String toString()
            {
                return "ExecutableQuery[" + JsonEntityPropertyQuery.this.toString() + ']';
            }
        }
    }
}
