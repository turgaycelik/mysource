package com.atlassian.jira.issue.link;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.priority.MockPriority;
import com.atlassian.jira.issue.resolution.MockResolution;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LinkCollection}.
 *
 * @since v6.2
 */
public class TestLinkCollectionImpl
{
    public static final long ISSUE_ID = 1l;
    public static final String LINK_TYPE_BLOCK = "blocks";
    public static final String LINK_TYPE_RELATES = "relates";
    public static final String LINK_TYPE_YOLO = "yolo";

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    private Set<IssueLinkType> linkTypes = Sets.<IssueLinkType>newHashSet(
            new MockIssueLinkType(0, LINK_TYPE_BLOCK, null, null, null),
            new MockIssueLinkType(1, LINK_TYPE_RELATES, null, null, null));
    private User remoteUser = new MockUser("user");
    private ApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    PermissionManager permissionManager;

    @Before
    public void setUp()
    {
        applicationProperties = new MockApplicationProperties();

        // all issues are browseable by default
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), any(Issue.class), eq(remoteUser))).thenReturn(true);
    }

    @Test
    public void checkSortableFields()
    {
        assertThat(Lists.newArrayList(LinkCollectionImpl.getSortableFields()),
                equalTo(Arrays.asList("type", "status", "priority", "key", "resolution")));
    }

    private Issue forbiddenIssue = mockIssue(3, "ISSUE-3", 2, 2, 2, 2);
    private Map<String, List<Issue>> inwardLinkMap = linkMap(LINK_TYPE_RELATES, mockIssue(1, "ISSUE-2", 1, 1, 1, 1), forbiddenIssue);
    private Map<String, List<Issue>> outwardLinkMap = linkMap(LINK_TYPE_BLOCK, mockIssue(4, "ISSUE-4", 0, 0, 0, 0));

    @Test
    public void getAllIssuesSortOrderWithPermissionExclusion()
    {
        when(permissionManager.hasPermission(Permissions.BROWSE, forbiddenIssue, remoteUser)).thenReturn(false);

        // by default they are sorted with issue type
        assertThat(getAllIssueIDs(outwardLinkMap, inwardLinkMap, false), equalTo(Lists.<Long>newArrayList(4l, 1l)));
    }

    @Test
    public void getAllIssuesSortOrderByType()
    {
        applicationProperties.setText(APKeys.JIRA_VIEW_ISSUE_LINKS_SORT_ORDER, "type");
        assertThat(getAllIssueIDs(outwardLinkMap, inwardLinkMap, false), equalTo(Lists.<Long>newArrayList(4l, 1l, 3l)));
    }

    @Test
    public void getAllIssuesSortOrderByStatus()
    {
        applicationProperties.setText(APKeys.JIRA_VIEW_ISSUE_LINKS_SORT_ORDER, "status");
        assertThat(getAllIssueIDs(outwardLinkMap, inwardLinkMap, false), equalTo(Lists.<Long>newArrayList(1l, 3l, 4l)));
    }

    @Test
    public void getAllIssuesSortOrderByPriority()
    {
        applicationProperties.setText(APKeys.JIRA_VIEW_ISSUE_LINKS_SORT_ORDER, "priority");
        assertThat(getAllIssueIDs(outwardLinkMap, inwardLinkMap, false), equalTo(Lists.<Long>newArrayList(1l, 3l, 4l)));
    }

    @Test
    public void getAllIssuesSortOrderByKey()
    {
        applicationProperties.setText(APKeys.JIRA_VIEW_ISSUE_LINKS_SORT_ORDER, "key");
        assertThat(getAllIssueIDs(outwardLinkMap, inwardLinkMap, false), equalTo(Lists.<Long>newArrayList(1l, 3l, 4l)));
    }

    @Test
    public void getAllIssuesSortOrderByResolution()
    {
        applicationProperties.setText(APKeys.JIRA_VIEW_ISSUE_LINKS_SORT_ORDER, "resolution");
        assertThat(getAllIssueIDs(outwardLinkMap, inwardLinkMap, false), equalTo(Lists.<Long>newArrayList(4l, 1l, 3l)));
    }

    @Test
    public void getAllIssuesSortOrderWithOverrideSecurity()
    {
        applicationProperties.setText(APKeys.JIRA_VIEW_ISSUE_LINKS_SORT_ORDER, "key");

        when(permissionManager.hasPermission(Permissions.BROWSE, forbiddenIssue, remoteUser)).thenReturn(false);
        assertThat(getAllIssueIDs(outwardLinkMap, inwardLinkMap, false), equalTo(Lists.<Long>newArrayList(1l, 4l)));

        // override security
        assertThat(getAllIssueIDs(outwardLinkMap, inwardLinkMap, true), equalTo(Lists.<Long>newArrayList(1l, 3l, 4l)));
    }

    @Test
    public void getAllIssuesDoesNotReturnNulls()
    {
        Map<String, List<Issue>> outwardLinkMap = linkMap(LINK_TYPE_BLOCK, mockIssue(4, "ISSUE-4", 0, 0, 0, 0), null);

        assertThat(getAllIssueIDs(outwardLinkMap, null, false), equalTo(Lists.<Long>newArrayList(4l)));
    }

    @Test
    public void displayLinkPanelWhenLinksAreAvailable()
    {
        Map<String, List<Issue>> outwardLinkMap = linkMap(LINK_TYPE_BLOCK, mockIssue(4, "ISSUE-4", 0, 0, 0, 0));
        Map<String, List<Issue>> inwardLinkMap = linkMap(LINK_TYPE_RELATES, mockIssue(1, "ISSUE-2", 1, 1, 1, 1));

        LinkCollectionImpl collection = new LinkCollectionImpl(ISSUE_ID, linkTypes, outwardLinkMap, inwardLinkMap, remoteUser,
                false, applicationProperties);
        assertTrue("DisplayLinkPanel is true", collection.isDisplayLinkPanel());
    }

    @Test
    public void dontDisplayLinkPanelForUnbrowseableLinks()
    {
        Map<String, List<Issue>> outwardLinkMap = linkMap(LINK_TYPE_BLOCK, mockIssue(4, "ISSUE-4", 0, 0, 0, 0));
        Map<String, List<Issue>> inwardLinkMap = linkMap(LINK_TYPE_RELATES, mockIssue(1, "ISSUE-2", 1, 1, 1, 1));

        when(permissionManager.hasPermission(eq(Permissions.BROWSE), any(Issue.class), eq(remoteUser))).thenReturn(false);

        LinkCollectionImpl collection = new LinkCollectionImpl(ISSUE_ID, linkTypes, outwardLinkMap, inwardLinkMap, remoteUser,
                false, applicationProperties);
        assertFalse("DisplayLinkPanel is false", collection.isDisplayLinkPanel());
    }

    @Test
    public void displayLinkPanelForUnbrowseableLinksButOverridenSecurity()
    {
        Map<String, List<Issue>> outwardLinkMap = linkMap(LINK_TYPE_BLOCK, mockIssue(4, "ISSUE-4", 0, 0, 0, 0));
        Map<String, List<Issue>> inwardLinkMap = linkMap(LINK_TYPE_RELATES, mockIssue(1, "ISSUE-2", 1, 1, 1, 1));

        when(permissionManager.hasPermission(eq(Permissions.BROWSE), any(Issue.class), eq(remoteUser))).thenReturn(false);

        LinkCollectionImpl collection = new LinkCollectionImpl(ISSUE_ID, linkTypes, outwardLinkMap, inwardLinkMap, remoteUser,
                true, applicationProperties);
        assertTrue("Display link panel is true", collection.isDisplayLinkPanel());
    }

    @Test
    public void dontDisplayLinkPanelForUnknownLinkTypes()
    {
        Map<String, List<Issue>> outwardLinkMap = linkMap(LINK_TYPE_YOLO, mockIssue(4, "ISSUE-4", 0, 0, 0, 0));
        Map<String, List<Issue>> inwardLinkMap = linkMap(LINK_TYPE_YOLO, mockIssue(1, "ISSUE-2", 1, 1, 1, 1));

        LinkCollectionImpl collection = new LinkCollectionImpl(ISSUE_ID, linkTypes, outwardLinkMap, inwardLinkMap, remoteUser, applicationProperties);
        assertFalse("Display link panel is false", collection.isDisplayLinkPanel());
    }

    @Test
    public void displayLinkPanelReturnsNullForEmptyLinkMaps()
    {
        LinkCollectionImpl collection = new LinkCollectionImpl(ISSUE_ID, linkTypes, null, null, remoteUser, false, applicationProperties);
        assertFalse("Display link panel is false", collection.isDisplayLinkPanel());
    }

    private Map<String, List<Issue>> linkMap(String linkTypeName, Issue... issues)
    {
        return ImmutableMap.of(linkTypeName, Arrays.<Issue>asList(issues));
    }

    private Issue mockIssue(int id, String key, long issueTypeId, long statusId, long priorityId, long resolutionId)
    {
        MockIssue issue = new MockIssue(id, key);

        MockIssueType issueType = new MockIssueType(issueTypeId, Long.toString(issueTypeId));
        issueType.setSequence(issueTypeId);
        issue.setIssueTypeObject(issueType);

        MockStatus status = new MockStatus(Long.toString(statusId), Long.toString(statusId));
        status.setSequence(statusId);
        issue.setStatusObject(status);

        MockPriority priority = new MockPriority(Long.toString(priorityId), Long.toString(priorityId));
        priority.setSequence(priorityId);
        issue.setPriorityObject(priority);

        MockResolution resolution = new MockResolution(Long.toString(resolutionId), Long.toString(resolutionId));
        resolution.setSequence(resolutionId);
        issue.setResolutionObject(resolution);

        return issue;
    }

    private ArrayList<Long> getAllIssueIDs(Map<String, List<Issue>> outwardLinkMap, Map<String, List<Issue>> inwardLinkMap,
            boolean overrideSecurity)
    {
        LinkCollectionImpl collection = new LinkCollectionImpl(ISSUE_ID, linkTypes, outwardLinkMap, inwardLinkMap,
                remoteUser, overrideSecurity, applicationProperties);

        return new ArrayList<Long>(Collections2.transform(collection.getAllIssues(), new Function<Issue, Long>()
        {
            @Override
            public Long apply(final Issue input)
            {
                return input.getId();
            }
        }));
    }
}
