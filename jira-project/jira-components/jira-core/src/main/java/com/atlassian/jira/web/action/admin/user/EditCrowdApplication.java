/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.jira.crowd.embedded.JaacsService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ValidationFailureException;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;

@WebSudoRequired
public class EditCrowdApplication extends JiraWebActionSupport
{
    /**
     * The name of this action.
     */
    static final String ACTION_NAME = "EditCrowdApplication";

    /**
     * The name of the view used for this action.
     */
    private static final String EDITSCREEN = "editscreen";

    /**
     * Link for configure crowd server.
     */
    private static final String CFG_CROWD_SERVER = ConfigureCrowdServer.ACTION + ".jspa";

    /**
     * Separator character used for the remote address text area.
     */
    public static final String REMOTE_ADDR_SEP = "\r\n";
    private final JaacsService jaacsService;
    private final BeanFactory i18nFactory;

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private Long id;
    private String name;
    private String credential;
    private String remoteAddresses = "127.0.0.1\n::1";

    public EditCrowdApplication(JaacsService jaacsService, BeanFactory i18nFactory, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jaacsService = jaacsService;
        this.i18nFactory = i18nFactory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    protected String doExecute() throws Exception
    {
        if (id != null)
        {
            Application application = jaacsService.findById(getLoggedInUser(), id);
            if (application == null)
            {
                return redirect(CFG_CROWD_SERVER);
            }

            name = application.getName();
            credential = null;
            remoteAddresses = stringify(application.getRemoteAddresses());
        }

        return EDITSCREEN;
    }

    /**
     * Creates a new Crowd application.
     */
    @RequiresXsrfCheck
    public String doCreate()
    {
        ApplicationImpl application = ApplicationImpl.newInstanceWithPassword(name, ApplicationType.GENERIC_APPLICATION, credential);
        application.setRemoteAddresses(remoteAddressify(remoteAddresses));
        application.setType(ApplicationType.GENERIC_APPLICATION);

        try
        {
            jaacsService.create(getLoggedInUser(), application);
            return redirect(CFG_CROWD_SERVER, "success", i18n().getText("admin.jaacs.application.application.created", name));
        }
        catch (ValidationFailureException e)
        {
            addErrorCollection(e.errors());
            return EDITSCREEN;
        }
    }

    /**
     * Updates an existing Crowd application.
     */
    @RequiresXsrfCheck
    public String doUpdate()
    {
        if (id != null)
        {
            ApplicationImpl application = jaacsService.findById(getLoggedInUser(), id);
            if (application == null)
            {
                return redirect(CFG_CROWD_SERVER);
            }

            application.setName(name);
            application.setRemoteAddresses(remoteAddressify(remoteAddresses));

            try
            {
                jaacsService.update(getLoggedInUser(), application);
                if (!isBlank(credential))
                {
                    if (!jaacsService.resetPassword(getJiraServiceContext(), credential, id))
                    {
                        return EDITSCREEN;
                    }
                }
                return redirect(CFG_CROWD_SERVER, "success", i18n().getText("admin.jaacs.application.application.updated", name));
            }
            catch (ValidationFailureException e)
            {
                addErrorCollection(e.errors());
            }
        }

        return EDITSCREEN;
    }

    @RequiresXsrfCheck
    public String doDelete() throws Exception
    {
        if (jaacsService.validateDeleteApplication(getJiraServiceContext(), id))
        {
            ApplicationImpl application = jaacsService.findById(getLoggedInUser(), id);
            if (jaacsService.deleteApplication(getJiraServiceContext(), id))
            {
                return redirect(CFG_CROWD_SERVER, "success", i18n().getText("admin.jaacs.application.application.deleted", application.getName()));
            }
        }

        return redirect(CFG_CROWD_SERVER);
    }

    /**
     * @return the i18n key for the page's title.
     */
    public String getTitleTextKey()
    {
        return isCreate() ? "admin.jaacs.application.create.title" : "admin.jaacs.application.edit.title";
    }

    /**
     * @return the i18n key for the password Description.
     */
    public String getPasswordDescriptionKey()
    {
        return isCreate() ? "admin.jaacs.application.password.description.add" : "admin.jaacs.application.password.description.edit";
    }

    /**
     * @return the i18n key for the page's help text
     */
    public String getHelpTextKey()
    {
        return isCreate() ? "admin.jaacs.application.create.description" : "admin.jaacs.application.edit.description";
    }

    /**
     * @return the name of the WebWork action to execute on submit
     */
    public String getSubmitAction()
    {
        return String.format("%s!%s.jspa", ACTION_NAME, isCreate() ? "create" : "update");
    }

    public String getCancelAction()
    {
        return CFG_CROWD_SERVER;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setCredential(String credential)
    {
        this.credential = credential;
    }

    public String getRemoteAddresses()
    {
        return remoteAddresses;
    }

    public void setRemoteAddresses(String remoteAddresses)
    {
        this.remoteAddresses = remoteAddresses;
    }

    /**
     * Returns true if this is the create action.
     *
     * @return true if this is the create action
     */
    protected boolean isCreate()
    {
        return id == null;
    }

    /**
     * Redirects to the Crowd application list page.
     *
     * @param params the URL parameters to append to the URL
     * @return null
     */
    protected String redirect(String defaultUrl, String... params)
    {
        String paramString = "";
        if (params.length > 0)
        {
            StringBuilder builder = new StringBuilder("?");
            try
            {
                for (int i = 0; i < params.length; i++)
                {
                    if (i % 2 != 0) { builder.append("="); }
                    builder.append(URLEncoder.encode(params[i], "UTF-8"));

                    paramString = builder.toString();
                }
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }

        getRedirect(String.format("%s%s", defaultUrl, paramString));
        return null;
    }

    /**
     * @return an I18nBean for the logged in user
     */
    protected I18nHelper i18n()
    {
        return i18nFactory.getInstance(jiraAuthenticationContext.getLoggedInUser());
    }

    /**
     * Returns a string representation for the given set of remote addresses. The remote addresses are then concatenated
     * into a String using {@link #REMOTE_ADDR_SEP} as the separator character.
     *
     * @param remoteAddresses a set of RemoteAddress
     * @return a String
     * @see #remoteAddressify(String)
     */
    protected String stringify(Set<RemoteAddress> remoteAddresses)
    {
        return join(Collections2.transform(remoteAddresses, new StringifyFn()), REMOTE_ADDR_SEP);
    }

    /**
     * Builds a Set of RemoteAddress objects from the stringified version. The remote addresses are extracted from the
     * passed-in String using {@link #REMOTE_ADDR_SEP} as the separator character.
     *
     * @param stringifiedRemoteAddresses a String representing a set of RemoteAddress
     * @return a Set of RemoteAddress
     * @see #stringify(java.util.Set)
     */
    protected Set<RemoteAddress> remoteAddressify(String stringifiedRemoteAddresses)
    {
        List<String> remoteAddresses = asList(split(stringifiedRemoteAddresses, REMOTE_ADDR_SEP));
        return Sets.newHashSet(Collections2.transform(remoteAddresses, new RemoteAddressifyFn()));
    }

    /**
     * Creates a RemoteAddress from a String.
     */
    protected static class RemoteAddressifyFn implements Function<String, RemoteAddress>
    {
        @Override
        public RemoteAddress apply(@Nullable String address)
        {
            return new RemoteAddress(address);
        }
    }

    /**
     * Creates a String from a RemoteAddress.
     */
    protected static class StringifyFn implements Function<RemoteAddress, String>
    {
        @Override
        public String apply(@Nullable RemoteAddress remoteAddress)
        {
            return remoteAddress.getAddress();
        }
    }
}
