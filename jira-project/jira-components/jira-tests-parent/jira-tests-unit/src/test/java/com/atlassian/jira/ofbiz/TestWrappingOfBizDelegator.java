package com.atlassian.jira.ofbiz;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.Transformation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestWrappingOfBizDelegator
{
    private static final List<String> ORDER_BY = Arrays.asList("a", "b");
    private static final String ENTITY_NAME = "Foo";
    private static final String LOCK_FIELD = "Bar";

    private OfBizDelegator ofBizDelegator;
    @Mock private OfBizDelegator mockDelegate;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        ofBizDelegator = new WrappingOfBizDelegator(mockDelegate);
    }

    @Test
    public void transformShouldNotWrapGenericValuesThatAreNotIssues()
    {
        // Set up
        final EntityCondition mockEntityCondition = mock(EntityCondition.class);
        final Transformation mockTransformation = mock(Transformation.class);

        final List<GenericValue> results = Arrays.asList(mock(GenericValue.class), mock(GenericValue.class));
        when(mockDelegate.transform(ENTITY_NAME, mockEntityCondition, ORDER_BY, LOCK_FIELD, mockTransformation))
                .thenReturn(results);

        // Invoke
        final List<GenericValue> transformedValues =
                ofBizDelegator.transform(ENTITY_NAME, mockEntityCondition, ORDER_BY, LOCK_FIELD, mockTransformation);

        // Check
        assertEquals(results, transformedValues);
    }

    @Test
    public void transformOneShouldNotWrapGenericValueThatIsNotIssue()
    {
        // Set up
        final EntityCondition mockEntityCondition = mock(EntityCondition.class);
        final Transformation mockTransformation = mock(Transformation.class);
        final GenericValue mockGenericValue = mock(GenericValue.class);
        when(mockGenericValue.getEntityName()).thenReturn(ENTITY_NAME);
        when(mockDelegate.transformOne(ENTITY_NAME, mockEntityCondition, LOCK_FIELD, mockTransformation))
                .thenReturn(mockGenericValue);

        // Invoke
        final GenericValue transformedValue =
                ofBizDelegator.transformOne(ENTITY_NAME, mockEntityCondition, LOCK_FIELD, mockTransformation);

        // Check
        assertEquals(mockGenericValue, transformedValue);
    }
}
