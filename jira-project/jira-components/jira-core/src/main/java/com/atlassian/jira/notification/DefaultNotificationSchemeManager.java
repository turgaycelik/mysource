package com.atlassian.jira.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.cache.CacheManager;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.notification.NotificationAddedEvent;
import com.atlassian.jira.event.notification.NotificationDeletedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeAddedToProjectEvent;
import com.atlassian.jira.event.notification.NotificationSchemeCopiedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeCreatedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeDeletedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.notification.NotificationSchemeUpdatedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.notification.type.GroupCFValue;
import com.atlassian.jira.notification.type.UserCFValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.AbstractSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.core.util.map.EasyMap.build;

public class DefaultNotificationSchemeManager extends AbstractSchemeManager implements NotificationSchemeManager, Startable
{
    private static final Logger log = Logger.getLogger(DefaultNotificationSchemeManager.class);

    private static final String SCHEME_ENTITY_NAME = "NotificationScheme";
    private static final String NOTIFICATION_ENTITY_NAME = "Notification";
    private static final String SCHEME_DESC = "Notification";
    private static final String DEFAULT_NAME_KEY = "admin.schemes.notifications.default";
    private static final long DEFAULT_NOTIFICATION_SCHEME_ID = 10000L;

    private final OfBizDelegator delegator;
    private final NotificationTypeManager notificationTypeManager;
    private final UserPreferencesManager userPreferencesManager;

    public DefaultNotificationSchemeManager(ProjectManager projectManager, PermissionTypeManager permissionTypeManager,
            PermissionContextFactory permissionContextFactory, OfBizDelegator delegator,
            SchemeFactory schemeFactory, EventPublisher eventPublisher, NotificationTypeManager notificationTypeManager,
            final NodeAssociationStore nodeAssociationStore, final GroupManager groupManager,
            final UserPreferencesManager userPreferencesManager, CacheManager cacheManager)
    {
        super(projectManager, permissionTypeManager, permissionContextFactory, schemeFactory,
                nodeAssociationStore, delegator, groupManager, eventPublisher, cacheManager);
        this.delegator = delegator;
        this.notificationTypeManager = notificationTypeManager;
        this.userPreferencesManager = userPreferencesManager;
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    @Override
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
    }

    @Override
    public String getSchemeEntityName()
    {
        return SCHEME_ENTITY_NAME;
    }

    @Override
    public String getEntityName()
    {
        return NOTIFICATION_ENTITY_NAME;
    }

    @Override
    public String getSchemeDesc()
    {
        return SCHEME_DESC;
    }

    @Override
    public String getDefaultNameKey()
    {
        return DEFAULT_NAME_KEY;
    }

    @Override
    public String getDefaultDescriptionKey()
    {
        return null;
    }

    @Override
    protected AbstractSchemeEvent createSchemeCreatedEvent(final Scheme scheme)
    {
        return new NotificationSchemeCreatedEvent(scheme);
    }

    @Nonnull
    @Override
    protected AbstractSchemeCopiedEvent createSchemeCopiedEvent(@Nonnull final Scheme oldScheme, @Nonnull final Scheme newScheme)
    {
        return new NotificationSchemeCopiedEvent(oldScheme, newScheme);
    }

    @Override
    protected AbstractSchemeUpdatedEvent createSchemeUpdatedEvent(final Scheme scheme, final Scheme originalScheme)
    {
        return new NotificationSchemeUpdatedEvent(scheme, originalScheme);
    }

    @Override
    public void deleteScheme(Long id) throws GenericEntityException
    {
        final Scheme scheme = getSchemeObject(id);

        super.deleteScheme(id);

        eventPublisher.publish(new NotificationSchemeDeletedEvent(id, scheme.getName()));
    }

    @Nonnull
    @Override
    protected AbstractSchemeAddedToProjectEvent createSchemeAddedToProjectEvent(final Scheme scheme, final Project project)
    {
        return new NotificationSchemeAddedToProjectEvent(scheme, project);
    }

    @Override
    public GenericValue getDefaultScheme() throws GenericEntityException
    {
        return getScheme(DEFAULT_NOTIFICATION_SCHEME_ID);
    }

    @Override
    public GenericValue createDefaultScheme() throws GenericEntityException
    {
        if (getDefaultScheme() == null)
        {
            String name = getApplicationI18n().getText(DEFAULT_NAME_KEY);
            GenericValue defaultScheme;
            if (DEFAULT_NAME_KEY.equals(name))
            {
                defaultScheme = createSchemeGenericValue(FieldMap.build("name", "Default Notification Scheme", "description", null));
            }
            else
            {
                defaultScheme = createSchemeGenericValue(FieldMap.build("name", name, "description", null));
            }
            return defaultScheme;
        }
        else
        {
            return getDefaultScheme();
        }
    }

    @Override
    public void removeSchemeEntitiesForField(String customFieldId) throws RemoveException
    {
        // this hack will only work for the two built in Notification Event Types that use Custom Fields.
        // If anyone writes a plug-in that uses Custom Fields, we will currently not be able to delete this automatically.
        // Remove for User_Custom_Field_Value
        removeEntities(UserCFValue.ID, customFieldId);
        removeEntities(GroupCFValue.ID, customFieldId);
    }

    @Override
    protected SchemeEntity makeSchemeEntity(GenericValue schemeEntityGV)
    {
        return new SchemeEntity(schemeEntityGV.getLong("id"), schemeEntityGV.getString("type"),
                schemeEntityGV.getString("parameter"), schemeEntityGV.get("eventTypeId"),
                schemeEntityGV.get("templateId"), schemeEntityGV.getLong("scheme"));
    }

    @Override
    protected Object createSchemeEntityDeletedEvent(final GenericValue entity)
    {
        return new NotificationDeletedEvent(entity.getLong("scheme"), makeSchemeEntity(entity));
    }

    @Override
    public List<SchemeEntity> getNotificationSchemeEntities(Project project, long entityTypeId)
            throws GenericEntityException
    {
        List<SchemeEntity> schemeEntities = Lists.newArrayList();

        GenericValue notificationScheme = getNotificationSchemeForProject(project.getGenericValue());
        if (notificationScheme == null)
            return schemeEntities;

        List<GenericValue> entities = getEntities(notificationScheme, entityTypeId);
        for (GenericValue entity : entities)
        {
            schemeEntities.add(makeSchemeEntity(entity));
        }

        return schemeEntities;
    }

    @Override
    public GenericValue getNotificationSchemeForProject(GenericValue projectGV)
    {
        try
        {
            List<GenericValue> notificationSchemes = getSchemes(projectGV);
            if (notificationSchemes != null && !notificationSchemes.isEmpty()) {
                // Ensure that there is only one notification scheme associated with the project
                if (notificationSchemes.size() > 1)
                {
                    log.error("There are multiple notification schemes associated with the project: " + projectGV.getString("name") + ". " +
                            "No emails will be sent for issue events in this project.");
                }
                else
                {
                    return notificationSchemes.get(0);
                }
            }
        }
        catch (GenericEntityException e)
        {
            log.error("There was an error retrieving the notification schemes for the project: " + projectGV.getString("name") + ". " +
                    "No emails will be sent for issue events in this project.", e);
        }
        // No notification scheme associated with this project or multiple
        return null;
    }

    @Nonnull
    @Override
    public Map<Long, String> getSchemesMapByConditions(Map<String, ?> conditions)
    {
        Map<Long, String> schemeMap = new LinkedHashMap<Long, String>();
        OfBizListIterator listIterator = null;
        Long schemeId = null;

        try
        {
            // Set the search to be distinct
            EntityFindOptions entityFindOptions = new EntityFindOptions();
            entityFindOptions.setDistinct(true);

            // Retrieve the notification scheme entities that are associated with the conditions map
            listIterator = delegator.findListIteratorByCondition("Notification", new EntityFieldMap(conditions, EntityOperator.AND), null, null, null, entityFindOptions);
            GenericValue resultMap = listIterator.next();
            while (resultMap != null)
            {
                schemeId = resultMap.getLong("scheme");
                if (schemeId != null)
                {
                    final Scheme notificationScheme = getSchemeObject(schemeId);
                    schemeMap.put(schemeId, notificationScheme.getName());
                }
                resultMap = listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                // Close the iterator
                listIterator.close();
            }
        }

        return schemeMap;
    }

    @Nonnull
    @Override
    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter)
    {
        Map<String, ?> conditions = build("type", type, "parameter", parameter);
        Collection<GenericValue> entities = delegator.findByAnd(NOTIFICATION_ENTITY_NAME, conditions);
        Set<Long> schemeIds = new HashSet<Long>();
        List<EntityCondition> entityConditions = new ArrayList<EntityCondition>();
        for (GenericValue schemeEntity: entities)
        {
            // This is not needed if we can do a distinct select
            schemeIds.add(schemeEntity.getLong("scheme"));
        }
        for (Long id: schemeIds)
        {
            entityConditions.add(new EntityExpr("id", EntityOperator.EQUALS, id));
        }

        if (entityConditions.isEmpty())
        {
            return Collections.emptyList();
        }
        return delegator.findByOr(SCHEME_ENTITY_NAME, entityConditions, Collections.<String>emptyList());
    }

    @Override
    public boolean isHasMailServer() throws MailException
    {
        try
        {
            Object smtp = MailFactory.getServerManager().getDefaultSMTPMailServer();
            return (smtp != null);
        }
        catch (Exception ignored)
        {
            // This isn't the place to die if anything is wrong
        }
        return false;
    }

    @Override
    public GenericValue createSchemeEntity(GenericValue scheme, SchemeEntity schemeEntity) throws GenericEntityException
    {
        final GenericValue result = createSchemeEntityNoEvent(scheme, schemeEntity);
        eventPublisher.publish(new NotificationAddedEvent(scheme.getLong("id"), schemeEntity));
        return result;
    }

    @Override
    protected GenericValue createSchemeEntityNoEvent(GenericValue scheme, SchemeEntity schemeEntity) throws GenericEntityException
    {
        if (!(schemeEntity.getEntityTypeId() instanceof Long))
        {
            throw new IllegalArgumentException("Notification scheme IDs must be Long values.");
        }

        return EntityUtils.createValue(getEntityName(), build(
                "scheme", scheme.getLong("id"),
                "eventTypeId", schemeEntity.getEntityTypeId(),
                "type", schemeEntity.getType(),
                "parameter", schemeEntity.getParameter(),
                "templateId", schemeEntity.getTemplateId() ));
    }

    @Override
    public Set<NotificationRecipient> getRecipients(IssueEvent event, SchemeEntity notification) throws GenericEntityException
    {
        Set<NotificationRecipient> recipients = new HashSet<NotificationRecipient>();

        NotificationType notificationType = notificationTypeManager.getNotificationType(notification.getType());

        final List<NotificationRecipient> possibleRecipients = notificationType.getRecipients(event, notification.getParameter());
        // filter out inactive users
        for (NotificationRecipient recipient : possibleRecipients)
        {
            final User userRecipient = recipient.getUserRecipient();
            // null userRecipient is possible eg for SingleEmailAddress notification
            if (userRecipient == null || userRecipient.isActive())
            {
                recipients.add(recipient);
            }
        }

        final ApplicationUser user = ApplicationUsers.from(event.getUser());
        if (user != null)
        {
            // check if the user wishes to be notified of his own changes
            Preferences userPreference = userPreferencesManager.getExtendedPreferences(user);
            if (!userPreference.getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES))
            {
                recipients.remove(new NotificationRecipient(user));
                if (log.isDebugEnabled())
                {
                    log.debug("Removed user " + user.getDisplayName() + " with email address " + user.getEmailAddress() +
                            " from notification because they are the modifier and do not wish to be notified.");
                }
            }
        }
        return recipients;
    }

    @Override
    public Set<NotificationRecipient> getRecipients(final IssueEvent event)
    {
        Assertions.notNull("issueEvent", event);

        final Set<NotificationRecipient> ret = new LinkedHashSet<NotificationRecipient>();
        try
        {
            List<SchemeEntity> schemeEntities = getNotificationSchemeEntities(event.getProject(), event.getEventTypeId());
            for (SchemeEntity schemeEntity : schemeEntities)
            {
                ret.addAll(getRecipients(event, schemeEntity));
            }
        }
        catch (GenericEntityException e)
        {
            log.error("There was an error accessing the notification scheme for the project: " + event.getProject() + '.', e);
        }
        return ret;
    }

    @Override
    public boolean hasEntities(GenericValue scheme, Long eventTypeId, String type, String parameter, Long templateId)
            throws GenericEntityException
    {
        final List<GenericValue> entity = scheme.getRelatedByAnd("Child" + getEntityName(), build(
                "eventTypeId", eventTypeId,
                "type", type,
                "parameter", parameter,
                "templateId", templateId));
        return entity != null && !entity.isEmpty();
    }

    //This one if for Workflow Scheme Manager as the entity type is is a string
    @Override
    public List<GenericValue> getEntities(GenericValue scheme, String entityTypeId) throws GenericEntityException
    {
        throw new IllegalArgumentException("Notification scheme event type IDs must be Long values.");
    }

    @Override
    public List<GenericValue> getEntities(GenericValue scheme, Long eventTypeId) throws GenericEntityException
    {
        return scheme.getRelatedByAnd("Child" + getEntityName(), build("eventTypeId", eventTypeId));
    }

    @Override
    public List<GenericValue> getEntities(GenericValue scheme, Long eventTypeId, String parameter) throws GenericEntityException
    {
        return scheme.getRelatedByAnd("Child" + getEntityName(), build("eventTypeId", eventTypeId, "parameter", parameter));
    }

    @Override
    public List<GenericValue> getEntities(GenericValue scheme, String type, Long entityTypeId) throws GenericEntityException
    {
        return scheme.getRelatedByAnd("Child" + getEntityName(), build("eventTypeId", entityTypeId, "type", type));
    }

    @Override
    public boolean hasSchemeAuthority(Long entityType, GenericValue entity)
    {
        return false;
    }

    @Override
    public boolean hasSchemeAuthority(Long entityType, GenericValue entity, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
    {
        return false;
    }

    @Override
    protected AbstractSchemeRemovedFromProjectEvent createSchemeRemovedFromProjectEvent(final Scheme scheme, final Project project)
    {
        return new NotificationSchemeRemovedFromProjectEvent(scheme, project);
    }

    protected I18nHelper getApplicationI18n()
    {
        return new I18nBean((User) null);
    }
}
