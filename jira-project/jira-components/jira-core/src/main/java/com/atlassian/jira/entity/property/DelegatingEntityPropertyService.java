package com.atlassian.jira.entity.property;

import javax.annotation.Nonnull;

import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delegates execution of methods from {@link EntityPropertyService} to an instance of {@link BaseEntityPropertyService}.
 *
 * @since v6.2
 */
public class DelegatingEntityPropertyService<E extends WithId> implements EntityPropertyService<E>
{
    private final EntityPropertyService<E> delegate;

    public DelegatingEntityPropertyService(final EntityPropertyService<E> delegate)
    {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public ErrorCollection validatePropertyInput(final PropertyInput propertyInput)
    {
        return delegate.validatePropertyInput(propertyInput);
    }

    @Override
    public SetPropertyValidationResult validateSetProperty(final ApplicationUser user, @Nonnull final Long entityId, @Nonnull final PropertyInput propertyInput)
    {
        return delegate.validateSetProperty(user, entityId, propertyInput);
    }

    @Override
    public SetPropertyValidationResult validateSetProperty(final ApplicationUser user, @Nonnull final Long entityId, @Nonnull final PropertyInput propertyInput, @Nonnull final EntityPropertyOptions options)
    {
        return delegate.validateSetProperty(user, entityId, propertyInput, options);
    }

    @Override
    public PropertyResult setProperty(final ApplicationUser user, @Nonnull final SetPropertyValidationResult propertyValidationResult)
    {
        return delegate.setProperty(user, propertyValidationResult);
    }

    @Override
    public DeletePropertyValidationResult validateDeleteProperty(final ApplicationUser user, @Nonnull final Long entityId, @Nonnull final String propertyKey)
    {
        return delegate.validateDeleteProperty(user, entityId, propertyKey);
    }

    @Override
    public DeletePropertyValidationResult validateDeleteProperty(final ApplicationUser user, @Nonnull final Long entityId, @Nonnull final String propertyKey, @Nonnull final EntityPropertyOptions options)
    {
        return delegate.validateDeleteProperty(user, entityId, propertyKey, options);
    }

    @Override
    public void deleteProperty(final ApplicationUser user, @Nonnull final DeletePropertyValidationResult validationResult)
    {
        delegate.deleteProperty(user, validationResult);
    }

    @Override
    public PropertyResult getProperty(final ApplicationUser user, @Nonnull final Long entityId, @Nonnull final String propertyKey)
    {
        return delegate.getProperty(user, entityId, propertyKey);
    }

    @Override
    public PropertyResult getProperty(final ApplicationUser user, @Nonnull final Long entityId, @Nonnull final String propertyKey, @Nonnull final EntityPropertyOptions options)
    {
        return delegate.getProperty(user, entityId, propertyKey, options);
    }

    @Override
    public PropertyKeys<E> getPropertiesKeys(final ApplicationUser user, @Nonnull final Long entityId)
    {
        return delegate.getPropertiesKeys(user, entityId);
    }

    @Override
    public PropertyKeys<E> getPropertiesKeys(final ApplicationUser user, @Nonnull final Long entityId, @Nonnull final EntityPropertyOptions options)
    {
        return delegate.getPropertiesKeys(user, entityId, options);
    }
}
