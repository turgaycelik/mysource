package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.AbstractOFBizTestCase;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.Permissions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import java.io.Serializable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test {@link UpgradeTask_Build6211}
 *
 * @since v6.2
 */

@RunWith(ListeningMockitoRunner.class)
public class TestUpgradeTask_Build6211 extends AbstractOFBizTestCase
{
    public static final long SCHEME_ID = 0;

    @Mock
    @AvailableInContainer
    OfBizDelegator mockOfBizDelegator;

    @Test
    public void testUpgrade() throws Exception
    {
        GenericValue linkPermission = mock(GenericValue.class);
        GenericValue commentPermission = mock(GenericValue.class);
        GenericValue scheme = mock(GenericValue.class);
        ImmutableMap<String, ? extends Serializable> commentQuery = ImmutableMap.of("scheme", SCHEME_ID, "permission", Permissions.COMMENT_ISSUE, "type", "projectrole");
        ImmutableMap<String, ? extends Serializable> linkQuery = ImmutableMap.of("scheme", SCHEME_ID, "permission", Permissions.LINK_ISSUE, "type", "projectrole");

        UpgradeTask_Build6211 upgradeTask_build6211 = new UpgradeTask_Build6211(mockOfBizDelegator);

        when(scheme.getLong("id")).thenReturn(SCHEME_ID);
        when(scheme.getString("name")).thenReturn("1");
        when(mockOfBizDelegator.findAll("PermissionScheme")).thenReturn(ImmutableList.of(scheme));

        // If permissions added for commenting
        when(mockOfBizDelegator.findByAnd("SchemePermissions", commentQuery))
                .thenReturn(ImmutableList.of(commentPermission, commentPermission));
        upgradeTask_build6211.doUpgrade(false);
        verify(mockOfBizDelegator, never()).store(any(GenericValue.class));

        // If permission deleted for commenting
        when(mockOfBizDelegator.findByAnd("SchemePermissions", commentQuery))
                .thenReturn(ImmutableList.<GenericValue>of());
        upgradeTask_build6211.doUpgrade(false);
        verify(mockOfBizDelegator, never()).store(any(GenericValue.class));

        // If permission different than default for commenting
        when(commentPermission.getString("parameter")).thenReturn("different");
        when(mockOfBizDelegator.findByAnd("SchemePermissions", commentQuery))
                .thenReturn(ImmutableList.of(commentPermission));
        upgradeTask_build6211.doUpgrade(false);
        verify(mockOfBizDelegator, never()).store(any(GenericValue.class));

        // If permission is the default for commenting
        when(commentPermission.getString("parameter")).thenReturn(UpgradeTask_Build6211.USERS_PROJECT_ROLE_ID);
        when(mockOfBizDelegator.findByAnd("SchemePermissions", commentQuery))
                .thenReturn(ImmutableList.of(commentPermission));

        // If permissions added for linking
        when(mockOfBizDelegator.findByAnd("SchemePermissions", linkQuery))
            .thenReturn(ImmutableList.of(linkPermission, linkPermission));
        upgradeTask_build6211.doUpgrade(false);
        verify(mockOfBizDelegator, never()).store(any(GenericValue.class));

        // If permission deleted for linking
        when(mockOfBizDelegator.findByAnd("SchemePermissions", linkQuery))
                .thenReturn(ImmutableList.<GenericValue>of());
        upgradeTask_build6211.doUpgrade(false);
        verify(mockOfBizDelegator, never()).store(any(GenericValue.class));

        // If permission different than default for linking
        when(linkPermission.getString("parameter")).thenReturn("different");
        when(mockOfBizDelegator.findByAnd("SchemePermissions", linkQuery))
                .thenReturn(ImmutableList.of(linkPermission));
        upgradeTask_build6211.doUpgrade(false);
        verify(mockOfBizDelegator, never()).store(any(GenericValue.class));

        // If permission is the default for linking
        when(linkPermission.getString("parameter")).thenReturn(UpgradeTask_Build6211.DEVELOPERS_PROJECT_ROLE_ID);
        when(mockOfBizDelegator.findByAnd("SchemePermissions", linkQuery))
                .thenReturn(ImmutableList.of(linkPermission));
        upgradeTask_build6211.doUpgrade(false);
        verify(mockOfBizDelegator).store(linkPermission);
    }
}
