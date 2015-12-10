package com.atlassian.jira.license;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.license.SIDManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @since v4.0
 */
public class JiraLicenseManagerImpl implements JiraLicenseManager
{
    Logger log = LoggerFactory.getLogger(JiraLicenseManagerImpl.class);

    private final JiraLicenseStore licenseStore;
    private final BuildUtilsInfo buildUtilsInfo;
    private final SIDManager sidManager;
    private final EventPublisher eventPublisher;
    private final LicenseDetailsFactory licenseDetailsFactory;

    private final FeatureManager featureManager;
    private final MultiLicenseStore multiLicenseStore;

    public JiraLicenseManagerImpl(JiraLicenseStore licenseStore, BuildUtilsInfo buildUtilsInfo, SIDManager sidManager,
            EventPublisher eventPublisher, MultiLicenseStore multiLicenseStore, FeatureManager featureManager,
            LicenseDetailsFactory licenseDetailsFactory)
    {
        this.eventPublisher = eventPublisher;
        this.licenseDetailsFactory = notNull("licenseDetailsFactory", licenseDetailsFactory);
        this.featureManager = notNull("featureManager", featureManager);
        this.licenseStore = notNull("licenseStore", licenseStore);
        this.multiLicenseStore = notNull("multiLicenseStore", multiLicenseStore);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.sidManager = notNull("sidManager", sidManager);
    }

    @Override
    public String getServerId()
    {
        String serverId = multiLicenseStore.retrieveServerId();
        if (StringUtils.isBlank(serverId))
        {
            serverId = sidManager.generateSID();
            multiLicenseStore.storeServerId(serverId);
        }
        return serverId;
    }

    @Override
    public LicenseDetails getLicense()
    {
        Iterator<String> iterator = multiLicenseStore.retrieve().iterator();
        if (iterator.hasNext())
        {
            String next = iterator.next();
            if(iterator.hasNext())
            {
                log.debug("There are multiple licenses installed, but something is using the getLicense method.", new Exception());
            }
            return getLicense(next);
        }
        return NullLicenseDetails.NULL_LICENSE_DETAILS;
    }

    @Override
    public LicenseDetails getLicense(String licenseString)
    {
        return licenseDetailsFactory.getLicense(licenseString);
    }

    @Override
    public Iterable<LicenseDetails> getLicenses()
    {
        Iterable<String> licenseStrings = multiLicenseStore.retrieve();

        if (Iterables.isEmpty(licenseStrings))
            return ImmutableList.of(NullLicenseDetails.NULL_LICENSE_DETAILS);

        return Iterables.transform(licenseStrings, new Function<String, LicenseDetails>()
        {
            public LicenseDetails apply(@Nullable final String s)
            {
                return getLicense(s);
            }
        });
    }

    @Override
    public boolean isLicensed(@Nonnull LicenseRoleId role)
    {
        // mock only; this will change "soon"
        return true;
    }

    @Override
    public boolean isDecodeable(String licenseString)
    {
        return this.licenseDetailsFactory.isDecodeable(licenseString);
    }

    @Override
    public LicenseDetails setLicense(String licenseString)
    {
        return setLicense(licenseString, true);
    }

    @Override
    public LicenseDetails setLicenseNoEvent(String licenseString)
    {
        return setLicense(licenseString, false);
    }

    private LicenseDetails setLicense(String licenseString, boolean fireEvent)
    {
        if (!isDecodeable(licenseString))
        {
            throw new IllegalArgumentException("The licenseString is invalid and will not be stored.");
        }
        replaceLicense(licenseString);

        final LicenseDetails licenseDetails = getLicense(licenseString);

        // if the license maintenance is valid, then we should reset any app properties
        // if the license is not too old and the confirmation of installation with old license was made, remove the conformation
        if (licenseDetails.isMaintenanceValidForBuildDate(buildUtilsInfo.getCurrentBuildDate()) && licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone())
        {
            licenseStore.resetOldBuildConfirmation();
        }

        if (fireEvent)
        {
            eventPublisher.publish(new NewLicenseEvent(licenseDetails));
        }
        return licenseDetails;
    }

    /**
     * Removes all licenses that share a role with this license, and then stores this license.
     */
    private void replaceLicense(String licenseString)
    {
        if (featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED))
        {
            LicenseDetails license = getLicense(licenseString);
            final Set<LicenseRoleId> newLicenseRoles = license.getLicenseRoles().getLicenseRoles();

            Predicate<LicenseDetails> notContained = new NotContainedPredicate(newLicenseRoles);
            Iterable<LicenseDetails> withoutNewRoles = Iterables.filter(getLicenses(), notContained);

            ArrayList<String> toStore = newArrayList(licenseString);
            for (LicenseDetails pair: withoutNewRoles)
            {
                toStore.add(pair.getLicenseString());
            }

            multiLicenseStore.store(toStore);
        }
        else
        {
            multiLicenseStore.store(newArrayList(licenseString));
        }
    }

    @Override
    public void confirmProceedUnderEvaluationTerms(String userName)
    {
        licenseStore.confirmProceedUnderEvaluationTerms(userName);
    }


    private static class NotContainedPredicate implements Predicate<LicenseDetails>
    {
        private final Set<LicenseRoleId> newLicenseRoles;
        private final Predicate<LicenseRoleId> predicate;

        public NotContainedPredicate(Set<LicenseRoleId> newLicenseRoles)
        {
            this.newLicenseRoles = newLicenseRoles;
            this.predicate = new ContainsRolePredicate(newLicenseRoles);
        }

        public boolean apply(LicenseDetails licenseDetails)
        {
            if (licenseDetails == NullLicenseDetails.NULL_LICENSE_DETAILS)
            {
                //discard null-type licenses - don't store them.
                return false;
            }
            return !containsAlready(licenseDetails);
        }

        private boolean containsAlready(LicenseDetails licenseDetails)
        {
            Set<LicenseRoleId> localLicenseRoles = licenseDetails.getLicenseRoles().getLicenseRoles();

            if (newLicenseRoles.isEmpty())
            {
                return localLicenseRoles.isEmpty();
            }
            return Iterables.any(localLicenseRoles, predicate);
        }
    }

    private static class ContainsRolePredicate implements Predicate<LicenseRoleId>
    {
        private final Set<LicenseRoleId> newRoles;

        public ContainsRolePredicate(Set<LicenseRoleId> newRoles)
        {
            this.newRoles = newRoles;
        }

        public boolean apply(LicenseRoleId existingRole)
        {
            return newRoles.contains(existingRole);
        }
    }
}
