package com.atlassian.jira.datetime;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import com.atlassian.jira.junit.rules.MockitoContainer;

public class TestDateTimeVelocityUtils
{
    @Rule
    public MockitoContainer mockito = new MockitoContainer(this);
    
    @Mock
    DateTimeFormatter formatterMock;
    
    
    @Before
    public void setUp()
    {
        when(formatterMock.forLoggedInUser()).thenReturn(formatterMock);
    }

    @Test
    public void testFormatDMYShouldSetUpSystemTimeZone()
    {
        when(formatterMock.withStyle(DateTimeStyle.DATE)).thenReturn(formatterMock);
        when(formatterMock.withSystemZone()).thenReturn(formatterMock);

        try
        {
        
            utils().formatDMY(new Date());
        
        } catch (NullPointerException e)
        {
            fail("formatDMY method have to use system time zone together with DATE style.");
        }
        
        verify(formatterMock, times(1)).withSystemZone();
    }
    
    DateTimeVelocityUtils utils()
    {
        return new DateTimeVelocityUtils(formatterMock);
    }
    
}
