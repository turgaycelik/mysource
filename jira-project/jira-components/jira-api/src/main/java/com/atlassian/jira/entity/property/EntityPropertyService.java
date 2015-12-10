package com.atlassian.jira.entity.property;

import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The service used to add, update, retrieve and delete properties from entities. Each method of this service
 * ensures that the user has permission to perform the operation. For each operation an appropriate event is published.
 *
 * @see BaseEntityPropertyService
 *
 * @since v6.2
 */
@ExperimentalApi
public interface EntityPropertyService<T extends WithId>
{
    /**
     * Validates the property's key and property's value without permission checking.
     *
     * @param propertyInput the key and value of the property.
     *
     * @return all validation errors or empty error collection.
     */
    ErrorCollection validatePropertyInput(final PropertyInput propertyInput);

    /**
     * Checks if the provided entity's property is valid.
     * <p>
     *  This method checks if the entity with which the property will be associated exists and if the calling user
     *  has permissions to edit the entity. It validates if the property's key length is less then {@code 255} characters.
     *  It also checks if the length of the property's value is less then {@code 32,768}.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityId the id of the entity with which the property will be associated.
     * @param propertyInput the pair of key and value which will be associated with the entity.
     *
     * @return either entity ready to be persisted in DB or collection of errors.
     */
    SetPropertyValidationResult validateSetProperty(ApplicationUser user, @Nonnull Long entityId, @Nonnull PropertyInput propertyInput);

    /**
     * Checks if the provided entity's property is valid.
     * <p>
     *  This method checks if the entity with which the property will be associated exists.
     *  It validates if the property's key length is less then {@code 255} characters.
     *  It also checks if the length of the property's value is less then {@code 32,768}.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityId the id of the entity with which the property will be associated.
     * @param propertyInput the pair of key and value which will be associated with the entity.
     * @param options options to skip permission while performing the validation.
     *
     * @return either entity ready to be persisted in DB or collection of errors.
     */
    SetPropertyValidationResult validateSetProperty(ApplicationUser user, @Nonnull Long entityId,
            @Nonnull PropertyInput propertyInput, @Nonnull EntityPropertyOptions options);

    /**
     * Associates validated property with the entity. Upon successful set an instance of
     * {@link com.atlassian.jira.event.entity.EntityPropertySetEvent} is published.
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param propertyValidationResult validated entity property.
     *
     * @return the persisted entity property or the error collection.
     */
    PropertyResult setProperty(ApplicationUser user, @Nonnull SetPropertyValidationResult propertyValidationResult);

    /**
     * Check if it is possible to remove the entity property with specified entity's id and entity's property key.
     * <p>
     *     This method checks if the calling user has permissions to edit the selected entity and if the property for
     *     given entity id and property key exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityId the id of the entity with which the property is associated.
     * @param propertyKey the key of the entity's property.
     *
     * @return either entity ready to be removed or collection of errors.
     */
    DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, @Nonnull Long entityId, @Nonnull String propertyKey);

    /**
     * Check if it is possible to remove the entity property with specified entity's id and entity's property key.
     * <p>
     *     This method checks if the property for given entity id and property key exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityId the id of the entity with which the property is associated.
     * @param propertyKey the key of the entity's property.
     * @param options options to skip permission while performing the validation.
     *
     * @return either entity ready to be removed or collection of errors.
     */
    DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, @Nonnull Long entityId,
            @Nonnull String propertyKey, @Nonnull EntityPropertyOptions options);

    /**
     * Removes the entity property with specified entity's id and entity's property key.
     * Upon successful removal, {@link com.atlassian.jira.event.entity.EntityPropertyDeletedEvent} is published.
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param validationResult validation results of entity's property removal.
     */
    void deleteProperty(ApplicationUser user, @Nonnull DeletePropertyValidationResult validationResult);

    /**
     * Returns the JSON property with the specified key from specified entity.
     * <p>
     *     This method checks if the calling user has permissions to browse the entities and if the entity with given id
     *     exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityId the id of the entity with which the property is associated.
     * @param propertyKey the key of the entity's property.
     *
     * @return the chosen property of the entity if found or the error collection.
     */
    PropertyResult getProperty(ApplicationUser user, @Nonnull Long entityId, @Nonnull String propertyKey);

    /**
     * Returns the JSON property with the specified key from specified entity.
     * <p>
     *     This method checks if the entity with given id exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityId the id of the entity with which the property is associated.
     * @param propertyKey the key of the entity's property.
     * @param options options to skip permission while performing the validation.
     *
     * @return the chosen property of the entity if found or the error collection.
     */
    PropertyResult getProperty(ApplicationUser user, @Nonnull Long entityId, @Nonnull String propertyKey,
            @Nonnull EntityPropertyOptions options);

    /**
     * Returns the properties keys associated with the specified entity.
     * <p>
     *     This method checks if the calling user has permissions to browse the entities and if the entity with given id
     *     exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityId the id of the entity with which the property is associated.
     * @return the list of properties keys and associated entity or a collection with errors.
     */
    PropertyKeys<T> getPropertiesKeys(ApplicationUser user, @Nonnull Long entityId);

    /**
     * Returns the properties keys associated with the specified entity.
     * <p>
     *     This method checks if the entity with given id exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityId the id of the entity with which the property is associated.
     * @param options options to skip permission while performing the validation.
     * @return the list of properties keys and associated entity or a collection with errors.
     */
    PropertyKeys<T> getPropertiesKeys(ApplicationUser user, @Nonnull Long entityId, @Nonnull EntityPropertyOptions options);

    @ExperimentalApi
    static class PropertyKeys<E> extends ServiceResultImpl
    {
        private final List<String> keys;
        private final E entity;

        public PropertyKeys(ErrorCollection errorCollection, List<String> keys, E entity)
        {
            super(errorCollection);
            this.keys = ImmutableList.copyOf(keys);
            this.entity = entity;
        }

        public List<String> getKeys()
        {
            return keys;
        }

        public E getEntity()
        {
            return entity;
        }
    }

    static abstract class PropertyServiceResult extends ServiceResultImpl
    {
        private final Option<EntityProperty> entityProperty;

        public PropertyServiceResult(ErrorCollection errorCollection, Option<EntityProperty> entityProperty)
        {
            super(errorCollection);
            this.entityProperty = entityProperty;
        }

        public Option<EntityProperty> getEntityProperty()
        {
            return entityProperty;
        }
    }

    @ExperimentalApi
    class PropertyResult extends PropertyServiceResult
    {
        public PropertyResult(ErrorCollection errorCollection, Option<EntityProperty> entityProperty)
        {
            super(errorCollection, entityProperty);
        }
    }

    @ExperimentalApi
    class SetPropertyValidationResult extends ServiceResultImpl
    {
        private final Option<EntityPropertyInput> entityPropertyInput;

        public SetPropertyValidationResult(ErrorCollection errorCollection, Option<EntityPropertyInput> entityPropertyInput)
        {
            super(errorCollection);
            this.entityPropertyInput = entityPropertyInput;
        }

        public Option<EntityPropertyInput> getEntityPropertyInput()
        {
            return entityPropertyInput;
        }
    }

    @ExperimentalApi
    class DeletePropertyValidationResult extends PropertyServiceResult
    {
        public DeletePropertyValidationResult(ErrorCollection errorCollection, Option<EntityProperty> entityProperty)
        {
            super(errorCollection, entityProperty);
        }
    }

    /**
     * Tuple with property value, property key, entity id and entity name, which will be persisted by the service.
     */
    @ExperimentalApi
    class EntityPropertyInput extends PropertyInput
    {
        private final Long id;
        private final String entityName;

        public EntityPropertyInput(final String value, final String key, final Long id, final String entityName)
        {
            super(value, key);
            this.id = id;
            this.entityName = entityName;
        }

        public Long getEntityId()
        {
            return id;
        }

        public String getEntityName()
        {
            return entityName;
        }
    }

    /**
     * The key-value pair associated to the entity.
     */
    @ExperimentalApi
    class PropertyInput
    {
        private final String entityPropertyValue;
        private final String entityPropertyKey;

        public PropertyInput(String value, String key)
        {
            this.entityPropertyValue = checkNotNull(value);
            this.entityPropertyKey = checkNotNull(key);
        }

        /**
         * @return the non-null value of the entity property.
         */
        @Nonnull
        public String getPropertyValue()
        {
            return entityPropertyValue;
        }

        /**
         * @return the non-null key of the entity property.
         */
        @Nonnull
        public String getPropertyKey()
        {
            return entityPropertyKey;
        }
    }
}
