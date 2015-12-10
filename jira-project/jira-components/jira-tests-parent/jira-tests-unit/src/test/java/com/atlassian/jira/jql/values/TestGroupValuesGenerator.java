package com.atlassian.jira.jql.values;

import java.util.Collection;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestGroupValuesGenerator extends MockControllerTestCase
{
    private GroupValuesGenerator groupValuesGenerator;

    @Test
    public void testGetPossibleValuesHappyPath() throws Exception
    {
        final Group devGroup = new MockGroup("jira-dev");
        final Group userGroup = new MockGroup("jira-user");
        final Group otherGroup = new MockGroup("other");
        groupValuesGenerator = new GroupValuesGenerator(null)
        {
            @Override
            protected Collection<Group> getAllGroups()
            {
                return CollectionBuilder.newBuilder(devGroup, userGroup, otherGroup).asMutableList();
            }
        };

        final ClauseValuesGenerator.Results possibleValues = groupValuesGenerator.getPossibleValues(null, "Group Stuff", "", 15);
        assertEquals(CollectionBuilder.newBuilder(new ClauseValuesGenerator.Result("jira-dev"), new ClauseValuesGenerator.Result("jira-user"),
                new ClauseValuesGenerator.Result("other")).asList(), possibleValues.getResults());
    }

    @Test
    public void testGetPossibleValuesMatchFullValue() throws Exception
    {
        final Group devGroup = new MockGroup("jira-dev");
        final Group userGroup = new MockGroup("jira-user");
        final Group otherGroup = new MockGroup("other");
        groupValuesGenerator = new GroupValuesGenerator(null)
        {
            @Override
            protected Collection<Group> getAllGroups()
            {
                return CollectionBuilder.newBuilder(devGroup, userGroup, otherGroup).asMutableList();
            }
        };

        final ClauseValuesGenerator.Results possibleValues = groupValuesGenerator.getPossibleValues(null, "Group Stuff", "jira-dev", 15);
        assertEquals(1, possibleValues.getResults().size());
        assertEquals(CollectionBuilder.newBuilder(new ClauseValuesGenerator.Result("jira-dev")).asList(), possibleValues.getResults());
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final Group devGroup = new MockGroup("jira-dev");
        final Group userGroup = new MockGroup("jira-developers");
        final Group otherGroup = new MockGroup("other");
        groupValuesGenerator = new GroupValuesGenerator(null)
        {
            @Override
            protected Collection<Group> getAllGroups()
            {
                return CollectionBuilder.newBuilder(devGroup, userGroup, otherGroup).asMutableList();
            }
        };

        final ClauseValuesGenerator.Results possibleValues = groupValuesGenerator.getPossibleValues(null, "Group Stuff", "jira-dev", 15);
        assertEquals(2, possibleValues.getResults().size());
        assertEquals(CollectionBuilder.newBuilder(new ClauseValuesGenerator.Result("jira-dev"), new ClauseValuesGenerator.Result("jira-developers")).asList(), possibleValues.getResults());
    }

    @Test
    public void testGetPossibleValuesMatches() throws Exception
    {
        final Group devGroup = new MockGroup("jira-dev");
        final Group userGroup = new MockGroup("jira-user");
        final Group otherGroup = new MockGroup("other");
        groupValuesGenerator = new GroupValuesGenerator(null)
        {
            @Override
            protected Collection<Group> getAllGroups()
            {
                return CollectionBuilder.newBuilder(devGroup, userGroup, otherGroup).asMutableList();
            }
        };

        final ClauseValuesGenerator.Results possibleValues = groupValuesGenerator.getPossibleValues(null, "Group Stuff", "JIRA", 15);
        assertEquals(CollectionBuilder.newBuilder(new ClauseValuesGenerator.Result("jira-dev"), new ClauseValuesGenerator.Result("jira-user")).asList(), possibleValues.getResults());
    }

    @Test
    public void testGetPossibleValuesLimit() throws Exception
    {
        final Group devGroup = new MockGroup("jira-dev");
        final Group userGroup = new MockGroup("jira-user");
        final Group otherGroup = new MockGroup("other");
        groupValuesGenerator = new GroupValuesGenerator(null)
        {
            @Override
            protected Collection<Group> getAllGroups()
            {
                return CollectionBuilder.newBuilder(devGroup, userGroup, otherGroup).asMutableList();
            }
        };

        final ClauseValuesGenerator.Results possibleValues = groupValuesGenerator.getPossibleValues(null, "Group Stuff", "", 2);
        assertEquals(CollectionBuilder.newBuilder(new ClauseValuesGenerator.Result("jira-dev"), new ClauseValuesGenerator.Result("jira-user")).asList(), possibleValues.getResults());
    }

}
