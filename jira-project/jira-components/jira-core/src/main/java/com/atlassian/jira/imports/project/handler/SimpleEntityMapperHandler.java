package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.mapper.MapperEntityRegister;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * This will populate the given Mapper with the global values for a simple globally configured object in JIRA.
 * This gets all the objects of the given type in the system and puts them into the mapper.
 * The {@link IssueMapperHandler} is responsible for flagging which of these objects is required for a specific project.
 *
 * @since v3.13
 */
public class SimpleEntityMapperHandler implements ImportEntityHandler
{
    public static final String PRIORITY_ENTITY_NAME = "Priority";
    public static final String STATUS_ENTITY_NAME = "Status";
    public static final String RESOLUTION_ENTITY_NAME = "Resolution";
    public static final String PROJECT_ROLE_ENTITY_NAME = "ProjectRole";

    private final MapperEntityRegister mapperEntityRegister;
    private final String entityTypeName;

    public SimpleEntityMapperHandler(final String entityTypeName, final MapperEntityRegister mapperEntityRegister)
    {
        Null.not("entityTypeName", entityTypeName);
        this.mapperEntityRegister = mapperEntityRegister;
        this.entityTypeName = entityTypeName;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // Check if the incoming entity is one of the ones we are interested in.
        if (entityTypeName.equals(entityName))
        {
            final String id = (String) attributes.get("id");
            final String name = (String) attributes.get("name");
            if (StringUtils.isBlank(id))
            {
                throw new ParseException("Encountered an entity of type '" + entityTypeName + "' with a missing ID.");
            }
            if (StringUtils.isBlank(name))
            {
                throw new ParseException("The name of " + entityTypeName + " '" + id + "' is missing.");
            }
            mapperEntityRegister.registerOldValue(id, name);
        }
    }

    ///CLOVER:OFF
    public void startDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF - always used for tests
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final SimpleEntityMapperHandler handler = (SimpleEntityMapperHandler) o;

        if (entityTypeName != null ? !entityTypeName.equals(handler.entityTypeName) : handler.entityTypeName != null)
        {
            return false;
        }
        if (mapperEntityRegister != null ? !mapperEntityRegister.equals(handler.mapperEntityRegister) : handler.mapperEntityRegister != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (mapperEntityRegister != null ? mapperEntityRegister.hashCode() : 0);
        result = 31 * result + (entityTypeName != null ? entityTypeName.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
