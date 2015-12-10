package com.atlassian.jira.rest.v2.issue.version;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkImpl;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.DeleteValidationResult;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.PutValidationResult;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.entity.RemoteEntityLinkJsonBean;
import com.atlassian.jira.rest.v2.entity.RemoteEntityLinksJsonBean;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.rest.v2.issue.VersionResource;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.util.OutlookDateManager;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkServiceResultFactory.deleteValidationResult;
import static com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkServiceResultFactory.putValidationResult;
import static com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkServiceResultFactory.remoteVersionLinkResult;
import static com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkServiceResultFactory.remoteVersionLinkListResult;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertLocation;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseBody;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseCacheNever;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertStatus;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since JIRA REST v6.5.1 (JIRA v6.1.1)
 */
@RunWith (MockitoJUnitRunner.class)
public class TestRemoteVersionLinkResource
{
    private static final URI BASE_URI = URI.create("http://www.example.com/rest/api/2");

    private ApplicationUser fred = new MockApplicationUser("Fred");
    private I18nHelper i18n = new NoopI18nHelper();
    private ContextUriInfo info = mock(ContextUriInfo.class, BaseUriBuilderFactory.INSTANCE);
    @Mock private OutlookDateManager outlookDateManager;
    private JiraAuthenticationContext jiraAuthenticationContext = new MockAuthenticationContext(fred.getDirectoryUser(), i18n);
    private VersionResource versionResource;
    private RemoteVersionLinkResource remoteVersionLinkResource;

    @Mock private RemoteVersionLinkService remoteVersionLinkService;
    @Mock private JsonEntityPropertyManager jsonEntityPropertyManager;


    @Before
    public void setUp()
    {
        remoteVersionLinkResource = new RemoteVersionLinkResource(i18n,
                jiraAuthenticationContext, remoteVersionLinkService, jsonEntityPropertyManager, info);
        versionResource = new VersionResource(null, null, jiraAuthenticationContext, i18n,
                remoteVersionLinkResource, null, null, null, null, null);
    }

    @After
    public void tearDown()
    {
        fred = null;
        i18n = null;
        info = null;
        jiraAuthenticationContext = null;
        remoteVersionLinkService = null;
        remoteVersionLinkResource = null;
        versionResource = null;
    }



    @Test
    public void testCreateOrUpdateRemoteVersionLinkValidationFailure()
    {
        final Version version = new MockVersion(42L, "1.0");
        when(remoteVersionLinkService.validatePut(fred, version.getId(), "gid", "true"))
                .thenReturn(putValidationResult(errors("nope!")));
        try
        {
            fail("Should fail: " + versionResource.createOrUpdateRemoteVersionLink("42", "gid", "true"));
        }
        catch (RESTException e)
        {
            assertNope(e);
        }

        verify(remoteVersionLinkService, never()).put(any(ApplicationUser.class), any(PutValidationResult.class));
    }


    @Test
    public void testCreateOrUpdateRemoteVersionLinkPutFailure()
    {
        final Version version = new MockVersion(42L, "1.0");
        final PutValidationResult validated = putValidationResult(version, "gid", "true");
        when(remoteVersionLinkService.validatePut(fred, version.getId(), null, "true"))
                .thenReturn(validated);
        when(remoteVersionLinkService.put(fred, validated))
                .thenReturn(remoteVersionLinkResult(errors("nope!")));

        try
        {
            fail("Should fail: " + versionResource.createOrUpdateRemoteVersionLink("42", "true"));
        }
        catch (RESTException e)
        {
            assertNope(e);
        }
    }

    @Test
    public void testCreateOrUpdateRemoteVersionLinkSuccess()
    {
        final Version version = new MockVersion(42L, "1.0");
        final PutValidationResult validated = putValidationResult(version, "gid", "true");
        when(remoteVersionLinkService.validatePut(fred, version.getId(), null, "true"))
                .thenReturn(validated);
        when(remoteVersionLinkService.put(fred, validated))
                .thenReturn(remoteVersionLinkResult(new RemoteVersionLinkImpl(version, "gid", "true")));

        final Response actualResponse = versionResource.createOrUpdateRemoteVersionLink("42", null, "true");

        assertResponseCacheNever(actualResponse);
        assertResponseBody(null, actualResponse);
        assertLocation(selfUri(version, "gid"), actualResponse);
        assertStatus(Response.Status.CREATED, actualResponse);
    }

    @Test
    public void testGetRemoteVersionLinkFailure()
    {
        when(remoteVersionLinkService.getRemoteVersionLinkByVersionIdAndGlobalId(fred, 42L, "gid"))
                .thenReturn(remoteVersionLinkResult(errors("nope!")));

        try
        {
            fail("Should have failed, but got: " + versionResource.getRemoteVersionLink("42", "gid"));
        }
        catch (RESTException e)
        {
            assertNope(e);
        }
    }

    @Test
    public void testGetRemoteVersionLinkSuccess()
    {
        final Version version = new MockVersion(42L, "1.0");
        when(remoteVersionLinkService.getRemoteVersionLinkByVersionIdAndGlobalId(fred, 42L, "gid"))
                .thenReturn(remoteVersionLinkResult(new RemoteVersionLinkImpl(version, "gid", "true")));

        final Response actualResponse = versionResource.getRemoteVersionLink("42", "gid");

        assertResponseCacheNever(actualResponse);
        assertResponseBody(new RemoteEntityLinkJsonBean()
                .self(selfUri(version, "gid"))
                .link("true"),
                actualResponse);
        assertStatus(Response.Status.OK, actualResponse);
    }

    @Test
    public void testGetRemoteVersionLinksByVersionIdFailure()
    {
        when(remoteVersionLinkService.getRemoteVersionLinksByVersionId(fred, 42L))
                .thenReturn(remoteVersionLinkListResult(errors("nope!")));

        try
        {
            fail("Should have failed, but got: " + versionResource.getRemoteVersionLinksByVersionId("42"));
        }
        catch (RESTException e)
        {
            assertNope(e);
        }
    }

    @Test
    public void testGetRemoteVersionLinksByVersionIdSuccess()
    {
        final Version version = new MockVersion(42L, "1.0");
        when(remoteVersionLinkService.getRemoteVersionLinksByVersionId(fred, 42L))
                .thenReturn(remoteVersionLinkListResult(
                        new RemoteVersionLinkImpl(version, "gid1", "true"),
                        new RemoteVersionLinkImpl(version, "gid2", "true")));

        final Response actualResponse = versionResource.getRemoteVersionLinksByVersionId("42");

        assertResponseCacheNever(actualResponse);
        assertResponseBody(new RemoteEntityLinksJsonBean().links(ImmutableList.of(
                new RemoteEntityLinkJsonBean().self(selfUri(version, "gid1")).link("true"),
                new RemoteEntityLinkJsonBean().self(selfUri(version, "gid2")).link("true") )),
                actualResponse);
        assertStatus(Response.Status.OK, actualResponse);
    }

    @Test
    public void testGetRemoteVersionLinksByGlobalIdFailure()
    {
        when(remoteVersionLinkService.getRemoteVersionLinksByGlobalId(fred, "gid"))
                .thenReturn(remoteVersionLinkListResult(errors("nope!")));
        try
        {
            fail("Should have failed, but got: " + versionResource.getRemoteVersionLinks("gid"));
        }
        catch (RESTException e)
        {
            assertNope(e);
        }
    }

    @Test
    public void testGetRemoteVersionLinksByGlobalIdSuccess()
    {
        final Version version1 = new MockVersion(42L, "1.0");
        final Version version2 = new MockVersion(43L, "2.0");

        when(remoteVersionLinkService.getRemoteVersionLinksByGlobalId(fred, "gid"))
                .thenReturn(remoteVersionLinkListResult(
                        new RemoteVersionLinkImpl(version1, "gid", "true"),
                        new RemoteVersionLinkImpl(version2, "gid", "true")));

        final Response actualResponse = versionResource.getRemoteVersionLinks("gid");

        assertResponseCacheNever(actualResponse);
        assertResponseBody(new RemoteEntityLinksJsonBean().links(ImmutableList.of(
                new RemoteEntityLinkJsonBean().self(selfUri(version1, "gid")).link("true"),
                new RemoteEntityLinkJsonBean().self(selfUri(version2, "gid")).link("true") )),
                actualResponse);
        assertStatus(Response.Status.OK, actualResponse);
    }

    @Test
    public void testDeleteRemoteVersionLinkFailure()
    {
        final Version version = new MockVersion(42L, "1.0");
        when(remoteVersionLinkService.validateDelete(fred, version.getId(), "gid"))
                .thenReturn(deleteValidationResult(errors("nope!")));
        try
        {
            fail("Should fail: " + versionResource.deleteRemoteVersionLink("42", "gid"));
        }
        catch (RESTException e)
        {
            assertNope(e);
        }

        verify(remoteVersionLinkService, never()).delete(any(ApplicationUser.class), any(DeleteValidationResult.class));
    }


    @Test
    public void testDeleteRemoteVersionLinkSuccess()
    {
        final Version version = new MockVersion(42L, "1.0");
        final DeleteValidationResult validated = deleteValidationResult(version, "gid");
        when(remoteVersionLinkService.validateDelete(fred, version.getId(), null))
                .thenReturn(validated);

        final Response actualResponse = versionResource.deleteRemoteVersionLink("42", null);

        assertResponseCacheNever(actualResponse);
        assertResponseBody(null, actualResponse);
        assertStatus(Response.Status.NO_CONTENT, actualResponse);
        verify(remoteVersionLinkService).delete(fred, validated);
    }


    @Test
    public void testDeleteRemoteVersionLinksByVersionIdFailure()
    {
        final Version version = new MockVersion(42L, "1.0");
        when(remoteVersionLinkService.validateDeleteByVersionId(fred, version.getId()))
                .thenReturn(deleteValidationResult(errors("nope!")));
        try
        {
            fail("Should fail: " + versionResource.deleteRemoteVersionLinksByVersionId("42"));
        }
        catch (RESTException e)
        {
            assertNope(e);
        }

        verify(remoteVersionLinkService, never()).delete(any(ApplicationUser.class), any(DeleteValidationResult.class));
    }


    @Test
    public void testDeleteRemoteVersionLinksByVersionIdSuccess()
    {
        final Version version = new MockVersion(42L, "1.0");
        final DeleteValidationResult validated = deleteValidationResult(version, null);
        when(remoteVersionLinkService.validateDeleteByVersionId(fred, version.getId()))
                .thenReturn(validated);

        final Response actualResponse = versionResource.deleteRemoteVersionLinksByVersionId("42");

        assertResponseCacheNever(actualResponse);
        assertResponseBody(null, actualResponse);
        assertStatus(Response.Status.NO_CONTENT, actualResponse);
        verify(remoteVersionLinkService).delete(fred, validated);
    }




    private static URI selfUri(Version version, String globalId)
    {
        return selfUri(version.getId(), globalId);
    }

    private static URI selfUri(long versionId, String globalId)
    {
        return UriBuilder.fromUri(BASE_URI)
                .path("version")
                .path(String.valueOf(versionId))
                .path("remotelink")
                .path(globalId)
                .build();
    }

    private static com.atlassian.jira.rest.api.util.ErrorCollection restErrors(String... errors)
    {
        return com.atlassian.jira.rest.api.util.ErrorCollection.of(errors);
    }

    private static com.atlassian.jira.rest.api.util.ErrorCollection restErrors(ErrorCollection.Reason reason, String... errors)
    {
        return com.atlassian.jira.rest.api.util.ErrorCollection.of(errors).reason(reason);
    }

    private static void assertNope(final RESTException e)
    {
        final Response re = e.getResponse();
        assertResponseCacheNever(re);
        assertResponseBody(com.atlassian.jira.rest.api.util.ErrorCollection.of("nope!").reason(null), re);
    }

    private static ErrorCollection errors(String... errors)
    {
        final SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessages(Arrays.asList(errors));
        return collection;
    }


    static class BaseUriBuilderFactory implements Answer<UriBuilder>
    {
        static final BaseUriBuilderFactory INSTANCE = new BaseUriBuilderFactory();

        @Override
        public UriBuilder answer(final InvocationOnMock invocationOnMock) throws Throwable
        {
            return UriBuilder.fromUri(BASE_URI);
        }
    }
}
