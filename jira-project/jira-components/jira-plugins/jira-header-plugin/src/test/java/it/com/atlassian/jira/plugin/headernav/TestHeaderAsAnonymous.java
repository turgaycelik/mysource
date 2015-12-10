package it.com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@User(Users.Anonymous)
public class TestHeaderAsAnonymous
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
    public void testAppSwitcherVisible()
    {
        assertTrue(header.hasAppSwitcher());
    }

    @Test
    public void testAppSwitcherBaseUrl()
    {
        assertThat(header.getAppSwitcherMenuLinkTarget(), allOf(startsWith("http://"), endsWith("/secure/MyJiraHome.jspa")));
    }

    @Test
    public void testNoAppSwitcherMenuWhenOnlyOneApplicationVisible()
    {
        assertFalse(header.hasAppSwitcherMenu());
    }

    @Test
    public void testDashboardLinkVisible()
    {
        assertTrue(header.hasMainHeaderLinks());
        assertThat(header.getMainHeaderLinkIds(), IsIterableContainingInOrder.contains("home_link"));
    }

    @Test
    public void testCreateIssueButtonVisible()
    {
        assertFalse(header.hasCreateIssueButton());
    }

    @Test
    public void testQuickSearchVisible()
    {
        assertTrue(header.hasQuickSearch());
    }

    @Test
    public void testHelpMenuVisible()
    {
        assertTrue(header.hasHelpMenu());
    }

    @Test
    public void testAdminMenuNotVisible()
    {
        assertFalse(header.hasAdminMenu());
    }

    @Test
    public void testLoginButtonVisible()
    {
        assertTrue(header.hasLoginButton());
    }

}
