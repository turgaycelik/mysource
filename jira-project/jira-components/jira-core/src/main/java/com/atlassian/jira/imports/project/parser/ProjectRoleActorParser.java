package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import java.util.Map;

/**
 * Converts projectRoleActor xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface ProjectRoleActorParser
{
    /**
     * Defines the element name that the parser will handle.
     */
    public static final String PROJECT_ROLE_ACTOR_ENTITY_NAME = "ProjectRoleActor";

    /**
     * Transforms a set of attributes into an {@link com.atlassian.jira.external.beans.ExternalProjectRoleActor}.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalProjectRoleActor. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>projectroleid (required)</li>
     * <li>roletype (required)</li>
     * <li>roletypeparameter (required)</li>
     * </ul>
     * @return a populated {@link com.atlassian.jira.external.beans.ExternalProjectRoleActor}, never null.
     *
     * @throws com.atlassian.jira.exception.ParseException If the attributes are invalid.
     */
    ExternalProjectRoleActor parse(Map attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the
     * provided ProjectRoleActor.
     *
     * @param projectRoleActor contains the populated fields that will end up in the EntityRepresentations map.
     * @return an EntityRepresentation that can be persisted using OfBiz.
     */
    EntityRepresentation getEntityRepresentation(ExternalProjectRoleActor projectRoleActor);
}
