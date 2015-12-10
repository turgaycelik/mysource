package com.atlassian.jira.web.action.admin.user;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 */
public class TestUserPasswordActionHelper extends MockControllerTestCase
{
    private User expectedUser = new MockUser("userName");
    private static final String EXPECTED_NEW_PASSWORD = "expectedNewPassword";
    private UserUtil userUtil;

    private static class ErrorAction extends JiraWebActionSupport
    {
        List errorMessages = new ArrayList();

        public void addErrorMessage(String anErrorMessage)
        {
            errorMessages.add(anErrorMessage);
        }

        public String getText(String key, String value1)
        {
            return key + value1;
        }

    }

    @Before
    public void setUp() throws Exception
    {
        userUtil = getMock(UserUtil.class);
    }

    @Test
    public void testNullUserInput()
    {
        final ErrorAction action = new ErrorAction();
        replay();
        UserPasswordActionHelper actionHelper = new UserPasswordActionHelper(action, userUtil);
        try
        {
            actionHelper.setPassword(null, "newpassword");
            fail("Should have barked with a IAE");
        }
        catch (IllegalArgumentException ignored)
        {
        }

    }

    @Test
    public void testSetPasswordOK()
            throws PermissionException, OperationNotPermittedException, UserNotFoundException, InvalidCredentialException
    {
        final ErrorAction action = new ErrorAction();

        userUtil.changePassword(expectedUser, EXPECTED_NEW_PASSWORD);
        expectLastCall();

        replay();
        UserPasswordActionHelper actionHelper = new UserPasswordActionHelper(action, userUtil);

        actionHelper.setPassword(expectedUser, EXPECTED_NEW_PASSWORD);
        assertEquals(0, action.errorMessages.size());
    }

    @Test
    public void testSetPasswordNotOK()
            throws PermissionException, OperationNotPermittedException, UserNotFoundException, InvalidCredentialException
    {
        final ErrorAction action = new ErrorAction();
        final String EXCEPTION_MSG = "Shite has occurred!";

        userUtil.changePassword(expectedUser, EXPECTED_NEW_PASSWORD);
        expectLastCall().andThrow(new RuntimeException(EXCEPTION_MSG));

        replay();
        UserPasswordActionHelper actionHelper = new UserPasswordActionHelper(action, userUtil);

        actionHelper.setPassword(expectedUser, EXPECTED_NEW_PASSWORD);
        assertEquals(1, action.errorMessages.size());

        final String msg = (String) action.errorMessages.get(0);
        assertTrue(msg.indexOf(EXCEPTION_MSG) != -1);
    }


}
