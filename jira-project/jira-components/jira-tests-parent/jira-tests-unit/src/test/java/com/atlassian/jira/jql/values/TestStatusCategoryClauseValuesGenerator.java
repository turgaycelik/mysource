package com.atlassian.jira.jql.values;

import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.status.category.StatusCategoryImpl;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

/**
 * @since v6.2
 */
public class TestStatusCategoryClauseValuesGenerator
{

    private StatusCategoryClauseValuesGenerator generator;

    @Before
    public void setUp() throws Exception
    {
        final StatusCategoryManager categoryManager = Mockito.mock(StatusCategoryManager.class);
        Mockito.when(categoryManager.getUserVisibleStatusCategories()).thenReturn(StatusCategoryImpl.getUserVisibleCategories());

        generator = new StatusCategoryClauseValuesGenerator(categoryManager);
    }

    @Test
    public void testGetAllConstantsInOrder() throws Exception
    {
        Assert.assertEquals(toResults(StatusCategoryImpl.getUserVisibleCategories()),
                generator.getPossibleValues(null, "statusCategory", "", 10).getResults());
    }

    @Test
    public void testGetAllConstantsWithLimit() throws Exception
    {
        Assert.assertEquals(toResults(StatusCategoryImpl.getUserVisibleCategories().subList(0, 2)),
                generator.getPossibleValues(null, "statusCategory", "", 2).getResults());
    }

    @Test
    public void testGetWithValidPrefix() throws Exception
    {
        Assert.assertEquals(toResults(ImmutableList.of(StatusCategoryImpl.findById(4L))),
                generator.getPossibleValues(null, "statusCategory", "I", 10).getResults());
        Assert.assertEquals(toResults(ImmutableList.of(StatusCategoryImpl.findById(4L))),
                generator.getPossibleValues(null, "statusCategory", "i", 10).getResults());
        Assert.assertEquals(toResults(ImmutableList.of(StatusCategoryImpl.findById(4L))),
                generator.getPossibleValues(null, "statusCategory", "in ", 10).getResults());
    }

    @Test
    public void testGetWithInvalidPrefix() throws Exception
    {
        Assert.assertEquals(Collections.<ClauseValuesGenerator.Result>emptyList(),
                generator.getPossibleValues(null, "statusCategory", "x", 10).getResults());
    }

    private List<ClauseValuesGenerator.Result> toResults(List<StatusCategory> categories)
    {
        return Lists.transform(categories, new Function<StatusCategory, ClauseValuesGenerator.Result>()
        {
            @Override
            public ClauseValuesGenerator.Result apply(StatusCategory input)
            {
                return new ClauseValuesGenerator.Result(input.getName());
            }
        });
    }
}
