package com.atlassian.jira.jql.values;

import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestProjectClauseValuesGenerator extends MockControllerTestCase
{
    
    private PermissionManager permissionManager;
    private ProjectClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        permissionManager = mockController.getMock(PermissionManager.class);
        valuesGenerator = new ProjectClauseValuesGenerator(permissionManager)
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
        final MockProject type1 = new MockProject(1L, "TST", "Aa it");
        final MockProject type2 = new MockProject(2L, "TST", "A it");
        final MockProject type3 = new MockProject(3L, "TST", "B it");
        final MockProject type4 = new MockProject(4L, "ANA", "C it");

        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "", 5);

        assertEquals(4, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName(), new String [] {type1.getName(), " (" + type1.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName(), new String [] {type2.getName(), " (" + type2.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result(type3.getName(), new String [] {type3.getName(), " (" + type3.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result(type4.getName(), new String [] {type4.getName(), " (" + type4.getKey() + ")"}));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchFullValue() throws Exception
    {
        final MockProject type1 = new MockProject(1L, "TST", "Aa it");
        final MockProject type2 = new MockProject(2L, "TST", "A it");
        final MockProject type3 = new MockProject(3L, "TST", "B it");
        final MockProject type4 = new MockProject(4L, "ANA", "C it");

        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Aa it", 5);

        assertEquals(1, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName(), new String [] {type1.getName(), " (" + type1.getKey() + ")"}));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final MockProject type1 = new MockProject(1L, "TST", "Aa it");
        final MockProject type2 = new MockProject(2L, "TST", "Aa it blah");
        final MockProject type3 = new MockProject(3L, "TST", "B it");
        final MockProject type4 = new MockProject(4L, "ANA", "C it");

        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Aa it", 5);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName(), new String [] {type1.getName(), " (" + type1.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName(), new String [] {type2.getName(), " (" + type2.getKey() + ")"}));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchNone() throws Exception
    {
        final MockProject type1 = new MockProject(1L, "TST", "Aa it");
        final MockProject type2 = new MockProject(2L, "TST", "A it");
        final MockProject type3 = new MockProject(3L, "TST", "B it");
        final MockProject type4 = new MockProject(4L, "ANA", "C it");

        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Z", 5);

        assertEquals(0, possibleValues.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchSome() throws Exception
    {
        final MockProject type1 = new MockProject(1L, "TST", "Aa it");
        final MockProject type2 = new MockProject(2L, "TST", "A it");
        final MockProject type3 = new MockProject(3L, "TST", "B it");
        final MockProject type4 = new MockProject(4L, "ZNZ", "C it");

        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "a", 5);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName(), new String [] {type1.getName(), " (" + type1.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName(), new String [] {type2.getName(), " (" + type2.getKey() + ")"}));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchSomeAndFromKey() throws Exception
    {
        final MockProject type1 = new MockProject(1L, "TST", "Aa it");
        final MockProject type2 = new MockProject(2L, "TST", "A it");
        final MockProject type3 = new MockProject(3L, "TST", "B it");
        final MockProject type4 = new MockProject(4L, "ANA", "C it");

        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "a", 5);

        assertEquals(3, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName(), new String [] {type1.getName(), " (" + type1.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName(), new String [] {type2.getName(), " (" + type2.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result(type4.getName(), new String [] {type4.getName(), " (" + type4.getKey() + ")"}));

        mockController.verify();
    }
    
    @Test
    public void testGetPossibleValuesMatchToLimit() throws Exception
    {
        final MockProject type1 = new MockProject(1L, "TST", "Aa it");
        final MockProject type2 = new MockProject(2L, "TST", "A it");
        final MockProject type3 = new MockProject(3L, "TST", "B it");
        final MockProject type4 = new MockProject(4L, "ANA", "C it");

        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "", 3);

        assertEquals(3, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName(), new String [] {type1.getName(), " (" + type1.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName(), new String [] {type2.getName(), " (" + type2.getKey() + ")"}));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result(type3.getName(), new String [] {type3.getName(), " (" + type3.getKey() + ")"}));

        mockController.verify();
    }

}
