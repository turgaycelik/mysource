package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.SubmitButton;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfSystemOps extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testSystemOps() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("Integrity Checker", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.gotoAdmin();
                        tester.clickLink("integrity_checker");
                    }
                }, new XsrfCheck.FormSubmission("check"))
                ,

                new XsrfCheck("License Details", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.gotoAdmin();
                        tester.clickLink("license_details");
                    }
                }, new XsrfCheck.FormSubmission("Add"))
                ,

                new XsrfCheck("Add Listener", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.gotoAdmin();
                        tester.clickLink("listeners");
                    }
                }, new XsrfCheck.FormSubmission("Add"))
                ,

                new XsrfCheck("Edit Listener", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.gotoAdmin();
                        tester.clickLink("listeners");

                        tester.setFormElement("name", "l1");
                        tester.setFormElement("clazz", "com.atlassian.jira.event.listeners.DebugParamListener");
                        tester.submit("Add");
                        // Find the node with the text value "l1" that we just created.
                        // Then find the Operations List for it.
                        // Then find the Edit operation (and not the delete operation)
                        final XPathLocator xPathLocator = locator.xpath("//tr[td/b/text()='l1']//ul[@class='operations-list']//a[text()='Edit']");
                        // Then get the id of the link
                        final String editId = xPathLocator.getNodes()[0].getAttributes().getNamedItem("id").getNodeValue();
                        tester.clickLink(editId);

                    }
                }, new XsrfCheck.FormSubmission("Update"))
                ,

                new XsrfCheck("Delete Listener", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        // relies on edit setting up the data
                        gotoHome();
                        navigation.gotoAdmin();
                        tester.clickLink("listeners");

                        tester.setFormElement("name", "ldel");
                        tester.setFormElement("clazz", "com.atlassian.jira.event.listeners.DebugParamListener");
                        tester.submit("Add");

                        tester.clickLink("listeners");
                    }
                }, new XsrfCheck.LinkWithTextSubmission("Delete"))
                ,

                new XsrfCheck("Mail Queue Flush", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        gotoHome();
                        navigation.gotoAdmin();
                        tester.clickLink("mail_queue");
                    }
                }, new XsrfCheck.LinkWithTextSubmission("Flush mail queue"))
                ,

                new XsrfCheck("Add Service", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.dashboard().navigateTo();
                        navigation.gotoAdmin();
                        tester.clickLink("services");
                        tester.setFormElement("name", "name");
                        tester.setFormElement("clazz", "com.atlassian.jira.service.services.export.ExportService");
                    }
                }, new XsrfCheck.FormSubmission("Add Service"))
                ,

                new XsrfCheck("Edit Service", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.dashboard().navigateTo();
                        navigation.gotoAdmin();
                        tester.clickLink("services");
                        tester.clickLink("edit_10000");
                    }
                }, new XsrfCheck.FormSubmission("Update"))
                ,

                new XsrfCheck("Delete Service", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.dashboard().navigateTo();
                        navigation.gotoAdmin();
                        tester.clickLink("services");
                    }
                }, new XsrfCheck.LinkWithTextSubmission("Delete"))
                ,

                //////////////////////////////////
                // ASYNCH action here
                //////////////////////////////////
                new XsrfCheck("ReIndex", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.dashboard().navigateTo();
                        navigation.gotoAdmin();
                        tester.clickLink("indexing");
                    }
                },
                        new XsrfCheck.AsynchFormSubmission("reindex", 30000)
                        {
                            public boolean isOperationFinished()
                            {
                                tester.setWorkingForm("jiraform");
                                final SubmitButton button = tester.getDialog().getSubmitButton("Refresh");
                                if (button != null)
                                {
                                    tester.submit("Refresh");
                                }
                                return tester.getDialog().getResponseText().contains("Re-indexing is 100% complete.");
                            }
                        })

        ).run(funcTestHelperFactory);
    }

    private void gotoHome()
    {
        tester.gotoPage("");
    }
}