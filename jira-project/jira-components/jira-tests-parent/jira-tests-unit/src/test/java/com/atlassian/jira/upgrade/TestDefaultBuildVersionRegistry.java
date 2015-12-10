package com.atlassian.jira.upgrade;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.BuildUtilsInfo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.1
 */
public class TestDefaultBuildVersionRegistry extends MockControllerTestCase
{
    private static final String BUILD_NUMBER = "88888888";
    private static final String LATEST_VERSION = "LATEST VERSION";

    @Test
    public void testLookups() throws Exception
    {
        final BuildUtilsInfo buildUtilsInfo = mockController.getMock(BuildUtilsInfo.class);
        expect(buildUtilsInfo.getCurrentBuildNumber())
                .andReturn(BUILD_NUMBER);
        expect(buildUtilsInfo.getVersion())
                .andReturn(LATEST_VERSION);

        final DefaultBuildVersionRegistry registry = mockController.instantiate(DefaultBuildVersionRegistry.class);

        assertEquals(new BuildVersionImpl(BUILD_NUMBER, LATEST_VERSION), registry.getVersionForBuildNumber("99999999"));
        assertEquals(new BuildVersionImpl("466", "4.0"), registry.getVersionForBuildNumber("466"));
        assertEquals(new BuildVersionImpl("466", "4.0"), registry.getVersionForBuildNumber("465"));
        assertEquals(new BuildVersionImpl("22", "1.4.3"), registry.getVersionForBuildNumber("1"));
    }
}
