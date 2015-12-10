package com.atlassian.jira.util;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mockito answers
 */
public class AnswerWith
{

    public static <X> Answer<X> firstParameter()
    {
        return new Answer<X>()
        {
            @Override
            public X answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                return (X) invocationOnMock.getArguments()[0];
            }
        };
    }

    public static <X> Answer<X> mockInstance()
    {
        return new Answer<X>()
        {
            @Override
            public X answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                return (X) invocationOnMock.getMock();
            }
        };
    }
}
