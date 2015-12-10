package com.atlassian.jira.web.action.greenhopper;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@WebSudoRequired
public class SetupGreenHopper extends JiraWebActionSupport
{
    private static final String COM_PYXIS_GREENHOPPER_JIRA = "com.pyxis.greenhopper.jira";
    private static final String GREENHOPPER_LICENSE_MANAGER = "greenhopper-license-manager";
    private final ExternalLinkUtil externalLinkUtil;
    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final JiraLicenseService licenseService;
    private String license;


    public SetupGreenHopper(ExternalLinkUtil externalLinkUtil, PluginAccessor pluginAccessor, JiraLicenseService licenseService, PluginController pluginController)
    {
        this.externalLinkUtil = externalLinkUtil;
        this.pluginAccessor = pluginAccessor;
        this.licenseService = licenseService;
        this.pluginController = pluginController;
    }

    public String doFetchLicense() throws Exception
    {
        return INPUT;
    }

    public String doReturnFromMAC() throws Exception
    {
        // Just repopulate and reshow all the fields
        return INPUT;
    }

    public String doDefault() throws Exception
    {
        doValidation();
        return super.doDefault();
    }

    protected void doValidation()
    {
        super.doValidation();
        boolean isGreenHopperInstalled = pluginAccessor.getPlugin("com.pyxis.greenhopper.jira") != null;
        if (!isGreenHopperInstalled)
        {
            addErrorMessage(getText("setup.greenhopper.no.plugin"));
        }
    }


    // Do not make this Constructor Injected! That is WRONG. Because we reset the PICO world in the middle of the import
    // we don't want to reference things from the old PICO. So we need to dynamically get it everytime to ensure we
    // always get it from the correct PICO.
    private JiraLicenseService getLicenseService()
    {
        return ComponentAccessor.getComponent(JiraLicenseService.class);
    }

    protected String doExecute() throws Exception
    {
        StringBuilder url = new StringBuilder();
        url.append(JiraUrl.constructBaseUrl(request));
        url.append("/plugins/servlet/upm#manage/" + COM_PYXIS_GREENHOPPER_JIRA);
        return getRedirect(url.toString());
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(final String license)
    {
        this.license = license;
    }

    public String getRequestEvaluatorLicenseURL() throws UnsupportedEncodingException
    {
        return externalLinkUtil.getProperty("external.link.greenhopper.license.evaluator", Arrays.<String>asList(COM_PYXIS_GREENHOPPER_JIRA));
    }

    public String getOrganisation()
    {
        return licenseService.getLicense().getOrganisation();
    }

    public String getCallbackUrl() throws UnsupportedEncodingException
    {
        StringBuilder url = new StringBuilder();
        url.append(JiraUrl.constructBaseUrl(request));
        url.append("/plugins/servlet/upm/license/" + COM_PYXIS_GREENHOPPER_JIRA);
        return url.toString();
    }

    public boolean isPluginInstalled()
    {
        return pluginAccessor.getPlugin("com.pyxis.greenhopper.jira") != null;
    }
}
