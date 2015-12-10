package com.atlassian.jira.security.auth;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.webwork.WebworkPluginSecurityServiceHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.atlassian.jira.security.auth.Authorisation.Decision;
import static com.atlassian.jira.security.auth.Authorisation.Decision.ABSTAIN;
import static com.atlassian.jira.security.auth.Authorisation.Decision.DENIED;
import static com.atlassian.jira.security.auth.Authorisation.Decision.toDecision;
import static com.google.common.collect.Lists.transform;
import static java.lang.String.format;

/**
 */
public class AuthorisationManagerImpl implements AuthorisationManager, Startable
{
    private static final Logger loggerSecurityEvents = LoginLoggers.LOGIN_SECURITY_EVENTS;

    private final PermissionManager permissionManager;

    private final PluginAccessor pluginAccessor;

    private final WebworkPluginSecurityServiceHelper webworkPluginSecurityServiceHelper;

    private final EventPublisher eventPublisher;

    /**
     * We use a lazy reference here to make the pluginAccessor.getEnabledModuleDescriptorsByClass()
     * call as cheap as possible. This will be called for every request, so iterating the enabled
     * modules every time is a price we don't want to pay.
     */
    @ClusterSafe
    private final ResettableLazyReference<Iterable<Authorisation>> authorisationsRef = new ResettableLazyReference<Iterable<Authorisation>>()
    {
        @Override
        protected Iterable<Authorisation> create() throws Exception
        {
            return buildEnabledAuthorisers();
        }
    };

    // package level for testing
    Iterable<Authorisation> buildEnabledAuthorisers()
    {
        List<Authorisation> modules = transform(pluginAccessor.getEnabledModuleDescriptorsByClass(AuthorisationModuleDescriptor.class), new Function<AuthorisationModuleDescriptor, Authorisation>()
        {
            @Override
            public Authorisation apply(AuthorisationModuleDescriptor input)
            {
                return input.getModule();
            }
        });
        //
        // its possible that getModule returns a null, which can happen when you are mid plugin unload
        // so we filter them out.  Sure the getEnabledModuleDescriptorsByClass() call has failed BUT concurrency is
        // hard so lets go shopping.
        //
        return Iterables.filter(modules, Predicates.notNull());
    }

    public AuthorisationManagerImpl(PermissionManager permissionManager, PluginAccessor pluginAccessor, WebworkPluginSecurityServiceHelper webworkPluginSecurityServiceHelper, EventPublisher eventPublisher)
    {
        this.permissionManager = permissionManager;
        this.pluginAccessor = pluginAccessor;
        this.webworkPluginSecurityServiceHelper = webworkPluginSecurityServiceHelper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @PluginEventListener
    public void onPluginModuleEnabled(final PluginModuleEnabledEvent event)
    {
        if (event.getModule() instanceof AuthorisationModuleDescriptor)
        {
            authorisationsRef.reset();
        }
    }


    @PluginEventListener
    public void onPluginModuleDisabled(final PluginModuleDisabledEvent event)
    {
        if (event.getModule() instanceof AuthorisationModuleDescriptor)
        {
            authorisationsRef.reset();
        }
    }

    @Override
    public boolean authoriseForLogin(@Nonnull ApplicationUser user, HttpServletRequest httpServletRequest)
    {
        Decision decision = authoriseForLoginViaPlugins(user, httpServletRequest);
        if (decision == ABSTAIN)
        {
            // we do JIRA second so that plugins can get in first.
            decision = authoriseForLoginViaJIRA(user);
        }
        boolean authorised = decision.toBoolean();
        if (!authorised)
        {
            loggerSecurityEvents.warn("The user '" + safeUserName(user) + "' is NOT AUTHORIZED to perform to login for this request");
        }
        return authorised;

    }

    private Decision authoriseForLoginViaPlugins(final ApplicationUser user, final HttpServletRequest httpServletRequest)
    {
        final User directoryUser = ApplicationUsers.toDirectoryUser(user);
        if (directoryUser != null) {
            for (final Authorisation authorisation : authorisationsRef.get())
            {
                Decision decision = safeRun(authorisation, user, new Callable<Authorisation.Decision>()
                {
                    @Override
                    public Decision call() throws Exception
                    {
                        return authorisation.authoriseForLogin(directoryUser, httpServletRequest);
                    }
                });
                if (decision == ABSTAIN)
                {
                    continue;
                }
                return decision;
            }
        }
        return ABSTAIN;
    }

    private Decision authoriseForLoginViaJIRA(ApplicationUser user)
    {
        return toDecision(permissionManager.hasPermission(Permissions.ADMINISTER, user) || permissionManager.hasPermission(Permissions.USE, user));
    }

    @Override
    public Set<String> getRequiredRoles(HttpServletRequest httpServletRequest)
    {
        Set<String> requiredRoles = Sets.newHashSet(webworkPluginSecurityServiceHelper.getRequiredRoles(httpServletRequest));
        for (Authorisation authorisation : authorisationsRef.get())
        {
            try
            {
                Set<String> set = authorisation.getRequiredRoles(httpServletRequest);
                requiredRoles.addAll(safeSet(set));
            }
            catch (RuntimeException e)
            {
                loggerSecurityEvents.error(format("Exception thrown by '%s'. The roles will be ignored : %s", authorisation.getClass().getName(), e.getMessage()));
            }
        }
        return requiredRoles;
    }

    @Override
    public boolean authoriseForRole(@Nullable ApplicationUser user, HttpServletRequest httpServletRequest, String role)
    {
        Decision decision = authoriseForRoleViaPlugins(user, httpServletRequest, role);
        if (decision == ABSTAIN)
        {
            // we do JIRA second so that plugins can get in first.
            decision = authoriseForRoleViaJIRA(user, role);
        }
        boolean authorised = decision.toBoolean();
        if (! authorised)
        {
            loggerSecurityEvents.warn("The user '" + safeUserName(user) + "' is NOT AUTHORIZED to perform this request");
        }
        return authorised;
    }

    private Decision authoriseForRoleViaPlugins(final ApplicationUser user, final HttpServletRequest httpServletRequest, final String role)
    {
        final User directoryUser = ApplicationUsers.toDirectoryUser(user);
        for (final Authorisation authorisation : authorisationsRef.get())
        {
            Decision decision = safeRun(authorisation, user, new Callable<Authorisation.Decision>()
            {
                @Override
                public Decision call() throws Exception
                {
                    return authorisation.authoriseForRole(directoryUser, httpServletRequest, role);
                }
            });
            if (decision == ABSTAIN)
            {
                continue;
            }
            return decision;
        }
        return ABSTAIN;
    }

    private Decision authoriseForRoleViaJIRA(ApplicationUser user, String role)
    {
        // taken from the old JiraRoleMapper code and moved into here for consistency
        final int permissionType = Permissions.getType(role);
        if (permissionType == -1)
        {
            // change of behaviour here.  In the old day before pluggable roles, then set of roles
            // was a known set and hence this post condition was a valid thing to do.
            //
            // but now with pluggable roles, this is no longer the case
            //
            //throw new IllegalArgumentException("Unknown role '" + role + "'");
            //
            return DENIED;
        }
        return toDecision(permissionManager.hasPermission(permissionType, user));
    }


    private Decision safeRun(Authorisation authorisation, ApplicationUser user, Callable<Authorisation.Decision> callable)
    {
        try
        {
            Decision decision = callable.call();
            if (loggerSecurityEvents.isDebugEnabled())
            {
                loggerSecurityEvents.debug(format("%s has authorised '%s' as %s", authorisation.getClass().getName(), safeUserName(user), decision));
            }
            return decision;
        }
        catch (Exception e)
        {
            //
            // if any plugin throws an exception then we define to mean ABSTAIN.  We dont want plugins bring down the request
            //
            loggerSecurityEvents.error(format("Exception thrown by '%s'. The decision will be treated as ABSTAIN : %s", authorisation.getClass().getName(), e.getMessage()));
            return ABSTAIN;
        }
    }

    private String safeUserName(ApplicationUser user)
    {
        return user == null ? "anonymous" : user.getUsername();
    }

    private Set<String> safeSet(Set<String> set)
    {
        return set == null ? Collections.<String>emptySet() : set;
    }
}
