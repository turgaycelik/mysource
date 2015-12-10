package com.atlassian.jira.functest.unittests.config.sharing;

import com.atlassian.jira.functest.config.sharing.ConfigSharedEntityCleaner;
import com.atlassian.jira.functest.config.sharing.ConfigSharedEntityId;
import com.atlassian.jira.functest.config.sharing.DefaultConfigSharedEntityCleaner;
import com.atlassian.jira.functest.config.sharing.SharePermissionsCleaner;
import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * Test for {@link com.atlassian.jira.functest.config.sharing.DefaultConfigSharedEntityCleaner}.
 *
 * @since v4.2
 */
public class TestDefaultConfigSharedEntityCleaner extends TestCase
{
    public void testClean() throws Exception
    {
        assertClean(true, true);
        assertClean(true, false);
        assertClean(false, true);
        assertClean(false, false);
    }

    private void assertClean(boolean oneClean, boolean twoClean)
    {
        final IMocksControl mocksControl = EasyMock.createControl();

        final SharePermissionsCleaner one = mocksControl.createMock(SharePermissionsCleaner.class);
        final SharePermissionsCleaner two = mocksControl.createMock(SharePermissionsCleaner.class);

        ConfigSharedEntityId id = new ConfigSharedEntityId(90L, "type1");

        EasyMock.expect(one.clean(id)).andReturn(oneClean);
        EasyMock.expect(two.clean(id)).andReturn(twoClean);

        mocksControl.replay();

        final ConfigSharedEntityCleaner configSharedEntityCleaner = new DefaultConfigSharedEntityCleaner(one, two);
        assertEquals(oneClean || twoClean, configSharedEntityCleaner.clean(id));

        mocksControl.verify();
    }
}
