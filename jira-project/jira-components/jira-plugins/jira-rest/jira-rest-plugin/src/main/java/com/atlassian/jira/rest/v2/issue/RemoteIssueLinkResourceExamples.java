package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateRequest;
import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateResponse;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Example JSON payloads for remote issue link use cases.
 *
 * @since v5.0
 */
public class RemoteIssueLinkResourceExamples
{
    public static final RemoteIssueLinkBean GET_EXAMPLE = bean();
    private static RemoteIssueLinkBean bean()
    {
        try
        {
            return new RemoteIssueLinkBean(
                    10000L,
                    new URI("http://www.example.com/jira/rest/api/issue/MKY-1/remotelink/10000"),
                    "system=http://www.mycompany.com/support&id=1",
                    "com.acme.tracker",
                    "My Acme Tracker",
                    "causes",
                    "http://www.mycompany.com/support?id=1",
                    "TSTSUP-111",
                    "Crazy customer support issue",
                    "http://www.mycompany.com/support/ticket.png",
                    "Support Ticket",
                    true,
                    "http://www.mycompany.com/support/resolved.png",
                    "Case Closed",
                    "http://www.mycompany.com/support?id=1&details=closed"
            );
        }
        catch (URISyntaxException e)
        {
            // not possible if the URIs are fine;
            throw new RuntimeException(e);
        }
    }

    public static final RemoteIssueLinkBean ANOTHER_GET_EXAMPLE = anotherBean();
    private static RemoteIssueLinkBean anotherBean()
    {
        try
        {
            return new RemoteIssueLinkBean(
                    10001L,
                    new URI("http://www.example.com/jira/rest/api/issue/MKY-1/remotelink/10001"),
                    "system=http://www.anothercompany.com/tester&id=1234",
                    "com.acme.tester",
                    "My Acme Tester",
                    "is tested by",
                    "http://www.anothercompany.com/tester/testcase/1234",
                    "Test Case #1234",
                    "Test that the submit button saves the thing",
                    "http://www.anothercompany.com/tester/images/testcase.gif",
                    "Test Case",
                    false,
                    "http://www.anothercompany.com/tester/images/person/fred.gif",
                    "Tested by Fred Jones",
                    "http://www.anothercompany.com/tester/person?username=fred"
            );
        }
        catch (URISyntaxException e)
        {
            // not possible if the URIs are fine;
            throw new RuntimeException(e);
        }
    }

    public static final List<RemoteIssueLinkBean> GET_LIST_EXAMPLE = Arrays.asList(GET_EXAMPLE, ANOTHER_GET_EXAMPLE);

    public static final RemoteIssueLinkCreateOrUpdateRequest CREATE_OR_UPDATE_REQUEST = createOrUpdateRequest();
    private static RemoteIssueLinkCreateOrUpdateRequest createOrUpdateRequest()
    {
        return new RemoteIssueLinkCreateOrUpdateRequest()
                .globalId("system=http://www.mycompany.com/support&id=1")
                .applicationType("com.acme.tracker")
                .applicationName("My Acme Tracker")
                .relationship("causes")
                .url("http://www.mycompany.com/support?id=1")
                .title("TSTSUP-111")
                .summary("Crazy customer support issue")
                .iconUrl("http://www.mycompany.com/support/ticket.png")
                .iconTitle("Support Ticket")
                .resolved(true)
                .statusIconUrl("http://www.mycompany.com/support/resolved.png")
                .statusIconTitle("Case Closed")
                .statusIconLink("http://www.mycompany.com/support?id=1&details=closed");
    }

    public static final RemoteIssueLinkCreateOrUpdateResponse CREATE_OR_UPDATE_RESPONSE_200 = createOrUpdateResponse200();
    private static RemoteIssueLinkCreateOrUpdateResponse createOrUpdateResponse200()
    {
        return new RemoteIssueLinkCreateOrUpdateResponse()
                .id(10000L)
                .self("http://www.example.com/jira/rest/api/issue/MKY-1/remotelink/10000");
    }

    public static final ErrorCollection CREATE_OR_UPDATE_RESPONSE_400 = createOrUpdateResponse400();
    private static ErrorCollection createOrUpdateResponse400()
    {
        final com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
        errors.addError("title", "'title' is required.");
        return ErrorCollection.of(errors);
    }
}
