package com.atlassian.jira.jql.values;

import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestVersionClauseValuesGenerator extends MockControllerTestCase
{
    private VersionManager versionManager;
    private PermissionManager permissionManager;

    private VersionClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        versionManager = mockController.getMock(VersionManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);

        valuesGenerator = new VersionClauseValuesGenerator(versionManager, permissionManager, null)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };
    }

    @Test
    public void testGetPossibleValuesHappyPath() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        final MockVersion version1 = new MockVersion(1L, "Aa comp", project1);
        final MockVersion version2 = new MockVersion(2L, "A comp", project1);
        final MockVersion version3 = new MockVersion(3L, "B comp", project1);
        final MockVersion version4 = new MockVersion(4L, "C comp", project2);
        final MockVersion version5 = new MockVersion(5L, "D comp", project1);

        versionManager.getAllVersions();
        mockController.setReturnValue(CollectionBuilder.newBuilder(version5, version4, version3, version2, version1).asList());

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);

        assertEquals(5, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A comp"));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result("B comp"));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result("C comp"));
        assertEquals(possibleValues.getResults().get(4), new ClauseValuesGenerator.Result("D comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchFullValue() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        final MockVersion version1 = new MockVersion(1L, "Aa comp", project1);
        final MockVersion version2 = new MockVersion(2L, "A comp", project1);
        final MockVersion version3 = new MockVersion(3L, "B comp", project1);
        final MockVersion version4 = new MockVersion(4L, "C comp", project2);
        final MockVersion version5 = new MockVersion(5L, "D comp", project1);

        versionManager.getAllVersions();
        mockController.setReturnValue(CollectionBuilder.newBuilder(version5, version4, version3, version2, version1).asList());

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "aa comp", 10);

        assertEquals(1, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        final MockVersion version1 = new MockVersion(1L, "Aa comp", project1);
        final MockVersion version2 = new MockVersion(2L, "Aa comp blah", project1);
        final MockVersion version3 = new MockVersion(3L, "B comp", project1);
        final MockVersion version4 = new MockVersion(4L, "C comp", project2);
        final MockVersion version5 = new MockVersion(5L, "D comp", project1);

        versionManager.getAllVersions();
        mockController.setReturnValue(CollectionBuilder.newBuilder(version5, version4, version3, version2, version1).asList());

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "aa comp", 10);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("Aa comp blah"));

        mockController.verify();
    }
    
    @Test
    public void testGetPossibleValuesNoMatching() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        final MockVersion version1 = new MockVersion(1L, "Aa comp", project1);
        final MockVersion version2 = new MockVersion(2L, "A comp", project1);
        final MockVersion version3 = new MockVersion(3L, "B comp", project1);
        final MockVersion version4 = new MockVersion(4L, "C comp", project2);
        final MockVersion version5 = new MockVersion(5L, "D comp", project1);

        versionManager.getAllVersions();
        mockController.setReturnValue(CollectionBuilder.newBuilder(version5, version4, version3, version2, version1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "Z", 10);

        assertEquals(0, possibleValues.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesSomeMatching() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        final MockVersion version1 = new MockVersion(1L, "Aa comp", project1);
        final MockVersion version2 = new MockVersion(2L, "A comp", project1);
        final MockVersion version3 = new MockVersion(3L, "B comp", project1);
        final MockVersion version4 = new MockVersion(4L, "C comp", project2);
        final MockVersion version5 = new MockVersion(5L, "D comp", project1);

        versionManager.getAllVersions();
        mockController.setReturnValue(CollectionBuilder.newBuilder(version5, version4, version3, version2, version1).asList());

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "a", 10);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesHitMax() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        final MockVersion version1 = new MockVersion(1L, "Aa comp", project1);
        final MockVersion version2 = new MockVersion(2L, "A comp", project1);
        final MockVersion version3 = new MockVersion(3L, "B comp", project1);
        final MockVersion version4 = new MockVersion(4L, "C comp", project2);
        final MockVersion version5 = new MockVersion(5L, "D comp", project1);

        versionManager.getAllVersions();
        mockController.setReturnValue(CollectionBuilder.newBuilder(version5, version4, version3, version2, version1).asList());

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 4);

        assertEquals(4, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A comp"));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result("B comp"));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result("C comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesNoPermForProject() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        final MockVersion version1 = new MockVersion(1L, "Aa comp", project1);
        final MockVersion version2 = new MockVersion(2L, "A comp", project1);
        final MockVersion version3 = new MockVersion(3L, "B comp", project1);
        final MockVersion version4 = new MockVersion(4L, "C comp", project2);
        final MockVersion version5 = new MockVersion(5L, "D comp", project1);

        versionManager.getAllVersions();
        mockController.setReturnValue(CollectionBuilder.newBuilder(version5, version4, version3, version2, version1).asList());

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null);
        mockController.setReturnValue(false);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);

        assertEquals(4, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A comp"));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result("B comp"));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result("D comp"));

        mockController.verify();
    }

}
