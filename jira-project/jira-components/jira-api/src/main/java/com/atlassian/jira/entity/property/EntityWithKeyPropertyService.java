package com.atlassian.jira.entity.property;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.WithKey;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Extension of the {@link EntityPropertyService} specific for entities which are identifiable by key.
 *
 * @see BaseEntityWithKeyPropertyService
 *
 * @since v6.2
 */
@ExperimentalApi
public interface EntityWithKeyPropertyService<T extends WithId & WithKey> extends EntityPropertyService<T>
{
    /**
     * Checks if the provided entity's property is valid.
     * <p>
     *  This method checks if the entity with which the property will be associated exists and if the calling user
     *  has permissions to edit the entity. It validates if the property's key length is less then {@code 255} characters.
     *  It also checks if the length of the property's value is less then {@code 32,768}.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityKey the key of the entity with which the property will be associated.
     * @param propertyInput the pair of key and value which will be associated with the entity.
     *
     * @return either entity ready to be persisted in DB or collection of errors.
     */
    SetPropertyValidationResult validateSetProperty(ApplicationUser user, @Nonnull String entityKey, @Nonnull PropertyInput propertyInput);

    /**
     * Checks if the provided entity's property is valid.
     * <p>
     *  This method checks if the entity with which the property will be associated exists.
     *  It validates if the property's key length is less then {@code 255} characters.
     *  It also checks if the length of the property's value is less then {@code 32,768}.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityKey the key of the entity with which the property will be associated.
     * @param propertyInput the pair of key and value which will be associated with the entity.
     * @param options options to skip permission while performing the validation.
     *
     * @return either entity ready to be persisted in DB or collection of errors.
     */
    SetPropertyValidationResult validateSetProperty(ApplicationUser user, @Nonnull String entityKey, @Nonnull PropertyInput propertyInput,
            @Nonnull EntityPropertyOptions options);

    /**
     * Check if it is possible to remove the entity property with specified entity's key and entity's property key.
     * <p>
     *     This method checks if the calling user has permissions to edit the selected entity and if the property for
     *     given entity key and property key exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityKey the key of the entity with which the property is associated.
     * @param propertyKey the key of the entity's property.
     *
     * @return either entity ready to be removed or collection of errors.
     */
    DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, @Nonnull String entityKey, @Nonnull String propertyKey);

    /**
     * Check if it is possible to remove the entity property with specified entity's key and entity's property key.
     * <p>
     *     This method checks if the property for given entity key and property key exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityKey the key of the entity with which the property is associated.
     * @param propertyKey the key of the entity's property.
     * @param options options to skip permission while performing the validation.
     *
     * @return either entity ready to be removed or collection of errors.
     */
    DeletePropertyValidationResult validateDeleteProperty(ApplicationUser user, @Nonnull String entityKey, @Nonnull String propertyKey,
            @Nonnull EntityPropertyOptions options);

    /**
     * Returns the JSON property with the specified key from specified entity.
     * <p>
     *     This method checks if the calling user has permissions to browse the entities and if the entity with given key
     *     exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityKey the key of the entity with which the property is associated.
     * @param propertyKey the key of the entity's property.
     *
     * @return the chosen property of the entity if found or the error collection.
     */
    PropertyResult getProperty(ApplicationUser user, @Nonnull String entityKey, @Nonnull String propertyKey);

    /**
     * Returns the JSON property with the specified key from specified entity.
     * <p>
     *     This method checks if the entity with given key exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityKey the key of the entity with which the property is associated.
     * @param propertyKey the key of the entity's property.
     * @param options options to skip permission while performing the validation.
     *
     * @return the chosen property of the entity if found or the error collection.
     */
    PropertyResult getProperty(ApplicationUser user, @Nonnull String entityKey, @Nonnull String propertyKey,
            @Nonnull EntityPropertyOptions options);

    /**
     * Returns the properties keys associated with the specified entity.
     * <p>
     *     This method checks if the calling user has permissions to browse the entitys and if the entity with given id
     *     exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityKey the key of the entity with which the property is associated.
     * @return the list of properties keys and associated entity or a collection with errors.
     */
    PropertyKeys<T> getPropertiesKeys(ApplicationUser user, @Nonnull String entityKey);

    /**
     * Returns the properties keys associated with the specified entity.
     * <p>
     *     This method checks if the entity with given id exists.
     * </p>
     *
     * @param user who the permission checks will be run against (can be null, indicating anonymous user).
     * @param entityKey the key of the entity with which the property is associated.
     * @return the list of properties keys and associated entity or a collection with errors.
     * @param options options to skip permission while performing the validation.
     */
    PropertyKeys<T> getPropertiesKeys(ApplicationUser user, @Nonnull String entityKey, @Nonnull EntityPropertyOptions options);
}
