package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfProjectCategories extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testProjectCategoryAdministration() throws Exception
    {
        // Note: Associating categories with projects is already tested in TestXsrfProject

        new XsrfTestSuite(
            new XsrfCheck("AddCategory", new ProjectCategorySetup() {
                public void setup()
                {
                    super.setup();
                    tester.setFormElement("name", "New Category");
                }
            }, new XsrfCheck.FormSubmission("Add")),
            new XsrfCheck("EditCategory", new ProjectCategorySetup() {
                public void setup()
                {
                    super.setup();
                    navigation.clickLinkWithExactText("Edit");
                    tester.setFormElement("description", "This is a category!");
                }
            }, new XsrfCheck.FormSubmission("Update")),
            new XsrfCheck("DeleteCategory", new ProjectCategorySetup() {
                public void setup()
                {
                    super.setup();
                    navigation.clickLinkWithExactText("Delete");
                }
            }, new XsrfCheck.FormSubmission("Delete"))
        ).run(funcTestHelperFactory);
    }

    class ProjectCategorySetup implements XsrfCheck.Setup
    {
        public void setup()
        {
            navigation.gotoAdminSection("view_categories");
            tester.setWorkingForm("jiraform");
        }
    }
}