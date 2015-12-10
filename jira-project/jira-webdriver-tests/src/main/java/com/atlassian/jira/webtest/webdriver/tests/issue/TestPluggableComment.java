package com.atlassian.jira.webtest.webdriver.tests.issue;

import java.util.List;

import javax.inject.Inject;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertThat;

@WebTest ({ Category.WEBDRIVER_TEST, Category.COMMENTS, Category.REFERENCE_PLUGIN })
public class TestPluggableComment extends BaseJiraWebTest
{
    private static final String COMMENT_FIELD_RENDERER_MODULE_KEY = "com.atlassian.jira.dev.reference-plugin:ref-plugin-comment-field-renderer";

    @Inject
    private PageElementFinder finder;

    @Before
    public void setup()
    {
        backdoor.dataImport().restoreBlankInstance();
        backdoor.plugins().enablePluginModule(COMMENT_FIELD_RENDERER_MODULE_KEY);
    }

    @After
    public void disable()
    {
        backdoor.plugins().disablePluginModule(COMMENT_FIELD_RENDERER_MODULE_KEY);
    }

    @Test
    public void customCommentsRenderedOnViewIssuePage()
    {
        IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with plugged comment field renderer");
        backdoor.issues().commentIssue(issue.key, "comment-text");

        jira.goToViewIssue("HSP-1");

        // This all elements come from plugged comment field rendering. They are not supposed to be used in production, therefore
        // we don't provide any proper page objects.
        List<PageElement> pageElements = finder.findAll(By.className("plugged-comment-view"));
        assertThat(pageElements, Matchers.<PageElement>hasItem(Matchers.<PageElement>hasProperty("text", Matchers.containsString("comment-text"))));

        finder.find(By.id("footer-comment-button")).click();
        assertThat(finder.find(By.className("plugged-comment-edit")).isVisible(), Matchers.is(true));
    }
}
