package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusCategoryJsonBean;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.util.StatusCategoryHelper;
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
 * Unit test for StatusCategoryResource
 *
 * @since v6.1
 */
public class StatusCategoryResourceTest extends TestCase
{
    static final String JIRA_BASE_URI = "http://localhost:8090/jira/";

    private JiraAuthenticationContext authContext;
    private ConstantsService constantsService;
    private ResourceUriBuilder uriBuilder;
    private StatusCategoryHelper statusCategoryHelper;
    private JiraBaseUrls jiraBaseUrls;

    public void testStatusFound() throws Exception
    {
        final Long categoryId = 2L;
        final String categoryKey = "category key";
        final String categoryColorName = "category color";
        final String categorySelf = "category self";
        final String categoryName = "The translated name of the category";

        final StatusCategory category = createMock(StatusCategory.class);
        expect(category.getId()).andReturn(categoryId).times(0, Integer.MAX_VALUE);
        expect(category.getKey()).andReturn(categoryKey).times(0, Integer.MAX_VALUE);
        expect(category.getColorName()).andReturn(categoryColorName).times(0, Integer.MAX_VALUE);

        final Request request = createMock(Request.class);
        final UriInfo uriInfo = createMock(UriInfo.class);

        final User user = new MockUser("mockUser");
        expect(authContext.getLoggedInUser()).andReturn(user).times(0, Integer.MAX_VALUE);
        expect(jiraBaseUrls.baseUrl()).andReturn(JIRA_BASE_URI);
        expect(constantsService.getStatusCategoryById(user, String.valueOf(categoryId))).andReturn(ServiceOutcomeImpl.ok(category));
        expect(statusCategoryHelper.createStatusCategoryBean(category, uriInfo, StatusCategoryResource.class)).andReturn(
                new StatusCategoryJsonBean(categorySelf, categoryId, categoryKey, categoryColorName, categoryName));

        replayMocks(category, request, uriInfo);
        final StatusCategoryResource statusCategoryResource = new StatusCategoryResource(authContext, constantsService, statusCategoryHelper);
        final Response resp = statusCategoryResource.getStatusCategory(String.valueOf(categoryId), request, uriInfo);

        assertEquals(HttpStatus.SC_OK, resp.getStatus());
        final StatusCategoryJsonBean categoryFromResponse = (StatusCategoryJsonBean) resp.getEntity();
        assertEquals(categoryId, categoryFromResponse.id());
        assertEquals(categoryKey, categoryFromResponse.key());
        assertEquals(categoryColorName, categoryFromResponse.colorName());
        assertEquals(categorySelf, categoryFromResponse.self());
        assertEquals(categoryName, categoryFromResponse.name());
    }

    public void testStatusCategoryNotFound() throws Exception
    {
        final String categoryName = "somecategory";

        final Request request = createMock(Request.class);
        final UriInfo uriInfo = createMock(UriInfo.class);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        errors.addError("somefield", "somemessage");

        final User user = new MockUser("mockUser");
        expect(authContext.getLoggedInUser()).andReturn(user).times(0, Integer.MAX_VALUE);
        expect(constantsService.getStatusCategoryById(user, categoryName)).andReturn(new ServiceOutcomeImpl<StatusCategory>(errors, null));
        expect(constantsService.getStatusCategoryByKey(user, categoryName)).andReturn(new ServiceOutcomeImpl<StatusCategory>(errors, null));
        expect(uriBuilder.build(uriInfo, StatusResource.class, categoryName)).andReturn(new URI(JIRA_BASE_URI + "/icon.jpg"));

        replayMocks(request, uriInfo);
        final StatusCategoryResource statusCategoryResource = new StatusCategoryResource(authContext, constantsService, statusCategoryHelper);
        try
        {
            statusCategoryResource.getStatusCategory(categoryName, request, uriInfo);
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
        statusCategoryHelper = createMock(StatusCategoryHelper.class);

    }

    protected void replayMocks(final Object... mocks)
    {
        replay(mocks);
        replay(
                authContext,
                constantsService,
                jiraBaseUrls,
                uriBuilder,
                statusCategoryHelper
        );
    }
}
