package com.atlassian.jira.rest.v2.entity.property;

import com.atlassian.fugue.Function2;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.WithKey;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.EntityWithKeyPropertyService;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.atlassian.jira.entity.property.EntityPropertyService.DeletePropertyValidationResult;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyKeys;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import static com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @since v6.2
 */
public class BasePropertyWithKeyResource<E extends WithId & WithKey> extends BasePropertyResource<E>
{
    private final EntityWithKeyPropertyService<E> entityPropertyService;
    private final Predicate<String> isValidKeyPredicate;

    public BasePropertyWithKeyResource(EntityWithKeyPropertyService<E> entityPropertyService, JiraAuthenticationContext authContext,
            JiraBaseUrls jiraBaseUrls, I18nHelper i18n, Predicate<String> validKeyPredicate, Function2<Long, String, String> entityIdAndPropertyKeyToSelfFunction,
            EntityPropertyType entityPropertyType)
    {
        super(entityPropertyService, authContext, jiraBaseUrls, i18n, entityIdAndPropertyKeyToSelfFunction, entityPropertyType);
        this.entityPropertyService = entityPropertyService;
        isValidKeyPredicate = checkNotNull(validKeyPredicate);
    }

    @Override
    protected PropertyKeys<E> getPropertiesKeys(final ApplicationUser user, final String idOrKey)
    {
        return withKeyValidation(idOrKey, new Function<String, PropertyKeys<E>>()
        {
            @Override
            public PropertyKeys<E> apply(final String entityKey)
            {
                return entityPropertyService.getPropertiesKeys(user, entityKey);
            }
        }, new Function<Long, PropertyKeys<E>>()
        {
            @Override
            public PropertyKeys<E> apply(final Long entityId)
            {
                return entityPropertyService.getPropertiesKeys(user, entityId);
            }
        });
    }

    @Override
    protected SetPropertyValidationResult validateSetProperty(final ApplicationUser user, final PropertyInput propertyInput,
            final String idOrKey)
    {
        return withKeyValidation(idOrKey, new Function<String, SetPropertyValidationResult>()
        {
            @Override
            public SetPropertyValidationResult apply(final String key)
            {
                return entityPropertyService.validateSetProperty(user, key, propertyInput);
            }
        }, new Function<Long, SetPropertyValidationResult>()
        {
            @Override
            public SetPropertyValidationResult apply(final Long id)
            {
                return entityPropertyService.validateSetProperty(user, id, propertyInput);
            }
        });
    }

    @Override
    protected PropertyResult getProperty(final ApplicationUser user, final String propertyKey, final String idOrKey)
    {
        return withKeyValidation(idOrKey, new Function<String, PropertyResult>()
        {
            @Override
            public PropertyResult apply(final String key)
            {
                return entityPropertyService.getProperty(user, key, propertyKey);
            }
        }, new Function<Long, PropertyResult>()
        {
            @Override
            public PropertyResult apply(final Long id)
            {
                return entityPropertyService.getProperty(user, id, propertyKey);
            }
        });
    }

    @Override
    protected DeletePropertyValidationResult validateDeleteProperty(final ApplicationUser user, final String propertyKey,
            final String id)
    {
        return withKeyValidation(id, new Function<String, DeletePropertyValidationResult>()
        {
            @Override
            public DeletePropertyValidationResult apply(final String entityKey)
            {
                return entityPropertyService.validateDeleteProperty(user, entityKey, propertyKey);
            }
        }, new Function<Long, DeletePropertyValidationResult>()
        {
            @Override
            public DeletePropertyValidationResult apply(final Long entityId)
            {
                return entityPropertyService.validateDeleteProperty(user, entityId, propertyKey);
            }
        });
    }

    private <T> T withKeyValidation(final String idOrKey,
            final Function<String, T> keyFunction, final Function<Long, T> idFunction)
    {
        if (isValidKeyPredicate.apply(idOrKey))
        {
            return keyFunction.apply(idOrKey);
        }
        else
        {
            return withIdValidation(idOrKey, idFunction);
        }
    }
}
