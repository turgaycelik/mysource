package com.atlassian.jira.i18n;

import java.util.Locale;
import java.util.Map;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableMap;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class TestJiraI18nResolver
{
    private JiraAuthenticationContext jiraAuthenticationContext;
    private I18nHelper.BeanFactory beanFactory;
    private I18nHelper i18nHelper;

    @Before
    public void setUp() throws Exception
    {
        jiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        beanFactory = createMock(I18nHelper.BeanFactory.class);
        i18nHelper = createMock(I18nHelper.class);

        expect(jiraAuthenticationContext.getLocale()).andStubReturn(Locale.getDefault());

        expect(beanFactory.getInstance(Locale.getDefault())).andStubReturn(i18nHelper);
    }

    @Test
    public void testGetAllTranslationsForPrefix() throws Exception
    {
        final Map<String, String> translations = new ImmutableMap.Builder<String, String>()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", "value3")
                .build();


        expect(i18nHelper.getKeysForPrefix("key")).andReturn(translations.keySet());

        expect(i18nHelper.getUnescapedText(EasyMock.startsWith("key"))).andStubAnswer(new IAnswer<String>()
        {
            @Override
            public String answer() throws Throwable
            {
                Object[] arguments = EasyMock.getCurrentArguments();
                return translations.get(arguments[0].toString());
            }
        });

        replay(jiraAuthenticationContext, beanFactory, i18nHelper);


        JiraI18nResolver i18nResolver = new JiraI18nResolver(jiraAuthenticationContext, beanFactory);

        Map<String, String> actual = i18nResolver.getAllTranslationsForPrefix("key");

        assertNotSame(actual, translations);
        assertEquals(actual, translations);


        verify(jiraAuthenticationContext, beanFactory, i18nHelper);

    }

    @Test
    public void testResolveText() throws Exception
    {
        String[] arguments = new String[] { "p1", "p2", "p3" };
        expect(i18nHelper.getText("question", arguments)).andReturn("answer");


        replay(jiraAuthenticationContext, beanFactory, i18nHelper);


        JiraI18nResolver i18nResolver = new JiraI18nResolver(jiraAuthenticationContext, beanFactory);

        String answer = i18nResolver.resolveText("question", arguments);

        assertEquals("answer", answer);


        verify(jiraAuthenticationContext, beanFactory, i18nHelper);


    }

    @Test
    public void testRawText() throws Exception
    {
        expect(i18nHelper.getUnescapedText("question")).andReturn("answer");


        replay(jiraAuthenticationContext, beanFactory, i18nHelper);


        JiraI18nResolver i18nResolver = new JiraI18nResolver(jiraAuthenticationContext, beanFactory);

        String answer = i18nResolver.getRawText("question");

        assertEquals("answer", answer);


        verify(jiraAuthenticationContext, beanFactory, i18nHelper);


    }

}
