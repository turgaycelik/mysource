package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.util.dbc.Null;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class ProjectParserImpl extends OSPropertyParser implements ProjectParser
{
    private final Map osPropertyIdMap = new HashMap();
    private final Map emailSenderMap = new HashMap();

    public ExternalProject parseProject(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("attributes cannot be null");
        }

        final String id = (String) attributes.get("id");
        final String name = (String) attributes.get("name");
        final String lead = (String) attributes.get("lead");
        final String description = (String) attributes.get("description");
        final String key = (String) attributes.get("key");
        final String url = (String) attributes.get("url");
        final String assigneeType = (String) attributes.get("assigneetype");
        final String counter = (String) attributes.get("counter");
        final String originalKey = (String) attributes.get("originalkey");

        // We don't want to return a project that is invalid
        if (id == null)
        {
            throw new ParseException("No id field for Project.");
        }
        if (key == null)
        {
            throw new ParseException("No key field for Project.");
        }

        final ExternalProject project = new ExternalProject();
        project.setId(id);
        project.setName(name);
        project.setLead(lead);
        project.setDescription(description);
        project.setKey(key);
        project.setOriginalKey(originalKey);
        project.setUrl(url);
        project.setAssigneeType(assigneeType);
        project.setCounter(counter);
        // Now add the optional "email sender" property saved from OSProperty.
        project.setEmailSender((String) emailSenderMap.get(id));
        return project;
    }

    public void parseOther(final String elementName, final Map attributes)
    {
        Null.not("elementName", elementName);

        if (elementName.equals(OSPROPERTY_ENTRY))
        {
            // <OSPropertyEntry id="10143" entityName="Project" entityId="10000" propertyKey="jira.project.email.sender" type="5"/>

            if (PROJECT_ENTITY_NAME.equals(getEntityName(attributes)))
            {
                // Check if this is the email sender property
                if (ProjectKeys.EMAIL_SENDER.equals(getPropertyKey(attributes)))
                {
                    // We remember the OSProperty ID, and the Project ID it is associated with. 
                    osPropertyIdMap.put(attributes.get("id"), attributes.get("entityId"));
                }
            }
        }

        if (elementName.equals(OSPROPERTY_STRING))
        {
            //     <OSPropertyString id="10143" value="dude@example.com"/>
            // Check if the id is one that we are interested in:
            final String projectID = (String) osPropertyIdMap.get(attributes.get("id"));
            if (projectID != null)
            {
                // This OSPropertyString holds the "email.sender" value for the given projectID
                emailSenderMap.put(projectID, attributes.get("value"));
            }
        }
    }
}
