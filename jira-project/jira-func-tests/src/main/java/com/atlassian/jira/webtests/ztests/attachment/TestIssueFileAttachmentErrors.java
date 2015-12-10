package com.atlassian.jira.webtests.ztests.attachment;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.page.Error404;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.HttpUnitOptions;

import static org.junit.Assert.assertThat;

/**
 * @since v3.13.5
 */
@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS })
public class TestIssueFileAttachmentErrors extends FuncTestCase
{
    @Override
    protected void setUpHttpUnitOptions()
    {
        log("not running normal test setup for " + getName());
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setScriptingEnabled(false);
    }

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        tester.getTestContext().setBaseUrl(getEnvironmentData().getBaseUrl().toExternalForm());
        administration.restoreData("TestDeleteAttachments.xml");
        administration.attachments().enable();
    }

    @Override
    public void tearDownTest()
    {
        log("not running normal test teardown for " + getName());
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
    }

    public void testViewAttachmentWithNonNumericId()
    {
        assertThat(new Error404(tester).visit("/secure/attachment/idontparse/DummyAttachment"), Error404.isOn404Page());
    }

    public void testViewAttachmentWithNoPath()
    {
        assertErrorResponse("/secure/attachment/", 400, "Invalid attachment path");
    }

    public void testTryDownloadAsZipWhenZipDisabled() throws Exception
    {
        administration.attachments().disableZipSupport();
        assertThat(new Error404(tester).visit("/secure/attachmentzip/10000.zip"), Error404.isOn404Page());
    }

    private void assertErrorResponse(final String url, final int errorCode, final String message)
    {
        tester.beginAt(url);
        assertEquals(errorCode, tester.getDialog().getResponse().getResponseCode());
        assertions.html().assertResponseContains(tester, message);
    }
}
