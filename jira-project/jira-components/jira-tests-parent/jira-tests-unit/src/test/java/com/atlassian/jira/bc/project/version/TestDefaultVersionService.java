package com.atlassian.jira.bc.project.version;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.plugin.ProjectPermissionOverrideModuleDescriptor;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.google.common.collect.Lists;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.BAD_NAME;
import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.BAD_PROJECT;
import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.BAD_RELEASE_DATE;
import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.BAD_START_DATE;
import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.DUPLICATE_NAME;
import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.FORBIDDEN;
import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.VERSION_NAME_TOO_LONG;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the DefaultVersionService class.
 *
 * @since v3.13
 */
public class TestDefaultVersionService
{
    private JiraServiceContext context;
    private User user;

    @Mock
    I18nBean.BeanFactory nopI18nFactory;

    @Mock
    private DateFieldFormat dateFieldFormat;
    @AvailableInContainer
    private PluginEventManager pluginEventManager = createMock(PluginEventManager.class);
    @AvailableInContainer
    private PluginAccessor pluginAccessor = createMock(PluginAccessor.class);

    @Rule
    public MockitoContainer initMockitoMocks = MockitoMocksInContainer.rule(this);

    @Before
    public void setUp() throws Exception
    {
        EasyMockAnnotations.initMocks(this);

        // use a no-op i18n bean
        NoopI18nHelper i18nHelper = new NoopI18nHelper();
        expect(nopI18nFactory.getInstance(EasyMock.<User>anyObject())).andStubReturn(i18nHelper);
        expect(nopI18nFactory.getInstance(EasyMock.<Locale>anyObject())).andStubReturn(i18nHelper);

        expect(pluginAccessor.getEnabledModuleDescriptorsByClass(EasyMock.<Class<ProjectPermissionOverrideModuleDescriptor>>anyObject())).andStubReturn(Lists.<ProjectPermissionOverrideModuleDescriptor>newArrayList());
        replay(pluginAccessor);

        context = new MockJiraServiceContext();
        user = new MockUser("admin");
    }

    @Test
    public void testDeleteVersionBadActionArguments()
    {
        final PermissionManager permissionManager = new MockPermissionManager(false);
        final VersionManager mockManager = createMock(VersionManager.class);
        replay(mockManager);


        final Long versionId = 10000L;

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockManager, permissionManager, null, null, null, dateFieldFormat, null);

        try
        {
            assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, null, null));
            fail("Cannot pass null VersionAction into validate method");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }

        verify(mockManager);
    }

    @Test
    public void testDeleteInvalidVersionToDelete()
    {
        final PermissionManager permissionManager = new MockPermissionManager(false);
        final VersionManager mockVersionManager = createMock(VersionManager.class);
        final Long versionId = 10000L;

        expect(mockVersionManager.getVersion(versionId)).andReturn(null);
        replay(mockVersionManager);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, null, dateFieldFormat, null);
        // cant use null versionId
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, null, VersionService.REMOVE, VersionService.REMOVE));

        // cant use versionId that does not exist
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, VersionService.REMOVE));

        verify(mockVersionManager);
    }

    /**
     * When swapping in a version for the one that is being deleted, the new version cannot be the same as the one which
     * is being deleted. It must also exist.
     */
    @Test
    public void testNoPermissionForProject()
    {
        final MockPermissionManager permissionManager = MyPermissionManager.createPermissionManager(false);

        final Long versionId = 10000L;
        final Long nonExistsVersionId = 9999L;
        final Long swapVersionId = 10001L;
        final Long swapVersionBadProjectId = 10002L;

        final Long goodProjectId = 20000L;
        final Long badProjectId = 20001L;

        final Version version = createMockVersion(goodProjectId);
        final Version swapVersionBadProject = createMockVersion(badProjectId);
        final Version swapVersion = createMockVersion(goodProjectId);

        final Map<Long, Version> versions = MapBuilder.newBuilder(versionId, version).add(swapVersionId, swapVersion)
                .add(swapVersionBadProjectId, swapVersionBadProject).add(nonExistsVersionId, null).toMap();
        final VersionManager mockVersionManager = createMappedVersionManager(versions);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, null, dateFieldFormat, null);
        // if user doesn't have admin permission then fail
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, VersionService.REMOVE));

        // reset permissions and it should pass
        permissionManager.setDefaultPermission(true);
        assertHasNoErrors(context, defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, VersionService.REMOVE));
    }

    /**
     * When swapping in a version for the one that is being deleted, the new version cannot be the same as the one which
     * is being deleted. It must also exist.
     */
    @Test
    public void testBadDeleteSwapArguments()
    {
        final MockPermissionManager permissionManager = MyPermissionManager.createPermissionManager(true);

        final Long versionId = 10000L;
        final Long nonExistsVersionId = 9999L;
        final Long swapVersionId = 10001L;
        final Long swapVersionBadProjectId = 10002L;

        final Long goodProjectId = 20000L;
        final Long badProjectId = 20001L;

        final Version version = createMockVersion(goodProjectId);
        final Version swapVersionBadProject = createMockVersion(badProjectId);
        final Version swapVersion = createMockVersion(goodProjectId);

        final Map<Long, Version> versions = MapBuilder.newBuilder(versionId, version).add(swapVersionId, swapVersion)
                .add(swapVersionBadProjectId, swapVersionBadProject).add(nonExistsVersionId, null).toMap();
        final VersionManager mockVersionManager = createMappedVersionManager(versions);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, null, dateFieldFormat, null);
        // first check SWAP functionality for Affects Version
        // null id, bad version id and same version id will all fail
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, new SwapVersionAction(null), VersionService.REMOVE));
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, new SwapVersionAction(nonExistsVersionId), VersionService.REMOVE));
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, new SwapVersionAction(versionId), VersionService.REMOVE));

        // good version id but different project ids will fail
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, new SwapVersionAction(swapVersionBadProjectId), VersionService.REMOVE));

        // if user has admin permission then pass
        assertHasNoErrors(context, defaultVersionService.validateDelete(context, versionId, new SwapVersionAction(swapVersionId), VersionService.REMOVE));

        // then check SWAP functionality for Fix Version
        // null id, bad version id and same version id will all fail
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, new SwapVersionAction(null)));
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, new SwapVersionAction(nonExistsVersionId)));
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, new SwapVersionAction(versionId)));

        // good version id but different project ids will fail
        assertHasErrorsAndFlush(context, defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, new SwapVersionAction(swapVersionBadProjectId)));

        // good id will work
        assertHasNoErrors(context, defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, new SwapVersionAction(swapVersionId)));
    }

    /**
     * When swapping in a version for the one that is being deleted, the new version cannot be the same as the one which
     * is being deleted. It must also exist.
     */
    @Test
    public void testBadMergeSwapArguments()
    {
        final MockPermissionManager permissionManager = MyPermissionManager.createPermissionManager(false);

        final Long versionId = 10000L;
        final Long nonExistsVersionId = 9999L;
        final Long swapVersionId = 10001L;
        final Long swapVersionBadProjectId = 10002L;

        final Long goodProjectId = 20000L;
        final Long badProjectId = 20001L;

        final Version version = createMockVersion(goodProjectId);
        final Version swapVersionBadProject = createMockVersion(badProjectId);
        final Version swapVersion = createMockVersion(goodProjectId);

        final Map<Long, Version> versions = MapBuilder.newBuilder(versionId, version).add(swapVersionId, swapVersion)
                .add(swapVersionBadProjectId, swapVersionBadProject).add(nonExistsVersionId, null).toMap();
        final VersionManager mockVersionManager = createMappedVersionManager(versions);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, null, dateFieldFormat, null);
        // first check SWAP functionality for Affects Version
        // null id, bad version id and same version id will all fail
        assertHasErrorsAndFlush(context, defaultVersionService.validateMerge(context, versionId, null));
        assertHasErrorsAndFlush(context, defaultVersionService.validateMerge(context, versionId, nonExistsVersionId));
        assertHasErrorsAndFlush(context, defaultVersionService.validateMerge(context, versionId, versionId));

        // good version id but different project ids will fail
        assertHasErrorsAndFlush(context, defaultVersionService.validateMerge(context, versionId, swapVersionBadProjectId));

        // if user doesn't have admin permission then fail
        assertHasErrorsAndFlush(context, defaultVersionService.validateMerge(context, versionId, swapVersionId));

        // reset permissions and it should pass
        permissionManager.setDefaultPermission(true);
        assertHasNoErrors(context, defaultVersionService.validateMerge(context, versionId, swapVersionId));
    }

    @Test
    public void testValidateDeleteResult()
    {
        final MockPermissionManager permissionManager = MyPermissionManager.createPermissionManager(true);
        final Long versionId = 10000L;
        final Long affectsSwapVersionId = 10001L;
        final Long fixSwapVersionId = 10002L;

        final Long projectId = 20000L;

        final Version version = createMockVersion(projectId);
        final Version affectsSwapVersion = createMockVersion(projectId);
        final Version fixSwapVersion = createMockVersion(projectId);

        final Map<Long, Version> versions = MapBuilder.build(versionId, version, affectsSwapVersionId,
                affectsSwapVersion, fixSwapVersionId, fixSwapVersion);
        final VersionManager mockVersionManager = createMappedVersionManager(versions);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, null, dateFieldFormat, null);
        // if not swapping in any versions for Affects or Fix, the returned Versions in the result object will be null,
        // regardless of id passed into method
        VersionService.ValidationResult result = defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, VersionService.REMOVE);
        assertHasNoErrors(context, result);
        assertEquals(version, result.getVersionToDelete());
        assertNull(result.getAffectsSwapVersion());
        assertNull(result.getFixSwapVersion());

        result = defaultVersionService.validateDelete(context, versionId, VersionService.REMOVE, VersionService.REMOVE);
        assertHasNoErrors(context, result);
        assertEquals(version, result.getVersionToDelete());
        assertNull(result.getAffectsSwapVersion());
        assertNull(result.getFixSwapVersion());

        // if swapping versions, the result object must contain the version that was referenced by the id
        result = defaultVersionService.validateDelete(context, versionId, new SwapVersionAction(affectsSwapVersionId), new SwapVersionAction(fixSwapVersionId));
        assertHasNoErrors(context, result);
        assertEquals(version, result.getVersionToDelete());
        assertEquals(affectsSwapVersion, result.getAffectsSwapVersion());
        assertEquals(fixSwapVersion, result.getFixSwapVersion());
    }

    @Test
    public void testValidateMergeResult()
    {
        final MockPermissionManager permissionManager = MyPermissionManager.createPermissionManager(true);
        final Long versionId = 10000L;
        final Long swapVersionId = 10001L;

        final Long projectId = 20000L;

        final Version version = createMockVersion(projectId);
        final Version swapVersion = createMockVersion(projectId);

        final Map<Long, Version> versions = MapBuilder.build(versionId, version, swapVersionId, swapVersion);
        final VersionManager mockVersionManager = createMappedVersionManager(versions);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, null, dateFieldFormat, null);

        // if swapping versions, the result object must contain the version that was referenced by the id
        VersionService.ValidationResult result = defaultVersionService.validateMerge(context, versionId, swapVersionId);
        assertHasNoErrors(context, result);
        assertEquals(version, result.getVersionToDelete());
        assertEquals(swapVersion, result.getAffectsSwapVersion());
        assertEquals(swapVersion, result.getFixSwapVersion());
    }

    @Test
    public void testDeleteWithInvalidResult() throws Exception
    {
        final Long projectId = 20000L;
        final Version version = createMockVersion(projectId);
        final Version affectsSwapVersion = createMockVersion(projectId);
        final Version fixSwapVersion = createMockVersion(projectId);

        // create dummy result object
        ValidationResultImpl badResult = new ValidationResultImpl(new SimpleErrorCollection(), version, affectsSwapVersion, fixSwapVersion, false, Collections.<VersionService.ValidationResult.Reason>emptySet());

        // test delete call with invalid result - should get exception
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            defaultVersionService.delete(context, badResult);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }
    }

    @Test
    public void testMergeWithInvalidResult() throws Exception
    {
        final Long projectId = 20000L;
        final Version version = createMockVersion(projectId);
        final Version affectsSwapVersion = createMockVersion(projectId);
        final Version fixSwapVersion = createMockVersion(projectId);

        // create dummy result object
        ValidationResultImpl badResult = new ValidationResultImpl(new SimpleErrorCollection(), version, affectsSwapVersion, fixSwapVersion, false, Collections.<VersionService.ValidationResult.Reason>emptySet());

        // test merge call with invalid result - should get exception
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            defaultVersionService.merge(context, badResult);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }
    }

    @Test
    public void testValidateCreateVersionDateOk()
    {
        final String versionName = "versionName";
        long projectId = 373738L;
        final Project project = new MockProject(projectId);

        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);
        final ProjectManager projectManager = control.createMock(ProjectManager.class);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user))
                .andStubReturn(true);

        expect(projectManager.getProjectObj(projectId))
                .andStubReturn(project);

        expect(mockVersionManager.getVersions(projectId))
                .andStubReturn(Collections.<Version>emptyList());

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(2000, Calendar.MARCH, 1, 1, 1, 1);
        calendar.set(Calendar.MILLISECOND, 101);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(2000, Calendar.APRIL, 1, 1, 1, 1);
        calendar.set(Calendar.MILLISECOND, 101);

        Date inputReleaseDate = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date expectedReleaseDate = calendar.getTime();

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, projectManager)
        {
            @Override
            DefaultVersionService.ValidateResult validateCreateParameters(User validateUser, Project validateProject, String validateVersion, String startDate, String releaseDate)
            {
                assertSame(user, validateUser);
                assertSame(project, validateProject);
                assertEquals(versionName, validateVersion);
                assertNull(startDate);
                assertNull(releaseDate);

                return new ValidateResult(new SimpleErrorCollection(), Collections.<CreateVersionValidationResult.Reason>emptySet());
            }
        };

        replayMocks(control);

        VersionService.CreateVersionValidationResult result = defaultVersionService.validateCreateVersion(user, project, versionName, inputReleaseDate, null, null);
        assertTrue(result.isValid());
        assertSame(project, result.getProject());
        assertEquals(versionName, result.getVersionName());
        assertNull(result.getStartDate());
        assertEquals(expectedReleaseDate, result.getReleaseDate());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionDateBad()
    {
        final String versionName = "versionName";
        long projectId = 373738L;
        final Project project = new MockProject(projectId);
        final String badDate = "invalid date";

        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);
        final ProjectManager projectManager = control.createMock(ProjectManager.class);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user))
                .andStubReturn(true);

        expect(projectManager.getProjectObj(projectId))
                .andStubReturn(project);

        expect(mockVersionManager.getVersions(projectId))
                .andStubReturn(Collections.<Version>emptyList());

        expect(dateFieldFormat.parseDatePicker(badDate)).andStubThrow(new IllegalArgumentException());
        expect(dateFieldFormat.getFormatHint()).andStubReturn("dd-MMM-yy");

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, projectManager);

        replayMocks(control);

        VersionService.CreateVersionValidationResult result = defaultVersionService.validateCreateVersion(user, project, versionName, badDate, null, null);
        assertFalse(result.isValid());
        assertEquals(EnumSet.of(BAD_RELEASE_DATE), result.getReasons());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionDateStringOk()
    {
        final String versionName = "versionName";
        long projectId = 373738L;
        final Project project = new MockProject(projectId);

        final String startDate = "22/10/21";
        final Date parsedStartDate = new Date(new Date().getTime() - 10000);
        final String releaseDate = "27/10/21";
        final Date parsedReleaseDate = new LocalDate(2012, 9, 27).toDate();

        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);
        final ProjectManager projectManager = control.createMock(ProjectManager.class);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user))
                .andStubReturn(true);

        expect(projectManager.getProjectObj(projectId))
                .andStubReturn(project);

        expect(mockVersionManager.getVersions(projectId))
                .andStubReturn(Collections.<Version>emptyList());


        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, projectManager)
        {

            @Override
            DefaultVersionService.ValidateResult validateCreateParameters(User validateUser, Project validateProject, String validateVersion, String validateStartDate, String validateReleaseDate)
            {
                assertSame(user, validateUser);
                assertSame(project, validateProject);
                assertEquals(versionName, validateVersion);
                if (validateStartDate != null)
                {
                    assertEquals(startDate, validateStartDate);
                }
                assertEquals(releaseDate, validateReleaseDate);

                return new ValidateResult(new SimpleErrorCollection(), Collections.<CreateVersionValidationResult.Reason>emptySet(), validateStartDate == null ? null : parsedStartDate, parsedReleaseDate);
            }
        };

        replayMocks(control);

        VersionService.CreateVersionValidationResult result = defaultVersionService.validateCreateVersion(user, project, versionName, releaseDate, null, null);
        assertTrue(result.isValid());
        assertSame(project, result.getProject());
        assertEquals(versionName, result.getVersionName());
        assertNull(result.getStartDate());
        assertEquals(parsedReleaseDate, result.getReleaseDate());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionDateStringBad()
    {
        final String startDate = "22/10/21";
        final String releaseDate = "27/10/21";
        final String error = "this is an error";
        final VersionService.CreateVersionValidationResult.Reason reason = BAD_NAME;
        final String versionName = "versionName";
        final Project project = new MockProject(373738L);

        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null)
        {
            @Override
            DefaultVersionService.ValidateResult validateCreateParameters(User validateUser, Project validateProject, String validateVersion, String validateStartDate, String validateReleaseDate)
            {
                assertSame(user, validateUser);
                assertSame(project, validateProject);
                assertEquals(versionName, validateVersion);
                if (validateStartDate != null)
                {
                    assertEquals(startDate, validateStartDate);
                }
                assertEquals(releaseDate, validateReleaseDate);

                SimpleErrorCollection errors = new SimpleErrorCollection();
                errors.addErrorMessage(error);
                return new ValidateResult(errors, EnumSet.of(reason));
            }
        };

        replayMocks(control);

        VersionService.CreateVersionValidationResult result = defaultVersionService.validateCreateVersion(user, project, versionName, releaseDate, null, null);
        assertFalse(result.isValid());
        assertEquals(errors(error), result.getErrorCollection());
        assertEquals(EnumSet.of(reason), result.getReasons());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionForNullValues()
    {
        String expectedError = "admin.errors.must.specify.valid.project{[]}";
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        replayMocks(control);

        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, null, null, null, null);
        assertFalse(result.isValid());
        assertEquals(errors(expectedError), result.getErrors());
        assertNull(result.getParsedStartDate());
        assertNull(result.getParsedReleaseDate());
        assertEquals(EnumSet.of(BAD_PROJECT), result.getReasons());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionNoPermission()
    {
        String expectedError = "admin.errors.version.no.permission{[]}";
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        Project mockProject = new MockProject(363637);
        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).andReturn(false);


        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        replayMocks(control);

        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, mockProject, null, null, null);
        assertFalse(result.isValid());
        assertEquals(errors(expectedError), result.getErrors());
        assertNull(result.getParsedStartDate());
        assertNull(result.getParsedReleaseDate());
        assertEquals(EnumSet.of(FORBIDDEN), result.getReasons());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionNullVersionName()
    {
        checkVersionNameError(null);
    }

    @Test
    public void testValidateCreateVersionEmptyVersionName()
    {
        checkVersionNameError("");
    }

    private void checkVersionNameError(String versionName)
    {
        String expectedMessage = "admin.errors.enter.valid.version.name{[]}";

        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        Project mockProject = new ProjectImpl(null);
        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, mockProject, versionName, null, null);
        assertFalse(result.isValid());
        assertEquals(errorMap("name", expectedMessage), result.getErrors());
        assertNull(result.getParsedStartDate());
        assertNull(result.getParsedReleaseDate());
        assertEquals(EnumSet.of(BAD_NAME), result.getReasons());

        control.verify();
    }


    @Test
    public void testValidateCreateVersionAlreadyExists()
    {
        String expectedMessage = "admin.errors.version.already.exists{[]}";

        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        Project mockProject = new MockProject(1L);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        final MockVersion mockVersion = new MockVersion(1, "name");
        expect(mockVersionManager.getVersions(1L)).andReturn(CollectionBuilder.<Version>list(mockVersion));

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, mockProject, "name", null, null);
        assertFalse(result.isValid());
        assertEquals(errorMap("name", expectedMessage), result.getErrors());
        assertNull(result.getParsedStartDate());
        assertNull(result.getParsedReleaseDate());
        assertEquals(EnumSet.of(DUPLICATE_NAME), result.getReasons());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionOk()
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);
        final ProjectManager projectManager = control.createMock(ProjectManager.class);

        Project mockProject = new ProjectImpl(null)
        {
            public Long getId()
            {
                return 1L;
            }
        };

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);

        final MockVersion mockVersion = new MockVersion(1, "1.0");
        expect(mockVersionManager.getVersions(1L)).andStubReturn(CollectionBuilder.<Version>list(mockVersion));

        expect(projectManager.getProjectObj(1L))
                .andStubReturn(mockProject);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, projectManager);

        VersionService.CreateVersionValidationResult result = defaultVersionService.validateCreateVersion(user, mockProject, "1.1", (String) null, null, null);
        assertTrue(result.isValid());
        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertFalse(result.getProject() == null);
        assertFalse(result.getVersionName() == null);
        assertTrue(result.getStartDate() == null);
        assertTrue(result.getReleaseDate() == null);

        control.verify();
    }

    @Test
    public void testValidateCreateVersionDatesOk() throws ParseException
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        final String startDate = "2007-01-01";
        Date startDateResult = new Date();
        final String releaseDate = "2008-01-01";
        Date releaseDateResult = new Date();

        Project mockProject = new MockProject(1L);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        final MockVersion mockVersion = new MockVersion(1, "1.0");
        expect(mockVersionManager.getVersions(1L)).andReturn(CollectionBuilder.<Version>list(mockVersion));

        expect(dateFieldFormat.parseDatePicker(startDate)).andStubReturn(startDateResult);
        expect(dateFieldFormat.parseDatePicker(releaseDate)).andStubReturn(releaseDateResult);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, mockProject, "1.1", startDate, releaseDate);
        assertTrue(result.isValid());
        assertEquals(startDateResult, result.getParsedStartDate());
        assertEquals(releaseDateResult, result.getParsedReleaseDate());
        assertTrue(result.getReasons().isEmpty());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionDatesWrongOrder() throws ParseException
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        final String startDate = "2007-01-01";
        Date startDateResult = new Date();
        final String releaseDate = "2008-01-01";
        Date releaseDateResult = new Date(System.currentTimeMillis() - 1000);

        Project mockProject = new MockProject(1L);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        final MockVersion mockVersion = new MockVersion(1, "1.0");
        expect(mockVersionManager.getVersions(1L)).andReturn(CollectionBuilder.<Version>list(mockVersion));

        expect(dateFieldFormat.parseDatePicker(startDate)).andStubReturn(startDateResult);
        expect(dateFieldFormat.parseDatePicker(releaseDate)).andStubReturn(releaseDateResult);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, mockProject, "1.1", startDate, releaseDate);
        assertFalse(result.isValid());
        assertEquals(startDateResult, result.getParsedStartDate());
        assertEquals(releaseDateResult, result.getParsedReleaseDate());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionStartDateFail() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);
        final I18nHelper mockI18nBean = control.createMock(I18nHelper.class);

        final String format = "dd-MMMMMMM-y";
        final Project mockProject = new MockProject(1L);
        final MockVersion mockVersion = new MockVersion(1, "1.0");
        final Locale locale = new Locale("en");
        final String date = "2008-01-01";
        String expectedMessage = "admin.errors.incorrect.date.format{[dd-MMMMMMM-y]}";

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(mockVersionManager.getVersions(1L)).andStubReturn(CollectionBuilder.<Version>list(mockVersion));
        expect(mockI18nBean.getLocale()).andStubReturn(locale);
        expect(dateFieldFormat.parseDatePicker(date)).andStubThrow(new IllegalArgumentException());
        expect(dateFieldFormat.getFormatHint()).andStubReturn(format);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, mockProject, "1.1", date, null);
        assertFalse(result.isValid());
        assertEquals(errorMap("startDate", expectedMessage), result.getErrors());
        assertNull(result.getParsedStartDate());
        assertNull(result.getParsedReleaseDate());
        assertEquals(EnumSet.of(BAD_START_DATE), result.getReasons());

        control.verify();
    }

    @Test
    public void testValidateCreateVersionReleaseDateFail() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);
        final I18nHelper mockI18nBean = control.createMock(I18nHelper.class);

        final String format = "dd-MMMMMMM-y";
        final Project mockProject = new MockProject(1L);
        final MockVersion mockVersion = new MockVersion(1, "1.0");
        final Locale locale = new Locale("en");
        final String date = "2008-01-01";
        String expectedMessage = "admin.errors.incorrect.date.format{[dd-MMMMMMM-y]}";

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(mockVersionManager.getVersions(1L)).andStubReturn(CollectionBuilder.<Version>list(mockVersion));
        expect(mockI18nBean.getLocale()).andStubReturn(locale);
        expect(dateFieldFormat.parseDatePicker(date)).andStubThrow(new IllegalArgumentException());
        expect(dateFieldFormat.getFormatHint()).andStubReturn(format);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, mockProject, "1.1", null, date);
        assertFalse(result.isValid());
        assertEquals(errorMap("releaseDate", expectedMessage), result.getErrors());
        assertNull(result.getParsedStartDate());
        assertNull(result.getParsedReleaseDate());
        assertEquals(EnumSet.of(BAD_RELEASE_DATE), result.getReasons());

        control.verify();
    }

    @Test
    public void testCreateVersionNameTooLong() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final Project mockProject = new MockProject(1L, "MKY");
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);
        String longText = "";

        while (longText.length() < 256)
        {
            longText += "a";
        }

        String expectedMessage = "admin.errors.version.name.toolong{[]}";

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);
        expect(mockVersionManager.getVersions(mockProject.getId())).andReturn(Collections.<Version>emptyList());

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, null);
        DefaultVersionService.ValidateResult result = defaultVersionService.validateCreateParameters(user, mockProject, longText, null, null);
        assertFalse(result.isValid());
        assertEquals(errorMap("name", expectedMessage), result.getErrors());
        assertNull(result.getParsedStartDate());
        assertNull(result.getParsedReleaseDate());
        assertEquals(EnumSet.of(VERSION_NAME_TOO_LONG), result.getReasons());

        control.verify();
    }

    @Test
    public void testCreateVersionReleased() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final Date date = new LocalDate().toDate();

        long projectId = 1L;
        Project mockProject = new MockProject(projectId);

        final MockVersion mockVersion = new MockVersion(1, "1.1");
        expect(mockVersionManager.getVersions(projectId))
                .andReturn(Collections.<Version>emptyList());
        expect(mockVersionManager.createVersion("1.1", null, date, null, 1L, null)).andReturn(mockVersion);

        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user))
                .andStubReturn(true);

        final ProjectManager projectManager = control.createMock(ProjectManager.class);

        expect(projectManager.getProjectObj(projectId))
                .andStubReturn(mockProject);


        replayMocks(control);

        VersionService.CreateVersionValidationResult request = new VersionService.CreateVersionValidationResult(new SimpleErrorCollection(), mockProject, "1.1", null, date, null, null);
        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, projectManager);

        Version version = defaultVersionService.createVersion(user, request);
        assertFalse(version == null);
        assertEquals("1.1", version.getName());

        control.verify();
    }

    @Test
    public void testCreateVersionWrongRequest() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, null, null, null, null, dateFieldFormat, null);
        VersionService.CreateVersionValidationResult request = new VersionService.CreateVersionValidationResult(new SimpleErrorCollection(), null, null, null, null, null, null);

        try
        {
            defaultVersionService.createVersion(user, request);
            fail();
        }
        catch (RuntimeException ex)
        {
            // ok
        }

        control.verify();
    }

    @Test
    public void testCreateVersionOk() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);

        final Date startDate = new LocalDate().toDate();
        final Date releaseDate = new LocalDate().toDate();
        long projectId = 1L;
        Project mockProject = new MockProject(projectId);

        final MockVersion mockVersion = new MockVersion(1, "1.1");
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user))
                .andStubReturn(true);

        final ProjectManager projectManager = control.createMock(ProjectManager.class);

        expect(projectManager.getProjectObj(projectId))
                .andStubReturn(mockProject);

        expect(mockVersionManager.getVersions(projectId))
                .andReturn(Collections.<Version>emptyList());
        expect(mockVersionManager.createVersion("1.1", startDate, releaseDate, null, 1L, null)).andReturn(mockVersion);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, projectManager);
        VersionService.CreateVersionValidationResult request = new VersionService.CreateVersionValidationResult(new SimpleErrorCollection(), mockProject, "1.1", startDate, releaseDate, null, null);

        Version version = defaultVersionService.createVersion(user, request);
        assertFalse(version == null);
        assertEquals("1.1", version.getName());

        verifyMocks(control);
    }

    @Test
    public void testGetVersionByIdNullId()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, nopI18nFactory, dateFieldFormat, null);
        try
        {
            defaultVersionService.getVersionById(user, null, null);
            fail("Version is null.  Should have thrown exception.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("versionId should not be null!", e.getMessage());
        }
    }

    @Test
    public void testGetVersionByIdNoPermission()
    {
        IMocksControl control = EasyMock.createControl();

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        Project mockProject = new MockProject((Long) null);

        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, mockProject, user)).andReturn(false);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        replayMocks(control);
        final VersionService.VersionResult result = defaultVersionService.getVersionById(user, mockProject, 1L);

        assertFalse(result.isValid());
        assertEquals("admin.errors.version.no.read.permission{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testGetVersionsByProjectNoPermission()
    {
        IMocksControl control = EasyMock.createControl();

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        Project mockProject = new MockProject((Long) null);

        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, mockProject, user)).andReturn(false);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        replayMocks(control);
        final VersionService.VersionsResult result = defaultVersionService.getVersionsByProject(user, mockProject);

        assertFalse(result.isValid());
        assertEquals("admin.errors.version.no.read.permission{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testGetVersionByProjectAndNameNoPermission()
    {
        IMocksControl control = EasyMock.createControl();

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        Project mockProject = new MockProject((Long) null);

        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, mockProject, user)).andReturn(false);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        replayMocks(control);
        final VersionService.VersionResult result = defaultVersionService.getVersionByProjectAndName(user, mockProject, "Version 1");

        assertFalse(result.isValid());
        assertEquals("admin.errors.version.no.read.permission{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testGetVersionByIdNoVersion()
    {
        IMocksControl control = EasyMock.createControl();

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        Project mockProject = new MockProject((Long) null);

        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).andReturn(true);

        VersionManager mockVersionManager = control.createMock(VersionManager.class);
        expect(mockVersionManager.getVersion(1L)).andReturn(null);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        replayMocks(control);
        final VersionService.VersionResult result = defaultVersionService.getVersionById(user, mockProject, 1L);

        assertFalse(result.isValid());
        assertEquals("admin.errors.version.not.exist.with.id{[1]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testGetVersionByIdSuccess()
    {
        IMocksControl control = EasyMock.createControl();

        Project mockProject = new MockProject((Long) null);
        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);

        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        Version mockVersion = control.createMock(Version.class);

        VersionManager mockVersionManager = control.createMock(VersionManager.class);
        expect(mockVersionManager.getVersion(1L)).andReturn(mockVersion);

        DefaultVersionService defaultVersionService =
                new DefaultVersionService(mockVersionManager, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        replayMocks(control);

        final VersionService.VersionResult result = defaultVersionService.getVersionById(user, mockProject, 1L);
        assertTrue(result.isValid());
        assertEquals(mockVersion, result.getVersion());

        control.verify();
    }

    @Test
    public void testValidateReleaseVersionNoVersion()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, nopI18nFactory, dateFieldFormat, null);
        try
        {
            defaultVersionService.validateReleaseVersion(user, null, (Date) null);
            fail("Version is null.  Should have thrown exception.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("version should not be null!", e.getMessage());
        }
    }

    @Test
    public void testValidateReleaseVersionNoProject()
    {
        IMocksControl control = EasyMock.createControl();

        final Version mockVersion = control.createMock(Version.class);
        expect(mockVersion.getProjectObject()).andReturn(null);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ReleaseVersionValidationResult result =
                defaultVersionService.validateReleaseVersion(user, mockVersion, (Date) null);
        assertFalse(result.isValid());
        assertEquals("admin.errors.must.specify.valid.project{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testValidateReleaseVersionNoPermission()
    {
        final Project mockProject = new MockProject((Long) null);

        IMocksControl control = EasyMock.createControl();

        final Version mockVersion = control.createMock(Version.class);
        expect(mockVersion.getProjectObject()).andReturn(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).andReturn(false);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ReleaseVersionValidationResult result =
                defaultVersionService.validateReleaseVersion(user, mockVersion, (Date) null);
        assertFalse(result.isValid());
        assertEquals("admin.errors.version.no.permission{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testValidateReleaseVersionNoVersionName()
    {
        IMocksControl control = EasyMock.createControl();

        final Project mockProject = new MockProject((Long) null);
        final MockVersion mockVersion = new MockVersion(678L, "");
        mockVersion.setProjectObject(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ReleaseVersionValidationResult result =
                defaultVersionService.validateReleaseVersion(user, mockVersion, (Date) null);
        assertFalse(result.isValid());
        assertEquals("admin.errors.enter.valid.version.name{[]}", result.getErrorCollection().getErrors().get("name"));

        control.verify();
    }

    @Test
    public void testValidateReleaseVersionNoReleaseDateAndReleased()
    {
        IMocksControl control = EasyMock.createControl();

        final Project mockProject = new MockProject((Long) null);

        MockVersion version = new MockVersion(474747, "JIRA 3.13");
        version.setReleased(true);
        version.setProjectObject(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ReleaseVersionValidationResult result =
                defaultVersionService.validateReleaseVersion(user, version, (Date) null);
        assertFalse(result.isValid());
        assertEquals("admin.errors.release.already.released{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testValidateReleaseVersionSuccess() throws GenericEntityException
    {
        IMocksControl control = EasyMock.createControl();

        final Project mockProject = new MockProject((Long) null);

        MockVersion version = new MockVersion(474747, "JIRA 3.13");
        version.setReleased(false);
        version.setProjectObject(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final Date releaseDate = new Date();
        final VersionService.ReleaseVersionValidationResult result =
                defaultVersionService.validateReleaseVersion(user, version, releaseDate);
        assertTrue(result.isValid());
        assertEquals(version, result.getVersion());
        assertEquals(releaseDate, result.getReleaseDate());

        control.verify();
    }

    /*
     * Note, this does not test the checkVersionDetails validation, since the all the testValidateRelease**() methods
     * above already test this.
     */
    @Test
    public void testValidateUnreleaseVersionError() throws GenericEntityException
    {
        IMocksControl control = EasyMock.createControl();

        final Project mockProject = new MockProject((Long) null);
        MockVersion version = new MockVersion(474747, "JIRA 3.13");
        version.setReleased(false);
        version.setProjectObject(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ReleaseVersionValidationResult result =
                defaultVersionService.validateUnreleaseVersion(user, version, (Date) null);
        assertFalse(result.isValid());
        assertEquals("admin.errors.release.not.released{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testValidateUnreleaseVersionSuccess()
    {
        IMocksControl control = EasyMock.createControl();

        final Project mockProject = new MockProject((Long) null);

        Version version = control.createMock(Version.class);
        expect(version.getProjectObject()).andReturn(mockProject);
        expect(version.getName()).andReturn("JIRA 3.13");
        expect(version.isReleased()).andReturn(true);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ReleaseVersionValidationResult result =
                defaultVersionService.validateUnreleaseVersion(user, version, (Date) null);
        assertTrue(result.isValid());
        assertEquals(version, result.getVersion());

        control.verify();
    }

    @Test
    public void testReleaseVersionNoResult()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            defaultVersionService.releaseVersion(null);
            fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("You can not release a version with a null validation result.", e.getMessage());
        }
    }

    @Test
    public void testReleaseVersionInvalidResult()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage("Something bad happened!");
            VersionService.ReleaseVersionValidationResult result = new VersionService.ReleaseVersionValidationResult(errors);
            defaultVersionService.releaseVersion(result);
            fail("Should have thrown exception");
        }
        catch (IllegalStateException e)
        {
            assertEquals("You can not release a version with an invalid validation result.", e.getMessage());
        }
    }

    @Test
    public void testReleaseVersionSuccess() throws GenericEntityException
    {
        final Date releaseDate = new Date();

        IMocksControl control = EasyMock.createControl();

        Version version = control.createMock(Version.class);

        version.setReleaseDate(releaseDate);
        version.setReleased(true);
        expect(version.getId()).andReturn(99L).anyTimes();

        Version versionDb = control.createMock(Version.class);

        VersionManager mockVersionManager = control.createMock(VersionManager.class);
        mockVersionManager.releaseVersion(version, true);

        expect(mockVersionManager.getVersion(99L)).andReturn(versionDb);
        expect(versionDb.getId()).andReturn(99L).anyTimes();

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, null, null, null, null, dateFieldFormat, null);

        replayMocks(control);

        ErrorCollection errors = new SimpleErrorCollection();
        VersionService.ReleaseVersionValidationResult result = new VersionService.ReleaseVersionValidationResult(errors, version, releaseDate);
        final Version releasedVersion = defaultVersionService.releaseVersion(result);
        assertEquals(versionDb, releasedVersion);

        verifyMocks(control);
    }

    @Test
    public void testUnreleaseVersionNoResult()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            defaultVersionService.unreleaseVersion(null);
            fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("You can not unrelease a version with a null validation result.", e.getMessage());
        }
    }

    @Test
    public void testUnreleaseVersionInvalidResult()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage("Something bad happened!");
            VersionService.ReleaseVersionValidationResult result = new VersionService.ReleaseVersionValidationResult(errors);
            defaultVersionService.unreleaseVersion(result);
            fail("Should have thrown exception");
        }
        catch (IllegalStateException e)
        {
            assertEquals("You can not unrelease a version with an invalid validation result.", e.getMessage());
        }
    }

    @Test
    public void testUnreleaseVersionSuccess() throws GenericEntityException
    {
        final Date releaseDate = new Date();
        IMocksControl control = EasyMock.createControl();

        Version mockVersion = control.createMock(Version.class);
        mockVersion.setReleaseDate(releaseDate);
        mockVersion.setReleased(false);
        expect(mockVersion.getId()).andReturn(99L).anyTimes();

        MockVersion databaseVersion = new MockVersion();

        VersionManager mockVersionManager = control.createMock(VersionManager.class);
        mockVersionManager.releaseVersion(mockVersion, false);

        expect(mockVersionManager.getVersion(99L)).andReturn(databaseVersion);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, null, null, null, null, dateFieldFormat, null);
        ErrorCollection errors = new SimpleErrorCollection();
        VersionService.ReleaseVersionValidationResult result = new VersionService.ReleaseVersionValidationResult(errors, mockVersion, releaseDate);
        final Version releasedVersion = defaultVersionService.unreleaseVersion(result);
        assertEquals(databaseVersion, releasedVersion);

        verifyMocks(control);
    }

    /*
     * Note, this does not test the checkVersionDetails validation, since the all the testValidateRelease**() methods
     * above already test this.
     */
    @Test
    public void testValidateArchiveVersionError() throws GenericEntityException
    {
        IMocksControl control = EasyMock.createControl();

        final MockProject mockProject = new MockProject(3737L);
        MockVersion mockVersion = new MockVersion(4747, "JIRA 3.13");
        mockVersion.setArchived(true);
        mockVersion.setProjectObject(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ArchiveVersionValidationResult result =
                defaultVersionService.validateArchiveVersion(user, mockVersion);
        assertFalse(result.isValid());
        assertEquals("admin.errors.archive.already.archived{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testValidateArchiveVersionSuccess() throws GenericEntityException
    {
        IMocksControl control = EasyMock.createControl();

        final MockProject mockProject = new MockProject(3737L);
        MockVersion mockVersion = new MockVersion(4747, "JIRA 3.13");
        mockVersion.setArchived(false);
        mockVersion.setProjectObject(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ArchiveVersionValidationResult result =
                defaultVersionService.validateArchiveVersion(user, mockVersion);
        assertTrue(result.isValid());
        assertEquals(mockVersion, result.getVersion());

        control.verify();
    }

    /*
     * Note, this does not test the checkVersionDetails validation, since the all the testValidateRelease**() methods
     * above already test this.
     */
    @Test
    public void testValidateUnarchiveVersionError() throws GenericEntityException
    {
        IMocksControl control = EasyMock.createControl();

        final MockProject mockProject = new MockProject(3737L);
        MockVersion mockVersion = new MockVersion(4747, "JIRA 3.13");
        mockVersion.setArchived(false);
        mockVersion.setProjectObject(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ArchiveVersionValidationResult result =
                defaultVersionService.validateUnarchiveVersion(user, mockVersion);
        assertFalse(result.isValid());
        assertEquals("admin.errors.archive.not.archived{[]}", result.getErrorCollection().getErrorMessages().iterator().next());

        control.verify();
    }

    @Test
    public void testValidateUnarchiveVersionSuccess() throws GenericEntityException
    {
        IMocksControl control = EasyMock.createControl();

        final I18nHelper mockI18nBean = control.createMock(I18nHelper.class);

        final MockProject mockProject = new MockProject(3737L);
        MockVersion mockVersion = new MockVersion(4747, "JIRA 3.13");
        mockVersion.setArchived(true);
        mockVersion.setProjectObject(mockProject);

        PermissionManager mockPermissionManager = control.createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(null, mockPermissionManager, null, null, nopI18nFactory, dateFieldFormat, null);

        final VersionService.ArchiveVersionValidationResult result =
                defaultVersionService.validateUnarchiveVersion(user, mockVersion);
        assertTrue(result.isValid());
        assertEquals(mockVersion, result.getVersion());

        control.verify();
    }

    @Test
    public void testArchiveVersionNoResult()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            defaultVersionService.archiveVersion(null);
            fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("You can not archive a version with a null validation result.", e.getMessage());
        }
    }

    @Test
    public void testArchiveVersionInvalidResult()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage("Something bad happened!");
            VersionService.ArchiveVersionValidationResult result = new VersionService.ArchiveVersionValidationResult(errors);
            defaultVersionService.archiveVersion(result);
            fail("Should have thrown exception");
        }
        catch (IllegalStateException e)
        {
            assertEquals("You can not archive a version with an invalid validation result.", e.getMessage());
        }
    }

    @Test
    public void testArchiveVersionSuccess()
    {
        IMocksControl control = EasyMock.createControl();

        MockVersion dbVersion = new MockVersion(99, "Something");
        MockVersion version = new MockVersion(99, null);
        version.setArchived(false);

        VersionManager mockVersionManager = control.createMock(VersionManager.class);
        mockVersionManager.archiveVersion(version, true);

        expect(mockVersionManager.getVersion(99L)).andReturn(dbVersion);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, null, null, null, null, dateFieldFormat, null);

        ErrorCollection errors = new SimpleErrorCollection();
        VersionService.ArchiveVersionValidationResult result = new VersionService.ArchiveVersionValidationResult(errors, version);
        final Version releasedVersion = defaultVersionService.archiveVersion(result);
        assertEquals(dbVersion, releasedVersion);

        verifyMocks(control);
    }


    @Test
    public void testUnarchiveVersionNoResult()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            defaultVersionService.unarchiveVersion(null);
            fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("You can not unarchive a version with a null validation result.", e.getMessage());
        }
    }

    @Test
    public void testUnarchiveVersionInvalidResult()
    {
        DefaultVersionService defaultVersionService = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        try
        {
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage("Something bad happened!");
            VersionService.ArchiveVersionValidationResult result = new VersionService.ArchiveVersionValidationResult(errors);
            defaultVersionService.unarchiveVersion(result);
            fail("Should have thrown exception");
        }
        catch (IllegalStateException e)
        {
            assertEquals("You can not unarchive a version with an invalid validation result.", e.getMessage());
        }
    }

    @Test
    public void testUnarchiveVersionSuccess()
    {
        IMocksControl control = EasyMock.createControl();

        Version mockVersion = control.createMock(Version.class);
        mockVersion.setArchived(false);
        expect(mockVersion.getId()).andReturn(99L).anyTimes();

        Version mockVersionDB = control.createMock(Version.class);

        VersionManager mockVersionManager = control.createMock(VersionManager.class);
        mockVersionManager.archiveVersion(mockVersion, false);

        expect(mockVersionManager.getVersion(99L)).andReturn(mockVersionDB);
        expect(mockVersionDB.getId()).andReturn(99L).anyTimes();

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, null, null, null, null, dateFieldFormat, null);

        ErrorCollection errors = new SimpleErrorCollection();
        VersionService.ArchiveVersionValidationResult result = new VersionService.ArchiveVersionValidationResult(errors, mockVersion);
        final Version releasedVersion = defaultVersionService.unarchiveVersion(result);
        assertEquals(mockVersionDB, releasedVersion);

        verifyMocks(control);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testIsOverdueNoVersion()
    {
        DefaultVersionService service = new DefaultVersionService(null, null, null, null, null, dateFieldFormat, null);
        service.isOverdue(null);
    }

    @Test
    public void testIsOverdueProject()
    {
        IMocksControl control = EasyMock.createControl();

        VersionManager versionManager = control.createMock(VersionManager.class);
        Version version = new MockVersion(18388338L, "Brenden");

        expect(versionManager.isVersionOverDue(version)).andReturn(false).andReturn(true);
        DefaultVersionService service = new DefaultVersionService(versionManager, null, null, null, null, dateFieldFormat, null);

        replayMocks(control);

        assertFalse(service.isOverdue(version));
        assertTrue(service.isOverdue(version));

        control.verify();
    }

    @Test
    public void testDatesAreConvertedToMidnight()
    {
        long projectId = 1234L;
        MockProject mockProject = new MockProject(projectId);

        final Version mockVersion = new MockVersion(1L, "Version Name", mockProject);
        final DateTime now = new DateTime();
        final Date midnightNow = new LocalDate(now.getMillis()).toDate();
        final Date nonMidnight = new DateTime(now).withHourOfDay(4).toDate();

        final VersionService.VersionBuilder builder = new VersionService.VersionBuilder(mockVersion)
                .startDate(nonMidnight)
                .releaseDate(nonMidnight);

        final IMocksControl control = EasyMock.createControl();
        final VersionManager mockVersionManager = control.createMock(VersionManager.class);
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);
        final ProjectManager projectManager = control.createMock(ProjectManager.class);

        expect(projectManager.getProjectObj(projectId))
                .andStubReturn(mockProject);

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user))
                .andStubReturn(true);

        expect(mockVersionManager.isDuplicateName(mockVersion, "Version Name"))
                .andStubReturn(false);

        replayMocks(control);

        DefaultVersionService defaultVersionService = new DefaultVersionService(mockVersionManager, permissionManager, null, null, nopI18nFactory, dateFieldFormat, projectManager);
        VersionService.VersionBuilderValidationResult result = defaultVersionService.validateUpdate(user, builder);

        assertThat(result.getResult().startDate, Matchers.is(midnightNow));
        assertThat(result.getResult().releaseDate, Matchers.is(midnightNow));

        verifyMocks(control);
    }

    private ErrorCollection errorMap(String name, String message)
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addError(name, message);
        return collection;
    }

    private ErrorCollection errors(String... args)
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        for (String arg : args)
        {
            collection.addErrorMessage(arg);
        }
        return collection;
    }

    private VersionManager createMappedVersionManager(final Map<Long, Version> versions)
    {
        return (VersionManager) DuckTypeProxy.getProxy(VersionManager.class, new Object()
        {
            public Version getVersion(Long id)
            {
                return versions.get(id);
            }
        });
    }

    private void assertHasErrorsAndFlush(JiraServiceContext context, VersionService.ValidationResult result)
    {
        assertNotNull(result);
        assertFalse(context.getErrorCollection().getFlushedErrorMessages().isEmpty());
        assertFalse(result.isValid());
    }

    private void assertHasNoErrors(JiraServiceContext context, VersionService.ValidationResult result)
    {
        assertNotNull(result);
        if (context.getErrorCollection().hasAnyErrors())
        {
            assertFalse(context.getErrorCollection().getErrorMessages().iterator().next(), context.getErrorCollection().hasAnyErrors());
        }
        assertTrue(result.isValid());
    }

    private MockVersion createMockVersion(final Long projectId)
    {
        return new MockVersion()
        {
            public Project getProjectObject()
            {
                return new MockProject(projectId);
            }
        };
    }

    private void verifyMocks(IMocksControl control)
    {
        verify(dateFieldFormat, nopI18nFactory);
        control.verify();
    }

    private void replayMocks(IMocksControl control)
    {
        EasyMockAnnotations.replayMocks(this);
        control.replay();
    }

    private class IdentityEqualsMockIssue extends MockIssue
    {
        private IdentityEqualsMockIssue(final Long id)
        {
            super(id);
        }

        public boolean equals(final Object o)
        {
            if (!(o instanceof IdentityEqualsMockIssue))
            {
                return false;
            }
            Long otherId = ((IdentityEqualsMockIssue) o).getId();
            return getId().equals(otherId);
        }

        public int hashCode()
        {
            return (int) getId().longValue();
        }
    }
}
