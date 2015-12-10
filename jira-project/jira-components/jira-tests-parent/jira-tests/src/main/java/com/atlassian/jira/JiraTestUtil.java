package com.atlassian.jira;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockSequenceUtil;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.io.IOException;
import java.util.Map;

public class JiraTestUtil
{

    public static final String TESTS_BASE = "com/atlassian/jira";

    /**
     * This method will login a user to the current webwork action context.
     *
     * @param user The user to login, or null if you want to behave anonymously.
     */
    public static void loginUser(User user)
    {
        loginUserImpl(user);
    }

    private static void loginUserImpl(com.atlassian.crowd.embedded.api.User user)
    {
        final Map session = ActionContext.getSession();
        session.put(DefaultAuthenticator.LOGGED_IN_KEY, user);
        session.put(DefaultAuthenticator.LOGGED_OUT_KEY, null);
        ComponentAccessor.getComponent(JiraAuthenticationContext.class).setLoggedInUser(user);
    }

    /**
     * This method will logout a user to the current webwork action context.
     *
     * @param user The user to logout
     */
    public static void logoutUser(User user)
    {
        logoutUserImpl(user);
    }


    private static void logoutUserImpl( com.atlassian.crowd.embedded.api.User user)
    {
        ActionContext.getSession().put(DefaultAuthenticator.LOGGED_OUT_KEY, user);
        ComponentAccessor.getComponent(JiraAuthenticationContext.class).clearLoggedInUser();
    }

    public static void resetRequestAndResponse()
    {
        ServletActionContext.setRequest(null);
        ServletActionContext.setResponse(null);
    }

    public static MockHttpServletResponse setupExpectedRedirect(final String url) throws IOException
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setExpectedRedirect(url);
        ServletActionContext.setResponse(response);
        return response;
    }

//    protected void sendInternalRedirect(String redirectLocation) throws ServletException, IOException {
//        final HttpServletRequest request = ServletActionContext.getRequest();
//        request.getRequestDispatcher(redirectLocation).forward(request, ServletActionContext.getResponse());
//    }

    public static MockHttpServletResponse setupExpectedInternalRedirect(final String url) throws IOException
    {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setExpectedRedirect(url);
        ServletActionContext.setResponse(response);

        return response;
    }

    public static void setupMockSequenceUtil()
    {
        String helperName = CoreFactory.getGenericDelegator().getEntityHelperName("SequenceValueItem");
        ModelEntity seqEntity = CoreFactory.getGenericDelegator().getModelEntity("SequenceValueItem");
        CoreFactory.getGenericDelegator().setSequencer(new MockSequenceUtil(helperName, seqEntity, "seqName", "seqId"));
    }

    public static GenericValue setupAndAssociateDefaultPermissionSchemeWithPermission(GenericValue project, int permType)
            throws GenericEntityException, CreateException
    {
        GenericValue scheme = setupAndAssociateDefaultPermissionScheme(project);
        ComponentAccessor.getPermissionManager().addPermission(permType, scheme, null, GroupDropdown.DESC);
        return scheme;
    }

    public static GenericValue setupAndAssociateDefaultPermissionScheme(GenericValue project)
            throws GenericEntityException
    {
        SchemeManager permManager = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = permManager.createDefaultScheme();
        permManager.addSchemeToProject(project, scheme);
        return scheme;
    }

    public static MockHttpServletRequest setupGetContextPath(String contextPath)
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupGetContextPath(contextPath);
        ServletActionContext.setRequest(request);
        return request;
    }

}
