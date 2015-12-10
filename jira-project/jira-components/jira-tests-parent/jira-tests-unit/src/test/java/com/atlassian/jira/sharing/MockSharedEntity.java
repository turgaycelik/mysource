package com.atlassian.jira.sharing;

import com.atlassian.crowd.embedded.api.User;

public class MockSharedEntity extends SharedEntity.Identifier
{
    public static final TypeDescriptor<MockSharedEntity> TYPE = new TypeDescriptor<MockSharedEntity>(MockSharedEntity.class.getSimpleName());
    private final SharePermissions permissions;

    public MockSharedEntity(final Long id, final User owner, final SharePermissions permissions)
    {
        this(id, TYPE, owner, permissions);
    }

    public MockSharedEntity(final Long id, final TypeDescriptor<? extends SharedEntity> type, final User owner, final SharePermissions permissions)
    {
        super(id, type, owner);
        this.permissions = permissions;
    }

    @Override
    public SharePermissions getPermissions()
    {
        return permissions;
    }
}
