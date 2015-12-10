package com.atlassian.jira.user;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.is;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public class UserHistoryItem
{
    private static final int VERY_SHORT = 10;

    /*
     * WARNING: These are defined as field type "very-short" and hence must be 10 or fewer characters!
     */
    final public static Type ADMIN_PAGE = new Type("AdminPage");
    final public static Type ASSIGNEE = new Type("Assignee");
    final public static Type DASHBOARD = new Type("Dashboard");
    final public static Type ISSUE = new Type("Issue");
    final public static Type ISSUELINKTYPE = new Type("IssueLink");
    final public static Type ISSUESEARCHER = new Type("Searcher");
    final public static Type JQL_QUERY = new Type("JQLQuery");
    final public static Type PROJECT = new Type("Project");
    final public static Type RESOLUTION = new Type("Resolution");
    final public static Type USED_USER = new Type("UsedUser");

    final private static Map<String, Type> KNOWN_TYPES = ImmutableMap.<String, Type>builder()
            .put(ADMIN_PAGE.getName(), ADMIN_PAGE)
            .put(ASSIGNEE.getName(), ASSIGNEE)
            .put(DASHBOARD.getName(), DASHBOARD)
            .put(ISSUE.getName(), ISSUE)
            .put(ISSUELINKTYPE.getName(), ISSUELINKTYPE)
            .put(ISSUESEARCHER.getName(), ISSUESEARCHER)
            .put(JQL_QUERY.getName(), JQL_QUERY)
            .put(PROJECT.getName(), PROJECT)
            .put(RESOLUTION.getName(), RESOLUTION)
            .put(USED_USER.getName(), USED_USER)
            .build();

    /**
     * extracts the entity id, or null if input is null
     */
    public static final Function<UserHistoryItem, String> GET_ENTITY_ID = new Function<UserHistoryItem, String>()
    {
        @Nullable
        @Override
        public String apply(@Nullable UserHistoryItem input)
        {
            return input == null ? null : input.getEntityId();
        }
    };

    final private long lastViewed;
    private final Type type;
    private final String entityId;
    private final String data;

    public UserHistoryItem(final Type type, final String entityId, final long lastViewed, final String data)
    {
        notNull("type", type);
        notNull("entityId", entityId);
        notNull("lastViewed", lastViewed);

        this.type = type;
        this.entityId = entityId;
        this.lastViewed = lastViewed;
        this.data = data;
    }

    public UserHistoryItem(Type type, String entityId, long lastViewed)
    {
        this(type, entityId, lastViewed, null);
    }

    public UserHistoryItem(Type type, String entityId)
    {
        this(type, entityId, System.currentTimeMillis());
    }

    public UserHistoryItem(Type type, String entityId, String data)
    {
        this(type, entityId, System.currentTimeMillis(), data);
    }

    public long getLastViewed()
    {
        return lastViewed;
    }

    public Type getType()
    {
        return type;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public String getData()
    {
        return data;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final UserHistoryItem other = (UserHistoryItem) o;
        return entityId.equals(other.entityId) && lastViewed == other.lastViewed && type.equals(other.type);
    }

    @Override
    public int hashCode()
    {
        int result = (int)(lastViewed ^ (lastViewed >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "UserHistoryItem{" +
                "type=" + type +
                ", entityId='" + entityId + '\'' +
                ", lastViewed=" + lastViewed +
                '}';
    }



    public static class Type implements Serializable
    {
        private static final long serialVersionUID = 8237618370525069310L;
        private final String name;

        /**
         * Create a new Type.  This really should be made private but that would break API compatibility.
         * Prefer the use of {@link #getInstance(String name)} instead.
         * @param name Type name
         */
        public Type(final String name)
        {
            notBlank("name", name);
            is("name", name.length() <= VERY_SHORT);
            this.name = name;
        }

        /**
         * Get a Type.  This will retrieve one of the well known types if it exists.  Otherwise we just creat one dynamically.
         * Plugin developers should create a single (static) instance of any types they require and reuse them if possible.
         *
         * @param name Type name
         * @return a Type
         */
        public static Type getInstance(final String name)
        {
            Type type = KNOWN_TYPES.get(name);
            if (type != null)
            {
                return type;
            }
            return new Type(name);
        }

        public String getName()
        {
            return name;
        }

        @Override
        public int hashCode()
        {
            return (name == null) ? 0 : name.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            return Objects.equal(name, ((Type)obj).name);
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
