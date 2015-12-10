package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.util.StatusHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpStatus;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Unit test for StatusResource.
 *
 * @since v4.2
 */
public class StatusResourceTest extends TestCase
{
    static final String JIRA_BASE_URI = "http://localhost:8090/jira/";

	private JiraAuthenticationContext authContext;
    private ConstantsService constantsService;
    private JiraBaseUrls jiraBaseUrls;
    private ResourceUriBuilder uriBuilder;
	private StatusHelper statusHelper;

    public void testStatusFound() throws Exception
    {
        final String statusName = "superstatus";
        final String statusNameTranslated = "Super status";
        final String statusId = "statusId";
		final String statusSelf = "status self";
		final String statusColor = "status red";
		final String statusDesc = "This is a formidable status";
		final String statusIconUrl = "iconUrl";

		final Status status = createMock(Status.class);
		expect(status.getId()).andReturn(statusId).times(0, Integer.MAX_VALUE);
		expect(status.getName()).andReturn(statusName).times(0, Integer.MAX_VALUE);
		expect(status.getIconUrl()).andReturn(statusIconUrl).times(0, Integer.MAX_VALUE);
		expect(status.getNameTranslation()).andReturn(statusNameTranslated).times(0, Integer.MAX_VALUE);
		expect(status.getDescTranslation()).andReturn(statusDesc).times(0, Integer.MAX_VALUE);

        final Request request = createMock(Request.class);
        final UriInfo uriInfo = createMock(UriInfo.class);

        final User user = new MockUser("mockUser");
        expect(authContext.getLoggedInUser()).andReturn(user).times(0, Integer.MAX_VALUE);
        expect(jiraBaseUrls.baseUrl()).andReturn("http://localhost:8090/jira");
        expect(constantsService.getStatusById(user, statusName)).andReturn(ServiceOutcomeImpl.ok(status));
        expect(uriBuilder.build(uriInfo, StatusResource.class, statusId)).andReturn(new URI(JIRA_BASE_URI + "/icon.jpg"));
		expect(statusHelper.createStatusBean(status, uriInfo, StatusResource.class)).andReturn(
				new StatusJsonBean(statusSelf,statusColor,statusDesc,statusIconUrl,statusNameTranslated,statusId));

				replayMocks(status, request, uriInfo);
        final StatusResource statusResource = new StatusResource(authContext, constantsService, statusHelper);
        final Response resp = statusResource.getStatus(statusName, request, uriInfo);

        assertEquals(HttpStatus.SC_OK, resp.getStatus());
		final StatusJsonBean statusFromResponse = (StatusJsonBean) resp.getEntity();
		assertEquals(statusNameTranslated, statusFromResponse.name());
		assertEquals(statusSelf, statusFromResponse.self());
		assertEquals(statusColor, statusFromResponse.statusColor());
		assertEquals(statusDesc, statusFromResponse.description());
		assertEquals(statusIconUrl, statusFromResponse.iconUrl());
		assertEquals(statusId, statusFromResponse.id());
    }

    public void testStatusNotFound() throws Exception
    {
        final String statusName = "superstatus";

        final Request request = createMock(Request.class);
        final UriInfo uriInfo = createMock(UriInfo.class);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        errors.addError("somefield", "somemessage");

        final User user = new MockUser("mockUser");
        expect(authContext.getLoggedInUser()).andReturn(user).times(0, Integer.MAX_VALUE);
        expect(constantsService.getStatusById(user, statusName)).andReturn(new ServiceOutcomeImpl<Status>(errors, null));
        expect(constantsService.getStatusByTranslatedName(user, statusName)).andReturn(new ServiceOutcomeImpl<Status>(errors, null));
        expect(uriBuilder.build(uriInfo, StatusResource.class, statusName)).andReturn(new URI(JIRA_BASE_URI + "/icon.jpg"));

        replayMocks(request, uriInfo);
        final StatusResource statusResource = new StatusResource(authContext, constantsService, statusHelper);
        try
        {
            statusResource.getStatus(statusName, request, uriInfo);
            fail("Expected NotFoundWebException");
        }
        catch (NotFoundWebException e)
        {
            // success
        }
    }
    
    @Override
    protected void setUp() throws Exception
    {
        authContext = createMock(JiraAuthenticationContext.class);
        constantsService = createMock(ConstantsService.class);
        jiraBaseUrls = createMock(JiraBaseUrls.class);
        uriBuilder = createMock(ResourceUriBuilder.class);
        statusHelper = createMock(StatusHelper.class);

    }

    protected void replayMocks(final Object... mocks)
    {
        replay(mocks);
        replay(
                authContext,
                constantsService,
                jiraBaseUrls,
                uriBuilder,
				statusHelper
        );
    }
}
