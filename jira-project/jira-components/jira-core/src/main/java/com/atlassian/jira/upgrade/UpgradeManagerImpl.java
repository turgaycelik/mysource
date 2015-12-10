package com.atlassian.jira.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bean.export.AutoExport;
import com.atlassian.jira.bean.export.AutoExportImpl;
import com.atlassian.jira.bean.export.IllegalXMLCharactersException;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.JiraUpgradedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.DowngradeUtilsImpl;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.web.util.OutlookDateManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class UpgradeManagerImpl implements UpgradeManager, Startable
{
    private static final Logger log = Logger.getLogger(UpgradeManagerImpl.class);
    private static final String UPGRADES_XML = "upgrades.xml";
    private static final Comparator<String> BUILD_NUMBER_COMPARATOR = new BuildNumComparator();
    private static final Pattern BUILD_NUMBER_PATTERN = Pattern.compile("\\d+$");

    private static final String ENTITY_UPGRADE_VERSION_HISTORY = "UpgradeVersionHistory";
    private static final String ENTITY_UPGRADE_HISTORY = "UpgradeHistory";
    private static final String FIELD_TARGETBUILD = "targetbuild";
    private static final String FIELD_TIMEPERFORMED = "timeperformed";
    private static final String FIELD_TARGETVERSION = "targetversion";
    private static final String FIELD_UPGRADECLASS = "upgradeclass";
    private static final String REINDEX_ALLOWED_PROPERTY="upgrade.reindex.allowed";

    private final ApplicationProperties applicationProperties;
    private final JiraLicenseService jiraLicenseService;
    private final BuildUtilsInfo buildUtilsInfo;
    private final I18nHelper.BeanFactory i18HelperFactory;
    private final BuildVersionRegistry buildVersionRegistry;
    private final EventPublisher eventPublisher;
    private final FeatureManager featureManager;
    private final IndexLifecycleManager indexManager;
    private final OfBizDelegator ofBizDelegator;
    private final OutlookDateManager outlookDateManager;
    private final DowngradeUtilsImpl downgradeUtilsInfo;
    private final ReindexMessageManager reindexMessageManager;

    private volatile SortedMap<String, UpgradeTask> allUpgrades;
    private volatile SortedMap<String, UpgradeTask> setupUpgrades;

    private final String upgradeTaskFileName;
    private volatile Map<String, GenericValue> upgradeHistoryMap;
    private volatile String exportFilePath;

    /*
     * This constructor adds all the upgrade tasks and is used by PICO
     */
    public UpgradeManagerImpl(final JiraLicenseService jiraLicenseService, final BuildUtilsInfo buildUtilsInfo,
            final I18nHelper.BeanFactory i18HelperFactory, final ApplicationProperties applicationProperties,
            final BuildVersionRegistry buildVersionRegistry, final EventPublisher eventPublisher,
            OfBizDelegator ofBizDelegator, IndexLifecycleManager indexManager, OutlookDateManager outlookDateManager,
            final FeatureManager featureManager, final DowngradeUtilsImpl downgradeUtilsInfo,
            final ReindexMessageManager reindexMessageManager)
    {
        this(jiraLicenseService, buildUtilsInfo, i18HelperFactory, applicationProperties, buildVersionRegistry, eventPublisher,
                ofBizDelegator, indexManager, outlookDateManager, UPGRADES_XML, featureManager, downgradeUtilsInfo, reindexMessageManager);
    }

    UpgradeManagerImpl(JiraLicenseService jiraLicenseService, BuildUtilsInfo buildUtilsInfo,
            I18nHelper.BeanFactory i18HelperFactory, ApplicationProperties applicationProperties,
            BuildVersionRegistry buildVersionRegistry, EventPublisher eventPublisher, OfBizDelegator ofBizDelegator,
            IndexLifecycleManager indexManager, OutlookDateManager outlookDateManager, String upgradeTaskFileName,
            FeatureManager featureManager, final DowngradeUtilsImpl downgradeUtilsInfo,
            final ReindexMessageManager reindexMessageManager)
    {
        this.eventPublisher = eventPublisher;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.i18HelperFactory = notNull("i18HelperFactory", i18HelperFactory);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.buildVersionRegistry = notNull("buildVersionRegistry", buildVersionRegistry);
        this.outlookDateManager = outlookDateManager;
        this.upgradeTaskFileName = upgradeTaskFileName;
        this.ofBizDelegator = ofBizDelegator;
        this.indexManager = indexManager;
        this.featureManager = featureManager;
        this.downgradeUtilsInfo = downgradeUtilsInfo;
        this.reindexMessageManager = reindexMessageManager;
    }

    @VisibleForTesting
    UpgradeManagerImpl(JiraLicenseService jiraLicenseService, BuildUtilsInfo buildUtilsInfo,
            I18nHelper.BeanFactory i18HelperFactory, ApplicationProperties applicationProperties,
            BuildVersionRegistry buildVersionRegistry, OfBizDelegator ofBizDelegator, EventPublisher eventPublisher,
            IndexLifecycleManager indexManager, OutlookDateManager outlookDateManager, FeatureManager featureManager,
            Iterable<? extends UpgradeTask> upgradeTasks, Iterable<? extends UpgradeTask> setupUpgradeTasks,
            final DowngradeUtilsImpl downgradeUtilsInfo, final ReindexMessageManager reindexMessageManager)
    {
        this.eventPublisher = eventPublisher;
        this.indexManager = indexManager;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.i18HelperFactory = notNull("i18HelperFactory", i18HelperFactory);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.buildVersionRegistry = notNull("buildVersionRegistry", buildVersionRegistry);
        this.ofBizDelegator = ofBizDelegator;
        this.outlookDateManager = outlookDateManager;
        this.featureManager = featureManager;
        this.reindexMessageManager = reindexMessageManager;

        allUpgrades = new TreeMap<String, UpgradeTask>(BUILD_NUMBER_COMPARATOR);
        setupUpgrades = new TreeMap<String, UpgradeTask>(BUILD_NUMBER_COMPARATOR);
        for (final UpgradeTask upgradeTask : notNull("upgradeTasks", upgradeTasks))
        {
            allUpgrades.put(upgradeTask.getBuildNumber(), upgradeTask);
        }
        for (final UpgradeTask upgradeTask : notNull("setupUpgradeTasks", setupUpgradeTasks))
        {
            setupUpgrades.put(upgradeTask.getBuildNumber(), upgradeTask);
        }
        this.upgradeTaskFileName = null;
        this.downgradeUtilsInfo = downgradeUtilsInfo;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        upgradeHistoryMap = null;
    }

    /**
     * Gets a set of all the build numbers for which upgrade tasks must be performed. It will only return numbers which
     * are greater than the current JIRA build number
     * <p/>
     * The set will be sorted by ascending build number
     */
    private SortedSet<String> getAllRelevantUpgradeBuildNumbers()
    {
        final SortedSet<String> numbers = new TreeSet<String>(BUILD_NUMBER_COMPARATOR);

        final Map<String, UpgradeTask> standardupgrades = getRelevantUpgradesFromList(allUpgrades);

        addUpgradeNumbersFromMap(numbers, standardupgrades);

        return numbers;
    }

    /**
     * Gets a set of all the build numbers for which upgrade tasks must be performed by the JIRA Setup task.
     * <p/>
     * The set will be sorted by ascending build number
     */
    private SortedSet<String> getSetupUpgradeBuildNumbers()
    {
        final SortedSet<String> numbers = new TreeSet<String>(BUILD_NUMBER_COMPARATOR);

        addUpgradeNumbersFromMap(numbers, setupUpgrades);

        return numbers;
    }

    /**
     * Gets a set of upgrade build numbers for a specified Map of upgradeTasks.
     *
     * @param numbers This set may be already populated with numbers. Any additional numbers from the Map of
     * upgradeTasks will be added to this set
     * @param upgradeMap This is the map of upgradeTasks that the build numbers will be retrieved from
     */
    private void addUpgradeNumbersFromMap(final SortedSet<String> numbers, final Map<String, UpgradeTask> upgradeMap)
    {
        for (final String buildNumber : upgradeMap.keySet())
        {
            numbers.add(buildNumber);
        }
    }

    /**
     * <p/>
     * Reads an upgrade XML file to get a list of all the upgrades specified in this file and adds these upgrades the
     * upgradeSet and setupSet accordingly.
     *
     * <p/>
     * For every <i>upgrade</i> element, load the class specified by the <i>class</i> element. Put this class into the
     * upgradeSet Map with the <i>build</i> attribute as the key.
     *
     * <p/>
     * If this element has a <i>setup</i> attribute which is set to "true" then also put this class into the setupSet
     * Map with the <i>build</i> attribute as the key as this upgrade has to be performed as part of a setup
     */
    private void addAllUpgradesInResourceFile(final Map<String, UpgradeTask> upgradeSet, final Map<String, UpgradeTask> setupSet, final String fileName)
    {
        final InputStream is = ClassLoaderUtils.getResourceAsStream(fileName, this.getClass());
        try
        {
            final Document doc = new Document(is);
            final Element root = doc.getRoot();
            final Elements actions = root.getElements("upgrade");

            while (actions.hasMoreElements())
            {
                final Element action = (Element) actions.nextElement();
                final String className = action.getElement("class").getTextString();
                try
                {
                    final UpgradeTask task = (UpgradeTask) JiraUtils.loadComponent(className, this.getClass());

                    //
                    // if its setup=only this means it only will ONLY be run on setup and not in general upgrade runs
                    // so in that case we DONT add it to the overall list of upgrade tasks, just to the setup set
                    //
                    if ("only".equals(action.getAttribute("setup")))
                    {
                        setupSet.put(action.getAttribute("build"), task);
                    }
                    else
                    {
                        upgradeSet.put(action.getAttribute("build"), task);
                        //
                        // if this task is to be done on setup as well then add it to the setup set as well
                        // as the overall list.
                        //
                        // Later in the setupOnlyUpgrades() method, we only look in this setup set
                        if ("true".equals(action.getAttribute("setup")))
                        {
                            setupSet.put(action.getAttribute("build"), task);
                        }
                    }
                }
                catch (final Exception e)
                {
                    log.error("Exception loading type: " + className, e);
                }
            }
        }
        catch (final ParseException e)
        {
            log.error("Exception: ", e);
        }

        try
        {
            is.close();
        }
        catch (final IOException e)
        {
            log.warn("Could not close " + fileName + " inputStream");
        }
    }

    /**
     * Returns true if the current build number is not equal to the build number in the database. NB - There may not be
     * any upgrades to run.  However, you will need to run doUpgrade() to increment the build number in the database.
     */
    private boolean needUpgrade()
    {
        return buildUtilsInfo.getApplicationBuildNumber() > buildUtilsInfo.getDatabaseBuildNumber();
    }

    public Status doUpgradeIfNeededAndAllowed(@Nullable final String backupPath, boolean setupMode) throws IllegalXMLCharactersException
    {
        final LicenseDetails licenseDetails = jiraLicenseService.getLicense();
        if (licenseDetails.isLicenseSet())
        {
            boolean disallowUpgrade =
                !licenseDetails.isMaintenanceValidForBuildDate(buildUtilsInfo.getCurrentBuildDate())
                && !licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone();

            if (disallowUpgrade)
            {
                // If it hasn't we need to get the user to update the license or confirm the installation under
                // the Evaluation Terms (Note: the user can always fall back to their previous release of JIRA)
                final String logMsg1 = "The current license is too old to run this version of JIRA " + buildUtilsInfo.getVersion() + " - " + buildUtilsInfo.getCurrentBuildDate();
                final String logMsg2 = "The maintenance period of your license expired on " + licenseDetails.getMaintenanceEndString(outlookDateManager.getOutlookDate(Locale.ENGLISH));
                log.info(logMsg1);
                log.info(logMsg2);
                log.info("Cannot proceed with the upgrade.");
                return new Status(logMsg1 + '\n' + logMsg2);
            }

            final JiraLicenseService.ValidationResult validationResult = jiraLicenseService.validate(i18HelperFactory.getInstance(Locale.ENGLISH), licenseDetails.getLicenseString());
            if (validationResult.getErrorCollection().hasAnyErrors())
            {
                log.info("The current license is not compatible with this installation of JIRA.");
                return createErrorMessages(validationResult);
            }
        }

        final Status status = doUpgradeIfNeeded(backupPath, setupMode);
        if (status.succesful())
        {
            eventPublisher.publish(new JiraUpgradedEvent(false));
        }
        return status;
    }

    private Status createErrorMessages(final JiraLicenseService.ValidationResult validationResult)
    {
        Collection<String> licenseErrors = new ArrayList<String>();
        final ErrorCollection errorCollection = validationResult.getErrorCollection();
        for (Object err : errorCollection.getErrorMessages())
        {
            licenseErrors.add(String.valueOf(err));
        }
        @SuppressWarnings ( { "unchecked" })
        final Map<String, String> errorMap = errorCollection.getErrors();
        for (Map.Entry<String,String> entry : errorMap.entrySet())
        {
            licenseErrors.add(entry.getKey() + " : " + entry.getValue());
        }
        return new Status(false,licenseErrors);

    }

    private Status doUpgradeIfNeeded(final String defaultBackupPath, boolean setupMode) throws IllegalXMLCharactersException
    {
        if (needUpgrade())
        {
            log.info("Detected that an upgrade is needed; existing data at build " + getJiraBuildNumber());

            if ("true".equals(applicationProperties.getString(APKeys.JIRA_SETUP)))
            {
                if (!autoExportDisabled() && (defaultBackupPath != null))
                {
                    log.info("Exporting the existing data..");

                    final AutoExport ae = getAutoExport(defaultBackupPath);
                    try
                    {
                        final String exportFilePath = ae.exportData();
                        log.info("Exported pre-upgrade data to: " + exportFilePath);
                        setExportFilePath(exportFilePath);
                    }
                    catch (final IllegalXMLCharactersException e)
                    {
                        throw e;
                    }
                    catch (final Exception e)
                    {
                        log.error(
                                "Error occurred during export before upgrade: " + e + ". If necessary, auto-export can be disabled; see " + ExternalLinkUtilImpl.getInstance().getProperty(
                                        "external.link.jira.doc.disable.autoexport"), e);
                        final Collection<String> errors = ImmutableList.of(
                                "Error occurred during export before upgrade: " + e.getMessage() + "\n If necessary, auto-export can be disabled; see " + ExternalLinkUtilImpl.getInstance().getProperty(
                                        "external.link.jira.doc.disable.autoexport") + '\n' + ExceptionUtils.getStackTrace(e));
                        return new Status(false, errors);
                    }
                }
            }
            else
            {
                log.debug("Not doing an auto-export.");
            }
            return doUpgrade(setupMode);
        }
        else
        {
            log.debug("Detected that no upgrade is neccessary");
            setVersions(buildUtilsInfo.getCurrentBuildNumber());
        }

        return new Status(false, Collections.<String>emptyList());
    }

    protected AutoExport getAutoExport(String defaultBackupPath)
    {
        return new AutoExportImpl(defaultBackupPath);
    }

    private boolean autoExportDisabled()
    {
        return featureManager.isOnDemand() || !applicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT) || JiraSystemProperties.isDevMode();
    }

    /**
     * Gets all the upgrades (standard, professional and enterprise) that need to be run from the build number stored in
     * the database to the current build number
     * <p/>
     * Get the set of upgradeNumbers which are to be performed for this upgrade.
     * <p/>
     * Get the Maps of relevant upgrades using {#getRelevantUpgradesFromList}
     * <p/>
     * Iterate over these numbers and if either of the standard, professional or enterprise maps contains an {@link
     * UpgradeTask} with this number then do the upgrade
     * <p/>
     * If errors are found, it will cancel the upgrade, and list errors to the console.
     * <p/>
     * For each upgrade that happens successfully, it will increment the build number in the database, so that if one
     * fails, you do not have to repeat all the upgrades that have already run.
     * <p/>
     * If there are no errors from the upgrade, the build number in the database is incremented to the current build
     * number.  This is because there may be no upgrades for a particular version & needUpgrade() checks build no in
     * database.
     *
     * @return a collection of error messages
     * @param setupMode {@code true} if this is the initial setup; {@code false} if it is a normal upgrade
     */
    private Status doUpgrade(boolean setupMode)
    {
        log.info("___ Performing Upgrade ____________________");

        getUpgrades();

        Status status;
        try
        {
            final Set<String> upgradeNumbers = getAllRelevantUpgradeBuildNumbers();

            //get all the relevant upgrades for both professional and enterprise
            final Map<String, UpgradeTask> standardUpgrades = getRelevantUpgradesFromList(allUpgrades);

            status = runUpgradeTasks(upgradeNumbers, standardUpgrades, setupMode);

            //if there were no errors then set the build number to the current number
            if (status.succesful())
            {
                logUpgradeSuccessfulMsg();
                // there may not be any patches for this version, so increment to latest build number.
                setVersions(buildUtilsInfo.getCurrentBuildNumber());
            }
            else
            {
                log.error("Errors occurred during upgrade:");
                printErrors(status.getErrors());
            }
        }
        catch (Exception upgradeException)
        {
            status = handleError(upgradeException);
        }
        finally
        {
            releaseUpgrades();
            refreshSequencer();
        }
        return status;
    }


    /**
     * Print errors to log4j at error level
     *
     * @param errors A collection of strings, describing all the errors that occurred.
     */
    private static void printErrors(final Collection<String> errors)
    {
        for (final String s : errors)
        {
            log.error("Upgrade Error: " + s);
        }
    }

    /**
     * Runs the given upgrade tasks for the given build numbers.
     * <p/>
     * The method iterates over the given build numbers and for each runs an upgrade tasks according to the currently
     * installed license.
     * <p/>
     * After all upgrade tasks are run for a build number JIRA's build number is set to its value.
     *
     * @param upgradeNumbers the build numbers for which upgrade tasks need to be run
     * @param upgradeTasks upgrades to run with build number as key
     * @param setupMode True if this is running in setup mode.
     * @return a collection of errors that occurred during upgrade (empty collection if no errors occurred)
     * @throws Exception if it all goes pear shaped
     */
    private Status runUpgradeTasks(final Collection<String> upgradeNumbers, final Map<String, UpgradeTask> upgradeTasks, boolean setupMode)
            throws Exception
    {
        boolean reindexRequired = false;
        boolean reindexingAllowed = Boolean.valueOf(JiraSystemProperties.getInstance().getProperty(REINDEX_ALLOWED_PROPERTY, "true"));
        final Collection<String> errors = new ArrayList<String>();

        //get a list of any previously run upgrades so that they are not run again
        final Map<String, GenericValue> upgradeHistoryMap = getPreviouslyRunUpgrades();
        boolean noErrors = true;

        for (final String number : upgradeNumbers)
        {
            //if there is a standard upgrade for this build then perform it
            final UpgradeTask standardUpgradeTask = upgradeTasks.get(number);

            if (!doUpgradeTaskSuccess(upgradeHistoryMap, standardUpgradeTask, errors, setupMode))
            {
                noErrors = false;
                break;
            }

            //if the number of the upgrade is greater than the current build number then set the build number to this number
            if (BUILD_NUMBER_COMPARATOR.compare(number, getJiraBuildNumber()) > 0)
            {
                setJiraBuildNumber(number);
            }

            // Check if a reindex is required.
            reindexRequired = reindexRequired || standardUpgradeTask.isReindexRequired();
        }

        // create a record of the version we are upgrading to
        if (reindexRequired && !reindexingAllowed)
        {
            reindexMessageManager.pushRawMessage(null, "admin.upgrade.reindex.deferred");
        }
        final boolean doReindex = reindexingAllowed && noErrors && reindexRequired;
        if (doReindex)
        {
            reindex(errors);
        }

        // create a record of the version we are upgrading to
        if (noErrors)
        {
            createUpgradeVersionHistory();
        }

        return new Status(doReindex,errors);
    }

    private void setVersions(String buildNumber)
    {
        setJiraBuildNumber(buildNumber);
        try
        {
            scrubUpgradeHistoryNewerThan(buildNumber);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException("Error while scrubbing upgrade history", e);
        }
        setJiraVersion(buildUtilsInfo.getVersion());
        setMinimumDowngradeVersion(downgradeUtilsInfo.getDowngradeAllowedVersion());
    }

    public Status doSetupUpgrade()
    {
        getUpgrades();

        Status status;

        try
        {
            final Set<String> upgradeNumbers = getSetupUpgradeBuildNumbers();
            log.info("___ Performing Setup Upgrade ____________________");

            status = runUpgradeTasks(upgradeNumbers, setupUpgrades, true);

            //if there were no errors then set the build number to the current number
            if (status.succesful())
            {
                logUpgradeSuccessfulMsg();
                setVersions(buildUtilsInfo.getCurrentBuildNumber());
            }
            else
            {
                log.error("Errors occurred during upgrade:");
                printErrors(status.getErrors());
            }
        }
        catch (final Exception setupUpgradeException)
        {
            status = handleError(setupUpgradeException);
        }
        finally
        {
            releaseUpgrades();
            refreshSequencer();
        }

        if (status.succesful())
        {
            eventPublisher.publish(new JiraUpgradedEvent(true));
        }
        return status;
    }

    private Status handleError(Throwable e)
    {
        log.error("Exception thrown during upgrade: " + e.getMessage(), e);
        return new Status("Exception thrown during upgrade: " + e.getMessage() +
                        '\n' + ExceptionUtils.getStackTrace(e));
    }

    private void refreshSequencer()
    {
        // just in case upograde tasks manipulate ID sequences
        ofBizDelegator.refreshSequencer();
    }

    private void getUpgrades()
    {
        if (setupUpgrades == null || allUpgrades == null)
        {
            //add all the upgrade tasks in here.
            allUpgrades = new TreeMap<String, UpgradeTask>(BUILD_NUMBER_COMPARATOR);
            setupUpgrades = new TreeMap<String, UpgradeTask>(BUILD_NUMBER_COMPARATOR);
            addAllUpgradesInResourceFile(allUpgrades, setupUpgrades, upgradeTaskFileName);
        }
    }

    /**
     * If we dont clear the upgrade objects out of memory, then they will live for the life of JIRA
     * <p/>
     * There is no need for this since upgrades happen only on startup or restore
     */
    private void releaseUpgrades()
    {
        setupUpgrades = null;
        allUpgrades = null;
    }

    private void logUpgradeSuccessfulMsg()
    {
        final String msg = "\n\n"
                + "***************************************************************\n"
                + "Upgrade Succeeded! JIRA has been upgraded to build number " + buildUtilsInfo.getCurrentBuildNumber() + '\n'
                + "***************************************************************\n";
        log.info(msg);
    }

    /**
     * Performs an upgrade by executing an Upgrade Task.
     *
     * @return True if the upgrade was performed without errors. False if the upgrade has errors
     */
    private boolean doUpgradeTaskSuccess(final Map<String, GenericValue> upgradeHistoryMap, final UpgradeTask upgradeTask, final Collection<String> errors, boolean setupMode)
            throws Exception
    {
        if (upgradeTask != null)
        {
            //if the upgrade has not been run then
            if (upgradeHistoryMap.get(upgradeTask.getClass().getName()) == null)
            {
                log.info("Performing Upgrade Task: " + upgradeTask.getShortDescription());
                upgradeTask.doUpgrade(setupMode);

                if (!upgradeTask.getErrors().isEmpty())
                {
                    log.error("Errors during Upgrade Task: " + upgradeTask.getShortDescription());
                    errors.addAll(upgradeTask.getErrors());
                    return false;
                }
                try
                {
                    addToUpgradeHistory(upgradeTask.getClass());
                }
                catch (final DataAccessException e)
                {
                    log.error("Problem adding upgrade task " + upgradeTask.getShortDescription() + " to the upgrade history", e);
                    errors.add("There was a problem adding Upgrade Task " + upgradeTask.getShortDescription() + " to the Upgrade History");
                }

                log.info("Upgrade Task: '" + upgradeTask.getShortDescription() + "' succeeded");
            }
            else
            {
                log.info("Not performing Upgrade Task: '" + upgradeTask.getShortDescription() + "' as it has already been run.");
            }
        }
        return true;
    }

    /**
     * Get a list of the Upgrades that have been previously run
     *
     * @return previously run upgrades
     */
    private Map<String, GenericValue> getPreviouslyRunUpgrades()
    {
        //if this list of upgrades has not been retrieved then retrieve it otherwise return it
        if (upgradeHistoryMap == null)
        {
            @SuppressWarnings ("unchecked")
            final List<GenericValue> upgradeHistoryList = ofBizDelegator.findAll(ENTITY_UPGRADE_HISTORY);

            upgradeHistoryMap = Maps.newHashMapWithExpectedSize(upgradeHistoryList.size());

            for (final Object element : upgradeHistoryList)
            {
                final GenericValue upgradeHist = (GenericValue) element;
                upgradeHistoryMap.put(upgradeHist.getString(FIELD_UPGRADECLASS), upgradeHist);
            }
        }
        return upgradeHistoryMap;
    }

    /**
     * For each upgrade in the upgradeMap, test whether it is needed (ie upgrade version is greater than the version in
     * the database), and then add to set.
     *
     * @return set of UpgradeTasks that need to be run.
     */
    SortedMap<String, UpgradeTask> getRelevantUpgradesFromList(final Map<String, UpgradeTask> upgradeMap)
    {
        try
        {
            final SortedMap<String, UpgradeTask> unAppliedUpgrades = new TreeMap<String, UpgradeTask>(BUILD_NUMBER_COMPARATOR);

            for (final Map.Entry<String, UpgradeTask> entry : upgradeMap.entrySet())
            {
                if (needUpgrade(entry.getKey()))
                {
                    final UpgradeTask upgradeTask = entry.getValue();
                    unAppliedUpgrades.put(upgradeTask.getBuildNumber(), upgradeTask);
                }
            }
            return unAppliedUpgrades;
        }
        catch (Exception upgradeListRetrievalException)
        {
            log.error("An occurred getting the upgrades that need to be run, returning an empty set.", upgradeListRetrievalException);
            return new TreeMap<String, UpgradeTask>();
        }
    }

    /**
     * Get the current build number from the database.  This represents the level that this application is patched to.
     * This may be different to the current version if there are patches waiting to be applied.
     *
     * @return The version information from the database
     */
    private String getJiraBuildNumber()
    {
        //if null in the database, we need to set to '0' so that it can be compared with
        //other versions.
        if (applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION) == null)
        {
            setJiraBuildNumber("0");
        }
        return applicationProperties.getString(APKeys.JIRA_PATCHED_VERSION);
    }

    private void setJiraBuildNumber(final String version)
    {
        log.info("Setting current build number to " + version);
        applicationProperties.setString(APKeys.JIRA_PATCHED_VERSION, version);
    }

    /**
     * We need the actual version string in the database so it is exported to XML
     * @param version  Jira version string
     */
    private void setJiraVersion(String version)
    {
        log.info("Setting current version to " + version);
        applicationProperties.setString(APKeys.JIRA_VERSION, version);
    }

    /**
     * Store the minimum downgrade version in the database so that it will be exposed in the xml backup
     * @param version Version string in form x.y.z
     */
    private void setMinimumDowngradeVersion(String version)
    {
        log.info("Setting downgrade version to " + version);
        applicationProperties.setString(APKeys.JIRA_DOWNGRADE_VERSION, version);
    }

    /**
     * Deletes all upgrade history for build numbers that are newer than {@code currentBuildNumber}. The only reason for
     * doing this is to fool JIRA into thinking that said upgrade have never run, which in turn allows the upgrade to
     * run successfully if JIRA is ever upgraded to the relevant version ever again.
     *
     * @param currentBuildNumber the build number that we are running right now
     */
    private void scrubUpgradeHistoryNewerThan(String currentBuildNumber) throws GenericEntityException
    {
        //JRADEV-18273: ignore this for build number 0
        if (!"0".equals(currentBuildNumber))
        {
            final long maxBuildNumber = Long.parseLong(currentBuildNumber);
            //JRADEV-18273 targetbuild is a string so 604 > 6020, have to filter list in java
            Predicate<GenericValue> buildNumberPredicate = new Predicate<GenericValue>() {
                @Override
                public boolean apply(GenericValue input)
                {
                    try
                    {
                        return Long.parseLong(input.getString(FIELD_TARGETBUILD)) > maxBuildNumber;
                    }
                    catch (Exception ignore)
                    {

                    }
                    return false;
                }
            };

            // select the upgrade history items that have a target build - these are upgrades done post 4.0

            final List<GenericValue> upgradeTasksToBeScrubbed = Lists.newArrayList(Iterables.filter(ofBizDelegator.findAll(ENTITY_UPGRADE_HISTORY), buildNumberPredicate));
            for (GenericValue upgradeTask : upgradeTasksToBeScrubbed)
            {
                log.info("Scrubbing upgrade history of task " + upgradeTask.getString(FIELD_UPGRADECLASS));
                upgradeTask.remove();
            }

            List<GenericValue> upgradeVersionsToBeScrubbed = Lists.newArrayList(Iterables.filter(ofBizDelegator.findAll(ENTITY_UPGRADE_VERSION_HISTORY), buildNumberPredicate));
            for (GenericValue upgradeVersion : upgradeVersionsToBeScrubbed)
            {
                log.info("Scrubbing upgrade history of build " + upgradeVersion.getString(FIELD_TARGETBUILD));
                upgradeVersion.remove();
            }
        }
    }

    /**
     * If the patch version is greater than the current version, then return true.  Else return false.
     */
    private boolean needUpgrade(final String buildNumber)
    {
        return patchBuildGreaterThanCurrent(getJiraBuildNumber(), buildNumber);
    }

    /**
     * If the patch version is greater than current version, return true.  Else return false
     */
    private static boolean patchBuildGreaterThanCurrent(final String currentBuild, final String patchBuild)
    {
        return (BUILD_NUMBER_COMPARATOR.compare(currentBuild, patchBuild) < 0);
    }

    private void addToUpgradeHistory(final Class<? extends UpgradeTask> upgradeClass)
    {
        ofBizDelegator.createValue(ENTITY_UPGRADE_HISTORY, MapBuilder.<String,Object>build(
                FIELD_UPGRADECLASS, upgradeClass.getName(),
                FIELD_TARGETBUILD, buildUtilsInfo.getCurrentBuildNumber())
        );
    }

    private void createUpgradeVersionHistory() throws GenericEntityException
    {
        final Timestamp timePerformed = new Timestamp(System.currentTimeMillis());
        final String currentBuildNumber = buildUtilsInfo.getCurrentBuildNumber();
        final String version = buildUtilsInfo.getVersion();

        try
        {
            // first check if the record already exists
            final GenericValue record = ofBizDelegator.findByPrimaryKey(ENTITY_UPGRADE_VERSION_HISTORY,
                    MapBuilder.build(FIELD_TARGETBUILD, currentBuildNumber));
            if (record == null)
            {
                ofBizDelegator.createValue(ENTITY_UPGRADE_VERSION_HISTORY, MapBuilder.<String,Object>build(
                        FIELD_TIMEPERFORMED, timePerformed,
                        FIELD_TARGETBUILD, currentBuildNumber,
                        FIELD_TARGETVERSION, version));
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("A record already exists for build number '" + currentBuildNumber + "' - skipping creation.");
                }
            }
        }
        catch (DataAccessException e)
        {
            log.error("Unable to add upgrade version history record : " + version, e);
            throw e;
        }
    }

    SortedMap<String, UpgradeTask> getAllUpgrades()
    {
        return allUpgrades;
    }

    public String getExportFilePath()
    {
        return exportFilePath;
    }

    public List<UpgradeHistoryItem> getUpgradeHistory()
    {
        try
        {
            final List<UpgradeHistoryItem> upgradeHistoryItems = new ArrayList<UpgradeHistoryItem>();

            final UpgradeHistoryItem itemFromTasks = getUpgradeHistoryItemFromTasks();
            String previousVersion = null;
            String previousBuildNumber = null;
            if (itemFromTasks != null)
            {
                upgradeHistoryItems.add(itemFromTasks);
                previousVersion = itemFromTasks.getTargetVersion();
                previousBuildNumber = itemFromTasks.getTargetBuildNumber();
            }

            // select the upgrade history items that have a target build - these are upgrades done post 4.0
            final List<GenericValue> upgradeTasksWithBuild = ofBizDelegator.findAll(ENTITY_UPGRADE_VERSION_HISTORY, CollectionBuilder.list("id"));

            for (GenericValue genericValue : upgradeTasksWithBuild)
            {
                final Timestamp timePerformed = genericValue.getTimestamp(FIELD_TIMEPERFORMED);
                final String targetBuildNumber = genericValue.getString(FIELD_TARGETBUILD);
                final String targetVersion = genericValue.getString(FIELD_TARGETVERSION);
                upgradeHistoryItems.add(new UpgradeHistoryItemImpl(timePerformed, targetBuildNumber, targetVersion, previousBuildNumber, previousVersion));
                previousVersion = targetVersion;
                previousBuildNumber = targetBuildNumber;
            }

            // reverse the list since we have been adding in chronological order
            Collections.reverse(upgradeHistoryItems);
            return Collections.unmodifiableList(upgradeHistoryItems);
        }
        catch (GenericEntityException e)
        {
            log.error(e);
            return Collections.emptyList();
        }
    }

    /**
     * Selects the most recent upgrade task that doesn't have a target build, and then extracts the build number from
     * the task class name so that we can infer a version which was upgraded to.
     *
     * @return an {@link UpgradeHistoryItem} representing the inferred upgrade; null if all upgrade tasks have an
     *         associated target build number
     * @throws GenericEntityException when search for upgrade tasks throws exception
     * @since v4.1
     */
    UpgradeHistoryItem getUpgradeHistoryItemFromTasks() throws GenericEntityException
    {
        final List<GenericValue> upgradeTasksWithoutBuild = ofBizDelegator.findByCondition("UpgradeHistoryLastClassForTargetBuild",
                new EntityExpr(FIELD_TARGETBUILD, EntityOperator.EQUALS, null),
                CollectionBuilder.<String>list(FIELD_UPGRADECLASS),
                Collections.<String>emptyList());

        if (!upgradeTasksWithoutBuild.isEmpty())
        {
            // take the first one - there should only be one
            final GenericValue genericValue = upgradeTasksWithoutBuild.get(0);
            final String upgradeClassName = genericValue.getString(FIELD_UPGRADECLASS);
            final String buildNumber = extractBuildNumberFromUpgradeClass(upgradeClassName);
            if (!StringUtils.isBlank(buildNumber))
            {
                final BuildVersionRegistry.BuildVersion targetVersion = buildVersionRegistry.getVersionForBuildNumber(buildNumber);
                return new UpgradeHistoryItemImpl(null, targetVersion.getBuildNumber(), targetVersion.getVersion(), buildNumber, null, true);
            }
        }
        return null;
    }

    /**
     * @param upgradeClassName the name of the upgrade task class e.g. <code>com.atlassian.jira.upgrade.tasks.UpgradeTask_Build207</code>
     * @return the build number associated with the class name; empty string if the build number could not be found
     * @since v4.1
     */
    static String extractBuildNumberFromUpgradeClass(final String upgradeClassName)
    {
        final Pattern pattern = BUILD_NUMBER_PATTERN;
        final Matcher matcher = pattern.matcher(upgradeClassName);
        if (matcher.find())
        {
            return matcher.group(0);
        }
        return "";
    }

    private void setExportFilePath(final String exportFilePath)
    {
        this.exportFilePath = exportFilePath;
    }

    public void reindex(Collection<String> errors) throws Exception
    {
        log.debug("Reindex all data if indexing is turned on.");

        indexManager.reIndexAll(Contexts.percentageLogger(indexManager, log));
    }
}
