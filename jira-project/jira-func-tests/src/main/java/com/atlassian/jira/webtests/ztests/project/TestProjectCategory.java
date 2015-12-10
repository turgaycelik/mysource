package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.webtests.JIRAWebTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.PROJECTS })
public class TestProjectCategory extends JIRAWebTest
{
    public TestProjectCategory(String name)
    {
        super(name);
    }

    private static final String CATEGORY_NAME = "New Project Category For Testing";
    private static final String CATEGORY_DESCRIPTION = "Testing for project category";

    @Override
    public void setUp()
    {
        super.setUp();
        administration.restoreBlankInstance();
    }

    public void testProjectCategory()
    {
        administration.project().addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        projectCategoryAddProjectCategory();
        projectCategoryDeleteProjectCategory();
        projectCategoryPlaceProjectInProjectCategory();
        projectCategoryAddDuplicateCategory();
        projectCategoryAddInvalidCategory();

        placeProjectInCategory(PROJECT_HOMOSAP, "None");
        deleteProjectCategory(CATEGORY_NAME);
    }

    /* -------- Project Category helper methods -------- */
    public void projectCategoryAddProjectCategory()
    {
        log("Project Category: Add project category");
        createProjectCategory(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        assertTextPresent(CATEGORY_NAME);
    }

    public void projectCategoryDeleteProjectCategory()
    {
        log("Project Category: Delete project category");
        deleteProjectCategory(CATEGORY_NAME);
        assertTextNotPresent(CATEGORY_NAME);

        createProjectCategory(CATEGORY_NAME, CATEGORY_DESCRIPTION);
    }

    public void projectCategoryPlaceProjectInProjectCategory()
    {
        log("Project Category: Place a project in a project category");
        placeProjectInCategory(PROJECT_HOMOSAP, CATEGORY_NAME);
        assertThat(backdoor.project().getProjectCategoryName(PROJECT_HOMOSAP_KEY), equalTo(CATEGORY_NAME));
    }

    public void projectCategoryAddDuplicateCategory()
    {
        log("Project Category: Attempt to create a project category with a duplicate name");
        createProjectCategory(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        assertions.getJiraFormAssertions().assertFieldErrMsg("The project category '" + CATEGORY_NAME + "' already exists");
    }

    public void projectCategoryAddInvalidCategory()
    {
        log("Project Category: Attempt to create a project category with an invalid name");
        createProjectCategory("", "");
        assertions.getJiraFormAssertions().assertFieldErrMsg("Please specify a name");
    }

}
