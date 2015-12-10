package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.avatar.TestAvatarSettingsMigration;
import com.atlassian.jira.webtests.ztests.avatar.TestGravatarSupport;

import junit.framework.Test;

public class FuncTestSuiteAvatars extends FuncTestSuite
{
    public static final FuncTestSuite SUITE = new FuncTestSuiteAvatars();

    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteAvatars()
    {
        addTest(TestAvatarSettingsMigration.class);
        addTest(TestGravatarSupport.class);
    }
}
