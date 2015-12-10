package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Parses global issue types and adds them to the appropriate mapper.
 *
 * @since v3.13
 */
public class IssueTypeMapperHandler implements ImportEntityHandler
{
    public static final String ISSUETYPE_ENTITY_NAME = "IssueType";

    private final IssueTypeMapper issueTypeMapper;
    private static final String JIRA_SUBTASK = "jira_subtask";

    public IssueTypeMapperHandler(final IssueTypeMapper issueTypeMapper)
    {
        this.issueTypeMapper = issueTypeMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // Check if the incoming entity is one of the ones we are interested in.
        if (ISSUETYPE_ENTITY_NAME.equals(entityName))
        {
            final String id = (String) attributes.get("id");
            final String name = (String) attributes.get("name");
            final String style = (String) attributes.get("style");
            if (StringUtils.isBlank(id))
            {
                throw new ParseException("Encountered an entity of type '" + ISSUETYPE_ENTITY_NAME + "' with a missing ID.");
            }
            if (StringUtils.isBlank(name))
            {
                throw new ParseException("The name of " + ISSUETYPE_ENTITY_NAME + " '" + id + "' is missing.");
            }
            // Keep track if our issue type is a subtask or not
            issueTypeMapper.registerOldValue(id, name, JIRA_SUBTASK.equals(style));
        }
    }

    public void startDocument()
    {
    // No-op
    }

    public void endDocument()
    {
    // No-op
    }

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

        final IssueTypeMapperHandler that = (IssueTypeMapperHandler) o;

        if (issueTypeMapper != null ? !issueTypeMapper.equals(that.issueTypeMapper) : that.issueTypeMapper != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (issueTypeMapper != null ? issueTypeMapper.hashCode() : 0);
    }
}
