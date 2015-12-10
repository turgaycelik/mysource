package com.atlassian.jira.action.admin;

import java.util.Map;
import java.util.TreeMap;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.Attributes;

/**
 * Builder for attributes
 *
 * @since v6.1
 */
class MockAttributesBuilder
{
    private static final String ID = "id";

    private final Map<String, String> map = new TreeMap<String, String>();

    MockAttributesBuilder id(String id)
    {
        map.put(ID, id);
        return this;
    }

    MockAttributesBuilder attr(String key, String value)
    {
        map.put(key, value);
        return this;
    }

    Attributes build()
    {
        final Attributes mock = Mockito.mock(Attributes.class);
        Mockito.when(mock.getValue(Mockito.anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                final String key = (String) args[0];
                return map.get(key);
            }
        });
        return mock;
    }

}
