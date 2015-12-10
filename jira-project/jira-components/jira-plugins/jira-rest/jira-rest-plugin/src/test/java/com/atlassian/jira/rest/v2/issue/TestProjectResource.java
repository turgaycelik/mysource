package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarPickerHelperImpl;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.rest.v2.issue.project.ProjectFinder;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Lists;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseBody;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseCacheNever;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @since v4.4
 */
public class TestProjectResource
{
    @Test
    public void testGetProjectVersionsAnnotations() throws NoSuchMethodException
    {
        Method method = ProjectResource.class.getMethod("getProjectVersions", String.class, String.class);
        Path path = method.getAnnotation(Path.class);
        assertNotNull(path);
        assertEquals("{projectIdOrKey}/versions", path.value());
    }

    @Test
    public void testGetProjectVersionsBadProject() throws Exception
    {
        IMocksControl control = createControl();
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);
        VersionService versionService = control.createMock(VersionService.class);
        AvatarService avatarService = control.createMock(AvatarService.class);
        PermissionManager permissionManager = control.createMock(PermissionManager.class);
        ProjectManager projectManager = control.createMock(ProjectManager.class);
        AvatarManager avatarManager = control.createMock(AvatarManager.class);
        AvatarPickerHelper avatarPickerHelper = control.createMock(AvatarPickerHelperImpl.class);
        AttachmentHelper attachmentHelper = control.createMock(AttachmentHelper.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        JiraBaseUrls jiraBaseUrls = control.createMock(JiraBaseUrls.class);
        ProjectFinder projectFinder = control.createMock(ProjectFinder.class);
        XsrfInvocationChecker xsrfChecker = control.createMock(XsrfInvocationChecker.class);

        ApplicationUser user = new MockApplicationUser("bbain");
        String key = "key";
        String error = "We have a problem";

        expect(context.getUser()).andReturn(user).anyTimes();
        expect(projectFinder.getGetProjectForActionByIdOrKey(user, key, ProjectAction.VIEW_PROJECT)).andReturn(
                new ProjectService.GetProjectResult(errors(error)));

        control.replay();

        ProjectResource resource = new ProjectResource(projectService, context, null, versionService, null, avatarService, null, null, versionBeanFactory, permissionManager, projectManager, avatarManager, avatarPickerHelper, attachmentHelper, jiraBaseUrls, null, null, null, null, projectFinder, xsrfChecker);
        final Response response = resource.getProjectVersions(key, null);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(com.atlassian.jira.rest.api.util.ErrorCollection.of(error).reason(Reason.VALIDATION_FAILED), response.getEntity());

        control.verify();
    }

    @Test
    public void testGetProjectVersionsBadVersion() throws Exception
    {
        IMocksControl control = createControl();
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);
        VersionService versionService = control.createMock(VersionService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        AvatarService avatarService = control.createMock(AvatarService.class);
        PermissionManager permissionManager = control.createMock(PermissionManager.class);
        ProjectManager projectManager = control.createMock(ProjectManager.class);
        AvatarManager avatarManager = control.createMock(AvatarManager.class);
        JiraBaseUrls jiraBaseUrls = control.createMock(JiraBaseUrls.class);

        AvatarPickerHelper avatarPickerHelper = control.createMock(AvatarPickerHelperImpl.class);
        AttachmentHelper attachmentHelper = control.createMock(AttachmentHelper.class);
        ProjectFinder projectFinder = control.createMock(ProjectFinder.class);
        XsrfInvocationChecker xsrfChecker = control.createMock(XsrfInvocationChecker.class);

        ApplicationUser applicationUser = new MockApplicationUser("bbain");
        User user = new MockUser("bbain");
        String key = "key";
        String error = "We have a problem";
        MockProject project = new MockProject(10101L);

        expect(context.getUser()).andReturn(applicationUser).anyTimes();
        expect(context.getLoggedInUser()).andReturn(user).anyTimes();
        expect(projectFinder.getGetProjectForActionByIdOrKey(applicationUser, key, ProjectAction.VIEW_PROJECT))
                .andReturn(new ProjectService.GetProjectResult(ok(), project));

        expect(versionService.getVersionsByProject(user, project))
                .andReturn(new VersionService.VersionsResult(errors(error)));

        control.replay();

        ProjectResource resource = new ProjectResource(projectService, context, null, versionService, null, avatarService, null, null, versionBeanFactory, permissionManager, projectManager, avatarManager, avatarPickerHelper, attachmentHelper, jiraBaseUrls, null, null, null, null, projectFinder, xsrfChecker);
        try
        {
            resource.getProjectVersions(key, null);
            fail("Should have thrown exceptions.");
        }
        catch (NotFoundWebException er)
        {
            assertEquals(com.atlassian.jira.rest.api.util.ErrorCollection.of(error).reason(Reason.VALIDATION_FAILED), er.getResponse().getEntity());
        }

        control.verify();
    }

    @Test
    public void testGetProjectVersionsGood() throws Exception
    {
        IMocksControl control = createControl();
        JiraAuthenticationContext context = control.createMock(JiraAuthenticationContext.class);
        ProjectService projectService = control.createMock(ProjectService.class);
        VersionService versionService = control.createMock(VersionService.class);
        AvatarService avatarService = control.createMock(AvatarService.class);
        VersionBeanFactory versionBeanFactory = control.createMock(VersionBeanFactory.class);
        PermissionManager permissionManager = control.createMock(PermissionManager.class);
        ProjectManager projectManager = control.createMock(ProjectManager.class);
        AvatarManager avatarManager = control.createMock(AvatarManager.class);
        AvatarPickerHelper avatarPickerHelper = control.createMock(AvatarPickerHelperImpl.class);
        AttachmentHelper attachmentHelper = control.createMock(AttachmentHelper.class);
        JiraBaseUrls jiraBaseUrls = control.createMock(JiraBaseUrls.class);
        ProjectFinder projectFinder = control.createMock(ProjectFinder.class);
        XsrfInvocationChecker xsrfChecker = control.createMock(XsrfInvocationChecker.class);

        User user = new MockUser("bbain");
        ApplicationUser applicationUser = new MockApplicationUser("bbain");
        String key = "key";
        MockProject project = new MockProject(10101L);
        MockVersion version = new MockVersion(181828L, "verion1");
        List<Version> versions = Lists.<Version>newArrayList(version);
        List<VersionBean> beans = Lists.newArrayList(new VersionBean());

        expect(context.getLoggedInUser()).andReturn(user).anyTimes();
        expect(context.getUser()).andReturn(applicationUser).anyTimes();
        expect(projectFinder.getGetProjectForActionByIdOrKey(applicationUser, key, ProjectAction.VIEW_PROJECT))
                .andReturn(new ProjectService.GetProjectResult(ok(), project));

        expect(versionService.getVersionsByProject(user, project))
                .andReturn(new VersionService.VersionsResult(ok(), versions));

        expect(versionBeanFactory.createVersionBeans(versions, false)).andReturn(beans);

        control.replay();

        ProjectResource resource = new ProjectResource(projectService, context, null, versionService, null, avatarService, null, null, versionBeanFactory, permissionManager, projectManager, avatarManager, avatarPickerHelper, attachmentHelper, jiraBaseUrls, null, null, null, null, projectFinder, xsrfChecker);
        Response actualResponse = resource.getProjectVersions(key, null);

        assertResponseBody(beans, actualResponse);
        assertResponseCacheNever(actualResponse);

        control.verify();
    }

    private static ErrorCollection errors(String... errors)
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessages(Arrays.asList(errors));
        return collection;
    }

    private static ErrorCollection ok()
    {
        return new SimpleErrorCollection();
    }
}
