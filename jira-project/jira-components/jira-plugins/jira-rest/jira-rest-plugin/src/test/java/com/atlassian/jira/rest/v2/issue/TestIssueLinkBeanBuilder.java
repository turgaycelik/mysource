package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.user.MockUser;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * * Tests for {@link com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilder}.
 *
 * @since v4.2
 */
public class TestIssueLinkBeanBuilder extends IssueResourceTest
{
    private static final String USER_NAME = "aUser";
    private static final String ISSUE_BASE_URI = "http://localhost:8090/jira/rest/api/2.0/issue/";

    @Override
    protected void doSetUp()
    {
        expect(jiraBaseUrls.restApi2BaseUrl()).andStubReturn("http://localhost/jira");
        expect(issue.getSummary()).andStubReturn("my issue");
    }

    /**
     * Verifies that any found links are added to the IssueBean.
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildIssueLinks_linkAdded() throws Exception
    {
        final User user = new MockUser(USER_NAME);
        expect(authContext.getLoggedInUser()).andReturn(user);
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).andReturn(true);
        expect(issue.getId()).andStubReturn(1234L);

        final String linkTypeName = "duplicates";
        final String linkedIssueKey = "LNK-2";
        final Issue linkedIssue = createMockIssue(10001L, linkedIssueKey);
        final IssueLinkType linkType = creatMockIssueLinkType(linkTypeName);
        IssueLink issueLink = createMock(IssueLink.class);
        expect(issueLink.getId()).andReturn(10014L);
        replay(issueLink);

        final LinkCollection linkCollection = createMock(LinkCollection.class);
        expect(linkCollection.getLinkTypes()).andReturn(singleton(linkType)).anyTimes();
        expect(linkCollection.getOutwardIssues(linkTypeName)).andReturn(singletonList(linkedIssue));
        expect(linkCollection.getInwardIssues(linkTypeName)).andReturn(Collections.<Issue>emptyList());
        replay(linkCollection);

        expect(issueLinkManager.getLinkCollection(issue, user)).andReturn(linkCollection);
        expect(issueLinkManager.getIssueLink(1234L, 10001L, 10000L)).andReturn(issueLink).anyTimes();
        expect(uriBuilder.build(contextUriInfo, IssueResource.class, linkedIssueKey)).andReturn(new URI(ISSUE_BASE_URI + linkedIssueKey));

        replayMocks();
        final IssueLinksBeanBuilder builder = createIssueLinkBeanBuilder();
        final List<IssueLinkJsonBean> links = builder.buildIssueLinks();

        // make sure the issue link was added
        assertEquals(1, links.size());
        assertEquals(linkedIssue.getId().toString(), links.get(0).outwardIssue().id());
        assertEquals(linkedIssue.getKey(), links.get(0).outwardIssue().key());
    }

    public void testBuildIssueLinks_noLinkTypes() throws Exception
    {
        final User user = new MockUser(USER_NAME);
        expect(authContext.getLoggedInUser()).andReturn(user);
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).andReturn(true);

        final String linkTypeName = "duplicates";
        final String linkedIssueKey = "LNK-2";
        final Issue linkedIssue = createMockIssue(10001L, linkedIssueKey);
        final IssueLinkType linkType = creatMockIssueLinkType(linkTypeName);

        final LinkCollection linkCollection = createMock(LinkCollection.class);
        expect(linkCollection.getLinkTypes()).andReturn(null).anyTimes();
        replay(linkCollection);

        expect(issueLinkManager.getLinkCollection(issue, user)).andReturn(linkCollection);
        expect(uriBuilder.build(uriInfo, IssueResource.class, linkedIssueKey)).andReturn(new URI(ISSUE_BASE_URI + linkedIssueKey));

        replayMocks();
        final IssueLinksBeanBuilder builder = createIssueLinkBeanBuilder();
        final List<IssueLinkJsonBean> links = builder.buildIssueLinks();

        // make sure no links were found
        assertTrue(links.isEmpty());
    }

    public void testBuildIssueLinks_noOutwardLinksOfType() throws Exception
    {
        final User user = new MockUser(USER_NAME);
        expect(authContext.getLoggedInUser()).andReturn(user);
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).andReturn(true);

        final String linkTypeName = "duplicates";
        final String linkedIssueKey = "LNK-2";
        final Issue linkedIssue = createMockIssue(10001L, linkedIssueKey);
        final IssueLinkType linkType = creatMockIssueLinkType(linkTypeName);

        final LinkCollection linkCollection = createMock(LinkCollection.class);
        expect(linkCollection.getLinkTypes()).andReturn(singleton(linkType)).anyTimes();
        expect(linkCollection.getOutwardIssues(linkTypeName)).andReturn(Collections.<Issue>emptyList());
        expect(linkCollection.getInwardIssues(linkTypeName)).andReturn(Collections.<Issue>emptyList());
        replay(linkCollection);

        expect(issueLinkManager.getLinkCollection(issue, user)).andReturn(linkCollection);
        expect(uriBuilder.build(uriInfo, IssueResource.class, linkedIssueKey)).andReturn(new URI(ISSUE_BASE_URI + linkedIssueKey));
        
        replayMocks();
        final IssueLinksBeanBuilder builder = createIssueLinkBeanBuilder();
        final List<IssueLinkJsonBean> links = builder.buildIssueLinks();

        // make sure no links were found
        assertTrue(links.isEmpty());
    }

    public void testBuildParentLink() throws Exception
    {
        final String parentIssueKey = "PAR-1";
        final Issue parent = createMockIssue(10001L, parentIssueKey);

        expect(issue.getParentObject()).andReturn(parent);
        expect(uriBuilder.build(contextUriInfo, IssueResource.class, parentIssueKey)).andReturn(new URI(ISSUE_BASE_URI + parentIssueKey));

        replayMocks();
        final IssueLinksBeanBuilder builder = createIssueLinkBeanBuilder();
        final IssueRefJsonBean parentLink = builder.buildParentLink();

        assertEquals(parent.getId().toString(), parentLink.id());
        assertEquals(parent.getKey(), parentLink.key());
    }

    public void testSubtaskLinkAdded() throws Exception
    {
        final String subtaskIssueKey = "SUB-1";
        final Issue subtask = createMockIssue(10001L, subtaskIssueKey);

        expect(issue.getSubTaskObjects()).andReturn(singletonList(subtask));
        expect(uriBuilder.build(contextUriInfo, IssueResource.class, subtaskIssueKey)).andReturn(new URI(ISSUE_BASE_URI + subtaskIssueKey));

        replayMocks();
        final IssueLinksBeanBuilder builder = createIssueLinkBeanBuilder();
        final List<IssueRefJsonBean> subtaskLinks = builder.buildSubtaskLinks();

        assertEquals(1, subtaskLinks.size());
        assertEquals(subtask.getId().toString(), subtaskLinks.get(0).id());
        assertEquals(subtask.getKey(), subtaskLinks.get(0).key());
    }

    private IssueLinkType creatMockIssueLinkType(final String name)
    {
        final IssueLinkType mockLinkType = createMock(IssueLinkType.class);
        expect(mockLinkType.getId()).andStubReturn(10000L);
        expect(mockLinkType.getName()).andStubReturn(name);
        expect(mockLinkType.getInward()).andStubReturn("my inward");
        expect(mockLinkType.getOutward()).andStubReturn("my outward");
        replay(mockLinkType);

        return mockLinkType;
    }
}
