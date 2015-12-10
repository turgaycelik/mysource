package com.atlassian.jira.jql.values;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestSecurityLevelClauseValuesGenerator extends MockControllerTestCase
{
    private IssueSecurityLevelManager issueSecurityLevelManager;
    private SecurityLevelClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        issueSecurityLevelManager = mockController.getMock(IssueSecurityLevelManager.class);

        valuesGenerator = new SecurityLevelClauseValuesGenerator(issueSecurityLevelManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };
    }

    @Test
    public void testGetRelevantSecurityLevelsHappyPath() throws Exception
    {
        final MockGenericValue level1 = new MockGenericValue("SecLevel", EasyMap.build("name", "Aa lev"));
        final MockGenericValue level2 = new MockGenericValue("SecLevel", EasyMap.build("name", "A lev"));
        final MockGenericValue level3 = new MockGenericValue("SecLevel", EasyMap.build("name", "B lev"));
        final MockGenericValue level4 = new MockGenericValue("SecLevel", EasyMap.build("name", "C lev"));

        issueSecurityLevelManager.getAllUsersSecurityLevels(null);
        final List<MockGenericValue> levels = CollectionBuilder.newBuilder(level4, level3, level2, level1).asList();
        mockController.setReturnValue(levels);

        mockController.replay();

        final List<GenericValue> result = valuesGenerator.getRelevantSecurityLevels(null);
        assertEquals(result, levels);

        mockController.verify();
    }

    @Test
    public void testGetRelevantSecurityLevelsNullLevels() throws Exception
    {
        issueSecurityLevelManager.getAllUsersSecurityLevels(null);
        mockController.setReturnValue(null);

        mockController.replay();

        final List<GenericValue> result = valuesGenerator.getRelevantSecurityLevels(null);
        assertEquals(result, Collections.emptyList());

        mockController.verify();
    }

    @Test
    public void testGetRelevantSecurityLevelsException() throws Exception
    {
        issueSecurityLevelManager.getAllUsersSecurityLevels(null);
        mockController.setThrowable(new GenericEntityException("blah"));

        mockController.replay();

        final List<GenericValue> result = valuesGenerator.getRelevantSecurityLevels(null);
        assertEquals(result, Collections.emptyList());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesHappyPath() throws Exception
    {
        final MockGenericValue level1 = new MockGenericValue("SecLevel", EasyMap.build("name", "Aa lev"));
        final MockGenericValue level2 = new MockGenericValue("SecLevel", EasyMap.build("name", "A lev"));
        final MockGenericValue level3 = new MockGenericValue("SecLevel", EasyMap.build("name", "B lev"));
        final MockGenericValue level4 = new MockGenericValue("SecLevel", EasyMap.build("name", "C lev"));
        final List<GenericValue> levels = CollectionBuilder.<GenericValue>newBuilder(level4, level3, level2, level1).asMutableList();

        valuesGenerator = new SecurityLevelClauseValuesGenerator(issueSecurityLevelManager)
        {
            @Override
            List<GenericValue> getRelevantSecurityLevels(final User searcher)
            {
                return levels;
            }

            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };

        mockController.replay();

        final ClauseValuesGenerator.Results result = valuesGenerator.getPossibleValues(null, "level", "", 10);
        assertEquals(4, result.getResults().size());
        assertEquals(new ClauseValuesGenerator.Result(level1.getString("name")), result.getResults().get(0));
        assertEquals(new ClauseValuesGenerator.Result(level2.getString("name")), result.getResults().get(1));
        assertEquals(new ClauseValuesGenerator.Result(level3.getString("name")), result.getResults().get(2));
        assertEquals(new ClauseValuesGenerator.Result(level4.getString("name")), result.getResults().get(3));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchFullValue() throws Exception
    {
        final MockGenericValue level1 = new MockGenericValue("SecLevel", EasyMap.build("name", "Aa lev"));
        final MockGenericValue level2 = new MockGenericValue("SecLevel", EasyMap.build("name", "A lev"));
        final MockGenericValue level3 = new MockGenericValue("SecLevel", EasyMap.build("name", "B lev"));
        final MockGenericValue level4 = new MockGenericValue("SecLevel", EasyMap.build("name", "C lev"));
        final List<GenericValue> levels = CollectionBuilder.<GenericValue>newBuilder(level4, level3, level2, level1).asMutableList();

        valuesGenerator = new SecurityLevelClauseValuesGenerator(issueSecurityLevelManager)
        {
            @Override
            List<GenericValue> getRelevantSecurityLevels(final User searcher)
            {
                return levels;
            }

            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };

        mockController.replay();

        final ClauseValuesGenerator.Results result = valuesGenerator.getPossibleValues(null, "level", "Aa lev", 10);
        assertEquals(1, result.getResults().size());
        assertEquals(new ClauseValuesGenerator.Result(level1.getString("name")), result.getResults().get(0));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final MockGenericValue level1 = new MockGenericValue("SecLevel", EasyMap.build("name", "Aa lev"));
        final MockGenericValue level2 = new MockGenericValue("SecLevel", EasyMap.build("name", "Aa lev blah"));
        final MockGenericValue level3 = new MockGenericValue("SecLevel", EasyMap.build("name", "B lev"));
        final MockGenericValue level4 = new MockGenericValue("SecLevel", EasyMap.build("name", "C lev"));
        final List<GenericValue> levels = CollectionBuilder.<GenericValue>newBuilder(level4, level3, level2, level1).asMutableList();

        valuesGenerator = new SecurityLevelClauseValuesGenerator(issueSecurityLevelManager)
        {
            @Override
            List<GenericValue> getRelevantSecurityLevels(final User searcher)
            {
                return levels;
            }

            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };

        mockController.replay();

        final ClauseValuesGenerator.Results result = valuesGenerator.getPossibleValues(null, "level", "Aa lev", 10);
        assertEquals(2, result.getResults().size());
        assertEquals(new ClauseValuesGenerator.Result(level1.getString("name")), result.getResults().get(0));
        assertEquals(new ClauseValuesGenerator.Result(level2.getString("name")), result.getResults().get(1));

        mockController.verify();
    }
    
    @Test
    public void testGetPossibleValuesMatchNone() throws Exception
    {
        final MockGenericValue level1 = new MockGenericValue("SecLevel", EasyMap.build("name", "Aa lev"));
        final MockGenericValue level2 = new MockGenericValue("SecLevel", EasyMap.build("name", "A lev"));
        final MockGenericValue level3 = new MockGenericValue("SecLevel", EasyMap.build("name", "B lev"));
        final MockGenericValue level4 = new MockGenericValue("SecLevel", EasyMap.build("name", "C lev"));
        final List<GenericValue> levels = CollectionBuilder.<GenericValue>newBuilder(level4, level3, level2, level1).asMutableList();

        valuesGenerator = new SecurityLevelClauseValuesGenerator(issueSecurityLevelManager)
        {
            @Override
            List<GenericValue> getRelevantSecurityLevels(final User searcher)
            {
                return levels;
            }

            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };

        mockController.replay();

        final ClauseValuesGenerator.Results result = valuesGenerator.getPossibleValues(null, "level", "Z", 10);
        assertEquals(0, result.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchSome() throws Exception
    {
        final MockGenericValue level1 = new MockGenericValue("SecLevel", EasyMap.build("name", "Aa lev"));
        final MockGenericValue level2 = new MockGenericValue("SecLevel", EasyMap.build("name", "A lev"));
        final MockGenericValue level3 = new MockGenericValue("SecLevel", EasyMap.build("name", "B lev"));
        final MockGenericValue level4 = new MockGenericValue("SecLevel", EasyMap.build("name", "C lev"));
        final List<GenericValue> levels = CollectionBuilder.<GenericValue>newBuilder(level4, level3, level2, level1).asMutableList();

        valuesGenerator = new SecurityLevelClauseValuesGenerator(issueSecurityLevelManager)
        {
            @Override
            List<GenericValue> getRelevantSecurityLevels(final User searcher)
            {
                return levels;
            }

            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };

        mockController.replay();

        final ClauseValuesGenerator.Results result = valuesGenerator.getPossibleValues(null, "level", "a", 10);
        assertEquals(2, result.getResults().size());
        assertEquals(new ClauseValuesGenerator.Result(level1.getString("name")), result.getResults().get(0));
        assertEquals(new ClauseValuesGenerator.Result(level2.getString("name")), result.getResults().get(1));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchToLimit() throws Exception
    {
        final MockGenericValue level1 = new MockGenericValue("SecLevel", EasyMap.build("name", "Aa lev"));
        final MockGenericValue level2 = new MockGenericValue("SecLevel", EasyMap.build("name", "A lev"));
        final MockGenericValue level3 = new MockGenericValue("SecLevel", EasyMap.build("name", "B lev"));
        final MockGenericValue level4 = new MockGenericValue("SecLevel", EasyMap.build("name", "C lev"));
        final List<GenericValue> levels = CollectionBuilder.<GenericValue>newBuilder(level4, level3, level2, level1).asMutableList();

        valuesGenerator = new SecurityLevelClauseValuesGenerator(issueSecurityLevelManager)
        {
            @Override
            List<GenericValue> getRelevantSecurityLevels(final User searcher)
            {
                return levels;
            }

            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };

        mockController.replay();

        final ClauseValuesGenerator.Results result = valuesGenerator.getPossibleValues(null, "level", "", 3);
        assertEquals(3, result.getResults().size());
        assertEquals(new ClauseValuesGenerator.Result(level1.getString("name")), result.getResults().get(0));
        assertEquals(new ClauseValuesGenerator.Result(level2.getString("name")), result.getResults().get(1));
        assertEquals(new ClauseValuesGenerator.Result(level3.getString("name")), result.getResults().get(2));

        mockController.verify();
    }

}
