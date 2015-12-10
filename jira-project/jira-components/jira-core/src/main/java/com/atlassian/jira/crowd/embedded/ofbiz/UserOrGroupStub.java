package com.atlassian.jira.crowd.embedded.ofbiz;

/**
 * A stub that represents the basic information about a user or group.
 * <p>
 * Decorates the basic {@code DirectoryEntity} from Crowd with an ID and lowercase name.
 * </p>
 */
public interface UserOrGroupStub extends com.atlassian.crowd.model.DirectoryEntity
{
    long getId();

    String getLowerName();
}
