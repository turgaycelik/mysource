package com.atlassian.jira.action.admin;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import junit.framework.AssertionFailedError;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.2.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestOfBizDowngradeHandler
{
    @Mock
    private OfBizDelegator ofBizDelegator;

    private OfBizDowngradeHandler downgradeHandler;

    @Before
    public void setUp()
    {
        this.downgradeHandler = new OfBizDowngradeHandler(ofBizDelegator);
    }

    @Test
    @Ignore
    public void testDowngradeShouldNotDoAnythingOnMaster()
    {
        final String message = "\n\n" +
                "The OfBizDowngradeHandler should always be a no-op on master.  If you are seeing this\n" +
                "message, then someone has added a downgrade task.  If this is a stable branch, then\n" +
                "that is fine; just add an @Ignore for this test on the stable branch.  However, when\n" +
                "you merge the stable branch into master, you need to remove both the downgrade logic\n" +
                "and the @Ignore to keep master clean!\n\nUnexpected call to: ";

        final OfBizDelegator cantTouchThis = mock(OfBizDelegator.class, new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock howVeryDareYou) throws Throwable
            {
                throw new AssertionFailedError(message + howVeryDareYou);
            }
        });

        new OfBizDowngradeHandler(cantTouchThis).downgrade();
    }

    @Test
    public void testDowngradeRemovesAllEntriesForProjectCentricNavigationFromTheFeaturesTable()
    {
        List<GenericValue> featureRowsToDelete = someGenericValues();
        when(ofBizDelegator.findByLike("Feature", ImmutableMap.of("featureName", "com.atlassian.jira.projects.ProjectCentricNavigation%"))).thenReturn(featureRowsToDelete);

        downgradeHandler.downgrade();

        verify(ofBizDelegator).removeAll(featureRowsToDelete);
    }

    private List<GenericValue> someGenericValues()
    {
        return Arrays.asList(mock(GenericValue.class), mock(GenericValue.class));
    }
}
