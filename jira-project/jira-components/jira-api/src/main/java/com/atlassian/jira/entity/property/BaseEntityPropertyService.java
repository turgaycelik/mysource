package com.atlassian.jira.entity.property;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import org.apache.commons.lang3.StringUtils;

import static com.atlassian.jira.entity.property.EntityPropertyHelper.CheckPermissionFunction;
import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND;
import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The base implementation of {@link EntityPropertyService<E>}.
 *
 * @param <E> the entity type which is identifiable by id.
 * @see BaseEntityWithKeyPropertyService
 * @since v6.2
 */
@ExperimentalApi
public class BaseEntityPropertyService<E extends WithId> implements EntityPropertyService<E>
{
    private final EntityPropertyHelper<E> entityPropertyHelper;
    private final EventPublisher eventPublisher;
    private final I18nHelper i18n;
    private final JsonEntityPropertyManager jsonEntityPropertyManager;
    private final String dbEntityName;

    public BaseEntityPropertyService(JsonEntityPropertyManager jsonEntityPropertyManager, I18nHelper i18n,
            EventPublisher eventPublisher, EntityPropertyHelper<E> entityPropertyHelper)
    {
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
        this.i18n = i18n;
        this.eventPublisher = eventPublisher;
        this.dbEntityName = entityPropertyHelper.getEntityPropertyType().getDbEntityName();
        this.entityPropertyHelper = entityPropertyHelper;
    }

    @Override
    public SetPropertyValidationResult validateSetProperty(ApplicationUser user, Long entityId, PropertyInput propertyInput)
    {
        return validateSetProperty(user, getEntity(entityId), propertyInput, EntityPropertyOptions.defaults());
    }

    @Override
    public SetPropertyValidationResult validateSetProperty(ApplicationUser user, @Nonnull Long entityId,
            @Nonnull PropertyInput propertyInput, @Nonnull EntityPropertyOptions options)
    {
        return validateSetProperty(null, getEntity(entityId), propertyInput, options);
    }

    protected SetPropertyValidationResult validateSetProperty(ApplicationUser user, Option<E> entity, PropertyInput propertyInput, EntityPropertyOptions options)
    {
        checkNotNull(propertyInput);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        if (checkEntity(entity, user, getEditPermissionFunction(options), errorCollection))
        {
            if (StringUtils.isEmpty(propertyInput.getPropertyValue()))
            {
                errorCollection.addErrorMessage(i18n.getText("jira.properties.service.empty.value"), VALIDATION_FAILED);
            }
            else
            {
                errorCollection.addErrorCollection(validatePropertyInput(propertyInput));
            }
        }

        Option<EntityPropertyInput> property = errorCollection.hasAnyErrors() ?
                Option.<EntityPropertyInput>none() : Option.some(createInputForPropertySet(entity.get(), propertyInput));

        return new SetPropertyValidationResult(errorCollection, property);
    }

    @Override
    public ErrorCollection validatePropertyInput(final PropertyInput propertyInput)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        try
        {
            jsonEntityPropertyManager.putDryRun(dbEntityName, propertyInput.getPropertyKey(), propertyInput.getPropertyValue());
        }
        catch (InvalidJsonPropertyException ex)
        {
            errorCollection.addErrorMessage(i18n.getText("jira.properties.service.invalid.json", propertyInput.getPropertyValue()), VALIDATION_FAILED);
        }
        catch (FieldTooLongJsonPropertyException ex)
        {
            errorCollection.addErrorMessage(i18n.getText("jira.properties.service.too.long.value", String.valueOf(ex.getMaximumLength()), String.valueOf(ex.getActualLength())), VALIDATION_FAILED);
        }
        return errorCollection;
    }

    @Override
    public PropertyResult setProperty(final ApplicationUser user, @Nonnull final SetPropertyValidationResult propertyValidationResult)
    {
        checkNotNull(propertyValidationResult);
        checkNotNull(propertyValidationResult.getEntityPropertyInput());
        checkArgument(propertyValidationResult.isValid());
        checkArgument(propertyValidationResult.getEntityPropertyInput().isDefined());

        EntityPropertyInput entityProperty = propertyValidationResult.getEntityPropertyInput().get();

        jsonEntityPropertyManager.put(user, dbEntityName, entityProperty.getEntityId(), entityProperty.getPropertyKey(), entityProperty.getPropertyValue(), entityPropertyHelper.createSetPropertyEventFunction(), true);

        return getProperty(user, entityProperty.getEntityId(), entityProperty.getPropertyKey());
    }

    @Override
    public DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, Long entityId, String propertyKey)
    {
        return validateDeleteProperty(user, getEntity(entityId), propertyKey, EntityPropertyOptions.defaults());
    }

    @Override
    public DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, @Nonnull Long entityId,
            @Nonnull String propertyKey, @Nonnull EntityPropertyOptions options)
    {
        return validateDeleteProperty(null, getEntity(entityId), propertyKey, options);
    }

    protected DeletePropertyValidationResult validateDeleteProperty(final ApplicationUser user, final Option<E> entity,
            @Nonnull final String propertyKey, @Nonnull EntityPropertyOptions options)
    {
        checkNotNull(propertyKey);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (checkEntity(entity, user, getEditPermissionFunction(options), errorCollection))
        {
            final PropertyResult property = getProperty(user, entity, propertyKey, options);

            return property.getEntityProperty().fold(
                    new Supplier<DeletePropertyValidationResult>()
                    {
                        @Override
                        public DeletePropertyValidationResult get()
                        {
                            errorCollection.addErrorMessage(i18n.getText("jira.properties.service.property.does.not.exist", propertyKey), NOT_FOUND);
                            return new DeletePropertyValidationResult(errorCollection, Option.<EntityProperty>none());
                        }
                    }, new Function<EntityProperty, DeletePropertyValidationResult>()
                    {
                        @Override
                        public DeletePropertyValidationResult apply(final EntityProperty entityProperty)
                        {
                            return new DeletePropertyValidationResult(errorCollection, Option.some(entityProperty));
                        }
                    }
            );
        }
        else
        {
            return new DeletePropertyValidationResult(errorCollection, Option.<EntityProperty>none());
        }
    }

    @Override
    public void deleteProperty(final ApplicationUser user, @Nonnull final DeletePropertyValidationResult validationResult)
    {
        checkNotNull(validationResult);
        checkArgument(validationResult.isValid());
        checkNotNull(validationResult.getEntityProperty());
        checkArgument(validationResult.getEntityProperty().isDefined());

        EntityProperty entityProperty = validationResult.getEntityProperty().get();

        jsonEntityPropertyManager.delete(dbEntityName, entityProperty.getEntityId(), entityProperty.getKey());
        eventPublisher.publish(entityPropertyHelper.createDeletePropertyEventFunction().apply(user, entityProperty));
    }

    @Override
    public PropertyResult getProperty(ApplicationUser user, Long entityId, String propertyKey)
    {
        return getProperty(user, getEntity(entityId), propertyKey, EntityPropertyOptions.defaults());
    }

    @Override
    public PropertyResult getProperty(ApplicationUser user, @Nonnull Long entityId, @Nonnull String propertyKey,
            @Nonnull EntityPropertyOptions options)
    {
        return getProperty(null, getEntity(entityId), propertyKey, options);
    }

    protected PropertyResult getProperty(final ApplicationUser user, Option<E> entity, @Nonnull final String propertyKey, EntityPropertyOptions options)
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (checkEntity(entity, user, getReadPermissionFunction(options), errorCollection))
        {
            final EntityProperty entityProperty = jsonEntityPropertyManager.get(dbEntityName, entity.get().getId(), propertyKey);
            return new PropertyResult(errorCollection, Option.option(entityProperty));
        }
        else
        {
            return new PropertyResult(errorCollection, Option.<EntityProperty>none());
        }
    }

    @Override
    public PropertyKeys<E> getPropertiesKeys(ApplicationUser user, Long entityId)
    {
        return getPropertiesKeys(user, getEntity(entityId), EntityPropertyOptions.defaults());
    }

    @Override
    public PropertyKeys<E> getPropertiesKeys(ApplicationUser user, @Nonnull Long entityId, @Nonnull EntityPropertyOptions options)
    {
        return getPropertiesKeys(user, getEntity(entityId), options);
    }

    protected PropertyKeys<E> getPropertiesKeys(final ApplicationUser user, final Option<E> entity, EntityPropertyOptions options)
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (checkEntity(entity, user, getReadPermissionFunction(options), errorCollection))
        {
            final List<String> keys = jsonEntityPropertyManager.findKeys(dbEntityName, entity.get().getId());
            return new PropertyKeys<E>(errorCollection, keys, entity.get());
        }
        else
        {
            return new PropertyKeys<E>(errorCollection, Collections.<String>emptyList(), null);
        }
    }

    private boolean checkEntity(final Option<E> entity, final ApplicationUser user,
            final CheckPermissionFunction<E> permissionFunction, final SimpleErrorCollection errorCollection)
    {
        return entity.fold(
                new Supplier<Boolean>()
                {
                    @Override
                    public Boolean get()
                    {
                        errorCollection.addErrorMessage(i18n.getText("jira.properties.service.entity.does.not.exist", i18n.getText(entityPropertyHelper.getEntityPropertyType().getI18nKeyForEntityName())), NOT_FOUND);
                        return false;
                    }
                }, new Function<E, Boolean>()
                {
                    @Override
                    public Boolean apply(final E input)
                    {
                        ErrorCollection permissionCheckResults = permissionFunction.apply(user, input);
                        if (permissionCheckResults.hasAnyErrors())
                        {
                            errorCollection.addErrorCollection(permissionCheckResults);
                        }
                        return !permissionCheckResults.hasAnyErrors();
                    }
                }
        );
    }

    private EntityPropertyInput createInputForPropertySet(final E entity, final PropertyInput propertyInput)
    {
        return new EntityPropertyInput(propertyInput.getPropertyValue(), propertyInput.getPropertyKey(), entity.getId(), dbEntityName);
    }

    private Option<E> getEntity(final Long entityId)
    {
        return entityPropertyHelper.getEntityByIdFunction().apply(entityId);
    }

    private CheckPermissionFunction<E> getReadPermissionFunction(EntityPropertyOptions options)
    {
        if (options.skipPermissionChecks())
        {
            return new NoPermissionCheckFunction();
        }
        else
        {
            return entityPropertyHelper.hasReadPermissionFunction();
        }
    }

    private CheckPermissionFunction<E> getEditPermissionFunction(EntityPropertyOptions options)
    {
        if (options.skipPermissionChecks())
        {
            return new NoPermissionCheckFunction();
        }
        else
        {
            return entityPropertyHelper.hasEditPermissionFunction();
        }
    }

    private class NoPermissionCheckFunction implements CheckPermissionFunction<E>
    {
        @Override
        public ErrorCollection apply(final ApplicationUser user, final E e)
        {
            return new SimpleErrorCollection();
        }
    }
}