package com.atlassian.jira.util.i18n;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 */
public class TestI18nTranslationModeImpl
{

    private I18nTranslationModeImpl i18nTranslationMode;
    private MockHttpServletRequest servletRequest;
    private MockHttpServletResponse servletResponse;

    @Before
    public void setUp()
    {
        i18nTranslationMode = new I18nTranslationModeImpl();
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
    }

    @Test
    public void testOff()
    {
        assertFalse(i18nTranslationMode.isTranslationMode());
        i18nTranslationMode.setTranslationsModeOff();
        assertFalse(i18nTranslationMode.isTranslationMode());
    }

    @Test
    public void testOn()
    {
        assertFalse(i18nTranslationMode.isTranslationMode());

        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURL("/rest/notandendpoint");
        i18nTranslationMode.setTranslationsModeOn(servletRequest, servletResponse);
        assertFalse(i18nTranslationMode.isTranslationMode());

        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURL("/sr/issieview:x");
        i18nTranslationMode.setTranslationsModeOn(servletRequest, servletResponse);
        assertFalse(i18nTranslationMode.isTranslationMode());

        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURL("/secure/SomeAction.jspa");
        servletRequest.setHeader("X-Requested-With", "XMLHttpRequest");
        i18nTranslationMode.setTranslationsModeOn(servletRequest, servletResponse);
        assertFalse(i18nTranslationMode.isTranslationMode());

        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURL("/secure/SomeAction.jspa");
        servletRequest.setHeader("Accept", "application/xml application/json");
        i18nTranslationMode.setTranslationsModeOn(servletRequest, servletResponse);
        assertFalse(i18nTranslationMode.isTranslationMode());


        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURL("/secure/SomeAction.jspa");
        i18nTranslationMode.setTranslationsModeOn(servletRequest, servletResponse);
        assertTrue(i18nTranslationMode.isTranslationMode());

    }


}
