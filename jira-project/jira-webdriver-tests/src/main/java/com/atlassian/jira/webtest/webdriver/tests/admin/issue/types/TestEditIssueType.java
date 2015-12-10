package com.atlassian.jira.webtest.webdriver.tests.admin.issue.types;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.elements.AvatarId;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.EditIssueTypePage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ViewIssueTypesPage;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

@ResetData
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.ISSUE_TYPES })
public class TestEditIssueType extends BaseJiraWebTest
{

    @Test
    public void shouldChangeIssueTypeAvatar()
    {
        final long selectedAvatarId = 10245;
        // when
        final EditIssueTypePage editIssueTypePage = jira.goTo(EditIssueTypePage.class, 1l);
        editIssueTypePage.setAvatar(String.valueOf(selectedAvatarId));
        final ViewIssueTypesPage viewIssueTypesPage = editIssueTypePage.submit();

        // expect
        assertThat(viewIssueTypesPage.getIssueTypes(), Matchers.<ViewIssueTypesPage.IssueType>hasItem(
                TestAddIssueTypes.issueType(
                        new ViewIssueTypesPage.IssueType(
                                "Bug",
                                "A problem which impairs or prevents the functions of the product.",
                                false,
                                AvatarId.fromId(selectedAvatarId)
                        )
                )
        ));
    }
}
