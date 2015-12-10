package com.atlassian.jira.bc.project.version.remotelink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.FieldTooLongJsonPropertyException;
import com.atlassian.jira.entity.property.InvalidJsonPropertyException;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.entity.remotelink.AbstractRemoteEntityLinkService;
import com.atlassian.jira.event.project.RemoteVersionLinkDeleteEvent;
import com.atlassian.jira.event.project.RemoteVersionLinkPutEvent;
import com.atlassian.jira.event.project.VersionDeleteEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.base.Function;

import org.apache.commons.lang3.StringUtils;

import static com.atlassian.jira.entity.remotelink.RemoteEntityLink.GLOBAL_ID;
import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND;
import static com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR;
import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @since v6.1.1
 */
@EventComponent
public class RemoteVersionLinkServiceImpl extends AbstractRemoteEntityLinkService implements RemoteVersionLinkService
{
    /** Entity name */
    public static final String REMOTE_VERSION_LINK = "RemoteVersionLink";

    static final String VERSION_ID = "versionId";
    static final String JSON = "json";

    // Messages
    private static final String MSG_VERSION_DOES_NOT_EXIST     = "remotelink.service.version.does.not.exist";      // 0=versionId
    private static final String MSG_NO_LINK_VERSION_PERMISSION = "remotelink.service.no.link.version.permission";  // 0=projectKey



    private final PermissionManager permissionManager;
    private final VersionManager versionManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final EventPublisher eventPublisher;



    public RemoteVersionLinkServiceImpl(JsonEntityPropertyManager jsonEntityPropertyManager,
            PermissionManager permissionManager, VersionManager versionManager,
            EventPublisher eventPublisher, I18nHelper.BeanFactory i18nFactory)
    {
        super(EntityPropertyType.REMOTE_VERSION_LINK.getDbEntityName(), jsonEntityPropertyManager);
        this.permissionManager = permissionManager;
        this.versionManager = versionManager;
        this.eventPublisher = eventPublisher;
        this.i18nFactory = i18nFactory;
    }



    @Override
    public RemoteVersionLinkListResult getRemoteVersionLinksByVersionId(final ApplicationUser user,
            final Long versionId)
    {
        final I18nHelper i18n = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();
        verifyNotNull(errors, i18n, VERSION_ID, versionId);
        if (errors.hasAnyErrors())
        {
            return new RemoteVersionLinkListResult(errors);
        }

        final Version version = versionManager.getVersion(versionId);
        if (!canView(user, version))
        {
            errors.addError(VERSION_ID, i18n.getText(MSG_VERSION_DOES_NOT_EXIST, String.valueOf(versionId)), NOT_FOUND);
            return new RemoteVersionLinkListResult(errors);
        }

        final List<EntityProperty> properties = jsonEntityPropertyManager.query()
                .entityName(REMOTE_VERSION_LINK)
                .entityId(versionId)
                .find();
        return new RemoteVersionLinkListResult(toRemoteVersionLinks(properties, version));
    }

    @Override
    public RemoteVersionLinkListResult getRemoteVersionLinksByGlobalId(final ApplicationUser user,
            final String globalId)
    {
        final I18nHelper i18n = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();
        verifyNotBlank(errors, i18n, GLOBAL_ID, globalId);
        if (errors.hasAnyErrors())
        {
            return new RemoteVersionLinkListResult(errors);
        }
        final List<EntityProperty> properties = jsonEntityPropertyManager.query()
                .entityName(REMOTE_VERSION_LINK)
                .key(globalId)
                .find();
        return new RemoteVersionLinkListResult(toAccessFilteredRemoteVersionLinks(properties, user));
    }

    @Override
    public Long getRemoteVersionLinkCountByGlobalId(String globalId)
    {
        return jsonEntityPropertyManager.query()
                .entityName(REMOTE_VERSION_LINK)
                .key(globalId)
                .count();
    }

    @Override
    public RemoteVersionLinkResult getRemoteVersionLinkByVersionIdAndGlobalId(final ApplicationUser user,
            Long versionId, final String globalId)
    {
        final I18nHelper i18n = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();
        verifyNotNull(errors, i18n, VERSION_ID, versionId);
        verifyNotBlank(errors, i18n, GLOBAL_ID, globalId);
        if (errors.hasAnyErrors())
        {
            return new RemoteVersionLinkResult(errors);
        }

        final Version version = versionManager.getVersion(versionId);
        if (!canView(user, version))
        {
            errors.addError(VERSION_ID, i18n.getText(MSG_VERSION_DOES_NOT_EXIST, String.valueOf(versionId)), NOT_FOUND);
            return new RemoteVersionLinkResult(errors);
        }

        final String json = getEntityPropertyValue(versionId, globalId);
        if (json == null)
        {
            errors.addErrorMessage(i18n.getText(MSG_DOES_NOT_EXIST), NOT_FOUND);
            return new RemoteVersionLinkResult(errors);
        }
        return new RemoteVersionLinkResult(new RemoteVersionLinkImpl(version, globalId, json));
    }



    @Override
    public PutValidationResult validatePut(final ApplicationUser user,
            Long versionId, String globalId, final String json)
    {
        final I18nHelper i18n = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();
        verifyNotNull(errors, i18n, VERSION_ID, versionId);
        verifyNotBlank(errors, i18n, JSON, json);
        if (errors.hasAnyErrors())
        {
            return new PutValidationResult(errors);
        }

        final Version version = versionManager.getVersion(versionId);
        if (!canView(user, version))
        {
            errors.addError(VERSION_ID, i18n.getText(MSG_VERSION_DOES_NOT_EXIST, String.valueOf(versionId)), VALIDATION_FAILED);
            return new PutValidationResult(errors);
        }
        if (!canEdit(user, version))
        {
            final String projectKey = version.getProjectObject().getKey();
            errors.addErrorMessage(i18n.getText(MSG_NO_LINK_VERSION_PERMISSION, projectKey), FORBIDDEN);
            return new PutValidationResult(errors);
        }

        if (StringUtils.isBlank(globalId))
        {
            globalId = null;  // Will be generated/extracted instead
        }
        try
        {
            final String fixedGlobalId = putEntityPropertyDryRun(versionId, globalId, json);
            return new PutValidationResult(version, fixedGlobalId, json);
        }
        catch (FieldTooLongJsonPropertyException ftl)
        {
            errors.addError(ftl.getField(),
                    i18n.getText(MSG_FIELD_TOO_LONG, ftl.getField(), String.valueOf(ftl.getMaximumLength())),
                    VALIDATION_FAILED);
            return new PutValidationResult(errors);
        }
        catch (InvalidJsonPropertyException ijpe)
        {
            errors.addError(JSON, i18n.getText(MSG_INVALID_JSON, ijpe.toString()), VALIDATION_FAILED);
            return new PutValidationResult(errors);
        }
    }

    @Override
    public RemoteVersionLinkResult put(final ApplicationUser user,
            final PutValidationResult putValidationResult)
    {
        notNull("putValidationResult", putValidationResult);
        if (!putValidationResult.isValid())
        {
            throw new IllegalArgumentException("You cannot put using an invalid PutValidationResult");
        }
        try
        {
            final Version version = putValidationResult.version;
            final String json = putValidationResult.json;
            final String globalId = putEntityPropertyValue(version.getId(), putValidationResult.globalId, json);
            eventPublisher.publish(new RemoteVersionLinkPutEvent(version, globalId));
            return new RemoteVersionLinkResult(new RemoteVersionLinkImpl(version, globalId, json));
        }
        catch (RuntimeException re)
        {
            final ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(getI18nBean(user).getText(MSG_ERROR_CREATING, re.toString()), SERVER_ERROR);
            return new RemoteVersionLinkResult(errors);
        }
    }



    @Override
    public DeleteValidationResult validateDelete(final ApplicationUser user,
            final Long versionId, final String globalId)
    {
        final I18nHelper i18n = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();
        verifyNotNull(errors, i18n, VERSION_ID, versionId);
        verifyNotBlank(errors, i18n, GLOBAL_ID, globalId);
        if (errors.hasAnyErrors())
        {
            return new DeleteValidationResult(errors);
        }

        final Version version = versionManager.getVersion(versionId);
        if (!canView(user, version))
        {
            errors.addError(VERSION_ID, i18n.getText(MSG_VERSION_DOES_NOT_EXIST, String.valueOf(versionId)), VALIDATION_FAILED);
            return new DeleteValidationResult(errors);
        }
        if (!canEdit(user, version))
        {
            final String projectKey = version.getProjectObject().getKey();
            errors.addErrorMessage(i18n.getText(MSG_NO_LINK_VERSION_PERMISSION, projectKey), FORBIDDEN);
            return new DeleteValidationResult(errors);
        }
        if (!jsonEntityPropertyManager.exists(REMOTE_VERSION_LINK, versionId, globalId))
        {
            errors.addErrorMessage(i18n.getText(MSG_DOES_NOT_EXIST), VALIDATION_FAILED);
            return new DeleteValidationResult(errors);
        }
        return new DeleteValidationResult(version, globalId);
    }

    @Override
    public DeleteValidationResult validateDeleteByVersionId(final ApplicationUser user,
            final Long versionId)
    {
        final I18nHelper i18n = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();
        verifyNotNull(errors, i18n, VERSION_ID, versionId);
        if (errors.hasAnyErrors())
        {
            return new DeleteValidationResult(errors);
        }

        final Version version = versionManager.getVersion(versionId);
        if (!canView(user, version))
        {
            errors.addError(VERSION_ID, i18n.getText(MSG_VERSION_DOES_NOT_EXIST, String.valueOf(versionId)), VALIDATION_FAILED);
            return new DeleteValidationResult(errors);
        }
        if (!canEdit(user, version))
        {
            final String projectKey = version.getProjectObject().getKey();
            errors.addErrorMessage(i18n.getText(MSG_NO_LINK_VERSION_PERMISSION, projectKey), FORBIDDEN);
            return new DeleteValidationResult(errors);
        }
        if (jsonEntityPropertyManager.countByEntity(REMOTE_VERSION_LINK, versionId) == 0)
        {
            errors.addErrorMessage(i18n.getText(MSG_DOES_NOT_EXIST), VALIDATION_FAILED);
            return new DeleteValidationResult(errors);
        }
        return new DeleteValidationResult(version, null);
    }

    @Override
    public void delete(final ApplicationUser user, final DeleteValidationResult deleteValidationResult)
    {
        notNull("deleteValidationResult", deleteValidationResult);
        if (!deleteValidationResult.isValid())
        {
            throw new IllegalArgumentException("You cannot delete using an invalid DeleteValidationResult");
        }
        if (deleteValidationResult.globalId != null)
        {
            jsonEntityPropertyManager.delete(REMOTE_VERSION_LINK, deleteValidationResult.versionId, deleteValidationResult.globalId);
        }
        else
        {
            jsonEntityPropertyManager.deleteByEntity(REMOTE_VERSION_LINK, deleteValidationResult.versionId);
        }
        eventPublisher.publish(new RemoteVersionLinkDeleteEvent(deleteValidationResult.version, deleteValidationResult.globalId));
    }

    @EventListener
    public void onVersionDeleted(VersionDeleteEvent event)
    {
        jsonEntityPropertyManager.deleteByEntity(REMOTE_VERSION_LINK, event.getVersionId());
    }



    I18nHelper getI18nBean(ApplicationUser user)
    {
        return i18nFactory.getInstance(user);
    }



    private boolean canView(final ApplicationUser user, final Version version)
    {
        return version != null && isProjectBrowser(user, version.getProjectObject());
    }

    private boolean canEdit(final ApplicationUser user, final Version version)
    {
        return version != null && isProjectAdmin(user, version.getProjectObject());
    }

    private boolean isProjectBrowser(final ApplicationUser user, final Project project)
    {
        return permissionManager.hasPermission(Permissions.BROWSE, project, user) ||
               isProjectAdmin(user, project);
    }

    private boolean isProjectAdmin(final ApplicationUser user, final Project project)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
               permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user);
    }



    private static List<RemoteVersionLink> toRemoteVersionLinks(List<EntityProperty> list, final Version version)
    {
        return newArrayList(transform(list, new Function<EntityProperty, RemoteVersionLink>()
        {
            @Override
            public RemoteVersionLink apply(final EntityProperty property)
            {
                return new RemoteVersionLinkImpl(version, property.getKey(), property.getValue());
            }
        }));
    }

    List<RemoteVersionLink> toAccessFilteredRemoteVersionLinks(List<EntityProperty> properties, ApplicationUser user)
    {
        final List<RemoteVersionLink> links = new ArrayList<RemoteVersionLink>(properties.size());
        final VersionResolver versionResolver = new VersionResolver(user);

        for (EntityProperty property : properties)
        {
            final Version version = versionResolver.apply(property.getEntityId());
            if (version != null)
            {
                links.add(new RemoteVersionLinkImpl(version, property.getKey(), property.getValue()));
            }
        }
        return links;
    }

    /**
     * This provides a caching filter for mapping version IDs to {@code Version} objects with
     * visibility rules enforced.  It is intended to be used when a single request might result
     * in many different versions across many different projects and ensures that the user's
     * permissions are checked at most once per project.
     */
    class VersionResolver implements Function<Long,Version>
    {
        private final Map<Long,Boolean> projectAccessCache = new HashMap<Long,Boolean>();
        private final ApplicationUser user;

        VersionResolver(ApplicationUser user)
        {
            this.user = user;
        }

        @Override
        public Version apply(final Long versionId)
        {
            final Version version = versionManager.getVersion(versionId);
            if (version == null)
            {
                return null;
            }

            final Long projectId = version.getProjectId();
            Boolean hasAccess = projectAccessCache.get(projectId);
            if (hasAccess == null)
            {
                hasAccess = isProjectBrowser(user, version.getProjectObject());
                projectAccessCache.put(projectId, hasAccess);
            }
            return hasAccess ? version : null;
        }
    }
}

