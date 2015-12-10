package it.com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@User(Users.SysAdmin)
public class TestHeaderAsSysAdmin
{
    private static final JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);
    private static final String BASE_URL = product.getProductInstance().getBaseUrl();
    private static final String ADMIN_PROJECT_LIST_URL = BASE_URL + "/secure/project/ViewProjects.jspa";

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
    public void testAdminMenuBaseUrl()
    {
        assertTrue(header.hasAdminMenu());
        assertThat(header.getAdminMenuLinkTarget(), is(ADMIN_PROJECT_LIST_URL));
    }

    @Test
    public void testAdminMenuLinksForSysadmin()
    {
        assertTrue(header.hasAdminMenu());
        assertThat(header.getAdminMenuLinkIds(), IsIterableContainingInOrder.contains(expectedAdminMenuLinks()));
    }

    @Test
    public void testSysadmin()
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

    public static String[] expectedAdminMenuLinks()
    {
        return new String[] { "admin_project_menu", "admin_plugins_menu", "admin_users_menu", "admin_issues_menu", "admin_system_menu", "admin_auditing_menu" };
    }
}
