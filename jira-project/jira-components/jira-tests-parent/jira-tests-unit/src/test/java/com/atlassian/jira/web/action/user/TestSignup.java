/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.UserServiceResultHelper;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.servlet.JiraCaptchaService;
import com.atlassian.jira.servlet.JiraCaptchaServiceImpl;
import com.atlassian.jira.servlet.NoOpImageCaptchaService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import webwork.action.Action;
import webwork.action.ActionContext;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith (MockitoJUnitRunner.class)
public class TestSignup
{

    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);
            
            
    JiraCaptchaService jiraCaptchaService;
    @Mock
    @AvailableInContainer
    private UserUtil mockUserUtil;
    @Mock
    @AvailableInContainer
    private UserService mockUserService;
    @Mock
    @AvailableInContainer
    private FeatureManager mockFeeatureManager;
    @Mock
    @AvailableInContainer
    private UserManager mockuserManager;
    @Mock
    @AvailableInContainer
    private ApplicationProperties mockApplicationProperties;
    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext mockJiraAuthenticationContext;
    @Mock
    private I18nHelper mockI18nHelper;
    @Mock
    @AvailableInContainer
    private FeatureManager mockFeatureManager;

    UserService.CreateUserValidationResult mockValidationResult;
                                                     

    @Before
    public void setUp() throws Exception
    {

        jiraCaptchaService = new JiraCaptchaServiceImpl();
        when(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP)).thenReturn(true);
        when(mockApplicationProperties.getString(APKeys.JIRA_MODE)).thenReturn("public");
        when(mockuserManager.hasWritableDirectory()).thenReturn(true);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        when(mockUserUtil.canActivateNumberOfUsers(anyInt())).thenReturn(true);
        when(mockI18nHelper.getText(anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return invocation.getArguments()[0];
            }
        });
        //when(mockUserService.createUserFromSignup(mockValidationResult)).thenReturn(mock(User.class));

    }

    @Test
    public void testValidationOfCaptcha() throws Exception
    {
        _testCaptchaValidation();
    }

    private void _testCaptchaValidation() throws Exception
    {
        final String sessionId = "testSessionId";
        Object sessionDelegate = new Object()
        {
            public String getId()
            {
                return sessionId;
            }
        };
        final HttpSession session = (HttpSession) DuckTypeProxy.getProxy(HttpSession.class, sessionDelegate);

        Object requestDelegate = new Object()
        {
            public Object getAttribute(String string)
            {
                return null;
            }

            public HttpSession getSession(boolean create)
            {
                return session;
            }
        };
        HttpServletRequest request = (HttpServletRequest) DuckTypeProxy.getProxy(HttpServletRequest.class, requestDelegate);

        mockValidationResult = UserServiceResultHelper.getCreateUserValidationResult();
        setupUserServiceValidation(mockValidationResult);

        Signup s = new Signup(mockApplicationProperties, mockUserService, mockUserUtil, jiraCaptchaService, null);
        s.setUsername("abc");

        jiraCaptchaService.getImageCaptchaService().getImageChallengeForID(sessionId);
        HttpServletRequest oldRequest = ActionContext.getRequest();
        try
        {
            ActionContext.setRequest(request);
            String result = s.execute();

            assertEquals(Action.INPUT, result);
            assertEquals(s.getText("signup.error.captcha.incorrect"), s.getErrors().get("captcha"));
        }
        finally
        {
            ActionContext.setRequest(oldRequest);
        }
    }

    @Test
    public void testNoValidationOfCaptchaIfOff() throws Exception
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addError("fullname", "You must specify a full name.");
        errors.addError("email", "You must specify an email address.");
        errors.addError("password", "You must specify a password and a confirmation password.");
        mockValidationResult = UserServiceResultHelper.getCreateUserValidationResult(errors);
        setupUserServiceValidation(mockValidationResult);
        MockApplicationProperties applicationProperties = new MockApplicationProperties();
        when(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP)).thenReturn(false);

        Signup s = new Signup(applicationProperties, mockUserService, mockUserUtil, jiraCaptchaService, null);
        s.setUsername("abc");

        String result = s.execute();

        assertEquals(Action.INPUT, result);
        assertNull(s.getErrors().get("captcha"));
    }

    @Test
    public void testExecute() throws Exception
    {
        mockValidationResult = UserServiceResultHelper.getCreateUserValidationResult();
        setupUserServiceValidation(mockValidationResult);
        Signup s = new Signup(new MockApplicationProperties(), mockUserService, mockUserUtil, new JiraCaptchaServiceImpl(), null);
        s.setUsername("a");
        s.setPassword("b");
        s.setConfirm("b");
        s.setFullname("c");
        s.setEmail("d@d.com");

        when(mockUserService.createUserFromSignup(mockValidationResult)).thenReturn(new MockUser("a","c","d@d.com"));
        String result = s.execute();

        assertEquals(Action.SUCCESS, result);
        verify(mockUserService).createUserFromSignup(mockValidationResult);

    }

    @Test
    public void testSignupInODMode() throws Exception
    {
       when(mockFeatureManager.isOnDemand()).thenReturn(true);
       _testCaptchaValidation();
    }



    @Test
    public void testSignupInPrivateODMode() throws Exception
    {
        when(mockFeatureManager.isOnDemand()).thenReturn(true);
        when(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP)).thenReturn(false);
        assertTrue(jiraCaptchaService.getImageCaptchaService() instanceof NoOpImageCaptchaService);
    }

    private void setupUserServiceValidation(UserService.CreateUserValidationResult result)
    {
        when(mockUserService.validateCreateUserForSignup(
                any(User.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString())).thenReturn(result);
    }
}
