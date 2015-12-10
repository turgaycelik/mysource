package com.atlassian.jira.entity;

import com.atlassian.annotations.ExperimentalApi;

/**
 * @since v5.2
 */
@ExperimentalApi
public interface NamedEntityBuilder<E> extends EntityBuilder<E>
{
    /**
     * The name of the Entity as defined in the entitymodel.xml file.
     *
     * @return the name of the Entity.
     */
    String getEntityName();
}
