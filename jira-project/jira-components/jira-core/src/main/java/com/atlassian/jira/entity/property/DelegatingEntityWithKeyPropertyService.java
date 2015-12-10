package com.atlassian.jira.entity.property;

import javax.annotation.Nonnull;

import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.WithKey;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Delegates execution of methods from {@link EntityWithKeyPropertyService} to an instance of {@link BaseEntityWithKeyPropertyService}.
 *
 * @since v6.2
 */
public class DelegatingEntityWithKeyPropertyService<E extends WithId & WithKey> extends DelegatingEntityPropertyService<E>
        implements EntityWithKeyPropertyService<E>
{
    private final BaseEntityWithKeyPropertyService<E> delegate;

    public DelegatingEntityWithKeyPropertyService(final BaseEntityWithKeyPropertyService<E> delegate)
    {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public EntityPropertyService.SetPropertyValidationResult validateSetProperty(ApplicationUser user, String entityKey, EntityPropertyService.PropertyInput propertyInput)
    {
        return delegate.validateSetProperty(user, entityKey, propertyInput);
    }

    @Override
    public SetPropertyValidationResult validateSetProperty(ApplicationUser user, @Nonnull final String entityKey, @Nonnull final PropertyInput propertyInput, @Nonnull EntityPropertyOptions options)
    {
        return delegate.validateSetProperty(user, entityKey, propertyInput, options);
    }

    @Override
    public EntityPropertyService.DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, String entityKey, String propertyKey)
    {
        return delegate.validateDeleteProperty(user, entityKey, propertyKey);
    }

    @Override
    public DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, @Nonnull final String entityKey, @Nonnull final String propertyKey, @Nonnull EntityPropertyOptions options)
    {
        return delegate.validateDeleteProperty(user, entityKey, propertyKey, options);
    }

    @Override
    public EntityPropertyService.PropertyResult getProperty(ApplicationUser user, String entityKey, String propertyKey)
    {
        return delegate.getProperty(user, entityKey, propertyKey);
    }

    @Override
    public PropertyResult getProperty(ApplicationUser user, @Nonnull final String entityKey, @Nonnull final String propertyKey, @Nonnull EntityPropertyOptions options)
    {
        return delegate.getProperty(user, entityKey, propertyKey, options);
    }

    @Override
    public EntityPropertyService.PropertyKeys<E> getPropertiesKeys(ApplicationUser user, String entityKey)
    {
        return delegate.getPropertiesKeys(user, entityKey);
    }

    @Override
    public PropertyKeys<E> getPropertiesKeys(ApplicationUser user, @Nonnull final String entityKey, @Nonnull EntityPropertyOptions options)
    {
        return delegate.getPropertiesKeys(user, entityKey, options);
    }
}
