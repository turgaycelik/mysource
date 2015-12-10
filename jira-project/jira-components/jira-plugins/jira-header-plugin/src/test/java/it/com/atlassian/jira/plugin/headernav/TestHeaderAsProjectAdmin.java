package it.com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@User(Users.ProjectAdmin)
public class TestHeaderAsProjectAdmin
{
    private static final JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);

    private CommonHeader header;

    @ClassRule
    public static final RuleChain ruleChain = RuleChain.outerRule(new PrepareJiraRule(product, "one-project.xml"))
                                                        .around(new LoginUserRule(product));

    @Before
    public void setUp()
    {
        header = CommonHeader.visit(product);
    }

    @Test
    public void testAdminMenuLinksForProjectAdmin()
    {
        assertTrue(header.hasAdminMenu());
        assertThat(header.getAdminMenuLinkIds(), IsIterableContainingInOrder.contains(expectedAdminMenuLinks()));
    }

    @Test
    public void testProjectAdmin()
    {
        assertTrue(header.hasAppSwitcher());
        assertTrue(header.hasMainHeaderLinks());
        assertTrue(header.hasCreateIssueButton());
        assertTrue(header.hasQuickSearch());
        assertTrue(header.hasHelpMenu());
        assertTrue(header.hasAdminMenu());
        assertFalse(header.hasLoginButton());
        assertTrue(header.hasUserOptionsMenu());
    }

    private String[] expectedAdminMenuLinks()
    {
        return new String[] { "admin_project_menu" };
    }
}
