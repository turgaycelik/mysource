package com.atlassian.jira.license;

import java.util.Locale;

import javax.servlet.ServletContext;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;

import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of {@link LicenseJohnsonEventRaiser}
 *
 * @since v4.0
 */
public class LicenseJohnsonEventRaiserImpl implements LicenseJohnsonEventRaiser
{
    private static final Logger log = Logger.getLogger(LicenseJohnsonEventRaiserImpl.class);
    private final BuildUtilsInfo buildUtilsInfo;
    private static final int V1 = 1;

    public LicenseJohnsonEventRaiserImpl(BuildUtilsInfo buildUtilsInfo)
    {
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public boolean checkLicenseIsTooOldForBuild(final ServletContext servletContext, final LicenseDetails licenseDetails)
    {
        final boolean shouldRaiseEvent = licenseDetails.isLicenseSet() &&
                licenseDetails.getLicenseVersion() > V1 && // if the license is V1 let the other check catch it.
                !licenseDetails.isEnterpriseLicenseAgreement() &&
                !licenseDetails.isMaintenanceValidForBuildDate(buildUtilsInfo.getCurrentBuildDate()) &&
                !licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone();

        if (shouldRaiseEvent)
        {
            // If it hasn't we need to get the user to update the license or confirm the installation under
            // the Evaluation Terms (Note: the user can always fall back to their previous release of JIRA)
            log.error("The current license is too old (" + licenseDetails.getMaintenanceEndString(new OutlookDate(Locale.getDefault())) + ") to run this version (" + buildUtilsInfo.getVersion() + " - " + buildUtilsInfo.getCurrentBuildDate() + ") of JIRA.");

            JohnsonEventContainer cont = JohnsonEventContainer.get(servletContext);
            String eventString = licenseDetails.isEnterpriseLicenseAgreement() ? SUBSCRIPTION_EXPIRED : LICENSE_TOO_OLD;
            Event newEvent = new Event(EventType.get(eventString), "The current license is too old to install this version of JIRA (" + buildUtilsInfo.getVersion() + ")", EventLevel.get(EventLevel.ERROR));
            cont.addEvent(newEvent);
        }

        return shouldRaiseEvent;
    }

}
