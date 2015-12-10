package com.atlassian.jira.appconsistency.integrity.integritycheck;

import java.util.List;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.junit.rules.InitMockitoMocks;

import com.google.common.collect.ImmutableList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
public class TestAbstractIntegrityCheck
{
    @Mock
    private Check check1;
    @Mock
    private Check check2;
    @Mock
    private Check check3;

    private AbstractIntegrityCheck abstractIntegrityCheck;

    private List<Check> getChecksResponse;


    @Rule
    public InitMockitoMocks mocks = new InitMockitoMocks(this);


    @Before
    public void before()
    {
        abstractIntegrityCheck = new TestAbstractIntegrityCheckImpl(123, "description");
        getChecksResponse = ImmutableList.of(check1, check2, check3);
    }

    @Test
    public void shouldNotBeAvailableWhenOneOfTheChecksIsNotAvailable()
    {
        setAvaibilityResponses(true, false, true);
        Assert.assertFalse(abstractIntegrityCheck.isAvailable());
    }

    @Test
    public void shouldBeAvailableWhenAllChecksAreAvailable()
    {
        setAvaibilityResponses(true, true, true);
        Assert.assertTrue(abstractIntegrityCheck.isAvailable());
    }

    @Test
    public void shouldNotBeAvailableWhenAllChecksAreNotAvailable()
    {
        setAvaibilityResponses(false, false, false);
        Assert.assertFalse(abstractIntegrityCheck.isAvailable());
    }

    @Test
    public void shouldBeAvailableWhenChecksListIsEmpty()
    {
        getChecksResponse = ImmutableList.of();
        Assert.assertTrue(abstractIntegrityCheck.isAvailable());
    }

    private void setAvaibilityResponses(boolean check1, boolean check2, boolean check3)
    {
        when(this.check1.isAvailable()).thenReturn(check1);
        when(this.check2.isAvailable()).thenReturn(check2);
        when(this.check3.isAvailable()).thenReturn(check3);
    }

    private class TestAbstractIntegrityCheckImpl extends AbstractIntegrityCheck
    {

        protected TestAbstractIntegrityCheckImpl(final int id, final String description)
        {
            super(id, description);
        }

        @Override
        public List<? extends Check> getChecks()
        {
            return getChecksResponse;
        }
    }
}
