package it.com.atlassian.jira.webtest.tests.just_jira;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.EnvironmentBasedProductInstance;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.pages.viewissue.MoveIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.link.IssueLink;
import com.atlassian.jira.pageobjects.pages.viewissue.link.IssueLinkSection;
import com.atlassian.jira.pageobjects.pages.viewissue.linkissue.LinkIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.linkissue.LinkJiraSection;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import it.com.atlassian.jira.webtest.pageobjects.applinks.OAuthConfirmPage;
import it.com.atlassian.jira.webtest.pageobjects.applinks.ViewApplinksPage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests around remote JIRA issue links
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUE_LINKS })
public class TestRemoteJiraIssueLinks extends BaseJiraWebTest
{
    private static final String JIRA2_APP_ID = "8835b6b9-5676-3de4-ad59-bbe987416662";

    private JiraTestedProduct jira2;

    @Before
    public void setUp() throws Exception
    {
        final Properties jira2Properties = LocalTestEnvironmentData.loadProperties("test.jira2.server.properties", "jira2test.properties");
        final JIRAEnvironmentData jira2EnvironmentData = new LocalTestEnvironmentData(jira2Properties, null);
        jira2 = new JiraTestedProduct(new EnvironmentBasedProductInstance(jira2EnvironmentData));
        final Backdoor jira2Backdoor = jira2.injector().getInstance(Backdoor.class);

        backdoor.restoreData("TestRemoteIssueLinks.xml");
        jira2Backdoor.restoreData("TestRemoteJiraIssueLinks.xml");

        backdoor.applicationProperties().setText("jira.baseurl", jira.environmentData().getBaseUrl().toString());
        jira2Backdoor.applicationProperties().setText("jira.baseurl", jira2.environmentData().getBaseUrl().toString());
    }

    @Test
    public void testCreateRemoteJiraLink() throws InterruptedException
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");

        final List<IssueLink> remoteLinks = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .setIssueKey("TST-1")
                .submit()
                .getIssueLinkSection()
                .getLinksForSource(IssueLinkSection.LinkSourceType.REMOTE);

        final IssueLink createdLink = getOnlyElement(remoteLinks);

        assertEquals("TST-1", createdLink.getTitle());
    }

    @Test
    public void testCreateMultipleRemoteJiraLinks() throws InterruptedException
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");

        final List<IssueLink> remoteLinks = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .clearIssueKeys()
                .addIssueKey("TST-1")
                .addIssueKey("TST-2")
                .addIssueKey("TST-3")
                .submit()
                .getIssueLinkSection()
                .getLinksForSource(IssueLinkSection.LinkSourceType.REMOTE);

        assertEquals(3, remoteLinks.size());
        assertEquals("TST-1", remoteLinks.get(0).getTitle());
        assertEquals("TST-2", remoteLinks.get(1).getTitle());
        assertEquals("TST-3", remoteLinks.get(2).getTitle());
    }

    @Test
    @Ignore("Adding the issue picker broke this test. See JRADEV-8782.")
    public void testCreateRemoteJiraLinkOAuth() throws Exception
    {
        createOAuthAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-3");
        LinkJiraSection linkRemoteJiraSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID);

        if (!linkRemoteJiraSection.requireCredentials())
        {
            // Now that we are using the issue picker, this line will fail
            linkRemoteJiraSection = linkRemoteJiraSection.setIssueKey("TST-1").submitExpectingError();
        }
        assertTrue(linkRemoteJiraSection.requireCredentials());

        final OAuthConfirmPage oAuthConfirmPage = startOAuthDance(linkRemoteJiraSection);
        oAuthConfirmPage.confirmHandlingWebLoginIfRequired("admin", "admin");

        assertFalse(linkRemoteJiraSection.requireCredentials());
        final IssueLinkSection issueLinkSection = linkRemoteJiraSection.setIssueKey("TST-1").submit().getIssueLinkSection();
        final IssueLink issueLink = getOnlyElement(issueLinkSection.getLinks());
        assertEquals("TST-1", issueLink.getTitle());
    }

    @Test
    @Ignore("Adding the issue picker broke this test. See JRADEV-8782.")
    public void testCreateRemoteJiraLinkOAuth_DenyThenApprove() throws Exception
    {
        createOAuthAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-3");
        LinkJiraSection linkRemoteJiraSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID);

        // OAuth tokens are not cleared locally when restoring from a backup, so this can go either way
        if (!linkRemoteJiraSection.requireCredentials())
        {
            // Now that we are using the issue picker, this line will fail
            linkRemoteJiraSection = linkRemoteJiraSection.setIssueKey("TST-1").submitExpectingError();
        }
        assertTrue(linkRemoteJiraSection.requireCredentials());

        // deny
        OAuthConfirmPage oAuthConfirmPage = startOAuthDance(linkRemoteJiraSection);
        oAuthConfirmPage.denyHandlingWebLoginIfRequired("admin", "admin");
        assertTrue(linkRemoteJiraSection.requireCredentials());

        // approve
        oAuthConfirmPage = startOAuthDance(linkRemoteJiraSection);
        oAuthConfirmPage.confirmHandlingWebLoginIfRequired("admin", "admin");
        assertFalse(linkRemoteJiraSection.requireCredentials());

        final IssueLinkSection issueLinkSection = linkRemoteJiraSection.setIssueKey("TST-1").submit().getIssueLinkSection();
        final IssueLink issueLink = getOnlyElement(issueLinkSection.getLinks());
        assertEquals("TST-1", issueLink.getTitle());
    }

    @Test
    public void testDeleteRemoteJiraLink() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        IssueLinkSection issueLinkSection = jira.goToViewIssue("HSP-4").getIssueLinkSection();

        final IssueLink issueLink = getOnlyElement(issueLinkSection.getLinks());

        issueLinkSection = issueLinkSection.deleteLink(issueLink).confirm().getIssueLinkSection();

        assertTrue(issueLinkSection.getLinks().isEmpty());
    }

    @Test
    public void testUpdatingRemoteIssueUpdatesLink() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        jira2.gotoLoginPage()
                .loginAsSysAdmin(ViewIssuePage.class, "TST-3")
                .editIssueViaKeyboardShortcut()
                .fill("summary", "New Summary")
                .submitExpectingViewIssue("TST-3")
                .closeIssue();

        final IssueLinkSection issueLinkSection = jira.goToViewIssue("HSP-4").getIssueLinkSection();

        final IssueLink issueLink = getOnlyElement(issueLinkSection.getLinks());

        assertEquals("New Summary", issueLink.getSummary());
        assertEquals(jira2.getProductInstance().getBaseUrl() + "/images/icons/statuses/closed.png", issueLink.getStatus());
        assertTrue(issueLink.isResolved());
    }

    @Test
    public void testMovingRemoteIssueUpdatesLink() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage movedIssue = jira2.gotoLoginPage()
                .loginAsSysAdmin(MoveIssuePage.class, "TST-3")
                .setNewProject("Public Project")
                .next()
                .next()
                .move();

        final IssueLinkSection issueLinkSection = jira.goToViewIssue("HSP-4").getIssueLinkSection();

        final IssueLink issueLink = getOnlyElement(issueLinkSection.getLinks());

        assertEquals(movedIssue.getIssueKey(), issueLink.getTitle());
        assertEquals(jira2.getProductInstance().getBaseUrl() + movedIssue.getUrl(), issueLink.getUrl());
    }

    @Test
    public void testCreateRemoteJiraLinkWithEmptyIssueKeyFails() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");

        final LinkJiraSection linkRemoteJiraSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .clearIssueKeys()
                .submitExpectingError();

        assertTrue(linkRemoteJiraSection.errorsPresent());
    }

    @Test
    public void testCreateRemoteJiraLinkWithNonExistingIssueKeyFails() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");

        final LinkJiraSection linkRemoteJiraSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .setIssueKey("NULL-99999")
                .submitExpectingError();

        assertTrue(linkRemoteJiraSection.errorsPresent());
    }

    @Test
    public void testCreateRemoteJiraLinksWithSingleNonExistingIssueKeyFails() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");

        final LinkJiraSection linkRemoteJiraSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .clearIssueKeys()
                .addIssueKey("TST-1")       // valid issue key
                .addIssueKey("NULL-99999")  // non-valid issue key
                .submitExpectingError();

        assertEquals(1, linkRemoteJiraSection.getErrorCount());
    }

    @Test
    public void testCreateRemoteJiraLinksWithMultipleNonExistingIssueKeyFails() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");

        final LinkJiraSection linkRemoteJiraSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .clearIssueKeys()
                .addIssueKey("NULL-11111")
                .addIssueKey("NULL-99999")
                .submitExpectingError();

        assertEquals(2, linkRemoteJiraSection.getErrorCount());
    }

    @Test
    public void testCreateDuplicateRemoteJiraLinkFails() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-4");

        final LinkJiraSection linkRemoteJiraSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .setIssueKey("TST-3")
                .submitExpectingError();

        assertTrue(linkRemoteJiraSection.errorsPresent());
    }

    @Test
    public void testCreateRemoteJiraLinkWithNoPermissionFails() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.quickLogin("fred", "fred", ViewIssuePage.class, "MKY-1");

        final LinkJiraSection linkRemoteJiraSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .setIssueKey("TST-1")
                .submitExpectingError();

        assertTrue(linkRemoteJiraSection.errorsPresent());
    }

    @Test
    public void testCreateReciprocalRemoteJiraLink() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");

        invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .setIssueKey("TST-3")
                .createReciprocal()
                .submit();

        final String localIssueUrl = jira.getProductInstance().getBaseUrl() + viewIssuePage.getUrl();
        assertReciprocalLinkExists("TST-3", "HSP-1", localIssueUrl);
    }

    @Test
    public void testCreateMultipleReciprocalRemoteJiraLinks() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");

        invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .clearIssueKeys()
                .addIssueKey("TST-1")
                .addIssueKey("TST-2")
                .addIssueKey("TST-3")
                .createReciprocal()
                .submit();

        final String localIssueUrl = jira.getProductInstance().getBaseUrl() + viewIssuePage.getUrl();
        assertReciprocalLinkExists("TST-1", "HSP-1", localIssueUrl);
        assertReciprocalLinkExists("TST-2", "HSP-1", localIssueUrl);
        assertReciprocalLinkExists("TST-3", "HSP-1", localIssueUrl);
    }

    @Test
    public void testCreateReciprocalRemoteJiraLinkWithNoPermissionWarns() throws Exception
    {
        createTrustedAppsAppLinkToJira2();

        final ViewIssuePage viewIssuePage = jira.quickLogin("fred", "fred", ViewIssuePage.class, "MKY-1");

        final IssueLinkSection issueLinkSection = invokeLinkIssue(viewIssuePage)
                .gotoJiraLink()
                .selectServer(JIRA2_APP_ID)
                .setIssueKey("PUB-1")
                .createReciprocal()
                .submit()
                .getIssueLinkSection();

        assertTrue(issueLinkSection.warningsPresent());
    }

    private void createTrustedAppsAppLinkToJira2()
    {
        jira.goTo(ViewApplinksPage.class).createTrustedAppLink(jira.getProductInstance().getBaseUrl(),
                        jira2.getProductInstance().getBaseUrl(),
                        "admin", "admin");
    }

    private void createOAuthAppLinkToJira2()
    {
        jira.goTo(ViewApplinksPage.class)
                .createOAuthAppLink(jira.getProductInstance().getBaseUrl(),
                        jira2.getProductInstance().getBaseUrl(),
                        "admin", "admin");
    }

    private LinkIssueDialog invokeLinkIssue(final ViewIssuePage viewIssuePage)
    {
        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        return pageBinder.bind(LinkIssueDialog.class, viewIssuePage.getIssueKey());
    }

    /**
     * Starts the OAuth dance
     *
     * @param linkRemoteJiraSection LinkRemoteJiraSection
     * @return OAuthConfirmPage
     */
    @SuppressWarnings("unchecked")
    private OAuthConfirmPage startOAuthDance(final LinkJiraSection linkRemoteJiraSection)
    {
        linkRemoteJiraSection.startOAuthDance();
        return pageBinder.bind(OAuthConfirmPage.class);
    }

    private void assertReciprocalLinkExists(final String remoteIssueKey, final String localIssueKey, final String localIssueUrl)
    {
        final IssueLink reciprocalLink = jira2.gotoLoginPage()
                .loginAsSysAdmin(ViewIssuePage.class, remoteIssueKey)
                .getIssueLinkSection()
                .getLinkByTitle(localIssueKey);

        assertNotNull(reciprocalLink);
        assertEquals(localIssueUrl, reciprocalLink.getUrl());
    }
}
