package com.atlassian.jira.entity.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.event.entity.EntityPropertyDeletedEvent;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

import com.google.common.base.Function;

/**
 * The implementations of this interface are defining permission checking, persistence layer and events for entities {@link E}
 * which are identifiable by id. These implementations can be used with
 * are required to provide functions specializing {@link EntityPropertyService}.
 *
 * @param <E> - entity type which extends {@link WithId} interface.
 *
 * @since v6.2
 */
@ExperimentalApi
public interface EntityPropertyHelper<E extends WithId>
{
    /**
     * @return the function which will check if the provided user has permissions to edit the entity.
     */
    CheckPermissionFunction<E> hasEditPermissionFunction();

    /**
     * @return the function which will check if the provided user has permissions to view the entity.
     */
    CheckPermissionFunction<E> hasReadPermissionFunction();

    /**
     * @return the function which will get the entity for the provided id.
     */
    Function<Long, Option<E>> getEntityByIdFunction();

    /**
     * @return the function which will create an instance of {@link EntityPropertySetEvent} for the proper entity type.
     */
    Function2<ApplicationUser, EntityProperty, ? extends EntityPropertySetEvent> createSetPropertyEventFunction();

    /**
     * @return the function which will create an instance of {@link EntityPropertyDeletedEvent} for the proper entity type.
     */
    Function2<ApplicationUser, EntityProperty, ? extends EntityPropertyDeletedEvent> createDeletePropertyEventFunction();

    /**
     * @return the type of the entity property.
     */
    EntityPropertyType getEntityPropertyType();

    /**
     * The base function for checking of permissions on chosen entities.
     *
     * @param <E> - entity type which extends {@link WithId} interface.
     */
    interface CheckPermissionFunction<E extends WithId> extends Function2<ApplicationUser, E, ErrorCollection> {}
}
