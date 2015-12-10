package com.atlassian.jira.issue.fields.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestVersionHelperBean
{
    @Mock
    private VersionManager versionManager;
    @Mock
    private PermissionManager permissionManager;

    private I18nHelper i18n = new MockI18nHelper();
    private final MockProject project = new MockProject(99L, "Good", "Good Name");
    private final MockProject badProject = new MockProject(666L, "Bad", "Bad Name");

    @Test
    public void testValidateVersionIdNullIds()
    {
        VersionHelperBean versionHelperBean = new VersionHelperBean(null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();


        versionHelperBean.validateVersionIds(null, errorCollection, i18n, "field");
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateVersionIdUnkownAndOther()
    {
        Collection versionIds = CollectionBuilder.newBuilder(1L, -1L, 2L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("issue.field.versions.noneselectedwithother", errorCollection.getErrors().get("field"));
    }

    @Test
    public void testValidateVersionIdUnkownByItsSelf()
    {
        Collection versionIds = CollectionBuilder.newBuilder(-1L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, "field");

        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateVersionIdReallyNegative()
    {
        Collection versionIds = CollectionBuilder.newBuilder(-2L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("issue.field.versions.releasedunreleasedselected", errorCollection.getErrors().get("field"));
    }

    @Test
    public void testValidateVersionIdReallyReallyNegative()
    {
        Collection versionIds = CollectionBuilder.newBuilder(-99L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("issue.field.versions.releasedunreleasedselected", errorCollection.getErrors().get("field"));
    }


    @Test
    public void testValidateForProjectNullProject()
    {
        Collection versionIds = CollectionBuilder.newBuilder(-99L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        versionHelperBean.validateVersionForProject(versionIds, null, errorCollection, i18n, "field");
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateForProjectNullIds()
    {
        VersionHelperBean versionHelperBean = new VersionHelperBean(null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        versionHelperBean.validateVersionForProject(null, new MockProject(11L, "HSP"), errorCollection, i18n, "field");
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateForProjectInvalidVersion()
    {
        Collection versionIds = CollectionBuilder.newBuilder(11L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, null);
        when(versionManager.getVersion(11L)).thenReturn(null);

        versionHelperBean.validateVersionForProject(versionIds, new MockProject(11L, "HSP"), errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("issue.field.versions.invalid.version.id [11]", errorCollection.getErrors().get("field"));
    }

    @Test
    public void testValidateForProjectInvalidVersionAmoungstGood()
    {
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, null);

        final Version version1 = mock(Version.class);
        when(versionManager.getVersion(11L)).thenReturn(version1);
        when(version1.getProjectObject()).thenReturn(project);

        when(versionManager.getVersion(12L)).thenReturn(null);

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("issue.field.versions.invalid.version.id [12]", errorCollection.getErrors().get("field"));
    }

    @Test
    public void testValidateForProjectInvalidVersionAmoungstOtherProjects()
    {
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L, 14L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, null);

        final Version version1 = mock(Version.class);
        final Version version2 = mock(Version.class);

        when(version1.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(11L)).thenReturn(version1);

        when(version2.getProjectObject()).thenReturn(badProject);
        when(version2.getName()).thenReturn("Bad Version");
        when(version2.getId()).thenReturn(12L);
        when(versionManager.getVersion(12L)).thenReturn(version2);

        when(versionManager.getVersion(13L)).thenReturn(null);

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("issue.field.versions.invalid.version.id [13]", errorCollection.getErrors().get("field"));
    }

    @Test
    public void testValidateForProjectBadProject()
    {
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, null);

        final Version version1 = mock(Version.class);
        final Version version2 = mock(Version.class);
        final Version version3 = mock(Version.class);

        when(version1.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(11L)).thenReturn(version1);

        when(version2.getProjectObject()).thenReturn(badProject);
        when(version2.getName()).thenReturn("Bad Version");
        when(version2.getId()).thenReturn(12L);
        when(versionManager.getVersion(12L)).thenReturn(version2);

        when(version3.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(13L)).thenReturn(version3);

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("issue.field.versions.versions.not.valid.for.project [Bad Version(12)] [Good Name]", errorCollection.getErrors().get("field"));
    }

    @Test
    public void testValidateForProjectBadProjects()
    {
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L, 14L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, null);

        final Version version1 = mock(Version.class);
        final Version version2 = mock(Version.class);
        final Version version3 = mock(Version.class);
        final Version version4 = mock(Version.class);

        when(version1.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(11L)).thenReturn(version1);

        when(version2.getProjectObject()).thenReturn(badProject);
        when(version2.getName()).thenReturn("Bad Version");
        when(version2.getId()).thenReturn(12L);
        when(versionManager.getVersion(12L)).thenReturn(version2);

        when(version3.getProjectObject()).thenReturn(badProject);
        when(version3.getName()).thenReturn("Bad Version 2");
        when(version3.getId()).thenReturn(13L);
        when(versionManager.getVersion(13L)).thenReturn(version3);

        when(version4.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(14L)).thenReturn(version4);

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("issue.field.versions.versions.not.valid.for.project [Bad Version(12), Bad Version 2(13)] [Good Name]", errorCollection.getErrors().get("field"));
    }

    @Test
    public void testValidateForProjectAllGood()
    {
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L, 14L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, null);

        final Version version1 = mock(Version.class);
        final Version version2 = mock(Version.class);
        final Version version3 = mock(Version.class);
        final Version version4 = mock(Version.class);

        when(version1.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(11L)).thenReturn(version1);

        when(version2.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(12L)).thenReturn(version2);

        when(version3.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(13L)).thenReturn(version3);

        when(version4.getProjectObject()).thenReturn(project);
        when(versionManager.getVersion(14L)).thenReturn(version4);

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateCreateNoPermission()
    {
        final VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, permissionManager);
        final MockApplicationUser dev = new MockApplicationUser("dev");

        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, dev)).thenReturn(false);

        final ErrorCollection errors = new SimpleErrorCollection();
        versionHelperBean.validateVersionsToCreate(dev, new MockI18nHelper(), project, "fixfor", Sets.newHashSet("nv_1.0", "nv_2.0"), errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("issue.field.versions.invalid.version.id [nv_2.0nv_1.0]", errors.getErrors().get("fixfor"));
    }

    @Test
    public void testValidateCreateAsProjectAdminWithInvalidVersion()
    {
        final VersionService versionService = Mockito.mock(VersionService.class);

        final VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, permissionManager)
        {
            @Override
            VersionService getVersionService()
            {
                return versionService;
            }
        };
        final MockApplicationUser admin = new MockApplicationUser("admin");

        when(permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, admin)).thenReturn(true);
        final ErrorCollection serviceErrors = new SimpleErrorCollection();
        serviceErrors.addError("name", "version name is too long");
        VersionService.VersionBuilderValidationResult result = Mockito.mock(VersionService.VersionBuilderValidationResult.class);
        when(result.isValid()).thenReturn(false);
        when(result.getErrorCollection()).thenReturn(serviceErrors);

        VersionService.VersionBuilder versionBuilder = Mockito.mock(VersionService.VersionBuilder.class);

        when(versionService.newBuilder()).thenReturn(versionBuilder);
        when(versionBuilder.projectId(project.getId())).thenReturn(versionBuilder);
        when(versionBuilder.name(anyString())).thenReturn(versionBuilder);

        when(versionService.validateCreate(Mockito.<User>anyObject(), Mockito.<VersionService.VersionBuilder>anyObject())).thenReturn(result);

        final ErrorCollection errors = new SimpleErrorCollection();
        versionHelperBean.validateVersionsToCreate(admin, new MockI18nHelper(), project, "fixfor", Sets.newHashSet("nv_1.0", "nv_2.0"), errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("version name is too long", errors.getErrors().get("fixfor"));
    }

    @Test
    public void testValidateCreateAsProjectAdminWithValidVersion()
    {
        final VersionService versionService = Mockito.mock(VersionService.class);

        final VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, permissionManager)
        {
            @Override
            VersionService getVersionService()
            {
                return versionService;
            }
        };
        final MockApplicationUser admin = new MockApplicationUser("admin");

        when(permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, admin)).thenReturn(true);
        VersionService.VersionBuilderValidationResult result = Mockito.mock(VersionService.VersionBuilderValidationResult.class);
        when(result.isValid()).thenReturn(true);
        when(result.getErrorCollection()).thenReturn(new SimpleErrorCollection());

        final VersionService.VersionBuilder versionBuilder = Mockito.mock(VersionService.VersionBuilder.class);

        when(versionService.newBuilder()).thenReturn(versionBuilder);
        when(versionBuilder.projectId(project.getId())).thenReturn(versionBuilder);
        when(versionBuilder.name(anyString())).thenReturn(versionBuilder);

        when(versionService.validateCreate(Mockito.<User>anyObject(), Mockito.<VersionService.VersionBuilder>anyObject())).thenReturn(result);

        final ErrorCollection errors = new SimpleErrorCollection();
        versionHelperBean.validateVersionsToCreate(admin, new MockI18nHelper(), project, "fixfor", Sets.newHashSet("nv_1.0", "nv_2.0"), errors);
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void testCreateDoesntCreateDuplicates() throws CreateException
    {
        final VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, permissionManager);

        long projectId = 10000L;
        final Version existingVersion = mock(Version.class);
        final Version newVersion = mock(Version.class);
        when(versionManager.getVersion(projectId, "1.0")).thenReturn(null);
        when(versionManager.getVersion(projectId, "2.0")).thenReturn(existingVersion);

        when(versionManager.createVersion("1.0", null, null, projectId, null)).thenReturn(newVersion);

        List<Version> newVersions = versionHelperBean.createNewVersions(projectId, Sets.newHashSet("nv_1.0", "nv_2.0"));

        assertEquals(newVersions.size(), 2);
        verify(versionManager, never()).createVersion("2.0", null, null, projectId, null);
    }
}
