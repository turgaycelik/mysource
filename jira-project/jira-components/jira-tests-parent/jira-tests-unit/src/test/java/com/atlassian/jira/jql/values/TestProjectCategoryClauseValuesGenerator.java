package com.atlassian.jira.jql.values;

import java.util.Locale;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestProjectCategoryClauseValuesGenerator extends MockControllerTestCase
{
    private ProjectManager projectManager;
    private ProjectCategoryClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        this.projectManager = mockController.getMock(ProjectManager.class);
        this.valuesGenerator = new ProjectCategoryClauseValuesGenerator(projectManager)
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
        final MockGenericValue type1 = new MockGenericValue("ProjCat", EasyMap.build("name", "Aa it"));
        final MockGenericValue type2 = new MockGenericValue("ProjCat", EasyMap.build("name", "A it"));
        final MockGenericValue type3 = new MockGenericValue("ProjCat", EasyMap.build("name", "B it"));
        final MockGenericValue type4 = new MockGenericValue("ProjCat", EasyMap.build("name", "C it"));

        projectManager.getProjectCategories();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "", 5);

        assertEquals(4, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getString("name")));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getString("name")));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result(type3.getString("name")));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result(type4.getString("name")));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchFullValue() throws Exception
    {
        final MockGenericValue type1 = new MockGenericValue("ProjCat", EasyMap.build("name", "Aa it"));
        final MockGenericValue type2 = new MockGenericValue("ProjCat", EasyMap.build("name", "A it"));
        final MockGenericValue type3 = new MockGenericValue("ProjCat", EasyMap.build("name", "B it"));
        final MockGenericValue type4 = new MockGenericValue("ProjCat", EasyMap.build("name", "C it"));

        projectManager.getProjectCategories();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Aa it", 5);

        assertEquals(1, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getString("name")));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final MockGenericValue type1 = new MockGenericValue("ProjCat", EasyMap.build("name", "Aa it"));
        final MockGenericValue type2 = new MockGenericValue("ProjCat", EasyMap.build("name", "Aa it blah"));
        final MockGenericValue type3 = new MockGenericValue("ProjCat", EasyMap.build("name", "B it"));
        final MockGenericValue type4 = new MockGenericValue("ProjCat", EasyMap.build("name", "C it"));

        projectManager.getProjectCategories();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Aa it", 5);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getString("name")));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getString("name")));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchNone() throws Exception
    {
        final MockGenericValue type1 = new MockGenericValue("ProjCat", EasyMap.build("name", "Aa it"));
        final MockGenericValue type2 = new MockGenericValue("ProjCat", EasyMap.build("name", "A it"));
        final MockGenericValue type3 = new MockGenericValue("ProjCat", EasyMap.build("name", "B it"));
        final MockGenericValue type4 = new MockGenericValue("ProjCat", EasyMap.build("name", "C it"));

        projectManager.getProjectCategories();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Z", 5);

        assertEquals(0, possibleValues.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchSome() throws Exception
    {
        final MockGenericValue type1 = new MockGenericValue("ProjCat", EasyMap.build("name", "Aa it"));
        final MockGenericValue type2 = new MockGenericValue("ProjCat", EasyMap.build("name", "A it"));
        final MockGenericValue type3 = new MockGenericValue("ProjCat", EasyMap.build("name", "B it"));
        final MockGenericValue type4 = new MockGenericValue("ProjCat", EasyMap.build("name", "C it"));

        projectManager.getProjectCategories();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "a", 5);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getString("name")));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getString("name")));

        mockController.verify();
    }
    
    @Test
    public void testGetPossibleValuesMatchToLimit() throws Exception
    {
        final MockGenericValue type1 = new MockGenericValue("ProjCat", EasyMap.build("name", "Aa it"));
        final MockGenericValue type2 = new MockGenericValue("ProjCat", EasyMap.build("name", "A it"));
        final MockGenericValue type3 = new MockGenericValue("ProjCat", EasyMap.build("name", "B it"));
        final MockGenericValue type4 = new MockGenericValue("ProjCat", EasyMap.build("name", "C it"));

        projectManager.getProjectCategories();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "", 3);

        assertEquals(3, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getString("name")));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getString("name")));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result(type3.getString("name")));

        mockController.verify();
    }

}
