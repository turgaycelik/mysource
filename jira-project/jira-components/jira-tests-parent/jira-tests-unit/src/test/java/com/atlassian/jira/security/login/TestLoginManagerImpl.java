package com.atlassian.jira.security.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginInfoImpl;
import com.atlassian.jira.bc.security.login.LoginReason;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpSession;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.auth.AuthorisationManager;
import com.atlassian.jira.servlet.JiraCaptchaService;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.AuthenticatorException;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.capture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class TestLoginManagerImpl extends MockControllerTestCase
{

    private static final String AUTHORISED_FAILURE = "com.atlassian.jira.security.login.LoginManager.AUTHORISED_FAILURE";
    private static final String ELEVATED_SECURITY_FAILURE = "com.atlassian.jira.security.login.LoginManager.ELEVATED_SECURITY_FAILURE";
    private static final String OS_CAPTCHA = "os_captcha";

    MockUser fred;
    MockApplicationUser fredAppUser;
    private LoginStore loginStore;
    private AuthorisationManager authorisationManager;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private LoginManagerImpl.StaticDependencies staticDependencies;
    private Authenticator authenticator;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private CrowdService crowdService;
    private JiraCaptchaService jiraCaptchaService;
    private ImageCaptchaService imageCaptchaService;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private VelocityRequestContext velocityRequestContext;
    private EventPublisher eventPublisher;

    @Before
    public void setUp() throws Exception
    {
        fred = new MockUser("fred");
        fredAppUser = new MockApplicationUser("fred");
        loginStore = getMock(LoginStore.class);
        authorisationManager = getMock(AuthorisationManager.class);
        httpServletRequest = getMock(HttpServletRequest.class);
        httpServletResponse = getMock(HttpServletResponse.class);
        staticDependencies = getMock(LoginManagerImpl.StaticDependencies.class);
        authenticator = getMock(Authenticator.class);
        jiraAuthenticationContext = getMock(JiraAuthenticationContext.class);
        crowdService = getMock(CrowdService.class);
        jiraCaptchaService = getMock(JiraCaptchaService.class);
        imageCaptchaService = createMock(ImageCaptchaService.class);
        velocityRequestContextFactory = createMock(VelocityRequestContextFactory.class);
        velocityRequestContext = createMock(VelocityRequestContext.class);
        eventPublisher = createMock(EventPublisher.class);

        componentAccessorWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext);
    }

    @Test
    public void testAuthoriseLogin_NoUsePermission()
    {
        expect(authorisationManager.authoriseForLogin(fredAppUser, httpServletRequest)).andReturn(false);

        httpServletRequest.removeAttribute(AUTHORISED_FAILURE);
        expectLastCall();
        httpServletRequest.setAttribute(AUTHORISED_FAILURE, true);
        expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertFalse(loginManager.authoriseForLogin(fredAppUser, httpServletRequest));
    }


    @Test
    public void testAuthoriseLogin_OK()
    {
        expect(authorisationManager.authoriseForLogin(fredAppUser, httpServletRequest)).andReturn(true);

        httpServletRequest.removeAttribute(AUTHORISED_FAILURE);
        expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertTrue(loginManager.authoriseForLogin(fredAppUser, httpServletRequest));
    }

    @Test
    public void testAuthoriseLogin_OK_admin()
    {
        expect(authorisationManager.authoriseForLogin(fredAppUser, httpServletRequest)).andReturn(true);

        httpServletRequest.removeAttribute(AUTHORISED_FAILURE);
        expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertTrue(loginManager.authoriseForLogin(fredAppUser, httpServletRequest));
    }

    @Test
    public void testGetLoginInfo()
    {

        LoginInfo expectedLoginInfo = makeLoginInfo();

        expect(crowdService.getUser(fred.getName())).andReturn(fred);

        expect(loginStore.getLoginInfo(fred)).andReturn(expectedLoginInfo);
        expect(loginStore.getMaxAuthenticationAttemptsAllowed()).andReturn(3L);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        final LoginInfo loginInfo = loginManager.getLoginInfo(fred.getName());
        assertNotNull(loginInfo);
        assertEquals(new Long(123L), loginInfo.getLastLoginTime());
        assertEquals(new Long(456L), loginInfo.getPreviousLoginTime());
        assertEquals(new Long(789L), loginInfo.getLastFailedLoginTime());
        assertEquals(new Long(101112L), loginInfo.getLoginCount());
        assertEquals(new Long(131415L), loginInfo.getCurrentFailedLoginCount());
        assertEquals(new Long(171819L), loginInfo.getTotalFailedLoginCount());
    }

    @Test
    public void test_performElevatedSecurityCheck_NotNeeded()
    {
        httpServletRequest.removeAttribute(ELEVATED_SECURITY_FAILURE);
        expectLastCall();

        LoginInfo expectedLoginInfo = makeLoginInfo(1L);
        expect(loginStore.getLoginInfo(fred)).andReturn(expectedLoginInfo);
        expect(loginStore.getMaxAuthenticationAttemptsAllowed()).andReturn(3L);

        expect(crowdService.getUser(fred.getName())).andReturn(fred);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertTrue(loginManager.performElevatedSecurityCheck(httpServletRequest, "fred"));
    }

    @Test
    public void test_performElevatedSecurityCheck_UserNotKnown()
    {
        httpServletRequest.removeAttribute(ELEVATED_SECURITY_FAILURE);
        expectLastCall();

        expect(crowdService.getUser(fred.getName())).andReturn(null);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertTrue(loginManager.performElevatedSecurityCheck(httpServletRequest, "fred"));
    }

    private void expect_getLoginInfo(final long currentFailedLoginCount, final long maxAttempts)
    {
        LoginInfo expectedLoginInfo = makeLoginInfo(currentFailedLoginCount);
        expect(loginStore.getLoginInfo(fred)).andStubReturn(expectedLoginInfo);
        expect(loginStore.getMaxAuthenticationAttemptsAllowed()).andStubReturn(maxAttempts);

        expect(crowdService.getUser(fred.getName())).andReturn(fred);
    }

    @Test
    public void test_performElevatedSecurityCheck_Needed_ButFAIL()
    {
        httpServletRequest.removeAttribute(ELEVATED_SECURITY_FAILURE);
        expectLastCall();
        expect(httpServletRequest.getParameter(OS_CAPTCHA)).andReturn("aseasyasabc");
        expect(httpServletRequest.getSession(true)).andReturn(new MockHttpSession());

        expect(jiraCaptchaService.getImageCaptchaService()).andReturn(imageCaptchaService);
        expect(imageCaptchaService.validateResponseForID("session1234", "aseasyasabc")).andReturn(false);

        httpServletRequest.setAttribute(ELEVATED_SECURITY_FAILURE, true);
        expectLastCall();

        expect_getLoginInfo(5L, 3L);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertFalse(loginManager.performElevatedSecurityCheck(httpServletRequest, "fred"));
    }

    @Test
    public void test_performElevatedSecurityCheck_Needed_ButCAPTCHAThrowsUp()
    {
        httpServletRequest.removeAttribute(ELEVATED_SECURITY_FAILURE);
        expectLastCall();
        expect(httpServletRequest.getParameter(OS_CAPTCHA)).andReturn("aseasyasabc");
        expect(httpServletRequest.getSession(true)).andReturn(new MockHttpSession());

        expect(jiraCaptchaService.getImageCaptchaService()).andReturn(imageCaptchaService);
        //noinspection ThrowableInstanceNeverThrown
        expect(imageCaptchaService.validateResponseForID("session1234", "aseasyasabc")).andThrow(new CaptchaServiceException("Expected exception"));

        httpServletRequest.setAttribute(ELEVATED_SECURITY_FAILURE, true);
        expectLastCall();

        expect_getLoginInfo(5L, 3L);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertFalse(loginManager.performElevatedSecurityCheck(httpServletRequest, "fred"));
    }

    @Test
    public void test_performElevatedSecurityCheck_Needed_AndOK()
    {
        httpServletRequest.removeAttribute(ELEVATED_SECURITY_FAILURE);
        expectLastCall();
        expect(httpServletRequest.getParameter(OS_CAPTCHA)).andReturn("aseasyasabc");
        expect(httpServletRequest.getSession(true)).andReturn(new MockHttpSession());

        expect(jiraCaptchaService.getImageCaptchaService()).andReturn(imageCaptchaService);
        expect(imageCaptchaService.validateResponseForID("session1234", "aseasyasabc")).andReturn(true);

        expect_getLoginInfo(5L, 3L);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertTrue(loginManager.performElevatedSecurityCheck(httpServletRequest, "fred"));
    }

    @Test
    public void test_authenticate_And_ElevatedSecurityNeeded()
    {
        expect_getLoginInfo(5L, 3L);     // causes elevated security to be needed!
        
        expect(loginStore.recordLoginAttempt(fred, false)).andStubReturn(makeLoginInfo());

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        final LoginResult loginResult = loginManager.authenticate(fred, "password");

        assertNotNull(loginResult);
        assertFalse(loginResult.isOK());
        assertEquals(LoginReason.AUTHENTICATION_DENIED, loginResult.getReason());
    }

    @Test
    public void test_authenticate_And_BadPassword()
    {
        expect_getLoginInfo(0L, 3L);
        expect(staticDependencies.authenticate(fred,"badpassword")).andReturn(false);
        expect(loginStore.recordLoginAttempt(fred, false)).andStubReturn(makeLoginInfo());

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        final LoginResult loginResult = loginManager.authenticate(fred, "badpassword");

        assertNotNull(loginResult);
        assertFalse(loginResult.isOK());
        assertEquals(LoginReason.AUTHENTICATED_FAILED, loginResult.getReason());
    }

    @Test
    public void test_authenticate_And_OK()
    {
        expect_getLoginInfo(0L, 3L);
        expect(staticDependencies.authenticate(fred,"password")).andReturn(true);
        expect(loginStore.recordLoginAttempt(fred, true)).andStubReturn(makeLoginInfo());
        expect(jiraAuthenticationContext.getLoggedInUser()).andReturn(fred);
        eventPublisher.publish(anyObject());
        expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        final LoginResult loginResult = loginManager.authenticate(fred, "password");

        assertNotNull(loginResult);
        assertTrue(loginResult.isOK());
        assertEquals(LoginReason.OK, loginResult.getReason());
    }

    @Test
    public void test_OnLoginAttempt_UnknownUser()
    {
        expect(crowdService.getUser("unknown")).andReturn(null);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertNull(loginManager.onLoginAttempt(httpServletRequest,"unknown", false));
    }

    @Test
    public void test_OnLoginAttempt_Failure()
    {

        expect_getLoginInfo(2L,5L);

        expect(loginStore.recordLoginAttempt(fred, false)).andStubReturn(makeLoginInfo());

        expect(httpServletRequest.getAttribute(ELEVATED_SECURITY_FAILURE)).andReturn(null);
        expect(httpServletRequest.getAttribute(AUTHORISED_FAILURE)).andReturn(null);
        expect(httpServletRequest.getAttribute(ELEVATED_SECURITY_FAILURE)).andReturn(null);
        Capture<LoginResult> loginResultCapture = new Capture<LoginResult>();
        httpServletRequest.setAttribute(eq(LoginService.LOGIN_RESULT), capture(loginResultCapture)); expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        final LoginInfo loginInfo = loginManager.onLoginAttempt(httpServletRequest, fred.getName(), false);
        assertNotNull(loginInfo);
        assertEquals(fred.getName(), loginResultCapture.getValue().getUserName());
        assertEquals(LoginReason.AUTHENTICATED_FAILED, loginResultCapture.getValue().getReason());
    }

    @Test
    public void test_OnLoginAttempt_Failure_butHadPreviouslyFailedElevatedCheck()
    {

        expect_getLoginInfo(2L,5L);

        expect(loginStore.recordLoginAttempt(fred, false)).andStubReturn(makeLoginInfo());

        expect(httpServletRequest.getAttribute(ELEVATED_SECURITY_FAILURE)).andReturn(true);
        expect(httpServletRequest.getAttribute(ELEVATED_SECURITY_FAILURE)).andReturn(true);
        Capture<LoginResult> loginResultCapture = new Capture<LoginResult>();
        httpServletRequest.setAttribute(eq(LoginService.LOGIN_RESULT), capture(loginResultCapture)); expectLastCall();

        expect(velocityRequestContextFactory.getJiraVelocityRequestContext()).andReturn(velocityRequestContext);
        expect(velocityRequestContext.getCanonicalBaseUrl()).andReturn("http://localhost");

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        final LoginInfo loginInfo = loginManager.onLoginAttempt(httpServletRequest, fred.getName(), false);
        assertNotNull(loginInfo);
        assertEquals(fred.getName(), loginResultCapture.getValue().getUserName());
        assertEquals(LoginReason.AUTHENTICATION_DENIED, loginResultCapture.getValue().getReason());
    }

    @Test
    public void test_OnLoginAttempt_Failure_butHadPreviouslyFailedAuthorisationCheck()
    {

        expect_getLoginInfo(2L,5L);

        expect(loginStore.recordLoginAttempt(fred, false)).andStubReturn(makeLoginInfo());

        expect(httpServletRequest.getAttribute(ELEVATED_SECURITY_FAILURE)).andReturn(null);
        expect(httpServletRequest.getAttribute(AUTHORISED_FAILURE)).andReturn(true);
        expect(httpServletRequest.getAttribute(ELEVATED_SECURITY_FAILURE)).andReturn(null);
        Capture<LoginResult> loginResultCapture = new Capture<LoginResult>();
        httpServletRequest.setAttribute(eq(LoginService.LOGIN_RESULT), capture(loginResultCapture)); expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        final LoginInfo loginInfo = loginManager.onLoginAttempt(httpServletRequest, fred.getName(), false);
        assertNotNull(loginInfo);
        assertEquals(fred.getName(), loginResultCapture.getValue().getUserName());
        assertEquals(LoginReason.AUTHORISATION_FAILED, loginResultCapture.getValue().getReason());
    }

       @Test
       public void test_OnLoginAttempt_OK()
    {

        expect_getLoginInfo(2L,5L);

        expect(loginStore.recordLoginAttempt(fred, true)).andStubReturn(makeLoginInfo());

        Capture<LoginResult> loginResultCapture = new Capture<LoginResult>();
        expect(httpServletRequest.getAttribute(ELEVATED_SECURITY_FAILURE)).andReturn(null).times(0, 1);
        httpServletRequest.setAttribute(eq(LoginService.LOGIN_RESULT), capture(loginResultCapture)); expectLastCall();
        expect(jiraAuthenticationContext.getLoggedInUser()).andReturn(fred);
        eventPublisher.publish(anyObject());
        expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        final LoginInfo loginInfo = loginManager.onLoginAttempt(httpServletRequest, fred.getName(), true);
        assertNotNull(loginInfo);
        assertEquals(fred.getName(), loginResultCapture.getValue().getUserName());
        assertEquals(LoginReason.OK, loginResultCapture.getValue().getReason());
    }

    @Test
    public void testLogout() throws AuthenticatorException
    {
        final MockHttpSession httpSession = new MockHttpSession();
        httpSession.setAttribute("somekey", "withsomevalue");
        assertNotNull(httpSession.getAttribute("somekey"));

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(httpSession);

        expect(staticDependencies.getAuthenticator()).andReturn(authenticator);
        expect(authenticator.logout(mockHttpServletRequest, httpServletResponse)).andReturn(true);

        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(fred);
        jiraAuthenticationContext.clearLoggedInUser();
        expectLastCall();
        eventPublisher.publish(anyObject());
        expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        loginManager.logout(mockHttpServletRequest, httpServletResponse);

        assertNull(httpSession.getAttribute("somekey"));
    }

    @Test
    public void testElevatedSecurityAlwaysShown_WhenZero()
    {
        expect(loginStore.getMaxAuthenticationAttemptsAllowed()).andReturn(0L);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertTrue(loginManager.isElevatedSecurityCheckAlwaysShown());
    }

    @Test
    public void testElevatedSecurityAlwaysShown_WhenGreaterZero()
    {
        expect(loginStore.getMaxAuthenticationAttemptsAllowed()).andReturn(3L);

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        assertFalse(loginManager.isElevatedSecurityCheckAlwaysShown());
    }

    @Test
    public void test_resetFailedLoginCount()
    {
        loginStore.resetFailedLoginCount(fred);
        expectLastCall();

        final LoginManagerImpl loginManager = instantiate(LoginManagerImpl.class);
        loginManager.resetFailedLoginCount(fred);
    }

    private LoginInfo makeLoginInfo()
    {
        return makeLoginInfo(131415L);
    }

    private LoginInfo makeLoginInfo(final long currentFailedLoginCount)
    {
        return new LoginInfoImpl(123L, 456L, 789L, 101112L, currentFailedLoginCount, 171819L, 202122L, false);
    }

}
