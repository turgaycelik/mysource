package com.atlassian.jira.web.util;

import com.atlassian.jira.util.UserAgentUtil;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestAccessKeyHelperImpl
{
    private AccessKeyHelperImpl accessKeyHelper;

    @Test
    public void testNullUserAgent() throws Exception
    {
        accessKeyHelper = new AccessKeyHelperImpl()
        {
            @Override
            UserAgentUtil.UserAgent getUserAgent()
            {
                return null;
            }
        };
        
        assertTrue(accessKeyHelper.isAccessKeySafe("a"));
    }

    @Test
    public void testFirefoxUserAgent() throws Exception
    {
        accessKeyHelper = new AccessKeyHelperImpl()
        {
            @Override
            UserAgentUtil.UserAgent getUserAgent()
            {
                return new UserAgentUtil.UserAgent(new UserAgentUtil.Browser(UserAgentUtil.BrowserFamily.FIREFOX, UserAgentUtil.BrowserMajorVersion.FIREFOX3, ""), new UserAgentUtil.OperatingSystem(UserAgentUtil.OperatingSystem.OperatingSystemFamily.MAC));
            }
        };
        
        assertTrue(accessKeyHelper.isAccessKeySafe("a"));
    }

    @Test
    public void testInternetExplorerUserAgent() throws Exception
    {
        accessKeyHelper = new AccessKeyHelperImpl()
        {
            @Override
            UserAgentUtil.UserAgent getUserAgent()
            {
                return new UserAgentUtil.UserAgent(new UserAgentUtil.Browser(UserAgentUtil.BrowserFamily.MSIE, UserAgentUtil.BrowserMajorVersion.MSIE7, ""), new UserAgentUtil.OperatingSystem(UserAgentUtil.OperatingSystem.OperatingSystemFamily.WINDOWS));
            }
        };

        assertTrue(accessKeyHelper.isAccessKeySafe("a"));
        assertFalse(accessKeyHelper.isAccessKeySafe("d"));
    }
}
