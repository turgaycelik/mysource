package com.atlassian.jira.security.auth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.plugin.webwork.WebworkPluginSecurityServiceHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
public class AuthorisationManagerImplTest
{

    private PermissionManager permissionManager;
    private List<Authorisation> authorisationList;
    private AuthorisationManagerImpl authorisationManager;
    private WebworkPluginSecurityServiceHelper webworkPluginSecurityServiceHelper;
    private ApplicationUser userGood;
    private ApplicationUser userBad;
    private ApplicationUser userAdmin;
    private MockHttpServletRequest httpServletRequest;
    private Authorisation positiveAuth;
    private Authorisation negativeAuth;
    private Authorisation abstainAuth;
    private Authorisation exceptionAuth;

    @Before
    public void setUp() throws Exception
    {
        userGood = new MockApplicationUser("userGood");
        userBad = new MockApplicationUser("userBad");
        userAdmin = new MockApplicationUser("userAdmin");
        webworkPluginSecurityServiceHelper = mock(WebworkPluginSecurityServiceHelper.class);

        httpServletRequest = new MockHttpServletRequest();

        permissionManager = mock(PermissionManager.class);
        authorisationList = new ArrayList<Authorisation>();
        authorisationManager = new AuthorisationManagerImpl(permissionManager, null, webworkPluginSecurityServiceHelper, null)
        {
            @Override
            Iterable<Authorisation> buildEnabledAuthorisers()
            {
                return authorisationList;
            }
        };

        exceptionAuth = new Authorisation()
        {
            @Override
            public Decision authoriseForLogin(@Nonnull User user, HttpServletRequest httpServletRequest)
            {
                throw new RuntimeException("authoriseForLogin!");
            }

            @Override
            public Set<String> getRequiredRoles(HttpServletRequest httpServletRequest)
            {
                throw new RuntimeException("getRequiredRoles!");
            }

            @Override
            public Decision authoriseForRole(@Nullable User user, HttpServletRequest httpServletRequest, String role)
            {
                throw new RuntimeException("authoriseForRole!");
            }
        };



        when(permissionManager.hasPermission(Permissions.USE, userGood)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, userGood)).thenReturn(true);

        when(permissionManager.hasPermission(Permissions.USE, userAdmin)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, userAdmin)).thenReturn(true);

        when(permissionManager.hasPermission(Permissions.USE, userBad)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, userBad)).thenReturn(false);


        positiveAuth = new Authorisation()
        {
            @Override
            public Decision authoriseForLogin(@Nonnull User user, HttpServletRequest httpServletRequest)
            {
                return Decision.GRANTED;
            }

            @Override
            public Set<String> getRequiredRoles(HttpServletRequest httpServletRequest)
            {
                return Sets.newHashSet("positive");
            }

            @Override
            public Decision authoriseForRole(@Nullable User user, HttpServletRequest httpServletRequest, String role)
            {
                if ("positive".equals(role)) {
                    return Decision.GRANTED;
                }
                return Decision.ABSTAIN;
            }
        };
        negativeAuth = new Authorisation()
        {
            @Override
            public Decision authoriseForLogin(@Nonnull User user, HttpServletRequest httpServletRequest)
            {
                return Decision.DENIED;
            }

            @Override
            public Set<String> getRequiredRoles(HttpServletRequest httpServletRequest)
            {
                return Sets.newHashSet();
            }

            @Override
            public Decision authoriseForRole(@Nullable User user, HttpServletRequest httpServletRequest, String role)
            {
                return Decision.DENIED;
            }
        };
        abstainAuth = new Authorisation()
        {
            @Override
            public Decision authoriseForLogin(@Nonnull User user, HttpServletRequest httpServletRequest)
            {
                return Decision.ABSTAIN;
            }

            @Override
            public Set<String> getRequiredRoles(HttpServletRequest httpServletRequest)
            {
                return Sets.newHashSet();
            }

            @Override
            public Decision authoriseForRole(@Nullable User user, HttpServletRequest httpServletRequest, String role)
            {
                return Decision.ABSTAIN;
            }
        };
    }

    @Test
    public void testBasicJiraAuthorationNoPlugins() throws Exception
    {
        assertTrue(authorisationManager.authoriseForLogin(userGood, httpServletRequest));
        assertTrue(authorisationManager.authoriseForLogin(userAdmin, httpServletRequest));
        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));
    }

    @Test
    public void testPluginProvidedAbstainAuthorisation() throws Exception
    {
        authorisationList.add(abstainAuth);
        authorisationList.add(abstainAuth);
        authorisationList.add(abstainAuth);
        assertTrue(authorisationManager.authoriseForLogin(userGood, httpServletRequest));
        assertTrue(authorisationManager.authoriseForLogin(userAdmin, httpServletRequest));
        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));
    }

    @Test
    public void testPluginProvidedNegativeAuthorisation() throws Exception
    {
        // abstain does nothing
        authorisationList.add(abstainAuth);
        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));

        // a neg auth is in play
        authorisationList.add(negativeAuth);
        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));

        // abstain does nothing
        authorisationList.add(abstainAuth);
        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));

        // a positive auth here is too late
        authorisationList.add(positiveAuth);
        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));

    }


    @Test
    public void testPluginProvidedPositiveAuthorisation() throws Exception
    {
        // abstain does nothing
        authorisationList.add(abstainAuth);
        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));

        // abstain does nothing
        authorisationList.add(abstainAuth);
        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));

        // a positive auth here is a yes
        authorisationList.add(positiveAuth);
        assertTrue(authorisationManager.authoriseForLogin(userBad, httpServletRequest));

        // a neg auth here doesnt matter
        authorisationList.add(negativeAuth);
        assertTrue(authorisationManager.authoriseForLogin(userBad, httpServletRequest));

    }

    @Test
    public void testPluginAbstainersHaveNoEffect() throws Exception
    {
        // abstain always is neutral
        authorisationList.add(abstainAuth);
        authorisationList.add(abstainAuth);
        authorisationList.add(abstainAuth);

        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));
        assertTrue(authorisationManager.authoriseForLogin(userAdmin, httpServletRequest));
        assertTrue(authorisationManager.authoriseForLogin(userGood, httpServletRequest));

    }


    @Test
    public void testPluginExceptionsHaveNoEffect() throws Exception
    {
        // abstain always is neutral
        authorisationList.add(exceptionAuth);
        authorisationList.add(abstainAuth);
        authorisationList.add(exceptionAuth);

        assertFalse(authorisationManager.authoriseForLogin(userBad, httpServletRequest));
        assertTrue(authorisationManager.authoriseForLogin(userAdmin, httpServletRequest));
        assertTrue(authorisationManager.authoriseForLogin(userGood, httpServletRequest));

    }

    @Test
    public void testWebworkRolesAreReturned() {

        HashSet<String> webworkRoles = Sets.newHashSet("webworksupplied", "webworkprovided");

        when(webworkPluginSecurityServiceHelper.getRequiredRoles(httpServletRequest)).thenReturn(webworkRoles);
        authorisationList.add(positiveAuth);
        authorisationList.add(negativeAuth);
        authorisationList.add(abstainAuth);

        Set<String> actual = authorisationManager.getRequiredRoles(httpServletRequest);
        assertEquals(actual.size(), 3);
        for (String webworkRole : webworkRoles)
        {
            assertTrue(actual.contains(webworkRole));
        }
        assertTrue(actual.contains("positive"));
    }

    @Test
    public void testExceptionsFromPluginsAreignored() {

        authorisationList.add(positiveAuth);
        authorisationList.add(negativeAuth);
        authorisationList.add(abstainAuth);
        authorisationList.add(exceptionAuth);

        Set<String> actual = authorisationManager.getRequiredRoles(httpServletRequest);
        assertEquals(actual.size(), 1);
        assertEquals(actual.iterator().next(), "positive");
    }



    @Test
    public void testHasJIRARole_FAIL()
    {
        when(permissionManager.hasPermission(Permissions.USE, userBad)).thenReturn(false);
        assertFalse(authorisationManager.authoriseForRole(userBad, httpServletRequest, "use"));
    }

    @Test
    public void testHasJIRARole_FAIL_RubbishRoleName()
    {
        assertFalse(authorisationManager.authoriseForRole(userBad, httpServletRequest, "rubbish"));
    }

    @Test
    public void testHasJIRARole_OK()
    {
        when(permissionManager.hasPermission(Permissions.USE, userGood)).thenReturn(true);
        assertTrue(authorisationManager.authoriseForRole(userGood, httpServletRequest, "use"));
    }

    @Test
    public void testPluginProvidedRoleChecks() {
        authorisationList.add(positiveAuth);

        assertTrue(authorisationManager.authoriseForRole(userGood, httpServletRequest, "positive"));
    }

    @Test
    public void testPluginProvidedRoleChecksBeatJIRA() {
        authorisationList.add(positiveAuth);

        assertTrue(authorisationManager.authoriseForRole(userBad, httpServletRequest, "positive"));
    }

    @Test
    public void testPluginProvidedRoleCheckExceptionsHaveNoEffect() {
        authorisationList.add(exceptionAuth);
        authorisationList.add(positiveAuth);

        assertTrue(authorisationManager.authoriseForRole(userGood, httpServletRequest, "positive"));
    }

}
