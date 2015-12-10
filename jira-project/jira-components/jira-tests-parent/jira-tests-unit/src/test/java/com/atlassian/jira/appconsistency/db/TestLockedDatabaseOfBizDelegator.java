package com.atlassian.jira.appconsistency.db;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.Transformation;

import static com.atlassian.jira.entity.Entity.Name.ISSUE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestLockedDatabaseOfBizDelegator
{
    private static final String LOCK_FIELD = "anyField";

    private OfBizDelegator ofBizDelegator;

    @Before
    public void setUp() throws Exception
    {
        this.ofBizDelegator = new LockedDatabaseOfBizDelegator();
    }

    @Test
    public void transformMultipleMethodShouldThrowUnsupportedOperationException()
    {
        // Set up
        final EntityCondition mockEntityCondition = mock(EntityCondition.class);
        final Transformation mockTransformation = mock(Transformation.class);
        final List<String> orderBy = Collections.emptyList();

        // Invoke
        try
        {
            ofBizDelegator.transform(ISSUE, mockEntityCondition, orderBy, LOCK_FIELD, mockTransformation);
        }
        catch (final UnsupportedOperationException expected)
        {
            // Check
            assertEquals(LockedDatabaseOfBizDelegator.MESSAGE, expected.getMessage());
        }
    }

    @Test
    public void transformOneMethodShouldThrowUnsupportedOperationException()
    {
        // Set up
        final EntityCondition mockEntityCondition = mock(EntityCondition.class);
        final Transformation mockTransformation = mock(Transformation.class);

        // Invoke
        try
        {
            ofBizDelegator.transformOne(ISSUE, mockEntityCondition, LOCK_FIELD, mockTransformation);
        }
        catch (final UnsupportedOperationException expected)
        {
            // Check
            assertEquals(LockedDatabaseOfBizDelegator.MESSAGE, expected.getMessage());
        }
    }
}
