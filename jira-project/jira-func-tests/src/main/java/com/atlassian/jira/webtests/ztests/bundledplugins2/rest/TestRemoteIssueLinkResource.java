package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.RemoteIssueLink;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateRequest;
import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateResponse;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang.ObjectUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Tests the remote issue links REST end point.
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestRemoteIssueLinkResource extends RestFuncTest
{
    private static final String ISSUE_KEY = "HSP-1";
    private static final String ANOTHER_ISSUE_KEY = "HSP-2";
    private static final String THIRD_ISSUE_KEY = "HSP-3";
    private static final String ISSUE_THAT_DOES_NOT_EXIST = "HSP-999";
    private static final String LINK_ID_THAT_DOES_NOT_EXIST = "-1";
    private static final String BLANK = "    ";
    private static final String INVALID_URI = "this is not a valid URI";
    private static int BAD_REQUEST = javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode();
    private static int CREATED = javax.ws.rs.core.Response.Status.CREATED.getStatusCode();
    private static int FORBIDDEN = javax.ws.rs.core.Response.Status.FORBIDDEN.getStatusCode();
    private static int NOT_FOUND = javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode();
    private static int NO_CONTENT = javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode();

    private IssueClient issueClient;

    public void testCreate()
    {
        createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest());
    }

    public void testCreateReturnsLocationHeader()
    {
        final ClientResponse response = issueClient.createOrUpdateRemoteIssueLinkAndGetClientResponse(ISSUE_KEY, populatedCreateOrUpdateRequest());
        assertEquals(CREATED, response.getStatus());
        final RemoteIssueLinkCreateOrUpdateResponse entity = response.getEntity(RemoteIssueLinkCreateOrUpdateResponse.class);
        final URI expectedLocation = URI.create(getRestApiUrl("issue", ISSUE_KEY, "remotelink", entity.id().toString()));
        assertEquals(expectedLocation, response.getLocation());
    }

    public void testCreateAndThenUpdate()
    {
        final Long createId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id();
        final Long updateId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id();
        assertEquals(createId, updateId);
    }

    public void testMultipleCreatesWithoutGlobalId()
    {
        final RemoteIssueLinkCreateOrUpdateRequest request = populatedCreateOrUpdateRequest().globalId(null);
        final Long id1 = createOrUpdateExpectingSuccess(ISSUE_KEY, request).id();
        final Long id2 = createOrUpdateExpectingSuccess(ISSUE_KEY, request).id();
        assertFalse(id1.equals(id2));
    }

    public void testCreateWithoutNonRequiredFields()
    {
        // Set non-required fields to null
        final RemoteIssueLinkCreateOrUpdateRequest request = populatedCreateOrUpdateRequest()
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
                .applicationName(null);

        createOrUpdateExpectingSuccess(ISSUE_KEY, request);
    }

    public void testCreateWithBlankNonRequiredFields()
    {
        // Set non-required string fields to blank string
        // Any fields validated as a URI must be empty, otherwise invalid URI
        final RemoteIssueLinkCreateOrUpdateRequest request = populatedCreateOrUpdateRequest()
                .globalId(BLANK)
                .summary(BLANK)
                .iconUrl("")
                .iconTitle(BLANK)
                .relationship(BLANK)
                .statusIconUrl("")
                .statusIconTitle(BLANK)
                .statusIconLink("")
                .applicationType(BLANK)
                .applicationName(BLANK);

        createOrUpdateExpectingSuccess(ISSUE_KEY, request);
    }

    public void testCreateWithoutRequiredFields()
    {
        // Set required fields to null
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest().title(null), BAD_REQUEST, "title");
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest().url(null), BAD_REQUEST, "url");
    }

    public void testCreateWithBlankRequiredFields()
    {
        // Set required string fields to blank string
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest().title(BLANK), BAD_REQUEST, "title");
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest().url(BLANK), BAD_REQUEST, "url");
    }

    public void testCreateWithInvalidIssue()
    {
        createOrUpdateExpectingFailure(ISSUE_THAT_DOES_NOT_EXIST, populatedCreateOrUpdateRequest(), NOT_FOUND);
    }

    public void testCreateWithInvalidUrls()
    {
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest().url(INVALID_URI), BAD_REQUEST, "url");
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest().iconUrl(INVALID_URI), BAD_REQUEST, "iconUrl");
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest().statusIconUrl(INVALID_URI), BAD_REQUEST, "statusIconUrl");
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest().statusIconLink(INVALID_URI), BAD_REQUEST, "statusIconLink");
    }

    public void testCreateWhenLinkingDisabled()
    {
        administration.issueLinking().disable();
        createOrUpdateExpectingFailure(ISSUE_KEY, populatedCreateOrUpdateRequest(), FORBIDDEN);
    }

    public void testUpdate()
    {
        final String linkId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();
        updateExpectingSuccess(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest());
    }

    public void testUpdateWithoutNonRequiredFields()
    {
        final String linkId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();

        // Set non-required fields to null
        final RemoteIssueLinkCreateOrUpdateRequest request = populatedCreateOrUpdateRequest()
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
                .applicationName(null);

        updateExpectingSuccess(ISSUE_KEY, linkId, request);
    }

    public void testUpdateWithBlankNonRequiredFields()
    {
        final String linkId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();

        // Set non-required string fields to blank string
        // Any fields validated as a URI must be empty, otherwise invalid URI
        final RemoteIssueLinkCreateOrUpdateRequest request = populatedCreateOrUpdateRequest()
                .globalId(BLANK)
                .summary(BLANK)
                .iconUrl("")
                .iconTitle(BLANK)
                .relationship(BLANK)
                .statusIconUrl("")
                .statusIconTitle(BLANK)
                .statusIconLink("")
                .applicationType(BLANK)
                .applicationName(BLANK);

        updateExpectingSuccess(ISSUE_KEY, linkId, request);
    }

    public void testUpdateWithoutRequiredFields()
    {
        final String linkId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();

        // Set required fields to null
        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest().title(null), BAD_REQUEST, "title");
        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest().url(null), BAD_REQUEST, "url");
    }

    public void testUpdateWithBlankRequiredFields()
    {
        final String linkId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();

        // Set required string fields to blank string
        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest().title(BLANK), BAD_REQUEST, "title");
        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest().url(BLANK), BAD_REQUEST, "url");
    }

    public void testUpdateWithInvalidIssue()
    {
        final String linkId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();
        updateExpectingFailure(ISSUE_THAT_DOES_NOT_EXIST, linkId, populatedCreateOrUpdateRequest(), NOT_FOUND);
    }

    public void testUpdateWithInvalidLinkId()
    {
        updateExpectingFailure(ISSUE_KEY, LINK_ID_THAT_DOES_NOT_EXIST, populatedCreateOrUpdateRequest(), NOT_FOUND);
    }

    public void testUpdateWithInvalidUrls()
    {
        final String linkId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();

        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest().url(INVALID_URI), BAD_REQUEST, "url");
        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest().iconUrl(INVALID_URI), BAD_REQUEST, "iconUrl");
        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest().statusIconUrl(INVALID_URI), BAD_REQUEST, "statusIconUrl");
        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest().statusIconLink(INVALID_URI), BAD_REQUEST, "statusIconLink");
    }

    public void testUpdateWhenLinkingDisabled()
    {
        final String linkId = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();

        administration.issueLinking().disable();
        updateExpectingFailure(ISSUE_KEY, linkId, populatedCreateOrUpdateRequest(), FORBIDDEN);
    }

    public void testDelete()
    {
        // Create a remote issue link to delete
        final String id = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();
        deleteExpectingSuccess(ISSUE_KEY, id);
    }

    public void testDeleteIdDoesNotExist()
    {
        deleteExpectingFailure(ISSUE_KEY, LINK_ID_THAT_DOES_NOT_EXIST, NOT_FOUND);
    }

    public void testDeleteIdNotNumeric()
    {
        deleteExpectingFailure(ISSUE_KEY, "99a", BAD_REQUEST);
    }

    public void testDeleteWhenLinkingDisabled()
    {
        // Create a remote issue link to delete
        final String id = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();

        administration.issueLinking().disable();
        deleteExpectingFailure(ISSUE_KEY, id, FORBIDDEN);
    }

    public void testDeleteByGlobalId()
    {
        final String globalId = UUID.randomUUID().toString();
        // Create a remote issue link to delete
        createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest().globalId(globalId)).id().toString();
        deleteByGlobalIdExpectingSuccess(ISSUE_KEY, globalId);
    }

    public void testDeleteByGlobalIdDeletesMany()
    {
        deleteByGlobalIdExpectingSuccess(THIRD_ISSUE_KEY, "example.org");
    }

    public void testDeleteByGlobalIdWhenGlobalIdDoesNotExist()
    {
        final String globalId = UUID.randomUUID().toString();
        deleteByGlobalIdExpectingFailure(ISSUE_KEY, globalId, NOT_FOUND);
    }

    public void testDeleteByGlobalIdWhenLinkingDisabled()
    {
        final String globalId = UUID.randomUUID().toString();
        // Create a remote issue link to delete
        createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest().globalId(globalId));

        administration.issueLinking().disable();
        deleteByGlobalIdExpectingFailure(ISSUE_KEY, globalId, FORBIDDEN);
    }

    public void testGetWhenDoesNotExist()
    {
        final Response response = issueClient.getRemoteIssueLinkResponse(ISSUE_KEY, LINK_ID_THAT_DOES_NOT_EXIST);
        assertFailure(response, NOT_FOUND);
    }

    public void testGetWhenLinkingDisabled()
    {
        // Create a remote issue link
        final String id = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest()).id().toString();

        administration.issueLinking().disable();
        final Response response = issueClient.getRemoteIssueLinkResponse(ISSUE_KEY, id);
        assertFailure(response, FORBIDDEN);
    }

    public void testGetForIssue()
    {
        // Create a couple of links for an issue, and one for another issue
        final RemoteIssueLinkCreateOrUpdateRequest request1 = populatedCreateOrUpdateRequest();
        final RemoteIssueLinkCreateOrUpdateRequest request2 = populatedCreateOrUpdateRequest("6789");
        final RemoteIssueLinkCreateOrUpdateRequest request3 = populatedCreateOrUpdateRequest("999");
        final RemoteIssueLinkCreateOrUpdateResponse response1 = createOrUpdateExpectingSuccess(ISSUE_KEY, request1);
        final RemoteIssueLinkCreateOrUpdateResponse response2 = createOrUpdateExpectingSuccess(ISSUE_KEY, request2);
        createOrUpdateExpectingSuccess(ANOTHER_ISSUE_KEY, request3);

        // Check links after request, should have two new links
        final List<RemoteIssueLink> linksAfter = issueClient.getRemoteIssueLinks(ISSUE_KEY);
        assertEquals(2, linksAfter.size());

        // Check the first remote issue link
        if (response1.id().equals(linksAfter.get(0).id))
        {
            assertRequestEqualsLink(request1, linksAfter.get(0));
        }
        else if (response1.id().equals(linksAfter.get(1).id))
        {
            assertRequestEqualsLink(request1, linksAfter.get(1));
        }
        else
        {
            fail("The remote issue link was not found in the links for issue query");
        }

        // Check the second remote issue link
        if (response2.id().equals(linksAfter.get(0).id))
        {
            assertRequestEqualsLink(request2, linksAfter.get(0));
        }
        else if (response2.id().equals(linksAfter.get(1).id))
        {
            assertRequestEqualsLink(request2, linksAfter.get(1));
        }
        else
        {
            fail("The remote issue link was not found in the links for issue query");
        }
    }

    public void testGetForIssueWhenLinkingDisabled()
    {
        // Create a remote issue link
        createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest());

        administration.issueLinking().disable();
        final Response response = issueClient.getRemoteIssueLinksResponse(ISSUE_KEY);
        assertFailure(response, FORBIDDEN);
    }

    public void testGetByGlobalId()
    {
        final String globalId = "url=http://www.blah.com&id=99999";

        // Create two remote issue links for an issue, with different globalIds
        final Long id = createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest().globalId(globalId)).id();
        createOrUpdateExpectingSuccess(ISSUE_KEY, populatedCreateOrUpdateRequest());
        // Also create a remote issue link for another issue, with the same url
        createOrUpdateExpectingSuccess(ANOTHER_ISSUE_KEY, populatedCreateOrUpdateRequest().globalId(globalId));

        final RemoteIssueLink found = issueClient.getRemoteIssueLinkByGlobalId(ISSUE_KEY, globalId);
        assertEquals(id, found.id);
        assertEquals(globalId, found.globalId);
    }

    private RemoteIssueLinkCreateOrUpdateResponse createOrUpdateExpectingSuccess(final String issueKey, final RemoteIssueLinkCreateOrUpdateRequest request)
    {
        final RemoteIssueLinkCreateOrUpdateResponse response = issueClient.createOrUpdateRemoteIssueLink(issueKey, request);
        assertNotNull(response.id());
        assertEquals(getRestApiUrl("issue", issueKey, "remotelink", response.id().toString()), response.self());
        assertExists(issueKey, response.id(), request);

        return response;
    }

    private Response createOrUpdateExpectingFailure(final String issueKey, final RemoteIssueLinkCreateOrUpdateRequest request, final int statusCode, final String ... erroneousFields)
    {
        final Response response = issueClient.createOrUpdateRemoteIssueLinkAndGetResponse(issueKey, request);
        assertFailure(response, statusCode, erroneousFields);
        return response;
    }

    private Response updateExpectingSuccess(final String issueKey, final String linkId, final RemoteIssueLinkCreateOrUpdateRequest request)
    {
        final Response response = issueClient.updateRemoteIssueLink(issueKey, linkId, request);
        assertEquals(NO_CONTENT, response.statusCode);
        assertExists(issueKey, new Long(linkId), request);

        return response;
    }

    private Response updateExpectingFailure(final String issueKey, final String linkId, final RemoteIssueLinkCreateOrUpdateRequest request, final int statusCode, final String ... erroneousFields)
    {
        final Response response = issueClient.updateRemoteIssueLink(issueKey, linkId, request);
        assertFailure(response, statusCode, erroneousFields);
        return response;
    }

    private Response deleteExpectingSuccess(final String issueKey, final String remoteIssueLinkId)
    {
        final Response response = issueClient.deleteRemoteIssueLink(issueKey, remoteIssueLinkId);
        assertNotExists(issueKey, remoteIssueLinkId);
        return response;
    }

    private Response deleteExpectingFailure(final String issueKey, final String remoteIssueLinkId, final int statusCode, final String ... erroneousFields)
    {
        final Response response = issueClient.deleteRemoteIssueLink(issueKey, remoteIssueLinkId);
        assertFailure(response, statusCode, erroneousFields);
        return response;
    }

    private Response deleteByGlobalIdExpectingSuccess(final String issueKey, final String globalId)
    {
        final Response response = issueClient.deleteRemoteIssueLinkByGlobalId(issueKey, globalId);
        final Iterable<RemoteIssueLink> remoteIssueLinks = issueClient.getRemoteIssueLinks(issueKey);
        assertThat(remoteIssueLinks, not(Matchers.<RemoteIssueLink>hasItem(withGlobalId(globalId))));
        return response;
    }

    private Response deleteByGlobalIdExpectingFailure(final String issueKey, final String globalId, final int statusCode, final String ... erroneousFields)
    {
        final Response response = issueClient.deleteRemoteIssueLinkByGlobalId(issueKey, globalId);
        assertFailure(response, statusCode, erroneousFields);
        return response;
    }

    private void assertExists(final String issueKey, final Long remoteIssueLinkId, final RemoteIssueLinkCreateOrUpdateRequest request)
    {
        final RemoteIssueLink found = issueClient.getRemoteIssueLink(issueKey, remoteIssueLinkId.toString());
        assertEquals(remoteIssueLinkId, found.id);
        assertRequestEqualsLink(request, found);
    }

    private void assertNotExists(final String issueKey, final String remoteIssueLinkId)
    {
        final Response response = issueClient.getRemoteIssueLinkResponse(issueKey, remoteIssueLinkId);
        assertEquals(NOT_FOUND, response.statusCode);
    }

    private void assertRequestEqualsLink(final RemoteIssueLinkCreateOrUpdateRequest request, final RemoteIssueLink remoteIssueLink)
    {
        assertEquals(request.globalId(), remoteIssueLink.globalId);
        assertEquals(request.title(), remoteIssueLink.object.title);
        assertEquals(request.summary(), remoteIssueLink.object.summary);
        assertEquals(request.url(), remoteIssueLink.object.url);
        assertEquals(request.iconUrl(), remoteIssueLink.object.icon.url16x16);
        assertEquals(request.iconTitle(), remoteIssueLink.object.icon.title);
        assertEquals(request.relationship(), remoteIssueLink.relationship);
        assertEquals(request.resolved(), remoteIssueLink.object.status.resolved);
        assertEquals(request.statusIconUrl(), remoteIssueLink.object.status.icon.url16x16);
        assertEquals(request.statusIconTitle(), remoteIssueLink.object.status.icon.title);
        assertEquals(request.statusIconLink(), remoteIssueLink.object.status.icon.link);
        assertEquals(request.applicationType(), remoteIssueLink.application.type);
        assertEquals(request.applicationName(), remoteIssueLink.application.name);
    }

    private void assertFailure(final Response response, final int statusCode, final String ... erroneousFields)
    {
        assertEquals(statusCode, response.statusCode);

        for (final String erroneousField : erroneousFields)
        {
            assertTrue(response.entity.errors.containsKey(erroneousField));
        }
    }

    private RemoteIssueLinkCreateOrUpdateRequest populatedCreateOrUpdateRequest()
    {
        return populatedCreateOrUpdateRequest("12345");
    }

    private RemoteIssueLinkCreateOrUpdateRequest populatedCreateOrUpdateRequest(final String remoteId)
    {
        return new RemoteIssueLinkCreateOrUpdateRequest()
                .globalId("url=http://www.remoteapplication.com&id=" + remoteId)
                .title("Ticket #" + remoteId)
                .summary("Summary of the ticket goes here")
                .url("http://www.remoteapplication.com/ticket/" + remoteId)
                .iconUrl("http://www.remoteapplication.com/images/ticket.gif")
                .iconTitle("Ticket")
                .relationship("relates to")
                .resolved(false)
                .statusIconUrl("http://www.remoteapplication.com/images/status.gif")
                .statusIconTitle("Status")
                .statusIconLink("http://www.remoteapplication.com/blah")
                .applicationType("com.mycompany.myhelpdesksystem")
                .applicationName("My Company IT Helpdesk");
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestRemoteIssueLinkResource.xml");
    }

    static Matcher<RemoteIssueLink> withGlobalId(String globalId) {
        return new RemoteIssueLinkGlobalIdIsEqual(globalId);
    }

    private static class RemoteIssueLinkGlobalIdIsEqual extends BaseMatcher<RemoteIssueLink>
    {
        private final String globalId;

        public RemoteIssueLinkGlobalIdIsEqual(String globalId) {
            this.globalId = globalId;
        }


        @Override
        public boolean matches(Object o)
        {
            final RemoteIssueLink link = (RemoteIssueLink) o;
            return ObjectUtils.equals(link.globalId, globalId);
        }

        @Override
        public void describeTo(Description desc)
        {
            desc.appendText("{globalId is ")
                .appendValue(globalId)
                .appendText("}");
        }
    }
}
