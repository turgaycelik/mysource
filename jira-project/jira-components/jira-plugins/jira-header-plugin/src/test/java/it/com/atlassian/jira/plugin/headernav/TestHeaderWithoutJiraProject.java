package it.com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static it.com.atlassian.jira.plugin.headernav.TestHeaderAsSysAdmin.expectedAdminMenuLinks;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestHeaderWithoutJiraProject
{
    private static final JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);

    private CommonHeader header;

    @BeforeClass
    public static void prepareJira()
    {
        product.backdoor().restoreDataFromResource("blank-jira.xml");
        product.backdoor().darkFeatures().enableForSite("com.atlassian.jira.darkfeature.CommonHeader");
    }

    @Rule
    public final LoginUserRule loginUserRule = new LoginUserRule(product);

    @Before
    public void setUp()
    {
        header = CommonHeader.visit(product);
    }

    @Test
    @User(Users.Anonymous)
    public void testDashboardForAnonymous()
    {
        assertTrue(header.hasMainHeaderLinks());
        assertThat(header.getMainHeaderLinkIds(), IsIterableContainingInOrder.contains("home_link"));
    }

    @Test
    @User(Users.AuthenticatedUser)
    public void testDashboardForAuthenticatedUser()
    {
        assertTrue(header.hasMainHeaderLinks());
        assertThat(header.getMainHeaderLinkIds(), IsIterableContainingInOrder.contains("home_link"));
    }

    @Test
    @User(Users.SysAdmin)
    public void testAdminMenuLinksForSysadmin()
    {
        assertTrue(header.hasAdminMenu());
        assertThat(header.getAdminMenuLinkIds(), IsIterableContainingInOrder.contains(expectedAdminMenuLinks()));
    }

    @Test
    @User(Users.Anonymous)
    public void testAnonymous() throws Exception
    {
        assertTrue(header.hasAppSwitcher());
        assertTrue(header.hasMainHeaderLinks());
        assertFalse(header.hasCreateIssueButton());
        assertTrue(header.hasQuickSearch());
        assertTrue(header.hasHelpMenu());
        assertFalse(header.hasAdminMenu());
        assertTrue(header.hasLoginButton());
    }

    @Test
    @User(Users.SysAdmin)
    public void testSysadmin()
    {
        assertTrue(header.hasAppSwitcher());
        assertTrue(header.hasMainHeaderLinks());
        assertFalse(header.hasCreateIssueButton());
        assertTrue(header.hasQuickSearch());
        assertTrue(header.hasHelpMenu());
        assertTrue(header.hasAdminMenu());
        assertFalse(header.hasLoginButton());
        assertTrue(header.hasUserOptionsMenu());
    }

    @Test
    @User(Users.AuthenticatedUser)
    public void testUser()
    {
        assertTrue(header.hasAppSwitcher());
        assertTrue(header.hasMainHeaderLinks());
        assertFalse(header.hasCreateIssueButton());
        assertTrue(header.hasQuickSearch());
        assertTrue(header.hasHelpMenu());
        assertFalse(header.hasAdminMenu());
        assertFalse(header.hasLoginButton());
        assertTrue(header.hasUserOptionsMenu());
    }
}
