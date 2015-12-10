package com.atlassian.jira.security;

import com.atlassian.jira.permission.GlobalPermissionKey;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.doAnswer;

public class MockGlobalPermissionManager
{
    public static GlobalPermissionManager withSystemGlobalPermissions()
    {
        final GlobalPermissionManager globalPermissionManager = Mockito.mock(GlobalPermissionManager.class);
        doAnswer(new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                int permissionId = (Integer) invocationOnMock.getArguments()[0];
                return GlobalPermissionKey.GLOBAL_PERMISSION_ID_TRANSLATION.containsKey(permissionId);
            }
        }).when(globalPermissionManager).isGlobalPermission(Mockito.anyInt());
        return globalPermissionManager;
    }
}
