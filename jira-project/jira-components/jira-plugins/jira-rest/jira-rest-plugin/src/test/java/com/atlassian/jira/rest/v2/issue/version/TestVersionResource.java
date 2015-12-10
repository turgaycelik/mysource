package com.atlassian.jira.rest.v2.issue.version;

import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.core.Response;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.v2.issue.VersionResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.BAD_NAME;
import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.FORBIDDEN;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseBody;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseCacheNever;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertStatus;
import static java.util.EnumSet.of;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
@RunWith (MockitoJUnitRunner.class)
public class TestVersionResource
{
    @Mock
    private EventPublisher eventPublisher;
    
    @Test
    public void testGetVersionBadId() throws Exception
    {
        final VersionService versionService = mock(VersionService.class);

        final ProjectService projectService = mock(ProjectService.class);
        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final I18nHelper i18n = mock(I18nHelper.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        final String translation = "translation";
        final String badId = "badId";

        when(i18n.getText("admin.errors.version.not.exist.with.id", badId)).thenReturn(translation);

        final VersionResource resource = new VersionResource(versionService, projectService, context, i18n, null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);
        try
        {
            resource.getVersion(badId, null);
            fail("Expected exception.");
        }
        catch (NotFoundWebException e)
        {
            final Response re = e.getResponse();
            assertResponseCacheNever(re);
            assertResponseBody(com.atlassian.jira.rest.api.util.ErrorCollection.of(translation), re);
        }
    }

    @Test
    public void testGetVersionVersionDoesNotExist() throws Exception
    {
        final VersionService versionService = mock(VersionService.class);

        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final ProjectService projectMgr = mock(ProjectService.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final I18nHelper i18n = mock(I18nHelper.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        final ApplicationUser user = new MockApplicationUser("BBain");
        final String error = "error";
        final long id = 1;

        when(context.getUser()).thenReturn(user);
        when(versionService.getVersionById(user, id)).thenReturn(new VersionService.VersionResult(errors(error)));

        final VersionResource resource = new VersionResource(versionService, projectMgr, context, i18n, null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);

        try
        {
            resource.getVersion(String.valueOf(id), null);
            fail("Expected exception.");
        }
        catch (NotFoundWebException e)
        {
            final Response re = e.getResponse();
            assertResponseCacheNever(re);
            assertResponseBody(com.atlassian.jira.rest.api.util.ErrorCollection.of(error).reason(Reason.VALIDATION_FAILED), re);
        }
    }

    @Test
    public void testGetVersionGoodVersion() throws Exception
    {
        final VersionService versionService = mock(VersionService.class);

        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final ProjectService projectService = mock(ProjectService.class);
        final I18nHelper i18n = mock(I18nHelper.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        final ApplicationUser user = new MockApplicationUser("BBain");
        final Version version = new MockVersion(1718L, "Version1");
        final long id = 1;

        final VersionBean versionBean = new VersionBean();

        when(context.getUser()).thenReturn(user);
        when(versionService.getVersionById(user, id)).thenReturn(new VersionService.VersionResult(ok(), version));
        when(versionBeanFactory.createVersionBean(version, false, false)).thenReturn(versionBean);

        final VersionResource resource = new VersionResource(versionService, projectService, context, i18n, null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);

        final Response response = resource.getVersion(String.valueOf(id), null);
        assertResponseCacheNever(response);
        assertResponseBody(versionBean, response);
    }

    @Test
    public void testCreateVersionWithoutProject() throws Exception
    {
        final VersionService versionService = mock(VersionService.class);

        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final ProjectService projectService = mock(ProjectService.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        final VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);

        final Response response = resource.createVersion(new VersionBean.Builder().build());
        assertResponseCacheNever(response);
        assertResponseBody(restErrors(NoopI18nHelper.makeTranslation("rest.version.create.no.project")), response);
    }

    @Test
    public void testCreateVersionWithoutTwoReleaseDates() throws Exception
    {
        final VersionService versionService = mock(VersionService.class);

        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final ProjectService projectService = mock(ProjectService.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        final VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);

        final VersionBean.Builder builder = new VersionBean.Builder().setProject("brenden").setProjectId(10000l)
                .setReleaseDate(new Date()).setUserReleaseDate("10/12/2001");
        final Response response = resource.createVersion(builder.build());
        assertResponseCacheNever(response);
        assertResponseBody(restErrors(NoopI18nHelper.makeTranslation("rest.version.create.two.release.dates")), response);
    }
    
    @Test
    public void testCreateVersionBadProject() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("BBain");
        final String key = "BJB";

        final VersionService versionService = mock(VersionService.class);

        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final ProjectService projectService = mock(ProjectService.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        when(context.getUser()).thenReturn(user);
        when(projectService.getProjectByKeyForAction(user.getDirectoryUser(), key, ProjectAction.EDIT_PROJECT_CONFIG))
                .thenReturn(new ProjectService.GetProjectResult(errors("Error"), null));

        final VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);

        final VersionBean.Builder builder = new VersionBean.Builder().setProject(key);
        try
        {
            resource.createVersion(builder.build());
            fail("Expected an exception");
        }
        catch (NotFoundWebException e)
        {
            assertResponseCacheNever(e.getResponse());
            assertResponseBody(restErrors(NoopI18nHelper.makeTranslation("rest.version.no.create.permission", key)), e.getResponse());
        }
    }

    @Test
    public void testCreateVersionOkWithDate() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("BBain");
        final String key = "BJB";
        final MockProject project = new MockProject(2829L);
        final String name = "name";
        final String desc = "description";
        final Date startDate = new Date();
        final Date releaseDate = new Date();
        final VersionBean versionBean = new VersionBean();

        final Version version = new MockVersion(171718L, "Test Version");

        final VersionService versionService = mock(VersionService.class);

        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final ProjectService projectService = mock(ProjectService.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        final VersionService.VersionBuilder versionBuilder = mock(VersionService.VersionBuilder.class);
        when(versionBuilder.projectId(project.getId())).thenReturn(versionBuilder);
        when(versionBuilder.name(name)).thenReturn(versionBuilder);
        when(versionBuilder.description(desc)).thenReturn(versionBuilder);
        when(versionBuilder.startDate(startDate)).thenReturn(versionBuilder);
        when(versionBuilder.releaseDate(releaseDate)).thenReturn(versionBuilder);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final VersionService.VersionBuilderValidationResult validationResult = mock(VersionService.VersionBuilderValidationResult.class);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);
        when(validationResult.isValid()).thenReturn(true);

        when(context.getUser()).thenReturn(user);
        when(projectService.getProjectByIdForAction(user.getDirectoryUser(), project.getId(), ProjectAction.EDIT_PROJECT_CONFIG))
                .thenReturn(new ProjectService.GetProjectResult(ok(), project));
        when(versionService.newBuilder())
                .thenReturn(versionBuilder);
        when(versionService.validateCreate(user.getDirectoryUser(), versionBuilder))
                .thenReturn(validationResult);
        when(versionService.create(user.getDirectoryUser(), validationResult))
                .thenReturn(ServiceOutcomeImpl.ok(version));
        when(versionBeanFactory.createVersionBean(version, false, false)).thenReturn(versionBean);

        final VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);
        final VersionBean.Builder builder = new VersionBean.Builder().setProject(key).setProjectId(project.getId()).setName(name).setDescription(desc)
                .setStartDate(startDate).setReleaseDate(releaseDate);

        final Response actualResponse = resource.createVersion(builder.build());
        assertResponseCacheNever(actualResponse);
        assertResponseBody(versionBean, actualResponse);
    }

    @Test
    public void testCreateVersionOkWithUserDate() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("BBain");
        final String key = "BJB";
        final MockProject project = new MockProject(2829L);
        final String name = "name";
        final String desc = "description";
        final String startDateStr = "28829291";
        final Date startDate = new Date();
        final String releaseDateStr = "28829292";
        final Date releaseDate = new Date();
        final VersionBean versionBean = new VersionBean();

        final Version version = new MockVersion(171718L, "Test Version");

        final VersionService versionService = mock(VersionService.class);

        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final ProjectService projectService = mock(ProjectService.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        when(dateFieldFormat.parseDatePicker(startDateStr)).thenReturn(startDate);
        when(dateFieldFormat.parseDatePicker(releaseDateStr)).thenReturn(releaseDate);

        final VersionService.VersionBuilder versionBuilder = mock(VersionService.VersionBuilder.class);
        when(versionBuilder.projectId(project.getId())).thenReturn(versionBuilder);
        when(versionBuilder.name(name)).thenReturn(versionBuilder);
        when(versionBuilder.description(desc)).thenReturn(versionBuilder);
        when(versionBuilder.startDate(startDate)).thenReturn(versionBuilder);
        when(versionBuilder.releaseDate(releaseDate)).thenReturn(versionBuilder);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final VersionService.VersionBuilderValidationResult validationResult = mock(VersionService.VersionBuilderValidationResult.class);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);
        when(validationResult.isValid()).thenReturn(true);

        when(context.getUser()).thenReturn(user);
        when(projectService.getProjectByIdForAction(user.getDirectoryUser(), project.getId(), ProjectAction.EDIT_PROJECT_CONFIG))
                .thenReturn(new ProjectService.GetProjectResult(ok(), project));
        when(versionService.newBuilder())
                .thenReturn(versionBuilder);
        when(versionService.validateCreate(user.getDirectoryUser(), versionBuilder))
                .thenReturn(validationResult);
        when(versionService.create(user.getDirectoryUser(), validationResult))
                .thenReturn(ServiceOutcomeImpl.ok(version));
        when(versionBeanFactory.createVersionBean(version, false, false)).thenReturn(versionBean);

        final VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);
        final VersionBean.Builder builder = new VersionBean.Builder().setProject(key).setProjectId(project.getId()).setName(name).setDescription(desc)
                .setUserStartDate(startDateStr).setUserReleaseDate(releaseDateStr);

        final Response actualResponse = resource.createVersion(builder.build());
        assertResponseCacheNever(actualResponse);
        assertResponseBody(versionBean, actualResponse);
    }

    @Test
    public void testCreateVersionValidationAuthErrors() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("BBain");
        final String key = "BJB";
        final String name = "name";
        final MockProject project = new MockProject(2829L);

        final VersionService versionService = mock(VersionService.class);

        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final ProjectService projectService = mock(ProjectService.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        final VersionService.VersionBuilder versionBuilder = mock(VersionService.VersionBuilder.class);
        when(versionBuilder.projectId(project.getId())).thenReturn(versionBuilder);
        when(versionBuilder.name(name)).thenReturn(versionBuilder);
        when(versionBuilder.description(null)).thenReturn(versionBuilder);
        when(versionBuilder.startDate(null)).thenReturn(versionBuilder);
        when(versionBuilder.releaseDate(null)).thenReturn(versionBuilder);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final VersionService.VersionBuilderValidationResult validationResult = mock(VersionService.VersionBuilderValidationResult.class);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);
        when(validationResult.isValid()).thenReturn(false);
        when(validationResult.getSpecificReasons()).thenReturn(of(FORBIDDEN));

        when(context.getUser()).thenReturn(user);
        when(projectService.getProjectByIdForAction(user.getDirectoryUser(), project.getId(), ProjectAction.EDIT_PROJECT_CONFIG))
            .thenReturn(new ProjectService.GetProjectResult(ok(), project));
        when(versionService.newBuilder())
                .thenReturn(versionBuilder);
        when(versionService.validateCreate(user.getDirectoryUser(), versionBuilder))
                .thenReturn(validationResult);

        final VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);
        final VersionBean.Builder builder = new VersionBean.Builder().setProject(key).setProjectId(project.getId()).setName(name);

        final Response actualResponse = resource.createVersion(builder.build());
        assertResponseCacheNever(actualResponse);
        assertResponseBody(restErrors(NoopI18nHelper.makeTranslation("rest.version.no.create.permission", key)), actualResponse);
        assertStatus(Response.Status.NOT_FOUND, actualResponse);
    }

    @Test
    public void testCreateVersionValidationOtherErrors() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("BBain");
        final String errorMessage = "errorMessage";
        final String key = "BJB";
        final String name = "name";
        final MockProject project = new MockProject(2829L);

        final VersionService versionService = mock(VersionService.class);
        final VersionBeanFactory versionBeanFactory = mock(VersionBeanFactory.class);
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final ProjectService projectService = mock(ProjectService.class);
        final DateFieldFormat dateFieldFormat = mock(DateFieldFormat.class);

        final VersionService.VersionBuilder versionBuilder = mock(VersionService.VersionBuilder.class);
        when(versionBuilder.projectId(project.getId())).thenReturn(versionBuilder);
        when(versionBuilder.name(name)).thenReturn(versionBuilder);
        when(versionBuilder.description(null)).thenReturn(versionBuilder);
        when(versionBuilder.startDate(null)).thenReturn(versionBuilder);
        when(versionBuilder.releaseDate(null)).thenReturn(versionBuilder);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final VersionService.VersionBuilderValidationResult validationResult = mock(VersionService.VersionBuilderValidationResult.class);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);
        when(validationResult.isValid()).thenReturn(false);
        when(validationResult.getSpecificReasons()).thenReturn(of(BAD_NAME));
        when(validationResult.getErrorCollection()).thenReturn(errors(errorMessage));

        when(context.getUser()).thenReturn(user);
        when(projectService.getProjectByIdForAction(user.getDirectoryUser(), project.getId(), ProjectAction.EDIT_PROJECT_CONFIG))
            .thenReturn(new ProjectService.GetProjectResult(ok(), project));
        when(versionService.newBuilder())
                .thenReturn(versionBuilder);
        when(versionService.validateCreate(user.getDirectoryUser(), versionBuilder))
                .thenReturn(validationResult);

        final VersionResource resource = new VersionResource(versionService, projectService, context, new NoopI18nHelper(), null, versionBeanFactory, null, null, dateFieldFormat, eventPublisher);
        final VersionBean.Builder builder = new VersionBean.Builder().setProject(key).setProjectId(project.getId()).setName(name);

        final Response actualResponse = resource.createVersion(builder.build());
        assertResponseCacheNever(actualResponse);
        assertResponseBody(restErrors(Reason.VALIDATION_FAILED, errorMessage), actualResponse);
        assertStatus(Response.Status.BAD_REQUEST, actualResponse);
    }



    private static com.atlassian.jira.rest.api.util.ErrorCollection restErrors(String... errors)
    {
        return com.atlassian.jira.rest.api.util.ErrorCollection.of(errors);
    }

    private static com.atlassian.jira.rest.api.util.ErrorCollection restErrors(ErrorCollection.Reason reason, String... errors)
    {
        return com.atlassian.jira.rest.api.util.ErrorCollection.of(errors).reason(reason);
    }


    private static ErrorCollection errors(String... errors)
    {
        final SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessages(Arrays.asList(errors));
        return collection;
    }

    private static ErrorCollection ok()
    {
        return new SimpleErrorCollection();
    }
}
