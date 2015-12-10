package com.atlassian.jira.webtest.webdriver.qunit;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.qunit.test.runner.QUnitIndexPage;
import com.atlassian.qunit.test.runner.QUnitTestMeta;
import com.atlassian.qunit.test.runner.QUnitTestPage;
import com.atlassian.qunit.test.runner.QUnitTestResult;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Runs all Qunit tests in WebDriver.
 *
 * Utilises the aui-qunit-plugin to write all test outcomes in surefire format.
 *
 * @since v5.2
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.QUNIT })
public class TestQunit extends BaseJiraWebTest
{
    private static final List<String> QUNIT_EXCEPTION_LIST = Lists.newArrayList(
            "project-templates-plugin",
            "jira-quick-edit-plugin",
            "jira-development-integration-plugin"
    );
    private final File outdir;

    public TestQunit() {
        String location = System.getProperty("jira.qunit.testoutput.location");
        if (StringUtils.isEmpty(location)) {
            System.err.println("Writing result XML to tmp, jira.qunit.testoutput.location not defined");
            location = System.getProperty("java.io.tmpdir");
        }

        outdir = new File(location);
    }

    @Test
    @ResetData
    public void runAllTests() throws Exception {
        QUnitIndexPage indexPage = pageBinder.navigateToAndBind(QUnitIndexPage.class);
        for (QUnitTestMeta test : indexPage.getAllTests()) {
            if (shouldRunTest(test.getUrl())) {
                QUnitTestPage testPage = pageBinder.navigateToAndBind(QUnitTestPage.class, test);
                QUnitTestResult result = testPage.getResult();
                result.write(outdir);
            }
        }
    }

    private boolean shouldRunTest(final String testURL) {
        for (final String exception : QUNIT_EXCEPTION_LIST) {
            if (testURL.contains(exception)) {
                return false;
            }
        }
        return true;
    }
}
