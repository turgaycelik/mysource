package com.atlassian.jira.bc.issue.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.link.LinkCollectionImpl;
import com.atlassian.jira.issue.link.MockIssueLinkType;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestDefaultIssueLinkService extends MockControllerTestCase
{
    private static final String ABC_1 = "ABC-1";
    private static final Long ISSUE_ID = 1L;
    private IssueManager issueManager;
    private I18nHelper.BeanFactory beanFactory;
    private PermissionManager permissionManager;
    private MockUser user;
    private MockI18nHelper i18nHelper;
    private MockIssue mockIssue;
    private IssueLinkTypeManager issueLinkTypeManager;
    private IssueLinkManager issueLinkManager;
    private MockApplicationProperties applicationProperties;

    @Before
    public void setUp()
    {
        issueManager = createMock(IssueManager.class);
        permissionManager = createMock(PermissionManager.class);
        issueLinkManager = createMock(IssueLinkManager.class);
        issueLinkTypeManager = createMock(IssueLinkTypeManager.class);

        user = new MockUser("fred");
        i18nHelper = new MockI18nHelper();
        beanFactory = i18nHelper.factory();

        addObjectInstance(i18nHelper);
        addObjectInstance(beanFactory);

        mockIssue = new MockIssue(ISSUE_ID);
        applicationProperties = new MockApplicationProperties();
    }

    private void setupIssueMocks(final MockIssue issue, final int permissionsId, final boolean hasPermission)
    {
        expect(issueManager.getIssueObject(ABC_1)).andStubReturn(issue);
        if (issue != null)
        {
            expect(permissionManager.hasPermission(permissionsId, mockIssue, user)).andReturn(hasPermission);
        }
    }

    private ArrayList<IssueLinkType> setupdefaultLinkTypes()
    {
        return Lists.<IssueLinkType>newArrayList(
                new MockIssueLinkType(1, "Duplicate", "duplicates", "duplicated by", "freestyling"),
                new MockIssueLinkType(1, "Blocker", "blocks", "blocked by", "freestyling"),
                new MockIssueLinkType(1, "Subtask", "subtask", "subtasked by", "jira_subtaks")// <<-- this makes it a system link.  weird eh?
        );
    }

    @Test
    public void testAddValidation_NoIssuePermission()
    {
        setupIssueMocks(mockIssue, Permissions.LINK_ISSUE, false);

        expect(issueLinkTypeManager.getIssueLinkTypes()).andReturn(setupdefaultLinkTypes());

        DefaultIssueLinkService linkService = instantiate(DefaultIssueLinkService.class);
        IssueLinkService.AddIssueLinkValidationResult result = linkService.validateAddIssueLinks(user, mockIssue, "duplicates", Lists.newArrayList(ABC_1));
        Collection<String> errorMessages = result.getErrorCollection().getErrorMessages();
        assertThat(errorMessages, hasItem("issuelinking.service.error.issue.no.permission [null]"));
    }

    @Test
    public void testAddValidation_NoLinkTypeDefined()
    {
        setupIssueMocks(null, Permissions.LINK_ISSUE, true);

        expect(issueLinkTypeManager.getIssueLinkTypes()).andReturn(setupdefaultLinkTypes());

        DefaultIssueLinkService linkService = instantiate(DefaultIssueLinkService.class);
        IssueLinkService.AddIssueLinkValidationResult result = linkService.validateAddIssueLinks(user, mockIssue, "nonexistentlink", Lists.newArrayList(ABC_1));
        assertThat(result.getErrorCollection().getErrorMessages(), hasItem("issuelinking.service.error.invalid.link.name [nonexistentlink]"));
    }

    @Test
    public void testAddValidation_SystemLinkType()
    {
        setupIssueMocks(mockIssue, Permissions.LINK_ISSUE, true);

        expect(issueLinkTypeManager.getIssueLinkTypes()).andReturn(setupdefaultLinkTypes());

        DefaultIssueLinkService linkService = instantiate(DefaultIssueLinkService.class);
        IssueLinkService.AddIssueLinkValidationResult result = linkService.validateAddIssueLinks(user, mockIssue, "subtask", Lists.newArrayList(ABC_1));
        Collection<String> errorMessages = result.getErrorCollection().getErrorMessages();
        assertThat(errorMessages, hasItem("issuelinking.service.error.invalid.link.type [Subtask]"));
    }

    @Test
    public void testAddValidation_NoKeysDefined()
    {
        setupIssueMocks(mockIssue, Permissions.LINK_ISSUE, true);

        expect(issueLinkTypeManager.getIssueLinkTypes()).andReturn(setupdefaultLinkTypes());

        DefaultIssueLinkService linkService = instantiate(DefaultIssueLinkService.class);
        IssueLinkService.AddIssueLinkValidationResult result = linkService.validateAddIssueLinks(user, mockIssue, "blocks", Collections.<String>emptyList());
        assertTrue(result.getErrorCollection().getErrorMessages().contains("issuelinking.service.error.must.provide.issue.links"));
    }

    @Test
    public void testAddValidation()
    {
        setupIssueMocks(mockIssue, Permissions.LINK_ISSUE, true);

        expect(issueLinkTypeManager.getIssueLinkTypes()).andReturn(setupdefaultLinkTypes());

        DefaultIssueLinkService linkService = instantiate(DefaultIssueLinkService.class);

        ArrayList<String> linkKeys = Lists.newArrayList(ABC_1);
        IssueLinkService.AddIssueLinkValidationResult result = linkService.validateAddIssueLinks(user, mockIssue, "blocks", linkKeys);

        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertEquals(ISSUE_ID, result.getIssue().getId());
        assertEquals(user, result.getUser());
        assertEquals("blocks", result.getLinkName());
        assertEquals(linkKeys, result.getLinkKeys());
    }

    @Test
    public void testGetLinks_NoPermission()
    {
        setupIssueMocks(mockIssue, Permissions.BROWSE, false);

        DefaultIssueLinkService linkService = instantiate(DefaultIssueLinkService.class);

        IssueLinkService.IssueLinkResult result = linkService.getIssueLinks(user, mockIssue);

        assertEquals(false, result.isValid());
        assertThat(result.getErrorCollection().getErrorMessages(), hasItem("issuelinking.service.error.issue.no.permission [null]"));
    }


    @Test
    public void testGetLinks()
    {
        setupIssueMocks(mockIssue, Permissions.BROWSE, true);

        Map<String, List<Issue>> outward = Collections.emptyMap();
        Map<String, List<Issue>> inward = Collections.emptyMap();
        Set<IssueLinkType> linkTypes = Collections.emptySet();
        Long id = mockIssue.getId();
        LinkCollection linkCollection = new LinkCollectionImpl(id, linkTypes, outward, inward, user, applicationProperties);

        expect(issueLinkManager.getLinkCollection(mockIssue, user, true)).andReturn(linkCollection);

        DefaultIssueLinkService linkService = instantiate(DefaultIssueLinkService.class);

        IssueLinkService.IssueLinkResult result = linkService.getIssueLinks(user, mockIssue);

        assertEquals(true, result.isValid());
        assertNotNull(result.getLinkCollection());
    }

    @Test
    public void testGetEvenSystemLinks()
    {
        setupIssueMocks(mockIssue, Permissions.BROWSE, true);

        Map<String, List<Issue>> outward = Collections.emptyMap();
        Map<String, List<Issue>> inward = Collections.emptyMap();
        Set<IssueLinkType> linkTypes = Collections.emptySet();
        Long id = mockIssue.getId();
        LinkCollection linkCollection = new LinkCollectionImpl(id, linkTypes, outward, inward, user, applicationProperties);

        // we expect the delegate call to the manager to preserve the system link flag
        expect(issueLinkManager.getLinkCollection(mockIssue, user, false)).andReturn(linkCollection);

        DefaultIssueLinkService linkService = instantiate(DefaultIssueLinkService.class);

        IssueLinkService.IssueLinkResult result = linkService.getIssueLinks(user, mockIssue, false);

        assertEquals(true, result.isValid());
        assertNotNull(result.getLinkCollection());
    }
}
