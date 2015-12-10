package com.atlassian.jira.entity.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Option;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.WithKey;

import com.google.common.base.Function;

/**
 * The implementations of this interface extends the permission checking, persistence and events layers defined
 * by {@link EntityPropertyHelper} with operations identifying entities by keys.
 *
 * @since v6.2
 */
@ExperimentalApi
public interface EntityWithKeyPropertyHelper<E extends WithKey & WithId> extends EntityPropertyHelper<E>
{
    /**
     * @return the function which will get the entity for the provided key.
     */
    Function<String, Option<E>> getEntityByKeyFunction();
}
