package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.HttpServletVariables;

/**
 * Display report page if there was an error during attempt to save license for chosen bundle
 *
 * @since v6.3
 */
public class SetupProductBundleReport extends AbstractSetupAction
{
    private final String REPORT = "report";

    private SetupProductBundleHelper productBundleHelper;
    private SetupSharedVariables sharedVariables;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    private String src;

    public SetupProductBundleReport(final FileFactory fileFactory, final HttpServletVariables servletVariables, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        super(fileFactory);
        this.velocityRequestContextFactory = velocityRequestContextFactory;

        sharedVariables = new SetupSharedVariables(servletVariables, getApplicationProperties());
        productBundleHelper = new SetupProductBundleHelper(sharedVariables);
    }

    @Override
    public String doDefault() throws Exception
    {
        if (!productBundleHelper.hasLicenseError())
        {
            return getRedirectToDashboard();
        }

        return REPORT;
    }

    @Override
    protected String doExecute() throws Exception
    {
        productBundleHelper.cleanLicenseError();

        return getRedirectToDashboard();
    }

    public void setSrc(final String src)
    {
        this.src = src;
    }

    public String getSrc()
    {
        return src;
    }

    private String getRedirectToDashboard()
    {
        return getRedirect("Dashboard.jspa" + (src == null ? "" : "?src=" + src));
    }

    public String getUpmUrlForPlugin()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl() + "/plugins/servlet/upm/manage/user-installed#manage";
    }
}
