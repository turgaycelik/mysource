/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bean.export.IllegalXMLCharactersException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.license.JiraLicenseStore;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.startup.JiraLauncher;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;

/**
 * Tests if an upgrade is necessary, and performs it.  Note that upgrades are performed on the same thread
 */
public class UpgradeLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(UpgradeLauncher.class);
    private static final String NO_UPGRADE_MESSAGE = "No upgrade is being performed due to detected inconsistencies.";
    private static final int V1 = 1;
    private static final String LS = JiraSystemProperties.getInstance().getProperty("line.separator");

    /**
     * The upgrade runner loads the Upgrade Manager, which then performs any necessary upgrades.
     */
    public void start()
    {
        try
        {
            ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
            if ("true".equals(applicationProperties.getString(APKeys.JIRA_SETUP)))
            {
                checkIfUpgradeNeeded(ServletContextProvider.getServletContext());
            }
        }
        catch (RuntimeException rte)
        {
            log.fatal("A RuntimeException occurred during UpgradeLauncher servlet context initialisation - " + rte.getMessage() + ".", rte);
            throw rte;
        }
        catch (Error error)
        {
            log.fatal("An Error occurred during UpgradeLauncher servlet context initialisation - " + error.getMessage() + ".", error);
            throw error;
        }
    }

    public void stop()
    {
        // do nothing
    }

    /**
     * This will invoke the {@link com.atlassian.jira.upgrade.UpgradeManager} to see if an upgrade is needed.
     * <p/>
     * This is run at JIRA startup time and can be invoked later if you need to restart JIRA.
     *
     * @param servletContext this is need to put up Johnson events as the upgrade happens and also if the upgrade fails
     */
    public static void checkIfUpgradeNeeded(final ServletContext servletContext)
    {
        ensureLicenseNotCorrupted();
        if (JiraStartupChecklist.startupOK())
        {
            final JohnsonEventContainer eventContainer = JohnsonEventContainer.get(servletContext);

            /**
             * This will check that the database containers a valid 2.0 style license and also that
             * the license is valid for this build and .  A Johnson event will be raised if not all kosher.
             */
            if (checkLicenseIsValid(servletContext))
            {
                //Add a warning that an upgrade is in progress
                final Event upgradingEvent = new Event(EventType.get("upgrade"), "JIRA is currently being upgraded",
                        EventLevel.get(EventLevel.WARNING));

                eventContainer.addEvent(upgradingEvent);

                try
                {
                    final UpgradeManager manager = ComponentAccessor.getComponentOfType(UpgradeManager.class);
                    final File tempDir = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
                    try
                    {
                        final UpgradeManager.Status status = manager.doUpgradeIfNeededAndAllowed(tempDir.getAbsolutePath(), false);
                        addEventsForErrors(eventContainer, status);
                    }
                    catch (final IllegalXMLCharactersException ie)
                    {
                        log.error("Illegal XML characters detected while exporting before upgrade.");

                        final Event errorEvent = new Event(EventType.get("export-illegal-xml"),
                                "Illegal XML characters in data prevent successful export before the upgrade.", ie.getMessage(),
                                EventLevel.get(EventLevel.ERROR));
                        eventContainer.addEvent(errorEvent);
                        // do not continue with upgrade
                    }

                }
                catch (Exception upgradeException)
                {
                    log.error("An error occurred whilst trying to upgrade JIRA.", upgradeException);

                    final Event errorEvent = new Event(EventType.get("upgrade"), "An error occurred performing JIRA upgrade", upgradeException.getMessage(),
                            EventLevel.get(EventLevel.ERROR));
                    eventContainer.addEvent(errorEvent);
                }
                finally
                {
                    // upgrade completed
                    eventContainer.removeEvent(upgradingEvent);
                }
            }
            else
            {
                log.error(constructErrorMessage(eventContainer));
            }
        }
        else
        {
            log.fatal("Skipping, JIRA is locked.");
        }
    }

    /**
     * <p/>
     * A corrupted license results in the LicenseService throwing an exception. We want to pre-emptively check for that
     * case and remove the corrupted license from the store with an appropriate log message.
     *
     * <p/>
     * This will allow JIRA to start up and the instance admin to fix the problem by providing a valid license.
     *
     */
    private static void ensureLicenseNotCorrupted()
    {
        try
        {
            getJiraLicenseService().getLicense();
        }
        catch (Exception e)
        {
            log.error(String.format("The JIRA license stored in this instance's database is corrupted. "
                    + "The license entry will be removed and JIRA will act as if there was no valid license set. "
                    + "The corrupted license string: '%s'", getJiraLicenseStore().retrieve()));
            getJiraLicenseStore().remove();
        }
    }


    private static boolean checkLicenseIsValid(final ServletContext servletContext)
    {
        boolean invalid = false;
        final JiraLicenseService licenseService = getJiraLicenseService();

        final LicenseDetails licenseDetails = licenseService.getLicense();
        if (licenseDetails.isLicenseSet())
        {
            final LicenseJohnsonEventRaiser licenseJohnsonEventRaiser = getLicenseJohnsonEventRaiser();

            // check that if the current build is not newer than the license by more than 366 days
            invalid = licenseJohnsonEventRaiser.checkLicenseIsTooOldForBuild(servletContext, licenseDetails);

        }
        return !invalid;
    }

    private static void addEventsForErrors(final JohnsonEventContainer cont, final UpgradeManager.Status status)
    {
        if (cont != null && status != null && !status.succesful())
        {
            for (final String exception : status.getErrors())
            {
                final Event errorEvent = new Event(EventType.get("upgrade"), "An error occurred performing JIRA upgrade", exception,
                        EventLevel.get(EventLevel.ERROR));
                cont.addEvent(errorEvent);
            }
        }
    }

    private static String constructErrorMessage(final JohnsonEventContainer cont)
    {
        final StringBuilder errMsg = new StringBuilder(NO_UPGRADE_MESSAGE).append(" ");

        // we want a specific log message here for when they have started with a V1 license.  it cant be the same as the Johnson
        // event message because one is intended for the web and one is for the log file and its confusing if its re-used
        // See JRA-18778
        final JiraLicenseService licenseService = getJiraLicenseService();
        final JiraLicenseService.ValidationResult validationResult = licenseService.validate(getI18nHelper(), licenseService.getLicense().getLicenseString());
        if (validationResult.getLicenseVersion() == V1)
        {
            final BuildUtilsInfo buildUtilsInfo = getBuildUtilsInfo();
            ExternalLinkUtil externalLinkUtil = getExternalLinkUtil();

            final String serverId = licenseService.getServerId();
            final String upgradeLink = externalLinkUtil.getProperty("external.link.jira.upgrade.lic", Arrays.<String>asList(buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), "enterprise", serverId, String.valueOf(validationResult.getTotalUserCount()), String.valueOf(validationResult.getActiveUserCount())));
            final String evaluationLink = externalLinkUtil.getProperty("external.link.jira.license.view", Arrays.<String>asList(buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), "enterprise", serverId));

            errMsg.append(LS).append(LS)
                    .append("The current version of your license (v1) is incompatible with JIRA ").append(buildUtilsInfo.getVersion()).append(".").append(LS)
                    .append("You will need to upgrade your license or generate an evaluation license.").append(LS).append(LS)
                    .append("To upgrade your license visit : ").append(upgradeLink).append(LS)
                    .append("To generate an evaluation license visit : ").append(evaluationLink).append(LS).append(LS)
                    .append("Please follow the instructions that JIRA is presenting in your web browser.").append(LS);
        }
        else
        {
            //
            // use the Johnson event itself as the log message
            for (final Object element : cont.getEvents())
            {
                final Event errEvent = (Event) element;
                errMsg.append(errEvent.getDesc()).append(LS);
            }
        }
        return errMsg.toString();
    }


    private static JiraLicenseService getJiraLicenseService()
    {
        return ComponentAccessor.getComponent(JiraLicenseService.class);
    }

    private static JiraLicenseStore getJiraLicenseStore()
    {
        return ComponentAccessor.getComponent(JiraLicenseStore.class);
    }

    private static I18nHelper getI18nHelper()
    {
        return ComponentAccessor.getComponent(I18nHelper.BeanFactory.class).getInstance(Locale.getDefault());
    }

    private static LicenseJohnsonEventRaiser getLicenseJohnsonEventRaiser()
    {
        return ComponentAccessor.getComponent(LicenseJohnsonEventRaiser.class);
    }

    private static BuildUtilsInfo getBuildUtilsInfo()
    {
        return ComponentAccessor.getComponent(BuildUtilsInfo.class);
    }

    private static ExternalLinkUtil getExternalLinkUtil()
    {
        return ComponentAccessor.getComponent(ExternalLinkUtil.class);
    }
}
