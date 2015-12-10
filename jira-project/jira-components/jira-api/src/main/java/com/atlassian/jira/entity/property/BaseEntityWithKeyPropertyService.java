package com.atlassian.jira.entity.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.WithKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The base implementation of {@link EntityWithKeyPropertyService<E>}. This should be used for entities which are
 * identifiable by both: key and id, such as {@link com.atlassian.jira.issue.Issue}.
 *
 * @see BaseEntityPropertyService
 * @since v6.2
 */
@ExperimentalApi
public class BaseEntityWithKeyPropertyService<E extends WithKey & WithId> extends BaseEntityPropertyService<E>
        implements EntityWithKeyPropertyService<E>
{
    private final EntityWithKeyPropertyHelper<E> entityHelper;

    public BaseEntityWithKeyPropertyService(JsonEntityPropertyManager jsonEntityPropertyManager, I18nHelper i18n,
            EventPublisher eventPublisher, EntityWithKeyPropertyHelper<E> entityHelper)
    {
        super(jsonEntityPropertyManager, i18n, eventPublisher, entityHelper);
        this.entityHelper = entityHelper;
    }

    @Override
    public SetPropertyValidationResult validateSetProperty(ApplicationUser user, String entityKey, PropertyInput propertyInput)
    {
        return validateSetProperty(user, getEntity(entityKey), propertyInput, EntityPropertyOptions.defaults());
    }

    @Override
    public SetPropertyValidationResult validateSetProperty(ApplicationUser user, @NonNull final String entityKey, @NonNull final PropertyInput propertyInput, @NonNull final EntityPropertyOptions options)
    {
        return validateSetProperty(user, getEntity(entityKey), propertyInput, options);
    }

    @Override
    public DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, String entityKey, String propertyKey)
    {
        return validateDeleteProperty(user, getEntity(entityKey), propertyKey, EntityPropertyOptions.defaults());
    }

    @Override
    public DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, @NonNull final String entityKey,
            @NonNull final String propertyKey, @NonNull final EntityPropertyOptions options)
    {
        return validateDeleteProperty(user, getEntity(entityKey), propertyKey, options);
    }

    @Override
    public PropertyResult getProperty(ApplicationUser user, String entityKey, String propertyKey)
    {
        return getProperty(user, getEntity(entityKey), propertyKey, EntityPropertyOptions.defaults());
    }

    @Override
    public PropertyResult getProperty(ApplicationUser user, @NonNull final String entityKey,
            @NonNull final String propertyKey, @NonNull final EntityPropertyOptions options)
    {
        return getProperty(user, getEntity(entityKey), propertyKey, options);
    }

    @Override
    public PropertyKeys<E> getPropertiesKeys(ApplicationUser user, String entityKey)
    {
        return getPropertiesKeys(user, getEntity(entityKey), EntityPropertyOptions.defaults());
    }

    @Override
    public PropertyKeys<E> getPropertiesKeys(ApplicationUser user, @NonNull final String entityKey,
            @NonNull final EntityPropertyOptions options)
    {
        return getPropertiesKeys(user, getEntity(entityKey), options);
    }

    private Option<E> getEntity(String entityKey)
    {
        return entityHelper.getEntityByKeyFunction().apply(entityKey);
    }
}
