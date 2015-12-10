package com.atlassian.jira.security.auth.trustedapps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestUserNameTransformerFactory
{
    @Test
    public void testGetsLowerCaseUserNameTransformerByDefault()
    {
        UserNameTransformer.Factory factory = new UserNameTransformer.Factory(new UserNameTransformer.ClassNameRetriever()
        {
            public Class get()
            {
                return null;
            }
        });
        UserNameTransformer transformer = factory.get();
        assertEquals("name", transformer.transform("NAME"));
        assertEquals("name", transformer.transform("NaME"));
        assertEquals("name", transformer.transform("NAmE"));
        assertEquals("name", transformer.transform("NAMe"));
        assertEquals("name", transformer.transform("Name"));
        assertEquals("somethingelse", transformer.transform("SomethingElse"));
    }

    @Test
    public void testNoOpUserNameTransformer()
    {
        UserNameTransformer.Factory factory = new UserNameTransformer.Factory(new UserNameTransformer.ClassNameRetriever()
        {
            public Class get()
            {
                return UserNameTransformer.NoOp.class;
            }
        });
        UserNameTransformer transformer = factory.get();
        assertEquals("NAME", transformer.transform("NAME"));
        assertEquals("NaME", transformer.transform("NaME"));
        assertEquals("NAmE", transformer.transform("NAmE"));
        assertEquals("NAMe", transformer.transform("NAMe"));
        assertEquals("Name", transformer.transform("Name"));
        assertEquals("SomethingElse", transformer.transform("SomethingElse"));
    }

    @Test
    public void testGetsLowerCaseUserNameTransformer()
    {
        UserNameTransformer.Factory factory = new UserNameTransformer.Factory(new UserNameTransformer.ClassNameRetriever()
        {
            public Class get()
            {
                return UserNameTransformer.LowerCase.class;
            }
        });
        UserNameTransformer transformer = factory.get();
        assertEquals("name", transformer.transform("NAME"));
        assertEquals("name", transformer.transform("NaME"));
        assertEquals("name", transformer.transform("NAmE"));
        assertEquals("name", transformer.transform("NAMe"));
        assertEquals("name", transformer.transform("Name"));
        assertEquals("somethingelse", transformer.transform("SomethingElse"));
    }
}
