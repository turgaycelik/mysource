/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.mail.MailException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NotificationSchemeManager extends SchemeManager
{
    /**
     * Retrieve the set of recipients for the event using the specified notification entity.
     * @param event issue event
     * @param notification scheme entity
     * @return a Set of notification recipients
     * @throws GenericEntityException
     */
    public Set<NotificationRecipient> getRecipients(IssueEvent event, SchemeEntity notification) throws GenericEntityException;

    public boolean hasEntities(GenericValue scheme, Long event, String type, String parameter, Long templateId) throws GenericEntityException;

    public void removeSchemeEntitiesForField(String fieldId) throws RemoveException;

    public boolean isHasMailServer() throws MailException;

    /**
     * Given an issue event this method returns a set of {@link NotificationRecipient}s who will receive an e-mail
     * notification
     *
     * @param event The issue event that triggered the notification
     * @return A Set of recipients that will receive e-mails for this event.
     * @since v5.0
     */
    Set<NotificationRecipient> getRecipients(final IssueEvent event);

    /**
     * Get the notification scheme entities for this project and entity type.
     * <p/>
     * Returns an empty List if there problems (e.g. more than one scheme association for the Project) encountered.
     *
     * @param project the project
     * @param entityTypeId the type of entity
     *
     * @return notificationSchemeEntities scheme entities representing the notifications for the project
     */
    public List<SchemeEntity> getNotificationSchemeEntities(Project project, long entityTypeId)
            throws GenericEntityException;

    /**
     * Get the notification scheme for this project. There should be only one.
     * <p/>
     * Returns null if there problems (e.g. more than one scheme association) encountered.
     *
     * @param projectGV the project
     * @return notificationScheme   the GenericValue object representing a notification scheme
     */
    public GenericValue getNotificationSchemeForProject(GenericValue projectGV);

    /**
     * Retrieve a map of scheme ids to scheme names that match the specified conditions.
     *
     * @param conditions    Map of conditions on which to limit the search
     * @return Map          scheme ids -> scheme names
     */
    public Map<Long, String> getSchemesMapByConditions(Map<String, ?> conditions);

    /**
     * This is a method that is meant to quickly get you all the schemes that contain an entity of the
     * specified type and parameter.
     * @param type is the entity type
     * @param parameter is the scheme entries parameter value
     * @return Collection of GenericValues that represents a scheme
     */
    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter);

}
