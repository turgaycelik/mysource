package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.DeleteLinkConfirmationDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.DeleteRemoteIssueLinkPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.link.IssueLink;
import com.atlassian.jira.pageobjects.pages.viewissue.link.IssueLinkSection;
import com.atlassian.jira.pageobjects.pages.viewissue.link.activity.Comment;
import com.atlassian.jira.pageobjects.pages.viewissue.linkissue.LinkConfluenceSection;
import com.atlassian.jira.pageobjects.pages.viewissue.linkissue.LinkIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.linkissue.WebLinkSection;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openqa.selenium.By;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests linking an issue.
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.GADGETS, Category.ACTIVITY_STREAMS, Category.IE_INCOMPATIBLE })
@Restore ("xml/TestLinkIssueWithPriorityFieldHiddenEnterprise.xml")
public class TestLinkIssue extends BaseJiraWebTest
{
    private static final String WEB_LINK_RELATIONSHIP_TEXT = "links to";

    private static final String DUPLICATES_RELATIONSHIP = "duplicates";
    private static final String DUPLICATED_BY_RELATIONSHIP = "duplicated by";

    private static final String STATUS_OPEN = "OPEN";

    private static final String PRIORITY_IMAGE_CRITICAL = "/images/icons/priorities/critical.png";
    private static final String PRIORITY_IMAGE_MAJOR = "/images/icons/priorities/major.png";
    private static final String PRIORITY_IMAGE_MINOR = "/images/icons/priorities/minor.png";

    private static final String ISSUE_IMAGE_BUG = "/images/icons/issuetypes/bug.png";
    private static final String ISSUE_IMAGE_NEWFEATURE = "/images/icons/issuetypes/newfeature.png";
    private static final String ISSUE_IMAGE_TASK = "/images/icons/issuetypes/task.png";
    private static final String ISSUE_IMAGE_IMPROVEMENT = "/images/icons/issuetypes/improvement.png";

    private static final String GENERIC_LINK_ICON = "/images/icons/generic_link_16.png";

    @Test
    public void testPriorityIconIsHiddenIfPriorityFieldIsHidden()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        IssueLinkSection issueLinkSection = viewIssuePage.getIssueLinkSection();

        List<IssueLink> actualDuplicatesIssueLinks = issueLinkSection.getLinks(IssueLinkSection.LinkSourceType.INTERNAL, DUPLICATES_RELATIONSHIP);
        ImmutableList.Builder<IssueLink> expectedDuplicatesIssueLinks = ImmutableList.builder();
        expectedDuplicatesIssueLinks.add(IssueLink.builder().relationship(DUPLICATES_RELATIONSHIP).title("MKY-1").url(generateIssueUrl("MKY-1")).summary("new feature in project monkey with no priority set on creation as it is hidden").iconUrl(prependBaseUrl(ISSUE_IMAGE_NEWFEATURE)).status(STATUS_OPEN).build());
        expectedDuplicatesIssueLinks.add(IssueLink.builder().relationship(DUPLICATES_RELATIONSHIP).title("HSP-3").url(generateIssueUrl("HSP-3")).summary("this task is duplicated by hsp-1").iconUrl(prependBaseUrl(ISSUE_IMAGE_TASK)).priorityIconUrl(prependBaseUrl(PRIORITY_IMAGE_MAJOR)).status(STATUS_OPEN).build());
        expectedDuplicatesIssueLinks.add(IssueLink.builder().relationship(DUPLICATES_RELATIONSHIP).title("HSP-2").url(generateIssueUrl("HSP-2")).summary("improvement on project homosapien this issue will be linked to the homosapien bug and has its priority showing").iconUrl(prependBaseUrl(ISSUE_IMAGE_IMPROVEMENT)).priorityIconUrl(prependBaseUrl(PRIORITY_IMAGE_CRITICAL)).status(STATUS_OPEN).build());
        assertListsEquals(expectedDuplicatesIssueLinks.build(), actualDuplicatesIssueLinks);

        List<IssueLink> actualDuplicatedByIssueLinks = issueLinkSection.getLinks(IssueLinkSection.LinkSourceType.INTERNAL, DUPLICATED_BY_RELATIONSHIP);
        ImmutableList.Builder<IssueLink> expectedDuplicatedByIssueLinks = ImmutableList.builder();
        expectedDuplicatesIssueLinks.add(IssueLink.builder().relationship(DUPLICATED_BY_RELATIONSHIP).title("HSP-4").url(generateIssueUrl("HSP-4")).summary("this bug duplicates hsp-1").iconUrl(prependBaseUrl(ISSUE_IMAGE_BUG)).priorityIconUrl(prependBaseUrl(PRIORITY_IMAGE_MINOR)).status(STATUS_OPEN).build());
        expectedDuplicatesIssueLinks.add(IssueLink.builder().relationship(DUPLICATED_BY_RELATIONSHIP).title("MKY-1").url(generateIssueUrl("MKY-1")).summary("new feature in project monkey with no priority set on creation as it is hidden").iconUrl(prependBaseUrl(ISSUE_IMAGE_NEWFEATURE)).status(STATUS_OPEN).build());
        expectedDuplicatesIssueLinks.add(IssueLink.builder().relationship(DUPLICATED_BY_RELATIONSHIP).title("HSP-2").url(generateIssueUrl("HSP-2")).summary("improvement on project homosapien this issue will be linked to the homosapien bug and has its priority showing").iconUrl(prependBaseUrl(ISSUE_IMAGE_IMPROVEMENT)).priorityIconUrl(prependBaseUrl(PRIORITY_IMAGE_CRITICAL)).status(STATUS_OPEN).build());
        assertListsEquals(expectedDuplicatedByIssueLinks.build(), actualDuplicatedByIssueLinks);
    }

    // https://jdog.atlassian.net/browse/FLAKY-77
//    @Test
//    public void testDeleteRemoteIssueLink()
//    {
//        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
//        IssueLinkSection issueLinkSection = viewIssuePage.getIssueLinkSection().expandLinks();
//
//        List<IssueLink> blogLinks = issueLinkSection.getLinks(IssueLinkSection.LinkSourceType.REMOTE, "Blog Links");
//        assertEquals(2, blogLinks.size());
//        assertEquals("My fantastic blog", blogLinks.get(0).getTitle());
//
//        DeleteLinkConfirmationDialog deleteLinkConfirmationDialog = issueLinkSection.deleteLink(blogLinks.get(0));
//        viewIssuePage = deleteLinkConfirmationDialog.confirm();
//        issueLinkSection = viewIssuePage.getIssueLinkSection().expandLinks();
//
//        blogLinks = issueLinkSection.getLinks(IssueLinkSection.LinkSourceType.REMOTE, "Blog Links");
//        assertEquals(1, blogLinks.size());
//    }

    @Test
    public void testCancelDeleteRemoteIssueLink()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        IssueLinkSection issueLinkSection = viewIssuePage.getIssueLinkSection().expandLinks();

        List<IssueLink> blogLinks = issueLinkSection.getLinks(IssueLinkSection.LinkSourceType.REMOTE, "Blog Links");
        assertEquals(2, blogLinks.size());
        assertEquals("My fantastic blog", blogLinks.get(0).getTitle());
        DeleteLinkConfirmationDialog deleteLinkConfirmationDialog = issueLinkSection.deleteLink(blogLinks.get(0));
        viewIssuePage = deleteLinkConfirmationDialog.cancel();
        issueLinkSection = viewIssuePage.getIssueLinkSection();

        blogLinks = issueLinkSection.getLinks(IssueLinkSection.LinkSourceType.REMOTE, "Blog Links");
        assertEquals(2, blogLinks.size());
    }

    @Test
    public void testViewRemoteIssueLinks()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        IssueLinkSection issueLinkSection = viewIssuePage.getIssueLinkSection().expandLinks();
        List<IssueLink> actualLinks = issueLinkSection.getLinksForSource(IssueLinkSection.LinkSourceType.REMOTE);

        ImmutableList.Builder<IssueLink> expectedLinks = ImmutableList.builder();
        expectedLinks.add(IssueLink.builder().relationship("Blog Links").title("My fantastic blog").url("http://myapp.mycompany.com/browse/BLOG-25").summary("Blog post about the fantastic nature of the blog").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).build());
        expectedLinks.add(IssueLink.builder().relationship("Blog Links").title("Yet Another Blog Post").url("http://myapp.mycompany.com/browse/BLOG-27").summary("Another test blog post").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).build());
        expectedLinks.add(IssueLink.builder().relationship("Tickets").title("Keyboard broken").url("http://myapp.helpdesk.com/browse/LP-77").summary("My wireless keyboard has no wires to connect to the computer").iconUrl("http://localhost:8090/jira/images/icons/bug.gif").build());
        expectedLinks.add(IssueLink.builder().relationship("Tickets").title("Mouse broken").url("http://myapp.helpdesk.com/browse/HELP-23").summary("My magic mouse doesn't have a button").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).build());
        assertListsEquals(expectedLinks.build(), actualLinks);
    }

    /**
     * Tests that a user with no "edit link" permission cannot delete a remote link.
     */
    @Test
    public void testDeleteRemoteIssueLinkWithoutEditLinkPermission()
    {
        jira.quickLogin("noaccessuser", "noaccessuser", DashboardPage.class);
        ViewIssuePage viewIssuePage = jira.goToViewIssue("GRL-1");
        String xsrfToken = viewIssuePage.getXsrfToken();
        long issueId = 10010L;
        long remoteIssueLinkId = 10100L;
        DeleteRemoteIssueLinkPage deleteRemoteIssueLinkPage = jira.goTo(DeleteRemoteIssueLinkPage.class, "GRL-1", issueId, remoteIssueLinkId, xsrfToken);
        List<String> errorMessages = deleteRemoteIssueLinkPage.getErrorMessages();
        assertEquals(1, errorMessages.size());
        assertEquals("You do not have permission to delete links in this project.", errorMessages.get(0));
    }

    /**
     * Tests that a remote link created with only the minimal fields filled in (i.e. title and url) will display
     * correctly.
     */
    @Test
    @Restore ("xml/TestLinkIssueWithMinimalRemoteIssueLinkFields.xml")
    public void testViewRemoteIssueLinksWithMinimalRemoteIssueLinkFields()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        IssueLinkSection issueLinkSection = viewIssuePage.getIssueLinkSection().expandLinks();
        List<IssueLink> actualLinks = issueLinkSection.getLinksForSource(IssueLinkSection.LinkSourceType.REMOTE);

        ImmutableList.Builder<IssueLink> expectedLinks = ImmutableList.builder();
        expectedLinks.add(IssueLink.builder().relationship("links to").title("COOL-111").url("http://www.mycompany.com/support?id=2").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).build());
        assertListsEquals(expectedLinks.build(), actualLinks);
    }

    @Test
    @Restore ("xml/TestLinkIssueWithIssueLinkingDisabled.xml")
    public void testDeleteRemoteIssueLinkWhileLinkingDisabled()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        String xsrfToken = viewIssuePage.getXsrfToken();
        long issueId = 10000L;
        long remoteIssueLinkId = 10006L;
        DeleteRemoteIssueLinkPage deleteRemoteIssueLinkPage = jira.goTo(DeleteRemoteIssueLinkPage.class, "HSP-1", issueId, remoteIssueLinkId, xsrfToken);
        List<String> errorMessages = deleteRemoteIssueLinkPage.getErrorMessages();
        assertEquals(1, errorMessages.size());
        assertEquals("Issue linking is currently disabled.", errorMessages.get(0));
    }

    /**
     * Tests the sorting order of the issue links.
     *
     * Links should appears in the following order:
     *
     * <pre>
     * blocker (mixed)
     *     - HSP-2 Critical improvement on project HSP
     *     - Alphabet (remote)
     * same name (local)
     *     - GRL-1 Gorilla needs to be fed
     *     - HSP-2 Critical improvement on project HSP
     * zzz (local)
     *     - GRL-1 Gorilla needs to be fed
     * Blog Links (remote)
     *     - BLOG 3 (com.companytype1) (Instance 1)
     *     - ALL (com.companytype1) (Instance 2)
     *     - BLOG 6 (com.companytype1) (Instance 2)
     *     - HELLO 3 (com.companytype1)
     *     - CAT 4  (com.companytype2)
     * Tickets (remote)
     *     - HELP-123 Need help
     * </pre>
     *
     */
    @Test
    @Restore ("xml/TestLinkIssueSortingOrder.xml")
    public void testIssueLinkSortOrder()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        IssueLinkSection issueLinkSection = viewIssuePage.getIssueLinkSection().expandLinks();
        List<IssueLink> actualLinks = issueLinkSection.getLinks();

        ImmutableList.Builder<IssueLink> expectedLinks = ImmutableList.builder();
        expectedLinks.add(IssueLink.builder().relationship("blocker (mixed)").title("HSP-2").url(generateIssueUrl("HSP-2")).summary("Critical improvement on project HSP").iconUrl(prependBaseUrl(ISSUE_IMAGE_IMPROVEMENT)).priorityIconUrl(prependBaseUrl(PRIORITY_IMAGE_CRITICAL)).status(STATUS_OPEN).build());
        expectedLinks.add(IssueLink.builder().relationship("blocker (mixed)").title("Alphabet (remote)").url("http://www.mycompany.com/").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).build());

        expectedLinks.add(IssueLink.builder().relationship("same name (local)").title("GRL-1").url(generateIssueUrl("GRL-1")).summary("Gorilla needs to be fed").iconUrl(prependBaseUrl(ISSUE_IMAGE_TASK)).priorityIconUrl(prependBaseUrl(PRIORITY_IMAGE_MAJOR)).status(STATUS_OPEN).build());
        expectedLinks.add(IssueLink.builder().relationship("same name (local)").title("HSP-2").url(generateIssueUrl("HSP-2")).summary("Critical improvement on project HSP").iconUrl(prependBaseUrl(ISSUE_IMAGE_IMPROVEMENT)).priorityIconUrl(prependBaseUrl(PRIORITY_IMAGE_CRITICAL)).status(STATUS_OPEN).build());

        expectedLinks.add(IssueLink.builder().relationship("zzz (local)").title("GRL-1").url(generateIssueUrl("GRL-1")).summary("Gorilla needs to be fed").iconUrl(prependBaseUrl(ISSUE_IMAGE_TASK)).priorityIconUrl(prependBaseUrl(PRIORITY_IMAGE_MAJOR)).status(STATUS_OPEN).build());

        expectedLinks.add(IssueLink.builder().relationship("Blog Links (remote)").title("BLOG 3").url("http://www.mycompany.com/").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).summary("(com.companytype1) (Instance 1)").build());
        expectedLinks.add(IssueLink.builder().relationship("Blog Links (remote)").title("ALL").url("http://www.mycompany.com/").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).summary("(com.companytype1) (Instance 2)").build());
        expectedLinks.add(IssueLink.builder().relationship("Blog Links (remote)").title("BLOG 6").url("http://www.mycompany.com/").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).summary("(com.companytype1) (Instance 2)").build());
        expectedLinks.add(IssueLink.builder().relationship("Blog Links (remote)").title("HELLO 3").url("http://www.mycompany.com/").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).summary("(com.companytype1)").build());
        expectedLinks.add(IssueLink.builder().relationship("Blog Links (remote)").title("CAT 4").url("http://www.mycompany.com/").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).summary("(com.companytype2)").build());

        expectedLinks.add(IssueLink.builder().relationship("Tickets (remote)").title("HELP-123").url("http://www.mycompany.com/").iconUrl(prependBaseUrl(GENERIC_LINK_ICON)).summary("Need help").build());

        assertListsEquals(expectedLinks.build(), actualLinks);
    }

    @Test
    public void testCanCreateWebLink()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        pageBinder.bind(LinkIssueDialog.class, "HSP-5")
                .gotoWebLink()
                .url("http://example.org/")
                .linkText("Example")
                .submit();

        final List<IssueLink> issueLinks = viewIssuePage.getIssueLinkSection().getLinks();
        final IssueLink expectedIssueLink = IssueLink.builder()
                .relationship("links to")
                .title("Example")
                .url("http://example.org/")
                .iconUrl(prependBaseUrl(GENERIC_LINK_ICON))
                .build();
        assertListsEquals(Collections.singletonList(expectedIssueLink), issueLinks);
    }

    @Test
    public void testCanCreateWebLinkWithComment()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        pageBinder.bind(LinkIssueDialog.class, "HSP-5")
                .gotoWebLink()
                .url("http://example.org/")
                .linkText("Example")
                .comment("Example comment")
                .submit();

        final List<IssueLink> issueLinks = viewIssuePage.getIssueLinkSection().getLinks();
        final IssueLink expectedIssueLink = IssueLink.builder()
                .relationship("links to")
                .title("Example")
                .url("http://example.org/")
                .iconUrl(prependBaseUrl(GENERIC_LINK_ICON))
                .build();
        assertListsEquals(Collections.singletonList(expectedIssueLink), issueLinks);
        assertThat(viewIssuePage.getComments(), hasItem(new Comment("Example comment")));
    }

    @Test
    public void testCanCreateWebLinkWithIconUrl() throws IOException
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        final HttpServer faviconServer = createSingleFileHttpServer(9999, "favicon.ico", "image/x-icon");
        faviconServer.start();
        try
        {
            final String hostUrl = "http://localhost:9999";
            viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
            pageBinder.bind(LinkIssueDialog.class, "HSP-5")
                    .gotoWebLink()
                    .url(hostUrl + "/foo")
                    .linkText("Foo Page")
                    .submit();

            final List<IssueLink> issueLinks = viewIssuePage.getIssueLinkSection().getLinks();
            final IssueLink expectedIssueLink = IssueLink.builder()
                    .relationship("links to")
                    .title("Foo Page")
                    .url(hostUrl + "/foo")
                    .iconUrl(hostUrl + "/favicon.ico")
                    .build();
            assertListsEquals(Collections.singletonList(expectedIssueLink), issueLinks);
        }
        finally
        {
            faviconServer.stop(0);
        }
    }

    @Test
    public void testCanCreateWebLinkWithMissingIcon() throws IOException
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        final String hostUrl = "http://localhost:0";
        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        pageBinder.bind(LinkIssueDialog.class, "HSP-5")
                .gotoWebLink()
                .url(hostUrl + "/foo")
                .linkText("Foo Page")
                .submit();

        final List<IssueLink> issueLinks = viewIssuePage.getIssueLinkSection().getLinks();
        final IssueLink expectedIssueLink = IssueLink.builder()
                .relationship("links to")
                .title("Foo Page")
                .url(hostUrl + "/foo")
                .iconUrl(prependBaseUrl(GENERIC_LINK_ICON))
                .build();
        assertListsEquals(Collections.singletonList(expectedIssueLink), issueLinks);
    }

    @Test
    public void testCannotCreateWebLinkWithEmptyUrl()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        final WebLinkSection webLinkSection = pageBinder.bind(LinkIssueDialog.class, "HSP-5").gotoWebLink();

        webLinkSection.url("")
                      .linkText("Example")
                      .submitExpectingError();

        assertTrue(webLinkSection.errorsPresent());
    }

    @Test
    public void testCannotCreateWebLinkWithEmptyLinkText()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        final WebLinkSection webLinkSection = pageBinder.bind(LinkIssueDialog.class, "HSP-5").gotoWebLink();

        webLinkSection.url("http://example.org")
                      .linkText("")
                      .submitExpectingError();

        assertTrue(webLinkSection.errorsPresent());
    }

    @Test
    public void testRemoveWebLink()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        viewIssuePage = pageBinder.bind(LinkIssueDialog.class, "HSP-5")
                .gotoWebLink()
                .url("http://example.org")
                .linkText("Example")
                .comment("Example comment")
                .submit();

        IssueLinkSection issueLinkSection = viewIssuePage.getIssueLinkSection();
        IssueLink issueLink = Iterables.getOnlyElement(issueLinkSection.getLinks(IssueLinkSection.LinkSourceType.REMOTE, WEB_LINK_RELATIONSHIP_TEXT));
        viewIssuePage = issueLinkSection.deleteLink(issueLink).confirm();
        assertEquals(0, Iterables.size(viewIssuePage.getIssueLinkSection().getLinks(IssueLinkSection.LinkSourceType.REMOTE, WEB_LINK_RELATIONSHIP_TEXT)));
    }

    /**
     * Tests that it is possible to create duplicate web links.
     */
    @Test
    public void testDuplicateWebLinkCreation()
    {
        final String TEST_URL = "http://example.org/";
        final String TEST_LINK_TEXT = "Example";

        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        viewIssuePage = pageBinder.bind(LinkIssueDialog.class, "HSP-5")
                .gotoWebLink()
                .url(TEST_URL)
                .linkText(TEST_LINK_TEXT)
                .submit();

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        viewIssuePage = pageBinder.bind(LinkIssueDialog.class, "HSP-5")
                .gotoWebLink()
                .url(TEST_URL)
                .linkText(TEST_LINK_TEXT)
                .submit();

        List<IssueLink> actualIssueLinks = viewIssuePage.getIssueLinkSection().getLinks(IssueLinkSection.LinkSourceType.REMOTE, WEB_LINK_RELATIONSHIP_TEXT);
        IssueLink issueLink = IssueLink.builder()
                .url(TEST_URL)
                .title(TEST_LINK_TEXT)
                .relationship(WEB_LINK_RELATIONSHIP_TEXT)
                .iconUrl(prependBaseUrl(GENERIC_LINK_ICON))
                .build();
        List<IssueLink> expectedIssueLinks = Arrays.asList(issueLink, issueLink);
        assertListsEquals(expectedIssueLinks, actualIssueLinks);
    }

    @Test
    public void testInitiallyHiddenLinksAreHidden()
    {
        assertEquals(5, jira.gotoLoginPage()
                            .loginAsSysAdmin(ViewIssuePage.class, "HSP-1")
                            .getIssueLinkSection()
                            .getLinks()
                            .size());
    }

    @Test
    public void testInitiallyHiddenLinksCanBeExpanded()
    {
        assertEquals(10, jira.gotoLoginPage()
                            .loginAsSysAdmin(ViewIssuePage.class, "HSP-1")
                            .getIssueLinkSection()
                            .expandLinks()
                            .getLinks()
                            .size());
    }

    @Test
    public void testConfluenceLinkDialogIsHidden() throws Exception
    {
        // Since there are no Confluence Application Links, we should not see the Confluence tab menu link
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        final LinkIssueDialog linkIssueDialog = pageBinder.bind(LinkIssueDialog.class, "HSP-5");

        assertFalse(linkIssueDialog.hasConfluenceLink());
    }

    @Test
    public void testConfluenceLinkDialogHasIssueId() throws Exception
    {
        String appLinkId = backdoor.getTestkit().applicationLink().addApplicationLink("confluence", "Confluence", "http://localhost:1990/confluence");
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        final LinkIssueDialog linkIssueDialog = pageBinder.bind(LinkIssueDialog.class, "HSP-5");

        assertTrue(linkIssueDialog.hasConfluenceLink());

        LinkConfluenceSection linkConfluence = linkIssueDialog.gotoConfluenceLink();
        assertEquals(linkConfluence.getForm().find(By.cssSelector("#confluence-page-link input[name=id]")).getAttribute("value"), backdoor.issues().getIssue("HSP-5").id);
    }

    @Test
    public void testRemoteJiraLinkDialogIsPresent() throws Exception
    {
        // The JIRA Issue tab menu link should always be shown, even when there are no JIRA Application Links,
        // as it is also used to create local issue links
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-5");

        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE);
        final LinkIssueDialog linkIssueDialog = pageBinder.bind(LinkIssueDialog.class, "HSP-5");

        assertTrue(linkIssueDialog.hasJiraLink());
    }

    private static void assertListsEquals(List<IssueLink> expectedList, List<IssueLink> actualList)
    {
        assertEquals(expectedList.size(), actualList.size());
        for (int i = 0, n = expectedList.size(); i < n; i++)
        {
            IssueLink expected = expectedList.get(i);
            IssueLink actual = actualList.get(i);
            assertIssueLinksEquals(expected, actual);
        }
    }

    private static void assertIssueLinksEquals(IssueLink expected, IssueLink actual)
    {
        if (expected.getUrl() != null ? !expected.getUrl().equals(actual.getUrl()) : actual.getUrl() != null)
        {
            fail("URLs differ. Expected: " + expected.getUrl() + " Actual: " + actual.getUrl());
        }
        if (expected.getSummary() != null ? !expected.getSummary().equals(actual.getSummary()) : actual.getSummary() != null)
        {
            fail("Summaries differ. Expected: " + expected.getSummary() + " Actual: " + actual.getSummary());
        }
        if (expected.getIconUrl() != null ? !expected.getIconUrl().equals(actual.getIconUrl()) : actual.getIconUrl() != null)
        {
            fail("Icon URLs differ. Expected: " + expected.getIconUrl() + " Actual: " + actual.getIconUrl());
        }
        if (expected.getPriorityIconUrl() != null ? !expected.getPriorityIconUrl().equals(actual.getPriorityIconUrl()) : actual.getPriorityIconUrl() != null)
        {
            fail("Priority icon URLs differ. Expected: " + expected.getPriorityIconUrl() + " Actual: " + actual.getPriorityIconUrl());
        }
        if (expected.getRelationship() != null ? !expected.getRelationship().equals(actual.getRelationship()) : actual.getRelationship() != null)
        {
            fail("Relationships differ. Expected: " + expected.getRelationship() + " Actual: " + actual.getRelationship());
        }
        if (expected.getStatus() != null ? !expected.getStatus().equals(actual.getStatus()) : actual.getStatus() != null)
        {
            fail("Status icon URLs differ. Expected: " + expected.getStatus() + " Actual: " + actual.getStatus());
        }
        if (expected.getTitle() != null ? !expected.getTitle().equals(actual.getTitle()) : actual.getTitle() != null)
        {
            fail("Titles differ. Expected: " + expected.getTitle() + " Actual: " + actual.getTitle());
        }

        // for the purpose of the test we do not need to worry about the delete URL which includes the anti-XSRF token
        /*if (expected.getDeleteUrl() != null ? !expected.getDeleteUrl().equals(actual.getDeleteUrl()) : actual.getDeleteUrl() != null)
        {
            fail("Delete URLs differ. Expected: " + expected.getDeleteUrl() + " Actual: " + actual.getDeleteUrl());
        }*/
    }

    private String prependBaseUrl(String url)
    {
        return jira.getProductInstance().getBaseUrl() + url;
    }

    private String generateIssueUrl(String issueKey)
    {
        return prependBaseUrl("/browse/" + issueKey);
    }

    private HttpServer createSingleFileHttpServer(final int port, final String file, final String contentType) throws IOException
    {
        InetSocketAddress addr = new InetSocketAddress(port);
        HttpServer server = HttpServer.create(addr, 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/" + file, new HttpHandler()
        {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException
            {
                final InputStream fileStream = getClass().getClassLoader().getResourceAsStream(file);

                if (fileStream == null)
                {
                    httpExchange.sendResponseHeaders(404, 0);
                    httpExchange.close();
                    return;
                }

                try
                {
                    if (!httpExchange.getRequestMethod().equalsIgnoreCase("get"))
                    {
                        httpExchange.sendResponseHeaders(404, 0);
                        httpExchange.close();
                        return;
                    }

                    final byte[] fileBytes = IOUtils.toByteArray(fileStream);

                    final Headers responseHeaders = httpExchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", contentType);
                    httpExchange.sendResponseHeaders(200, fileBytes.length);
                    final OutputStream responseBody = httpExchange.getResponseBody();
                    try
                    {
                            responseBody.write(fileBytes);
                    }
                    finally
                    {
                        responseBody.close();
                    }
                }
                finally
                {
                    fileStream.close();
                }
            }
        });

        return server;
    }
}
