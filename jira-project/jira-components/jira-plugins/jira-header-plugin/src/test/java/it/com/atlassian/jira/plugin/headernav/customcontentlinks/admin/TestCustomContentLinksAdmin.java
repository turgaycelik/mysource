package it.com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.pageobjects.TestedProductFactory;
import it.com.atlassian.jira.plugin.headernav.LoginUserRule;
import it.com.atlassian.jira.plugin.headernav.PrepareJiraRule;
import it.com.atlassian.jira.plugin.headernav.User;
import it.com.atlassian.jira.plugin.headernav.Users;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@User(Users.ProjectAdmin)
public class TestCustomContentLinksAdmin
{
    private static final JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);
    private static CustomContentLinksConfigPageTab page;

    @ClassRule
    public static final RuleChain ruleChain = RuleChain.outerRule(new PrepareJiraRule(product, "one-project.xml"))
            .around(new LoginUserRule(product));

    @Before
    public void setupPage() {
        product.backdoor().darkFeatures().enableForSite("rotp.project.shortcuts");
        ProjectSummaryPageTab summaryTab = product.goTo(ProjectSummaryPageTab.class, "DRAG");
        page = summaryTab.getTabs().gotoTab("custom-content-links-admin-page-link", CustomContentLinksConfigPageTab.class, "DRAG");
    }

    @Test
    public void testLinkAdmin() {
        // test validation
        assertEquals(1, page.getLinks().size());
        page.createLink("bar", "");
        assertEquals("Must not be empty", page.getCreateLinkForm().getUrlField().getError());
        page.createLink("", "xxx");
        assertEquals("Must not be empty", page.getCreateLinkForm().getLabelField().getError());
        assertEquals(1, page.getLinks().size());

        // test creation works
        assertEquals(1, page.getLinks().size());
        page.createLink("Foo", "http://foo.com");
        assertEquals(2, page.getLinks().size());

        // test links show in project shortcuts dialog
        page.createLink("Bar", "http://bar.com");
        page.createLink("Baz", "http://baz.com");
        ProjectShortcutsBrowseProjectPage projectPage = product.goTo(ProjectShortcutsBrowseProjectPage.class, "DRAG");
        ProjectShortcutsDialog projectShortcutsDialog = projectPage.clickAvatar();
        List<ProjectShortcutsDialog.Link> relatedLinks = projectShortcutsDialog.getRelatedLinks();
        assertEquals(Arrays.asList(link("foo"), link("bar"), link("baz")), relatedLinks);

    }

    private ProjectShortcutsDialog.Link link(String s) {
        return new ProjectShortcutsDialog.Link(s.substring(0,1).toUpperCase() + s.substring(1), "http://" + s + ".com/");
    }
}
