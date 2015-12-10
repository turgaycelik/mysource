package com.atlassian.jira.plugin.jql.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.3
 */
public class TestLatestReleasedVersionFunction
{
    private ApplicationUser theUser;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause = null;
    private List<GenericValue> projectsList1 = new ArrayList<GenericValue>();
    private List<GenericValue> projectsList2 = new ArrayList<GenericValue>();

    private List<Version> releasedVersionList = new ArrayList<Version>();
    private List<Version> unreleasedVersionList = new ArrayList<Version>();
    private Project project1;
    private Project project2;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockApplicationUser("fred");
        queryCreationContext = new QueryCreationContextImpl(theUser);

        project1 = new MockProject(21l, "c1");
        project2 = new MockProject(22l, "c2");

        projectsList1.add(project1.getGenericValue());

        projectsList2.add(project2.getGenericValue());

        releasedVersionList.add(new MockVersion(1, "V2", project1, 2l));
        releasedVersionList.add(new MockVersion(2, "V1", project1, 1l));
        releasedVersionList.add(new MockVersion(3, "V5", project1, 3l));
        unreleasedVersionList.add(new MockVersion(4, "V3", project1, 3l));  // Earliest
        unreleasedVersionList.add(new MockVersion(5, "V4", project1, 4l));
    }

    @Test
    public void testValidateProjectArgument() throws Exception
    {
        final PermissionManager permissionmanager = EasyMock.createMock(PermissionManager.class);
        final ProjectResolver projectResolver = EasyMock.createMock(ProjectResolver.class);
        final VersionManager versionManager = EasyMock.createMock(VersionManager.class);
        LatestReleasedVersionFunction latestreleasedversionFunction = new LatestReleasedVersionFunction(versionManager, projectResolver, permissionmanager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
        latestreleasedversionFunction.init(MockJqlFunctionModuleDescriptor.create("latestReleasedVersion", true));
        FunctionOperand function = new FunctionOperand(LatestReleasedVersionFunction.FUNCTION_LATEST_RELEASED_VERSION, Arrays.asList("c1"));
        EasyMock.expect(projectResolver.getIdsFromName("c1")).andReturn(Arrays.asList("21"));
        EasyMock.expect(projectResolver.get(21l)).andReturn(project1);
        EasyMock.expect(permissionmanager.hasPermission(Permissions.BROWSE, project1, (ApplicationUser)null)).andReturn(true);

        EasyMock.replay(projectResolver, permissionmanager);

        final MessageSet messageSet = latestreleasedversionFunction.validate(null, function, terminalClause);
        assertTrue(!messageSet.hasAnyMessages());
    }

    @Test
    public void testValidateBadProjectArgument() throws Exception
    {
        final PermissionManager permissionmanager = EasyMock.createMock(PermissionManager.class);
        final ProjectResolver projectResolver = EasyMock.createMock(ProjectResolver.class);
        final VersionManager versionManager = EasyMock.createMock(VersionManager.class);
        LatestReleasedVersionFunction latestreleasedversionFunction = new LatestReleasedVersionFunction(versionManager, projectResolver, permissionmanager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
        latestreleasedversionFunction.init(MockJqlFunctionModuleDescriptor.create("latestReleasedVersion", true));
        FunctionOperand function = new FunctionOperand(LatestReleasedVersionFunction.FUNCTION_LATEST_RELEASED_VERSION, Arrays.asList("badproject"));
        EasyMock.expect(projectResolver.getIdsFromName("badproject")).andReturn(Collections.<String>emptyList());
        EasyMock.replay(projectResolver);

        final MessageSet messageSet = latestreleasedversionFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Could not resolve the project 'badproject' provided to function 'latestReleasedVersion'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testDataType() throws Exception
    {
        final PermissionManager permissionmanager = EasyMock.createMock(PermissionManager.class);
        final ProjectResolver projectResolver = EasyMock.createMock(ProjectResolver.class);
        final VersionManager versionManager = EasyMock.createMock(VersionManager.class);
        LatestReleasedVersionFunction latestreleasedversionFunction = new LatestReleasedVersionFunction(versionManager, projectResolver, permissionmanager);
        assertEquals(JiraDataTypes.VERSION, latestreleasedversionFunction.getDataType());
    }

    @Test
    public void testGetValues() throws Exception
    {
        final PermissionManager permissionmanager = EasyMock.createMock(PermissionManager.class);
        final ProjectResolver projectResolver = EasyMock.createMock(ProjectResolver.class);
        final VersionManager versionManager = EasyMock.createMock(VersionManager.class);
        LatestReleasedVersionFunction latestreleasedversionFunction = new LatestReleasedVersionFunction(versionManager, projectResolver, permissionmanager);
        FunctionOperand function = new FunctionOperand(LatestReleasedVersionFunction.FUNCTION_LATEST_RELEASED_VERSION, Arrays.asList("c1"));

        EasyMock.expect(projectResolver.getIdsFromName("c1")).andReturn(Arrays.asList("21"));
        EasyMock.expect(projectResolver.get(21l)).andReturn(project1);
        EasyMock.expect(permissionmanager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(true);
        EasyMock.expect(versionManager.getVersionsReleased(Long.valueOf(21), true)).andReturn(releasedVersionList);
        EasyMock.replay(projectResolver, permissionmanager, versionManager);

        final List<QueryLiteral> value = latestreleasedversionFunction.getValues(queryCreationContext, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());
        assertEquals(3, value.get(0).getLongValue().longValue());
    }

    @Test
    public void testGetValuesNoVersions() throws Exception
    {
        final PermissionManager permissionmanager = EasyMock.createMock(PermissionManager.class);
        final ProjectResolver projectResolver = EasyMock.createMock(ProjectResolver.class);
        final VersionManager versionManager = EasyMock.createMock(VersionManager.class);
        LatestReleasedVersionFunction latestreleasedversionFunction = new LatestReleasedVersionFunction(versionManager, projectResolver, permissionmanager);
        FunctionOperand function = new FunctionOperand(LatestReleasedVersionFunction.FUNCTION_LATEST_RELEASED_VERSION, Arrays.asList("c1"));

        EasyMock.expect(projectResolver.getIdsFromName("c1")).andReturn(Arrays.asList("21"));
        EasyMock.expect(projectResolver.get(21l)).andReturn(project1);
        EasyMock.expect(permissionmanager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(true);
        EasyMock.expect(versionManager.getVersionsReleased(Long.valueOf(21), true)).andReturn(Collections.<Version>emptyList());
        EasyMock.replay(projectResolver, permissionmanager, versionManager);

        final List<QueryLiteral> value = latestreleasedversionFunction.getValues(queryCreationContext, function, terminalClause);
        assertNotNull(value);
        assertEquals(0, value.size());
    }

}
