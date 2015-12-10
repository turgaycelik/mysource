package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operand.SimpleLiteralFactory;
import com.atlassian.query.operand.EmptyOperand;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;


/**
 * @since v6.2
 */
public class TestStatusCategoryResolver
{
    @Test
    public void testEmptyResolvesToDefault() throws Exception
    {
        final StatusCategory unknownCatgory = Mockito.mock(StatusCategory.class);

        final StatusCategoryManager categoryManager = Mockito.mock(StatusCategoryManager.class);
        Mockito.when(categoryManager.getDefaultStatusCategory()).thenReturn(unknownCatgory);

        final StatusCategoryResolver resolver = new StatusCategoryResolver(categoryManager);
        final Set<StatusCategory> categories = resolver.getStatusCategories(ImmutableList.of(new QueryLiteral(EmptyOperand.EMPTY)));

        Assert.assertEquals(ImmutableSet.of(unknownCatgory), categories);
    }

    @Test
    public void testCategoryResolution() throws Exception
    {
        final StatusCategory unknownCatgory = Mockito.mock(StatusCategory.class);
        final StatusCategory c1 = Mockito.mock(StatusCategory.class);
        final StatusCategory cKey = Mockito.mock(StatusCategory.class);
        final StatusCategory cName = Mockito.mock(StatusCategory.class);


        final StatusCategoryManager categoryManager = Mockito.mock(StatusCategoryManager.class);
        Mockito.when(categoryManager.getDefaultStatusCategory()).thenReturn(unknownCatgory);
        Mockito.when(categoryManager.getStatusCategory(1L)).thenReturn(c1);
        Mockito.when(categoryManager.getStatusCategoryByKey("key")).thenReturn(cKey);
        Mockito.when(categoryManager.getStatusCategoryByName("Name")).thenReturn(cName);


        final StatusCategoryResolver resolver = new StatusCategoryResolver(categoryManager);
        Assert.assertEquals(ImmutableSet.of(c1), resolver.getStatusCategories(ImmutableSet.of(
                SimpleLiteralFactory.createLiteral(1L))));
        Assert.assertEquals(ImmutableSet.of(cKey), resolver.getStatusCategories(ImmutableSet.of(
                SimpleLiteralFactory.createLiteral("key"))));
        Assert.assertEquals(ImmutableSet.of(cName), resolver.getStatusCategories(ImmutableSet.of(
                SimpleLiteralFactory.createLiteral("Name"))));

        Assert.assertEquals(ImmutableSet.of(cKey, cName), resolver.getStatusCategories(ImmutableSet.of(
                SimpleLiteralFactory.createLiteral("key"),
                SimpleLiteralFactory.createLiteral("Name"))));

        Assert.assertEquals(ImmutableSet.of(cKey, unknownCatgory), resolver.getStatusCategories(ImmutableSet.of(
                SimpleLiteralFactory.createLiteral("key"),
                new QueryLiteral(EmptyOperand.EMPTY))));
    }
}
