package com.atlassian.jira.issue.util;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestIssueOperationsBarUtil extends MockControllerTestCase
{
    private ApplicationUser user;
    private SimpleLinkManager linkManager;
    private PermissionManager permissionManager;
    private IssueManager issueManager;
    private ApplicationProperties appProps;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("admin");

        linkManager = getMock(SimpleLinkManager.class);
        permissionManager = getMock(PermissionManager.class);
        issueManager = getMock(IssueManager.class);
        appProps = getMock(ApplicationProperties.class);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        linkManager = null;
        appProps = null;
    }

    @Test
    public void testGetGroupsForEmpty()
    {
        expect(linkManager.getSectionsForLocation("view.issue.opsbar", user.getDirectoryUser(), null)).andReturn(Collections.<SimpleLinkSection>emptyList());

        final IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null);
        replay(linkManager, appProps);


        List<SimpleLinkSection> list = opsBarUtil.getGroups();
        assertTrue(list.isEmpty());

        // It should be cached now.  Shouldn't hit link manager
        list = opsBarUtil.getGroups();
        assertTrue(list.isEmpty());

        verify(linkManager, appProps);
    }

    @Test
    public void testGetGroupsForNonEmpty()
    {
//        final List<SimpleLinkImpl> links = CollectionBuilder.list(createLink("1"), createLink("2"), createLink("3"));

        final List<SimpleLinkSection> groups = CollectionBuilder.list(createSection("1"), createSection("2"), createSection("3"));

        expect(linkManager.getSectionsForLocation("view.issue.opsbar", user.getDirectoryUser(), null)).andReturn(groups);

        final IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null);
        replay(linkManager, appProps);


        List<SimpleLinkSection> list = opsBarUtil.getGroups();
        assertEquals(groups, list);

        // It should be cached now.  Shouldn't hit link manager
        list = opsBarUtil.getGroups();
        assertEquals(groups, list);

        verify(linkManager, appProps);
    }

    @Test
    public void testGetLinkLabel()
    {
        final SimpleLinkImpl link1 = new SimpleLinkImpl("id", "label", "title", "", "", null, "", "");
        final SimpleLinkImpl link2 = new SimpleLinkImpl("id", "1234567890123456789012345", "title", "", "", null, "", "");
        final SimpleLinkImpl link3 = new SimpleLinkImpl("id", "12345678901234567890123456", "title", "", "", null, "", "");
        final SimpleLinkImpl link4 = new SimpleLinkImpl("id", "123456789012345678901234567890", "title", "", "", null, "", "");

        final IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null);
        replay(linkManager, appProps);

        assertEquals("label", opsBarUtil.getLabelForLink(link1));
        assertEquals("1234567890123456789012345", opsBarUtil.getLabelForLink(link2));
        assertEquals("1234567890123456789012...", opsBarUtil.getLabelForLink(link3));
        assertEquals("1234567890123456789012...", opsBarUtil.getLabelForLink(link4));

        verify(linkManager, appProps);
    }

    @Test
    public void testGetLinkTitle()
    {
        final SimpleLinkImpl link1 = new SimpleLinkImpl("id", "label", "title", "", "", null, "", "");
        final SimpleLinkImpl link2 = new SimpleLinkImpl("id", "1234567890123456789012345", null, "", "", null, "", "");
        final SimpleLinkImpl link3 = new SimpleLinkImpl("id", "12345678901234567890123456", "title", "", "", null, "", "");
        final SimpleLinkImpl link4 = new SimpleLinkImpl("id", "123456789012345678901234567890", null, "", "", null, "", "");

        final IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null);
        replay(linkManager, appProps);

        assertEquals("title", opsBarUtil.getTitleForLink(link1));
        assertEquals("", opsBarUtil.getTitleForLink(link2));
        assertEquals("title", opsBarUtil.getTitleForLink(link3));
        assertEquals("123456789012345678901234567890", opsBarUtil.getTitleForLink(link4));

        verify(linkManager, appProps);
    }

    @Test
    public void testGetPromotedLinksForGroup()
    {
        final SimpleLinkSection group = createSection("group");

        final SimpleLink link1 = createLink("1");
        final SimpleLink link2 = createLink("2");
        final SimpleLink link3 = createLink("3");
        final List<SimpleLink> links1 = CollectionBuilder.list(link1, link2, link3);

        final SimpleLinkSection section1 = createSection("1");
        final SimpleLinkSection section2 = createSection("2");
        final SimpleLinkSection section3 = createSection("3");
        final SimpleLinkSection section4 = createSection("4");
        final List<SimpleLinkSection> sections = CollectionBuilder.list(section1, section2, section3, section4);

        expect(appProps.getDefaultBackedString("ops.bar.group.size.id-group")).andReturn(null);
        expect(appProps.getDefaultBackedString("ops.bar.group.size")).andReturn(null);

        expect(linkManager.getSectionsForLocation(group.getId(), user.getDirectoryUser(), null)).andReturn(sections);
        expect(linkManager.getLinksForSection(section1.getId(), user.getDirectoryUser(), null)).andReturn(links1);

        final IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null);
        replay(linkManager, appProps);


        List<SimpleLink> list = opsBarUtil.getPromotedLinks(group);
        assertEquals(CollectionBuilder.list(link1), list);

        // It should be cached now.  Shouldn't hit link manager
        list = opsBarUtil.getPromotedLinks(group);
        assertEquals(CollectionBuilder.list(link1), list);

        verify(linkManager, appProps);
    }

    @Test
    public void testGetPromotedLinksForGroupMultipleSections()
    {
        final SimpleLinkSection group = createSection("group");

        final SimpleLink link1 = createLink("1");
        final SimpleLink link2 = createLink("2");
        final SimpleLink link3 = createLink("3");
        final List<SimpleLink> links1 = CollectionBuilder.list(link1, link2, link3);

        final SimpleLink link11 = createLink("11");
        final SimpleLink link12 = createLink("12");
        final SimpleLink link13 = createLink("13");
        final List<SimpleLink> links2 = CollectionBuilder.list(link11, link12, link13);

        final SimpleLinkSection section1 = createSection("1");
        final SimpleLinkSection section2 = createSection("2");
        final SimpleLinkSection section3 = createSection("3");
        final SimpleLinkSection section4 = createSection("4");
        final List<SimpleLinkSection> sections = CollectionBuilder.list(section1, section2, section3, section4);

        expect(appProps.getDefaultBackedString("ops.bar.group.size.id-group")).andReturn(null);
        expect(appProps.getDefaultBackedString("ops.bar.group.size")).andReturn("5");

        expect(linkManager.getSectionsForLocation(group.getId(), user.getDirectoryUser(), null)).andReturn(sections);
        expect(linkManager.getLinksForSection(section1.getId(), user.getDirectoryUser(), null)).andReturn(links1);
        expect(linkManager.getLinksForSection(section2.getId(), user.getDirectoryUser(), null)).andReturn(links2);

        final IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null);
        replay(linkManager, appProps);


        List<SimpleLink> list = opsBarUtil.getPromotedLinks(group);
        assertEquals(CollectionBuilder.list(link1, link2, link3, link11, link12), list);

        // It should be cached now.  Shouldn't hit link manager
        list = opsBarUtil.getPromotedLinks(group);
        assertEquals(CollectionBuilder.list(link1, link2, link3, link11, link12), list);

        verify(linkManager, appProps);
    }

    @Test
    public void testGetNonPromotedLinksForSectionNone()
    {
        final SimpleLinkSection group = createSection("group");

        final SimpleLink link1 = createLink("1");
        final SimpleLink link2 = createLink("2");
        final SimpleLink link3 = createLink("3");
        final List<SimpleLink> links1 = CollectionBuilder.newBuilder(link1, link2, link3).asMutableList();

        final SimpleLink link11 = createLink("11");
        final SimpleLink link12 = new SimpleLinkImpl("conjoined1", "conjoined1-label", "conjoined1-title", null,
                "conjoined", null, "url1", null);
        final SimpleLink link13 = createLink("13");
        final List<SimpleLink> links2 = CollectionBuilder.list(link11, link12, link13);

        final SimpleLinkSection section1 = createSection("1");
        final SimpleLinkSection section2 = createSection("2");
        final SimpleLinkSection section3 = createSection("3");
        final SimpleLinkSection section4 = createSection("4");
        final List<SimpleLinkSection> sections = CollectionBuilder.list(section1, section2, section3, section4);

        expect(linkManager.getLinksForSection(section1.getId(), user.getDirectoryUser(), null)).andReturn(links1);

        expect(appProps.getDefaultBackedString("ops.bar.group.size.id-group")).andReturn(null);
        // one less, but should return 5
        expect(appProps.getDefaultBackedString("ops.bar.group.size")).andReturn("4");

        expect(linkManager.getSectionsForLocation(group.getId(), user.getDirectoryUser(), null)).andReturn(sections);
        expect(linkManager.getLinksForSection(section2.getId(), user.getDirectoryUser(), null)).andReturn(links2);

        final IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null);
        replay(linkManager, appProps);


        List<SimpleLink> list = opsBarUtil.getNonPromotedLinksForSection(group, section1);
        assertEquals(0, list.size());

        // It should be cached now.  Shouldn't hit link manager
        list = opsBarUtil.getNonPromotedLinksForSection(group, section1);
        assertEquals(0, list.size());

        verify(linkManager, appProps);
    }

    @Test
    public void testGetNonPromotedLinksForSection()
    {
        final SimpleLinkSection group = createSection("group");

        final SimpleLink link1 = createLink("1");
        final SimpleLink link2 = createLink("2");
        final SimpleLink link3 = createLink("3");
        final List<SimpleLink> links1 = CollectionBuilder.newBuilder(link1, link2, link3).asMutableList();

        final SimpleLinkSection section1 = createSection("1");
        final SimpleLinkSection section2 = createSection("2");
        final SimpleLinkSection section3 = createSection("3");
        final SimpleLinkSection section4 = createSection("4");
        final List<SimpleLinkSection> sections = CollectionBuilder.list(section1, section2, section3, section4);

        expect(linkManager.getLinksForSection(section1.getId(), user.getDirectoryUser(), null)).andReturn(links1);

        expect(appProps.getDefaultBackedString("ops.bar.group.size.id-group")).andReturn("1");

        expect(linkManager.getSectionsForLocation(group.getId(), user.getDirectoryUser(), null)).andReturn(sections);

        final IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null);
        replay(linkManager, appProps);


        List<SimpleLink> list = opsBarUtil.getNonPromotedLinksForSection(group, section1);
        assertEquals(CollectionBuilder.list(link2, link3), list);

        // It should be cached now.  Shouldn't hit link manager
        list = opsBarUtil.getNonPromotedLinksForSection(group, section1);
        assertEquals(CollectionBuilder.list(link2, link3), list);

        verify(linkManager, appProps);
    }

    @Test
    public void testGetPrimaryOperationLinks()
    {
        testGetPrimaryOperationLinks(true, true, user, new GetPrimaryOperationLinksAsserter()
        {
            @Override
            public void doAssert(List<SimpleLink> actualLinks, SimpleLink editLink, SimpleLink commentLink, SimpleLink loginLink)
            {
                assertEquals(CollectionBuilder.list(editLink, commentLink), actualLinks);
            }
        });

        testGetPrimaryOperationLinks(true, false, user, new GetPrimaryOperationLinksAsserter()
        {
            @Override
            public void doAssert(List<SimpleLink> actualLinks, SimpleLink editLink, SimpleLink commentLink, SimpleLink loginLink)
            {
                assertEquals(CollectionBuilder.list(editLink), actualLinks);
            }
        });

        testGetPrimaryOperationLinks(false, true, user, new GetPrimaryOperationLinksAsserter()
        {
            @Override
            public void doAssert(List<SimpleLink> actualLinks, SimpleLink editLink, SimpleLink commentLink, SimpleLink loginLink)
            {
                assertEquals(CollectionBuilder.list(commentLink), actualLinks);
            }
        });

        testGetPrimaryOperationLinks(false, false, null, new GetPrimaryOperationLinksAsserter()
        {
            @Override
            public void doAssert(List<SimpleLink> actualLinks, SimpleLink editLink, SimpleLink commentLink, SimpleLink loginLink)
            {
                assertEquals(CollectionBuilder.list(loginLink), actualLinks);
            }
        });
    }

    private void testGetPrimaryOperationLinks(boolean expectedIsEditable, boolean expectedHasCommentPermission, ApplicationUser user, GetPrimaryOperationLinksAsserter asserter)
    {
        SimpleLink editLink = new SimpleLinkImpl("edit-issue", null, null, null, null, null, "", null);
        SimpleLink commentLink = new SimpleLinkImpl("comment-issue", null, null, null, null, null, "", null);
        final SimpleLink loginLink = new SimpleLinkImpl("ops-login-lnk", null, null, null, null, null, "", null);

        SimpleLink link1 = createLink("1");
        SimpleLink link2 = createLink("2");

        reset(linkManager, issueManager, permissionManager);

        User directoryUser = (user == null) ? null : user.getDirectoryUser();

        if (!expectedIsEditable && !expectedHasCommentPermission)
        {
            expect(linkManager.getSectionsForLocation("view.issue.opsbar", directoryUser, null)).andReturn(Collections.<SimpleLinkSection>emptyList());
        }
        else
        {
            expect(linkManager.getLinksForSection("operations-top-level", directoryUser, null)).andReturn(CollectionBuilder.list(editLink, commentLink, link1, link2));
        }

        expect(issueManager.isEditable(null, directoryUser)).andReturn(expectedIsEditable).atLeastOnce();
        expect(permissionManager.hasPermission(Permissions.COMMENT_ISSUE, (Issue) null, user)).andReturn(expectedHasCommentPermission);

        replay(linkManager, issueManager, permissionManager);

        IssueOperationsBarUtil opsBarUtil = new IssueOperationsBarUtil(null, user, linkManager, appProps, issueManager, permissionManager, null)
        {
            @Override
            SimpleLink createLoginLink()
            {
                return loginLink;
            }
        };

        List<SimpleLink> links = opsBarUtil.getPrimaryOperationLinks(null);

        asserter.doAssert(links, editLink, commentLink, loginLink);

        verify(linkManager, issueManager, permissionManager);
    }

    private interface GetPrimaryOperationLinksAsserter
    {
        void doAssert(List<SimpleLink> actualLinks, SimpleLink editLink, SimpleLink commentLink, SimpleLink loginLink);
    }

    private SimpleLink createLink(String id)
    {
        return new SimpleLinkImpl("id-" + id, "label-" + id, "title" + id, null, "style" + id, null,  "a url -" + id, null);
    }

    private SimpleLinkSection createSection(String id)
    {
        return new SimpleLinkSectionImpl("id-" + id, "label-" + id, "title" + id, null, "style" + id, null);
    }
}
