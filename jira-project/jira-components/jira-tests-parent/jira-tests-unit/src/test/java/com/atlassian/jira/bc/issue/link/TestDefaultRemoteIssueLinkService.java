package com.atlassian.jira.bc.issue.link;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.CreateValidationResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.DeleteByGlobalIdValidationResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.DeleteValidationResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.RemoteIssueLinkListResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.RemoteIssueLinkResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.UpdateValidationResult;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.GetException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.issue.link.TestDefaultRemoteIssueLinkManager;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link DefaultRemoteIssueLinkService}.
 *
 * @since v5.0
 */
@RunWith(ListeningMockitoRunner.class)
public class TestDefaultRemoteIssueLinkService
{
    private static final Long ISSUE_ID_THAT_EXISTS = 10000L;
    private static final Long ISSUE_ID_THAT_EXISTS1 = 10001L;
    private static final Long ISSUE_ID_THAT_EXISTS2 = 10002L;
    private static final Long ISSUE_ID_THAT_DOES_NOT_EXIST = 10100L;
    private static final MockIssue ISSUE_THAT_EXISTS = new MockIssue(ISSUE_ID_THAT_EXISTS);
    private static final String BLANK = "    ";
    private static final String GLOBAL_ID = "a-global-id";
    private static final String GLOBAL_ID2 = "another-global-id";
    private static final String INVALID_URI = "this is not a valid URI";

    private DefaultRemoteIssueLinkService remoteIssueLinkService;
    @Mock private RemoteIssueLinkManager remoteIssueLinkManager;
    @Mock private IssueService issueService;
    @Mock private IssueManager issueManager;
    @Mock private IssueLinkManager issueLinkManager;
    @Mock private PermissionManager permissionManager;
    @Mock private ApplicationUser user;
    @Mock private User directoryUser;

    @Before
    public void setUp()
    {
        remoteIssueLinkService = new DefaultRemoteIssueLinkService(remoteIssueLinkManager, issueService, issueManager, issueLinkManager, new MockI18nBean.MockI18nBeanFactory(), permissionManager);

        when(issueManager.getIssueObject(ISSUE_ID_THAT_EXISTS)).thenReturn(ISSUE_THAT_EXISTS);
        when(issueManager.getIssueObject(ISSUE_ID_THAT_DOES_NOT_EXIST)).thenReturn(null);
        when(user.getDirectoryUser()).thenReturn(directoryUser);
        when(issueService.getIssue(directoryUser, ISSUE_ID_THAT_EXISTS)).thenReturn(new IssueService.IssueResult(ISSUE_THAT_EXISTS));

        when(issueLinkManager.isLinkingEnabled()).thenReturn(true);

        // Default permissions
        when(permissionManager.hasPermission(Permissions.BROWSE, ISSUE_THAT_EXISTS, user)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.LINK_ISSUE, ISSUE_THAT_EXISTS, user)).thenReturn(true);
    }

    @Test
    public void testCreate()
    {
        final RemoteIssueLink remoteIssueLink = populatedBuilder().build();
        createExpectingSuccess(remoteIssueLink);
    }

    @Test
    public void testCreateWithDuplicateGlobalId()
    {
        final String globalId = "A unique globalId";
        final RemoteIssueLink existing = populatedBuilder()
                .id(200L)
                .globalId(globalId)
                .build();
        when(remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(ISSUE_THAT_EXISTS, globalId)).thenReturn(existing);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().globalId(globalId).build();
        createExpectingFailure(remoteIssueLink, Reason.VALIDATION_FAILED, "globalId");
    }

    @Test
    public void testCreateWithGlobalIdTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().globalId(tooLongValue).build();
        createExpectingFailure(remoteIssueLink, Reason.VALIDATION_FAILED, "globalId");
    }

    @Test
    public void testCreateWithTitleTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().title(tooLongValue).build();
        createExpectingFailure(remoteIssueLink, Reason.VALIDATION_FAILED, "title");
    }

    @Test
    public void testCreateWithRelationShipTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().relationship(tooLongValue).build();
        createExpectingFailure(remoteIssueLink, Reason.VALIDATION_FAILED, "relationship");
    }

    @Test
    public void testCreateWithApplicationTypeTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().applicationType(tooLongValue).build();
        createExpectingFailure(remoteIssueLink, Reason.VALIDATION_FAILED, "applicationType");
    }

    @Test
    public void testCreateWithApplicationNameTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().applicationName(tooLongValue).build();
        createExpectingFailure(remoteIssueLink, Reason.VALIDATION_FAILED, "applicationName");
    }

    @Test
    public void testCreateWithoutNonRequiredFields()
    {
        // Set non-required fields to null
        final RemoteIssueLink remoteIssueLink = populatedBuilder()
                .globalId(null)
                .summary(null)
                .iconUrl(null)
                .iconTitle(null)
                .relationship(null)
                .resolved(null)
                .statusIconUrl(null)
                .statusIconTitle(null)
                .statusIconLink(null)
                .applicationType(null)
                .applicationName(null)
                .build();

        createExpectingSuccess(remoteIssueLink);
    }

    @Test
    public void testCreateWithBlankNonRequiredFields()
    {
        // Set non-required string fields to blank string
        // Any fields validated as a URI must be empty, otherwise invalid URI
        final RemoteIssueLink remoteIssueLink = populatedBuilder()
                .globalId(BLANK)
                .summary(BLANK)
                .iconUrl("")
                .iconTitle(BLANK)
                .relationship(BLANK)
                .statusIconUrl("")
                .statusIconTitle(BLANK)
                .statusIconLink("")
                .applicationType(BLANK)
                .applicationName(BLANK)
                .build();

        createExpectingSuccess(remoteIssueLink);
    }

    @Test
    public void testCreateWithoutRequiredFields()
    {
        // Set required fields to null
        createExpectingFailure(populatedBuilder().issueId(null).build(), Reason.VALIDATION_FAILED, "issueId");
        createExpectingFailure(populatedBuilder().title(null).build(), Reason.VALIDATION_FAILED, "title");
        createExpectingFailure(populatedBuilder().url(null).build(), Reason.VALIDATION_FAILED, "url");
    }
    @Test
    public void testCreateWithBlankRequiredFields()
    {
        // Set required string fields to blank string
        createExpectingFailure(populatedBuilder().title(BLANK).build(), Reason.VALIDATION_FAILED, "title");
        createExpectingFailure(populatedBuilder().url(BLANK).build(), Reason.VALIDATION_FAILED, "url");
    }

    @Test
    public void testCreateWithInvalidIssue()
    {
        createExpectingFailure(populatedBuilder().issueId(ISSUE_ID_THAT_DOES_NOT_EXIST).build(), Reason.VALIDATION_FAILED, "issueId");
    }

    @Test
    public void testCreateWithInvalidUrls()
    {
        createExpectingFailure(populatedBuilder().url(INVALID_URI).build(), Reason.VALIDATION_FAILED, "url");
        createExpectingFailure(populatedBuilder().iconUrl(INVALID_URI).build(), Reason.VALIDATION_FAILED, "iconUrl");
        createExpectingFailure(populatedBuilder().statusIconUrl(INVALID_URI).build(), Reason.VALIDATION_FAILED, "statusIconUrl");
        createExpectingFailure(populatedBuilder().statusIconLink(INVALID_URI).build(), Reason.VALIDATION_FAILED, "statusIconLink");
    }

    @Test
    public void testCreateWhenLinkingDisabled()
    {
        when(issueLinkManager.isLinkingEnabled()).thenReturn(false);

        final RemoteIssueLink remoteIssueLink = populatedBuilder().build();
        createExpectingFailure(remoteIssueLink, Reason.FORBIDDEN);
    }

    @Test
    public void testCreateWithoutLinkIssuePermission()
    {
        when(permissionManager.hasPermission(Permissions.LINK_ISSUE, ISSUE_THAT_EXISTS, user)).thenReturn(false);
        createExpectingFailure(populatedBuilder().build(), Reason.FORBIDDEN);
    }
    
    @Test
    public void testUpdate()
    {
        final RemoteIssueLink created = mockCreation();

        // Update some fields
        final RemoteIssueLink updated = populatedBuilder(created)
                .title("An updated URL label")
                .relationship("a different relationship")
                .applicationName("com.different.application.name")
                .build();

        updateExpectingSuccess(updated);
    }

    @Test
    public void testUpdateWhenDoesNotExist()
    {
        final RemoteIssueLink remoteIssueLink = populatedBuilder().id(100L).build();
        when(remoteIssueLinkManager.getRemoteIssueLink(remoteIssueLink.getId())).thenReturn(null);
        updateExpectingFailure(remoteIssueLink, Reason.NOT_FOUND);
    }

    @Test
    public void testUpdateWithDuplicateGlobalId()
    {
        final String globalId = "A unique globalId";
        final RemoteIssueLink existing = populatedBuilder()
                .id(200L)
                .globalId(globalId)
                .build();
        when(remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(ISSUE_THAT_EXISTS, globalId)).thenReturn(existing);

        final RemoteIssueLink created = mockCreation();
        final RemoteIssueLink updated = populatedBuilder(created)
                .globalId(globalId)
                .build();

        updateExpectingFailure(updated, Reason.VALIDATION_FAILED, "globalId");
    }

    @Test
    public void testUpdateGlobalIdFromNullToDuplicate()
    {
        final String globalId = "A unique globalId";
        final RemoteIssueLink existing = populatedBuilder()
                .id(200L)
                .globalId(globalId)
                .build();
        when(remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(ISSUE_THAT_EXISTS, globalId)).thenReturn(existing);

        final RemoteIssueLink created = mockCreation(populatedBuilder().globalId(null).build());
        final RemoteIssueLink updated = populatedBuilder(created)
                .globalId(globalId)
                .build();

        updateExpectingFailure(updated, Reason.VALIDATION_FAILED, "globalId");
    }

    @Test
    public void testUpdateGlobalIdToNull()
    {
        final RemoteIssueLink created = mockCreation(populatedBuilder().build());
        final RemoteIssueLink updated = populatedBuilder(created)
                .globalId(null)
                .build();

        updateExpectingSuccess(updated);
    }

    @Test
    public void testUpdateWithoutNonRequiredFields()
    {
        final RemoteIssueLink created = mockCreation();

        // Set non-required fields to null
        final RemoteIssueLink updated = populatedBuilder(created)
                .globalId(null)
                .summary(null)
                .iconUrl(null)
                .iconTitle(null)
                .relationship(null)
                .resolved(null)
                .statusIconUrl(null)
                .statusIconTitle(null)
                .statusIconLink(null)
                .applicationType(null)
                .applicationName(null)
                .build();

        updateExpectingSuccess(updated);
    }
    
    @Test
    public void testUpdateWithBlankNonRequiredFields()
    {
        final RemoteIssueLink created = mockCreation();

        // Set non-required fields to blank string
        // Any fields validated as a URI must be empty, otherwise invalid URI
        final RemoteIssueLink updated = populatedBuilder(created)
                .globalId(BLANK)
                .summary(BLANK)
                .iconUrl("")
                .iconTitle(BLANK)
                .relationship(BLANK)
                .statusIconUrl("")
                .statusIconTitle(BLANK)
                .statusIconLink("")
                .applicationType(BLANK)
                .applicationName(BLANK)
                .build();

        updateExpectingSuccess(updated);
    }

    @Test
    public void testUpdateWithoutRequiredFields()
    {
        final RemoteIssueLink created = mockCreation();

        // Set required fields to null
        updateExpectingFailure(populatedBuilder(created).id(null).build(), Reason.VALIDATION_FAILED, "id");
        updateExpectingFailure(populatedBuilder(created).issueId(null).build(), Reason.VALIDATION_FAILED, "issueId");
        updateExpectingFailure(populatedBuilder(created).title(null).build(), Reason.VALIDATION_FAILED, "title");
        updateExpectingFailure(populatedBuilder(created).url(null).build(), Reason.VALIDATION_FAILED, "url");
    }
    @Test
    public void testUpdateWithBlankRequiredFields()
    {
        final RemoteIssueLink created = mockCreation();

        // Set required string fields to blank string
        updateExpectingFailure(populatedBuilder(created).title(BLANK).build(), Reason.VALIDATION_FAILED, "title");
        updateExpectingFailure(populatedBuilder(created).url(BLANK).build(), Reason.VALIDATION_FAILED, "url");
    }

    @Test
    public void testUpdateWithInvalidIssue()
    {
        final RemoteIssueLink created = mockCreation();
        updateExpectingFailure(populatedBuilder(created).issueId(ISSUE_ID_THAT_DOES_NOT_EXIST).build(), Reason.VALIDATION_FAILED, "issueId");
    }

    @Test
    public void testUpdateWithValidUrls()
    {
        final RemoteIssueLink created = mockCreation();

        updateExpectingSuccess(populatedBuilder(created).url("https://example.org").build());
        updateExpectingSuccess(populatedBuilder(created).url("http://example.org").build());
        updateExpectingSuccess(populatedBuilder(created).url("http://example.org:1234").build());
        updateExpectingSuccess(populatedBuilder(created).url("http://example.org/").build());
        updateExpectingSuccess(populatedBuilder(created).url("http://example.org/jira/").build());
    }

    @Test
    public void testUpdateWithInvalidUrls()
    {
        final RemoteIssueLink created = mockCreation();
        updateExpectingFailure(populatedBuilder(created).url(INVALID_URI).build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).iconUrl(INVALID_URI).build(), Reason.VALIDATION_FAILED, "iconUrl");
        updateExpectingFailure(populatedBuilder(created).statusIconUrl(INVALID_URI).build(), Reason.VALIDATION_FAILED, "statusIconUrl");
        updateExpectingFailure(populatedBuilder(created).statusIconLink(INVALID_URI).build(), Reason.VALIDATION_FAILED, "statusIconLink");

        updateExpectingFailure(populatedBuilder(created).url("path").build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).url("/path").build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).url(" ://host/path").build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).url("://host/path").build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).url("http:").build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).url("http: ").build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).url("http:opaque").build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).url("http:/path").build(), Reason.VALIDATION_FAILED, "url");
        updateExpectingFailure(populatedBuilder(created).url("http://").build(), Reason.VALIDATION_FAILED, "url");
    }

    @Test
    public void testUpdateWhenLinkingDisabled()
    {
        when(issueLinkManager.isLinkingEnabled()).thenReturn(false);

        final RemoteIssueLink created = mockCreation();
        updateExpectingFailure(created, Reason.FORBIDDEN);
    }

    @Test
    public void testUpdateWithoutLinkIssuePermission()
    {
        final RemoteIssueLink created = mockCreation();
        when(permissionManager.hasPermission(Permissions.LINK_ISSUE, ISSUE_THAT_EXISTS, user)).thenReturn(false);
        updateExpectingFailure(created, Reason.FORBIDDEN);
    }

    @Test
    public void testDelete()
    {
        final RemoteIssueLink created = mockCreation();
        deleteExpectingSuccess(created.getId());
    }

    @Test
    public void testDeleteIdDoesNotExist()
    {
        final Long id = 99L;
        when(remoteIssueLinkManager.getRemoteIssueLink(id)).thenReturn(null);
        deleteExpectingFailure(id, Reason.NOT_FOUND);
    }

    @Test
    public void testDeleteNullId()
    {
        deleteExpectingFailure(null, Reason.VALIDATION_FAILED);
    }

    @Test
    public void testDeleteWhenLinkingDisabled()
    {
        when(issueLinkManager.isLinkingEnabled()).thenReturn(false);

        final RemoteIssueLink created = mockCreation();
        deleteExpectingFailure(created.getId(), Reason.FORBIDDEN);
    }

    @Test
    public void testDeleteWithoutLinkIssuePermission()
    {
        final RemoteIssueLink created = mockCreation();
        when(permissionManager.hasPermission(Permissions.LINK_ISSUE, ISSUE_THAT_EXISTS, user)).thenReturn(false);
        deleteExpectingFailure(created.getId(), Reason.FORBIDDEN);
    }

    @Test
    public void testValidateDeleteByGlobalId()
    {
        final RemoteIssueLink link = mockCreation();

        when(remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(ISSUE_THAT_EXISTS, "globalId")).thenReturn(link);

        DeleteByGlobalIdValidationResult validationResult = remoteIssueLinkService.validateDeleteByGlobalId(user, ISSUE_THAT_EXISTS, "globalId");

        assertEquals(ISSUE_THAT_EXISTS, validationResult.getIssue());
        assertEquals("globalId", validationResult.getGlobalId());
        assertTrue("Expected positive validation",validationResult.isValid());
    }

    @Test
    public void testDeleteByGlobalId() throws RemoveException
    {
        final RemoteIssueLink link = mockCreation();

        when(remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(ISSUE_THAT_EXISTS, "globalId")).thenReturn(link);
        DeleteByGlobalIdValidationResult validationResult = remoteIssueLinkService.validateDeleteByGlobalId(user, ISSUE_THAT_EXISTS, "globalId");

        remoteIssueLinkService.deleteByGlobalId(user, validationResult);
        verify(remoteIssueLinkManager).removeRemoteIssueLinkByGlobalId(ISSUE_THAT_EXISTS, "globalId", user);
    }

    @Test
    public void testDeleteByGlobalIdWhenGlobalIdDoesNotExist() throws RemoveException
    {
        DeleteByGlobalIdValidationResult validationResult = remoteIssueLinkService.validateDeleteByGlobalId(user, ISSUE_THAT_EXISTS, "globalId");

        assertFailure(validationResult.getErrorCollection(), Reason.NOT_FOUND, "globalId");

        try
        {
            remoteIssueLinkService.deleteByGlobalId(user, validationResult);
            fail("Should not have deleted non-existant issue");
        }
        catch (IllegalStateException e)
        {
            // Success
        }

        verify(remoteIssueLinkManager, times(0)).removeRemoteIssueLinkByGlobalId(any(Issue.class), anyString(), any(ApplicationUser.class));
    }

    @Test
    public void testGet()
    {
        final RemoteIssueLink created = mockCreation();
        final RemoteIssueLinkResult result = remoteIssueLinkService.getRemoteIssueLink(user, created.getId());
        assertTrue("Expected positive result", result.isValid());
        assertNotNull(result.getRemoteIssueLink());
    }

    @Test
    public void testGetWhenLinkingDisabled()
    {
        final RemoteIssueLink created = mockCreation();
        when(issueLinkManager.isLinkingEnabled()).thenReturn(false);
        getExpectingFailure(created.getId(), Reason.FORBIDDEN);
    }

    @Test
    public void testGetWithoutViewIssuePermission()
    {
        final RemoteIssueLink created = mockCreation();
        when(permissionManager.hasPermission(Permissions.BROWSE, ISSUE_THAT_EXISTS, user)).thenReturn(false);
        getExpectingFailure(created.getId(), Reason.FORBIDDEN);
    }

    @Test
    public void testGetForIssue()
    {
        final MockIssue issue = new MockIssue(ISSUE_ID_THAT_EXISTS);
        final Iterable<RemoteIssueLink> remoteIssueLinks = mockLinksForIssue(issue);

        final RemoteIssueLinkListResult result = remoteIssueLinkService.getRemoteIssueLinksForIssue(user, issue);
        assertTrue("Expected positive result", result.isValid());
        assertNotNull(result.getRemoteIssueLinks());
        assertEquals(remoteIssueLinks, result.getRemoteIssueLinks());
    }

    @Test
    public void testGetForIssueWhenLinkingDisabled()
    {
        final MockIssue issue = new MockIssue(ISSUE_ID_THAT_EXISTS);
        mockLinksForIssue(issue);

        when(issueLinkManager.isLinkingEnabled()).thenReturn(false);
        getForIssueExpectingFailure(issue, Reason.FORBIDDEN);
    }

    @Test
    public void testGetForIssueWithoutViewIssuePermission()
    {
        final MockIssue issue = new MockIssue(ISSUE_ID_THAT_EXISTS);
        mockLinksForIssue(issue);

        when(permissionManager.hasPermission(Permissions.BROWSE, ISSUE_THAT_EXISTS, user)).thenReturn(false);
        getForIssueExpectingFailure(issue, Reason.FORBIDDEN);
    }

    @Test
    public void testFindByGlobalIds() throws GetException
    {
        final List<RemoteIssueLink> remoteIssueLinks = mockLinksForGlobalId(GLOBAL_ID, ISSUE_ID_THAT_EXISTS, ISSUE_ID_THAT_EXISTS1, ISSUE_ID_THAT_EXISTS2);
        final ImmutableList<String> globalIds = ImmutableList.of(GLOBAL_ID);
        when(remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(eqCollection(globalIds))).thenReturn(remoteIssueLinks);
        final List<Issue> issueList = mockIssuesForLinks(ImmutableList.of(ISSUE_ID_THAT_EXISTS, ISSUE_ID_THAT_EXISTS1, ISSUE_ID_THAT_EXISTS2), ImmutableList.<Long>of());
        for (Issue issue : issueList)
        {
            when(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).thenReturn(true);
        }

        final RemoteIssueLinkListResult result = remoteIssueLinkService.findRemoteIssueLinksByGlobalId(user, globalIds);
        assertTrue("Expected positive result", result.isValid());
        assertNotNull(result.getRemoteIssueLinks());
        assertEquals(remoteIssueLinks, result.getRemoteIssueLinks());
    }

    @Test
    public void testFindByGlobalIdsWithNonExistentIssueId() throws GetException
    {
        final List<RemoteIssueLink> remoteIssueLinks = mockLinksForGlobalId(GLOBAL_ID, ISSUE_ID_THAT_EXISTS1, ISSUE_ID_THAT_EXISTS2, ISSUE_ID_THAT_DOES_NOT_EXIST);
        final ImmutableList<String> globalIds = ImmutableList.of(GLOBAL_ID);
        when(remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(eqCollection(globalIds))).thenReturn(remoteIssueLinks);
        final List<Issue> issueList = mockIssuesForLinks(ImmutableList.of(ISSUE_ID_THAT_EXISTS1, ISSUE_ID_THAT_EXISTS2), ImmutableList.of(ISSUE_ID_THAT_DOES_NOT_EXIST));
        for (Issue issue : issueList)
        {
            when(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).thenReturn(true);
        }

        final RemoteIssueLinkListResult result = remoteIssueLinkService.findRemoteIssueLinksByGlobalId(user, globalIds);
        assertTrue("Expected positive result",result.isValid());
        assertNotNull(result.getRemoteIssueLinks());
        final List<RemoteIssueLink> expected = ImmutableList.copyOf(remoteIssueLinks);
        assertEquals(expected.subList(0, expected.size()-1), result.getRemoteIssueLinks());
    }

    @Test
    public void testFindByGlobalIdsWithPermissionFilter() throws GetException
    {
        final List<RemoteIssueLink> remoteIssueLinks = mockLinksForGlobalId(GLOBAL_ID, ISSUE_ID_THAT_EXISTS1, ISSUE_ID_THAT_EXISTS2, ISSUE_ID_THAT_DOES_NOT_EXIST);
        final List<Issue> issueList = mockIssuesForLinks(ImmutableList.of(ISSUE_ID_THAT_EXISTS1, ISSUE_ID_THAT_EXISTS2), ImmutableList.of(ISSUE_ID_THAT_DOES_NOT_EXIST));
        final ImmutableList<String> globalIds = ImmutableList.of(GLOBAL_ID);
        when(remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(eqCollection(globalIds))).thenReturn(remoteIssueLinks);
        // only the 2nd one is permitted
        when(permissionManager.hasPermission(Permissions.BROWSE, issueList.get(0), user)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issueList.get(1), user)).thenReturn(true);

        final RemoteIssueLinkListResult result = remoteIssueLinkService.findRemoteIssueLinksByGlobalId(user, globalIds);
        assertTrue("Expected positive result",result.isValid());
        assertNotNull(result.getRemoteIssueLinks());
        final List<RemoteIssueLink> expected = ImmutableList.copyOf(remoteIssueLinks);
        assertEquals(expected.subList(1, expected.size()-1), result.getRemoteIssueLinks());
    }

    @Test
    public void testFindByGlobalIdsMultipleGlobalIds() throws GetException
    {
        final List<RemoteIssueLink> remoteIssueLinks1 = mockLinksForGlobalId(GLOBAL_ID, ISSUE_ID_THAT_EXISTS1);
        final List<RemoteIssueLink> remoteIssueLinks2 = mockLinksForGlobalId(GLOBAL_ID2, ISSUE_ID_THAT_EXISTS2, ISSUE_ID_THAT_DOES_NOT_EXIST);
        final List<RemoteIssueLink> remoteIssueLinks = ImmutableList.copyOf(Iterables.concat(remoteIssueLinks1, remoteIssueLinks2));
        final List<Issue> issueList = mockIssuesForLinks(ImmutableList.of(ISSUE_ID_THAT_EXISTS1, ISSUE_ID_THAT_EXISTS2), ImmutableList.of(ISSUE_ID_THAT_DOES_NOT_EXIST));
        for (Issue issue : issueList)
        {
            when(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).thenReturn(true);
        }
        final ImmutableList<String> globalIds = ImmutableList.of(GLOBAL_ID, GLOBAL_ID2);
        when(remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(eqCollection(globalIds))).thenReturn(remoteIssueLinks);

        final RemoteIssueLinkListResult result = remoteIssueLinkService.findRemoteIssueLinksByGlobalId(user, globalIds);
        assertTrue("Expected positive result",result.isValid());
        assertNotNull(result.getRemoteIssueLinks());
        final List<RemoteIssueLink> expected = ImmutableList.copyOf(remoteIssueLinks);
        assertEquals(expected.subList(0, expected.size()-1), result.getRemoteIssueLinks());
    }

    private RemoteIssueLink mockCreation()
    {
        return mockCreation(populatedBuilder().build());
    }

    private RemoteIssueLink mockCreation(final RemoteIssueLink remoteIssueLink)
    {
        // Mock creation, with arbitrary id
        final RemoteIssueLink created = populatedBuilder(remoteIssueLink).id(100L).build();
        when(remoteIssueLinkManager.getRemoteIssueLink(created.getId())).thenReturn(created);

        return created;
    }

    private Iterable<RemoteIssueLink> mockLinksForIssue(final Issue issue)
    {
        // Mock finding remote issue links for an issue
        final RemoteIssueLink remoteIssueLink = populatedBuilder().issueId(issue.getId()).build();
        final List<RemoteIssueLink> remoteIssueLinks = Arrays.asList(remoteIssueLink);
        when(remoteIssueLinkManager.getRemoteIssueLinksForIssue(issue)).thenReturn(remoteIssueLinks);

        return remoteIssueLinks;
    }

    private List<RemoteIssueLink> mockLinksForGlobalId(final String globalId, final Long... issueIds) throws GetException
    {
        // Mock finding remote issue links for a globalId
        final ImmutableList<Long> issueIdList = ImmutableList.copyOf(issueIds);

        return ImmutableList.copyOf(Lists.transform(issueIdList, new Function<Long, RemoteIssueLink>()
        {
            @Override
            public RemoteIssueLink apply(@Nullable final Long issueId)
            {
                return populatedBuilder().issueId(issueId).globalId(globalId).build();
            }
        }));
    }

    private List<Issue> mockIssuesForLinks(List<Long> issueIds, List<Long> nonExistIssueIds)
    {
        final List<Issue> issueList = Lists.transform(issueIds, new Function<Long, Issue>()
        {
            @Override
            public Issue apply(@Nullable final Long issueId)
            {
                return new MockIssue(issueId);
            }
        });
        final List<Long> allIssueIds = ImmutableList.copyOf(Iterables.concat(issueIds, nonExistIssueIds));
        when(issueManager.getIssueObjects(eqCollection(allIssueIds))).thenReturn(issueList);

        return issueList;
    }

    private <T> Collection<T> eqCollection(final Collection<T> expectedItems)
    {
        return argThat(new TypeSafeMatcher<Collection<T>>()
        {
            @Override
            protected boolean matchesSafely(final Collection<T> items)
            {
                return expectedItems.size() == items.size() && CollectionUtils.intersection(items, expectedItems).size() == items.size();
            }

            @Override
            public void describeTo(final Description description)
            {
            }
        });
    }

    private void createExpectingSuccess(final RemoteIssueLink remoteIssueLink)
    {
        final CreateValidationResult createValidationResult = remoteIssueLinkService.validateCreate(user, remoteIssueLink);
        assertTrue(asString(createValidationResult.getErrorCollection()),
                createValidationResult.isValid());

        // An arbitrary id for the created remote issue link to be given
        final RemoteIssueLink created = new RemoteIssueLinkBuilder(remoteIssueLink).id(100L).build();
        try
        {
            when(remoteIssueLinkManager.createRemoteIssueLink(remoteIssueLink, user)).thenReturn(created);
        }
        catch (final CreateException e)
        {
            // Unexpected errors
            throw new RuntimeException(e);
        }

        final RemoteIssueLinkResult remoteIssueLinkResult = remoteIssueLinkService.create(user, createValidationResult);
        assertTrue("Expected positive result", remoteIssueLinkResult.isValid());

        when(remoteIssueLinkManager.getRemoteIssueLink(created.getId())).thenReturn(remoteIssueLinkResult.getRemoteIssueLink());
        assertExists(remoteIssueLinkResult.getRemoteIssueLink());
    }

    private CreateValidationResult createExpectingFailure(final RemoteIssueLink remoteIssueLink, final Reason reason, final String ... erroneousFields)
    {
        final CreateValidationResult createValidationResult = remoteIssueLinkService.validateCreate(user, remoteIssueLink);
        assertFailure(createValidationResult, reason, erroneousFields);
        return createValidationResult;
    }

    private void updateExpectingSuccess(final RemoteIssueLink remoteIssueLink)
    {
        final UpdateValidationResult updateValidationResult = remoteIssueLinkService.validateUpdate(user, remoteIssueLink);
        assertTrue(asString(updateValidationResult.getErrorCollection()),
                updateValidationResult.isValid());

        try
        {
            doNothing().when(remoteIssueLinkManager).updateRemoteIssueLink(remoteIssueLink, user);
        }
        catch (final UpdateException e)
        {
            // Unexpected errors
            throw new RuntimeException(e);
        }

        final RemoteIssueLinkResult remoteIssueLinkResult = remoteIssueLinkService.update(user, updateValidationResult);
        assertTrue("Expected positive result", remoteIssueLinkResult.isValid());

        when(remoteIssueLinkManager.getRemoteIssueLink(remoteIssueLink.getId())).thenReturn(remoteIssueLinkResult.getRemoteIssueLink());
        assertExists(remoteIssueLinkResult.getRemoteIssueLink());
    }

    private UpdateValidationResult updateExpectingFailure(final RemoteIssueLink remoteIssueLink, final Reason reason, final String ... erroneousFields)
    {
        final UpdateValidationResult updateValidationResult = remoteIssueLinkService.validateUpdate(user, remoteIssueLink);
        assertFailure(updateValidationResult, reason, erroneousFields);
        return updateValidationResult;
    }

    private void deleteExpectingSuccess(final Long remoteIssueLinkId)
    {
        final DeleteValidationResult deleteValidationResult = remoteIssueLinkService.validateDelete(user, remoteIssueLinkId);
        assertTrue(asString(deleteValidationResult.getErrorCollection()),
                deleteValidationResult.isValid());

        doNothing().when(remoteIssueLinkManager).removeRemoteIssueLink(remoteIssueLinkId, user);

        remoteIssueLinkService.delete(user, deleteValidationResult);

        when(remoteIssueLinkManager.getRemoteIssueLink(remoteIssueLinkId)).thenReturn(null);
        assertNull(remoteIssueLinkService.getRemoteIssueLink(user, remoteIssueLinkId).getRemoteIssueLink());
    }

    private DeleteValidationResult deleteExpectingFailure(final Long remoteIssueLinkId, final Reason reason)
    {
        final DeleteValidationResult deleteValidationResult = remoteIssueLinkService.validateDelete(user, remoteIssueLinkId);
        assertFalse("Expected that validation should not pass",deleteValidationResult.isValid());
        assertNull(deleteValidationResult.getRemoteIssueLinkId());
        assertFailure(deleteValidationResult.getErrorCollection(), reason);
        return deleteValidationResult;
    }

    private RemoteIssueLinkResult getExpectingFailure(final Long remoteIssueLinkId, final Reason reason, final String ... erroneousFields)
    {
        final RemoteIssueLinkResult remoteIssueLinkResult = remoteIssueLinkService.getRemoteIssueLink(user, remoteIssueLinkId);
        assertFailure(remoteIssueLinkResult, reason, erroneousFields);
        return remoteIssueLinkResult;
    }

    private RemoteIssueLinkListResult getForIssueExpectingFailure(final Issue issue, final Reason reason, final String ... erroneousFields)
    {
        final RemoteIssueLinkListResult remoteIssueLinkListResult = remoteIssueLinkService.getRemoteIssueLinksForIssue(user, issue);
        assertFailure(remoteIssueLinkListResult, reason, erroneousFields);
        return remoteIssueLinkListResult;
    }

    private void assertExists(final RemoteIssueLink expected)
    {
        final RemoteIssueLinkService.RemoteIssueLinkResult result = remoteIssueLinkService.getRemoteIssueLink(user, expected.getId());
        assertTrue(asString(result.getErrorCollection()), result.isValid());

        final RemoteIssueLink found = result.getRemoteIssueLink();
        assertNotNull(found);
        assertRemoteIssueLinksEqual(expected, found);
    }

    private void assertFailure(final RemoteIssueLinkResult remoteIssueLinkResult, final Reason reason, final String ... erroneousFields)
    {
        assertFalse("Expected failed results",remoteIssueLinkResult.isValid());
        assertNull(remoteIssueLinkResult.getRemoteIssueLink());
        assertFailure(remoteIssueLinkResult.getErrorCollection(), reason, erroneousFields);
    }

    private void assertFailure(final RemoteIssueLinkListResult remoteIssueLinkListResult, final Reason reason, final String ... erroneousFields)
    {
        assertFalse("Expected failed results",remoteIssueLinkListResult.isValid());
        assertNull(remoteIssueLinkListResult.getRemoteIssueLinks());
        assertFailure(remoteIssueLinkListResult.getErrorCollection(), reason, erroneousFields);
    }

    private void assertFailure(final ErrorCollection errors, final Reason reason, final String ... erroneousFields)
    {
        assertTrue("Expected error collection with errors",errors.hasAnyErrors());
        final Reason worstReason = Reason.getWorstReason(errors.getReasons());
        assertEquals(reason, worstReason);
        assertThat(errors.getErrors().keySet(), IsCollectionContaining.hasItems(erroneousFields));

    }

    private RemoteIssueLinkBuilder populatedBuilder()
    {
        return TestDefaultRemoteIssueLinkManager.populatedBuilder(ISSUE_ID_THAT_EXISTS);
    }

    private RemoteIssueLinkBuilder populatedBuilder(final RemoteIssueLink remoteIssueLink)
    {
        return new RemoteIssueLinkBuilder(remoteIssueLink);
    }

    private void assertRemoteIssueLinksEqual(final RemoteIssueLink expected, final RemoteIssueLink actual)
    {
        TestDefaultRemoteIssueLinkManager.assertRemoteIssueLinksEqual(expected, actual);
    }

    private String asString(final ErrorCollection errors)
    {
        return "Errors: " + errors.getErrors() + ", Error Messages: " + errors.getErrorMessages();
    }
}
