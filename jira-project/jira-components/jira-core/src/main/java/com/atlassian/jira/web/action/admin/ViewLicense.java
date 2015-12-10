/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.Iterator;

import javax.annotation.Nullable;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.johnson.config.JohnsonConfig;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import webwork.action.Action;
import webwork.action.ActionContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class ViewLicense extends JiraWebActionSupport
{
    private final UserUtil userUtil;
    private final LicenseJohnsonEventRaiser licenseJohnsonEventRaiser;
    private final JiraLicenseUpdaterService jiraLicenseService;
    private final FeatureManager featureManager;

    private String licenseString = "";
    private LicenseDetails licenseDetails;
    private JiraLicenseService.ValidationResult validationResult;

    public ViewLicense(UserUtil userUtil, JiraLicenseUpdaterService jiraLicenseService,
            LicenseJohnsonEventRaiser licenseJohnsonEventRaiser, FeatureManager featureManager)
    {
        this.featureManager = featureManager;
        this.licenseJohnsonEventRaiser = notNull("licenseJohnsonEventRaiser", licenseJohnsonEventRaiser);
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.userUtil = notNull("userUtil", userUtil);
    }

    //webwork wants an iterator for some reason - iterable doesn't cut it.
    public Iterator<LicenseDetails> getAllLicenseDetails()
    {
        return allLicenseDetails().iterator();
    }

    private Iterable<LicenseDetails> allLicenseDetails()
    {
        if (!featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED))
            return ImmutableList.of(getLicenseDetails());
        return jiraLicenseService.getLicenses();
    }

    private LicenseDetails getLicenseDetails()
    {
        if (licenseDetails == null)
        {
            licenseDetails = jiraLicenseService.getLicense();
        }
        return licenseDetails;
    }

    public boolean isLicenseSet()
    {
        Iterable<LicenseDetails> allLicenseDetails = allLicenseDetails();

        return Iterables.any(allLicenseDetails, new Predicate<LicenseDetails>()
        {
            public boolean apply(@Nullable final LicenseDetails licenseDetails)
            {
                return licenseDetails != null && licenseDetails.isLicenseSet();
            }
        });
    }

    public String doRefreshActiveUserCount()
    {
        //re-calculate the user limit.
        userUtil.clearActiveUserCount();
        userUtil.getActiveUserCount();

        return SUCCESS;
    }

    @Override
    protected void doValidation()
    {
        validationResult = jiraLicenseService.validate(this, licenseString);
        if (validationResult.getErrorCollection().hasAnyErrors())
        {
            addErrorCollection(validationResult.getErrorCollection());
        }
    }

    public String doDefault()
    {
        return INPUT;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        licenseDetails = jiraLicenseService.setLicense(validationResult);
        licenseString = "";

        //
        // this sucks as a behaviour but we are continuing to do this to keep backwards compatible with
        // its old behaviour.  Basically it takes you to a Johnson event that takes you to a page that allowws
        // the license to be set or a confirmation to take place. We should improve this....at some point...ha bloody ha ha!
        //
        if (licenseJohnsonEventRaiser.checkLicenseIsTooOldForBuild(ActionContext.getServletContext(), licenseDetails))
        {
            return getRedirect(JohnsonConfig.getInstance().getErrorPath());
        }

        return Action.SUCCESS;
    }

    public void setLicense(String license)
    {
        this.licenseString = license;
    }

    public String getLicense()
    {
        return licenseString;
    }

    public boolean isPersonalLicense()
    {
        return getLicenseDetails().isPersonalLicense();
    }

    public boolean isLicenseRequiresUserLimit(LicenseDetails licenseDetails)
    {
        return !licenseDetails.isUnlimitedNumberOfUsers();
    }

    public int getActiveUserCount()
    {
        return userUtil.getActiveUserCount();
    }

    public boolean hasExceededUserLimit()
    {
        return userUtil.hasExceededUserLimit();
    }

    public String getLicenseStatusMessage(LicenseDetails licenseDetails)
    {
        return licenseDetails.getLicenseStatusMessage(getLoggedInUser(), "<br/><br/>");
    }

    public String getLicenseExpiryStatusMessage(LicenseDetails licenseDetails)
    {
        return licenseDetails.getLicenseExpiryStatusMessage(getLoggedInUser());
    }

    public String getPurchaseDate(LicenseDetails licenseDetails)
    {
        return licenseDetails.getPurchaseDate(getOutlookDate());
    }

}
