package com.atlassian.jira.util.i18n;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.mock.servlet.MockHttpSession;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestI18nTranslationModeSwitch
{
    private static final String MAGIC_PARAMETER = "i18ntranslate";
    private I18nTranslationModeSwitch i18nSwitch;
    private I18nTranslationModeImpl i18nTranslationMode;
    private MockHttpSession httpSession;
    private MockHttpServletRequest httpServletRequest;
    private MockHttpServletResponse httpServletResponse;

    @Before
    public void setUp() throws Exception
    {
        i18nTranslationMode = new I18nTranslationModeImpl();
        i18nSwitch = new I18nTranslationModeSwitch();

        httpSession = new MockHttpSession();
        httpServletRequest = new MockHttpServletRequest(httpSession);
        httpServletResponse = new MockHttpServletResponse();

        assertFalse(i18nTranslationMode.isTranslationMode());
    }

    @Test
    public void testSwitchTranslationsModeSessionEstablished() throws Exception
    {
        httpSession.setAttribute(I18nTranslationModeSwitch.class.getName(), Boolean.TRUE);

        i18nSwitch.switchTranslationsMode(httpServletRequest, httpServletResponse);

        assertTrue(i18nTranslationMode.isTranslationMode());
        assertNotNull(httpSession.getAttribute(I18nTranslationModeSwitch.class.getName()));
    }

    @Test
    public void testSwitchTranslationsModeStatusQuoOff() throws Exception
    {
        i18nSwitch.switchTranslationsMode(httpServletRequest, httpServletResponse);

        assertFalse(i18nTranslationMode.isTranslationMode());

        // extra assert that noting changed in the status quo.  If it was on its still on if its off
        assertNull(httpSession.getAttribute(I18nTranslationModeSwitch.class.getName()));
    }

    @Test
    public void testSwitchTranslationsModeStatusQuoOn() throws Exception
    {
        httpServletRequest.setRequestURL("/some/ok/path");
        i18nTranslationMode.setTranslationsModeOn(httpServletRequest, httpServletResponse);

        assertTrue(i18nTranslationMode.isTranslationMode());

        i18nSwitch.switchTranslationsMode(httpServletRequest, httpServletResponse);

        assertTrue(i18nTranslationMode.isTranslationMode());

        // extra assert that noting changed in the status quo.  If it was on its still on if its off
        assertNull(httpSession.getAttribute(I18nTranslationModeSwitch.class.getName()));
    }

    @Test
    public void testSwitchTranslationsModeTurnItOffFalse() throws Exception
    {
        assertThatItsOff("false");
    }

    @Test
    public void testSwitchTranslationsModeTurnItOffOff() throws Exception
    {
        assertThatItsOff("off");
    }

    private void assertThatItsOff(final String parameterValue)
    {
        httpSession.setAttribute(I18nTranslationModeSwitch.class.getName(), Boolean.TRUE);
        httpServletRequest.setRequestURL("/some/ok/path");
        i18nTranslationMode.setTranslationsModeOn(httpServletRequest, httpServletResponse);
        assertTrue(i18nTranslationMode.isTranslationMode());

        httpServletRequest.setParameter(MAGIC_PARAMETER, parameterValue);
        i18nSwitch.switchTranslationsMode(httpServletRequest, httpServletResponse);

        assertFalse(i18nTranslationMode.isTranslationMode());
    }

    @Test
    public void testSwitchTranslationsModeTurnItOnTrue() throws Exception
    {
        assertThatsItsOn("true");
    }

    @Test
    public void testSwitchTranslationsModeTurnItOnOn() throws Exception
    {
        assertThatsItsOn("on");
    }

    private void assertThatsItsOn(final String parameterValue)
    {
        httpServletRequest.setRequestURL("/some/ok/path");

        httpServletRequest.setParameter(MAGIC_PARAMETER, parameterValue);
        i18nSwitch.switchTranslationsMode(httpServletRequest, httpServletResponse);

        assertTrue(i18nTranslationMode.isTranslationMode());
        assertNotNull(httpSession.getAttribute(I18nTranslationModeSwitch.class.getName()));
    }


}
