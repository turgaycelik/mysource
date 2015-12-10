package com.atlassian.jira.plugin.workflow;

import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.MockProjectPermission;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Map;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.jira.permission.ProjectPermissionCategory.ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.ISSUES;
import static com.atlassian.jira.permission.ProjectPermissionCategory.OTHER;
import static com.atlassian.jira.permission.ProjectPermissionCategory.PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.TIME_TRACKING;
import static com.atlassian.jira.permission.ProjectPermissionCategory.VOTERS_AND_WATCHERS;
import static com.atlassian.jira.permission.ProjectPermissions.RESOLVE_ISSUES;
import static com.atlassian.jira.util.collect.MapBuilder.singletonMap;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public abstract class AbstractTestWorkflowPermissionPluginFactory<F extends AbstractWorkflowPermissionPluginFactory, D extends AbstractDescriptor>
{
    private static final String PROJECT_PERMISSION_NAME = "Project Permission Name";
    private static final String ISSUE_PERMISSION_NAME = "Issue Permission Name";
    private static final String VOTERS_AND_WATCHERS_PERMISSION_NAME = "Voters And Watchers Permission Name";
    private static final String COMMENTS_PERMISSION_NAME = "Comments Permission Name";
    private static final String ATTACHMENTS_PERMISSION_NAME = "Attachments Permission Name";
    private static final String TIME_TRACKING_PERMISSION_NAME = "Time Tracking Permission Name";
    private static final String OTHER_PERMISSION_NAME = "Other Permission Name";

    @Mock protected JiraAuthenticationContext authenticationContext;
    @Mock protected I18nHelper i18nHelper;
    @Mock protected PermissionManager permissionManager;

    protected ProjectPermission projectPermission = new MockProjectPermission("projectKey", "projectNameKey", null, PROJECTS);
    private ProjectPermission issuePermission = new MockProjectPermission("issueKey", "issueNameKey", null, ISSUES);
    private ProjectPermission votersAndWatchersPermission = new MockProjectPermission("votersAndWatchersKey", "votersAndWatchersNameKey", null, VOTERS_AND_WATCHERS);
    private ProjectPermission commentsPermission = new MockProjectPermission("commentsKey", "commentsNameKey", null, COMMENTS);
    private ProjectPermission attachmentsPermission = new MockProjectPermission("attachmentsKey", "attachmentsNameKey", null, ATTACHMENTS);
    private ProjectPermission timeTrackingPermission = new MockProjectPermission("timeTrackingKey", "timeTrackingNameKey", null, TIME_TRACKING);
    private ProjectPermission otherPermission = new MockProjectPermission("otherKey", "otherNameKey", null, OTHER);

    protected F factory;
    protected D descriptor;

    protected Map<String, Object> parameters = newHashMap();
    protected Map<String, String> descriptorArgs = newHashMap();

    @Before
    public void setUp()
    {
        when(authenticationContext.getI18nHelper()).thenReturn(i18nHelper);

        when(i18nHelper.getText(projectPermission.getNameI18nKey())).thenReturn(PROJECT_PERMISSION_NAME);
        when(i18nHelper.getText(issuePermission.getNameI18nKey())).thenReturn(ISSUE_PERMISSION_NAME);
        when(i18nHelper.getText(votersAndWatchersPermission.getNameI18nKey())).thenReturn(VOTERS_AND_WATCHERS_PERMISSION_NAME);
        when(i18nHelper.getText(commentsPermission.getNameI18nKey())).thenReturn(COMMENTS_PERMISSION_NAME);
        when(i18nHelper.getText(attachmentsPermission.getNameI18nKey())).thenReturn(ATTACHMENTS_PERMISSION_NAME);
        when(i18nHelper.getText(timeTrackingPermission.getNameI18nKey())).thenReturn(TIME_TRACKING_PERMISSION_NAME);
        when(i18nHelper.getText(otherPermission.getNameI18nKey())).thenReturn(OTHER_PERMISSION_NAME);

        when(permissionManager.getProjectPermissions(PROJECTS)).thenReturn(asList(projectPermission));
        when(permissionManager.getProjectPermissions(ISSUES)).thenReturn(asList(issuePermission));
        when(permissionManager.getProjectPermissions(VOTERS_AND_WATCHERS)).thenReturn(asList(votersAndWatchersPermission));
        when(permissionManager.getProjectPermissions(COMMENTS)).thenReturn(asList(commentsPermission));
        when(permissionManager.getProjectPermissions(ATTACHMENTS)).thenReturn(asList(attachmentsPermission));
        when(permissionManager.getProjectPermissions(TIME_TRACKING)).thenReturn(asList(timeTrackingPermission));
        when(permissionManager.getProjectPermissions(OTHER)).thenReturn(asList(otherPermission));

        factory = createFactory();
        descriptor = createDescriptor();
    }

    protected abstract F createFactory();

    protected abstract D createDescriptor();

    @Test
    public void getVelocityParamsForInputPutsAllPermissionsIntoTemplateParameters()
    {
        factory.getVelocityParamsForInput(parameters);

        checkPermissionsParameter();
    }

    @Test
    public void getVelocityParamsForEditPutsAllPermissionsAndCurrentPermissionKeyIntoTemplateParameters()
    {
        descriptorArgs.put("permissionKey", projectPermission.getKey());

        factory.getVelocityParamsForEdit(parameters, descriptor);

        checkPermissionsParameter();
        checkCurrentPermissionParameter(projectPermission.getKey());
    }

    @Test
    public void getVelocityParamsForEditPutsAllPermissionsAndCurrentPermissionKeyIntoTemplateParametersWithRecognisableLegacyArgument()
    {
        descriptorArgs.put("permission", "Resolve Issues");

        factory.getVelocityParamsForEdit(parameters, descriptor);

        checkPermissionsParameter();
        checkCurrentPermissionParameter(RESOLVE_ISSUES.permissionKey());
    }

    @Test
    public void getVelocityParamsForEditPutsAllPermissionsAndCurrentPermissionKeyIntoTemplateParametersWithUnrecognisableLegacyArgument()
    {
        descriptorArgs.put("permission", "some crap");

        factory.getVelocityParamsForEdit(parameters, descriptor);

        checkPermissionsParameter();
        checkCurrentPermissionParameter("some crap");
    }

    private void checkCurrentPermissionParameter(String expectedValue)
    {
        String permissionKey = (String) parameters.get("permission");
        assertThat(permissionKey, equalTo(expectedValue));
    }

    @Test
    public void getVelocityParamsForEditAddsUnavailableGroupIfSelectedPermissionIsNotFound()
    {
        descriptorArgs.put("permissionKey", "unknown");

        factory.getVelocityParamsForEdit(parameters, descriptor);

        checkUnavailableGroup();
    }

    @Test
    public void getVelocityParamsForEditAddsUnavailableGroupIfSelectedPermissionIsNotFoundWithLegacyArgument()
    {
        descriptorArgs.put("permission", "unknown");

        factory.getVelocityParamsForEdit(parameters, descriptor);

        checkUnavailableGroup();
    }

    private void checkUnavailableGroup()
    {
        Map<String, Map<String, String>> permissionGroups = checkPermissionsParameter();
        Map<String, String> unavailablePermissions = permissionGroups.get("admin.permission.group.unavailable.permissions");
        assertThat(unavailablePermissions, equalTo(singletonMap("unknown", "unknown")));
    }

    private Map<String, Map<String, String>> checkPermissionsParameter()
    {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> permissionGroups = (Map<String, Map<String, String>>) parameters.get("permissions");

        assertThat(permissionGroups, notNullValue());

        Map<String, String> projectPermissions = permissionGroups.get("admin.permission.group.project.permissions");
        Map<String, String> issuePermissions = permissionGroups.get("admin.permission.group.issue.permissions");
        Map<String, String> votersAndWatchersPermissions = permissionGroups.get("admin.permission.group.voters.and.watchers.permissions");
        Map<String, String> commentsPermissions = permissionGroups.get("admin.permission.group.comments.permissions");
        Map<String, String> attachmentsPermissions = permissionGroups.get("admin.permission.group.attachments.permissions");
        Map<String, String> timeTrackingPermissions = permissionGroups.get("admin.permission.group.time.tracking.permissions");
        Map<String, String> otherPermissions = permissionGroups.get("admin.permission.group.other.permissions");

        assertThat(projectPermissions, equalTo(singletonMap(projectPermission.getKey(), PROJECT_PERMISSION_NAME)));
        assertThat(issuePermissions, equalTo(singletonMap(issuePermission.getKey(), ISSUE_PERMISSION_NAME)));
        assertThat(votersAndWatchersPermissions, equalTo(singletonMap(votersAndWatchersPermission.getKey(), VOTERS_AND_WATCHERS_PERMISSION_NAME)));
        assertThat(commentsPermissions, equalTo(singletonMap(commentsPermission.getKey(), COMMENTS_PERMISSION_NAME)));
        assertThat(attachmentsPermissions, equalTo(singletonMap(attachmentsPermission.getKey(), ATTACHMENTS_PERMISSION_NAME)));
        assertThat(timeTrackingPermissions, equalTo(singletonMap(timeTrackingPermission.getKey(), TIME_TRACKING_PERMISSION_NAME)));
        assertThat(otherPermissions, equalTo(singletonMap(otherPermission.getKey(), OTHER_PERMISSION_NAME)));

        return permissionGroups;
    }

    @Test
    public void getVelocityParamsForViewPutsPermissionNameKeyIntoTemplateParametersIfPermissionExist()
    {
        descriptorArgs.put("permissionKey", projectPermission.getKey());
        when(permissionManager.getProjectPermission(new ProjectPermissionKey(projectPermission.getKey()))).thenReturn(some(projectPermission));

        checkViewParameters(projectPermission.getNameI18nKey(), true);
    }

    @Test
    public void getVelocityParamsForViewPutsPermissionKeyIntoTemplateParametersIfPermissionDoesNotExist()
    {
        descriptorArgs.put("permissionKey", projectPermission.getKey());
        when(permissionManager.getProjectPermission(new ProjectPermissionKey(projectPermission.getKey()))).thenReturn(Option.<ProjectPermission>none());

        checkViewParameters(projectPermission.getKey(), false);
    }

    private void checkViewParameters(String expectedPermissionValue, boolean expectedDefinedValue)
    {
        factory.getVelocityParamsForView(parameters, descriptor);

        String permission = (String) parameters.get("permission");
        boolean defined = (Boolean) parameters.get("defined");

        assertThat(permission, equalTo(expectedPermissionValue));
        assertThat(defined, is(expectedDefinedValue));
    }
}
