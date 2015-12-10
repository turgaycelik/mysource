package com.atlassian.jira.bc.admin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.DefaultFeatureManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.event.config.ApplicationPropertyChangeEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.validation.Validated;
import com.atlassian.validation.Validator;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

import static com.atlassian.jira.event.config.ApplicationPropertyChangeEvent.KEY_METADATA;

public class ApplicationPropertiesServiceImpl implements ApplicationPropertiesService
{


    private static final Logger log = Logger.getLogger(ApplicationPropertiesServiceImpl.class);

    private final ApplicationPropertiesStore applicationPropertiesStore;
    private EventPublisher eventPublisher;
    private PermissionManager permissionManager;
    private JiraAuthenticationContext authenticationContext;
    private final Predicate<ApplicationProperty> featurePredicate;
    private FeatureManager featureManager;

    public ApplicationPropertiesServiceImpl(ApplicationPropertiesStore applicationPropertiesStore, EventPublisher eventPublisher, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, FeatureManager featureManager)
    {
        this.applicationPropertiesStore = applicationPropertiesStore;
        this.eventPublisher = eventPublisher;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.featureManager = featureManager;
        this.featurePredicate = new Predicate<ApplicationProperty>()
        {
            @Override
            public boolean apply(@Nullable ApplicationProperty input)
            {
                if (input.getMetadata().getRequiredFeatureKey() == null)
                {
                    return true;
                }
                if (input.getMetadata().getRequiredFeatureKey().second())
                {
                    return ApplicationPropertiesServiceImpl.this.featureManager.isEnabled(input.getMetadata().getRequiredFeatureKey().first());
                }
                else
                {
                    return !ApplicationPropertiesServiceImpl.this.featureManager.isEnabled(input.getMetadata().getRequiredFeatureKey().first());
                }
            }
        };
    }

    @Override
    public List<ApplicationProperty> getEditableApplicationProperties(String permissionLevel, String keyFilter)
    {
        if (permissionLevel == null || permissionLevel.isEmpty())
        {
            if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getUser()))
            {
                return getEditableApplicationProperties(EditPermissionLevel.SYSADMIN, keyFilter);
            }
            else if (permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser()))
            {
                return getEditableApplicationProperties(EditPermissionLevel.ADMIN, keyFilter);
            }
            return null;
        }
        ApplicationPropertiesService.EditPermissionLevel editPermission = null;
        try
        {
            editPermission = ApplicationPropertiesService.EditPermissionLevel.valueOf(permissionLevel.toUpperCase());
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
        return getEditableApplicationProperties(editPermission, keyFilter);
    }

    public boolean hasPermissionForLevel(String permissionLevel)
    {
        if (permissionLevel == null || permissionLevel.isEmpty())
        {
            return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getUser()) ||
                    permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser());
        }
        ApplicationPropertiesService.EditPermissionLevel editPermission = null;
        try
        {
            editPermission = ApplicationPropertiesService.EditPermissionLevel.valueOf(permissionLevel.toUpperCase());
        }
        catch (IllegalArgumentException iae)
        {
            return false;
        }

        if (editPermission == ApplicationPropertiesService.EditPermissionLevel.SYSADMIN || editPermission == ApplicationPropertiesService.EditPermissionLevel.SYSADMIN_ONLY)
        {
            return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getUser());
        }
        else if (editPermission == ApplicationPropertiesService.EditPermissionLevel.ADMIN)
        {
            return (permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser())
                    || permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getUser()));
        }
        return false;
    }


    @Override
    public List<ApplicationProperty> getEditableApplicationProperties(EditPermissionLevel permissionLevel, String keyFilter)
            throws DataAccessException
    {
        return Lists.newArrayList(Iterables.filter(applicationPropertiesStore.getEditableApplicationProperties(permissionLevel, keyFilter),this.featurePredicate));
    }

    @Override
    public ApplicationProperty getApplicationProperty(final String key)
    {
        return applicationPropertiesStore.getApplicationPropertyFromKey(key);
    }

    @Override
    public Validated<ApplicationProperty> setApplicationProperty(final String key, String value)
    {
        ApplicationProperty applicationProperty = applicationPropertiesStore.getApplicationPropertyFromKey(key);
        String oldValue = applicationProperty.getCurrentValue();

        log.debug("validating value: " + value);
        ApplicationPropertyMetadata metadata = applicationProperty.getMetadata();
        Validator.Result result = metadata.validate(value);
        if (result.isValid())
        {
            applicationProperty = applicationPropertiesStore.setApplicationProperty(key, value);

            eventPublisher.publish(createEvent(metadata, oldValue, value));
        }
        return new Validated<ApplicationProperty>(result, applicationProperty);

    }

    protected ApplicationPropertyChangeEvent createEvent(ApplicationPropertyMetadata metadata, String oldValue, String newValue)
    {
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put(KEY_METADATA, Assertions.notNull("metadata", metadata));
        params.put(ApplicationPropertyChangeEvent.KEY_OLD_VALUE, oldValue);
        params.put(ApplicationPropertyChangeEvent.KEY_NEW_VALUE, newValue);
        return new ApplicationPropertyChangeEvent(params);
    }


}