package com.atlassian.jira.license;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.entity.EntityEngine;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import static com.atlassian.jira.entity.Delete.from;
import static com.atlassian.jira.entity.Entity.PRODUCT_LICENSE;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.StringUtils.trimToNull;


/**
 * @since v6.3
 */
@SuppressWarnings ("deprecation")
public class MultiLicenseStoreImpl implements MultiLicenseStore
{
    private final EntityEngine entityEngine;
    private final JiraLicenseStore jiraLicenseStore;
    private final FeatureManager featureManager;

    public MultiLicenseStoreImpl(EntityEngine entityEngine, JiraLicenseStore jiraLicenseStore, FeatureManager featureManager)
    {
        this.entityEngine = entityEngine;
        this.jiraLicenseStore = jiraLicenseStore;
        this.featureManager = featureManager;
    }

    @Override
    @Nonnull
    public Iterable<String> retrieve()
    {
        if (licenseRolesAreEnabled())
        {
            List<ProductLicense> list = entityEngine.selectFrom(PRODUCT_LICENSE).findAll().orderBy("id");

            if (!list.isEmpty())
            {
                return copyOf(transform(filter(list, Predicates.notNull()), new Function<ProductLicense, String>()
                {
                    public String apply(ProductLicense input)
                    {
                        return input.getLicenseKey();
                    }
                }));
            }
        }

        String fallback = jiraLicenseStore.retrieve();
        if (fallback != null)
            return ImmutableList.of(fallback);
        return ImmutableList.of();
    }

    private boolean licenseRolesAreEnabled()
    {
        return featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED);
    }

    @Override
    public void store(@Nonnull final Iterable<String> newLicenseKeys)
    {
        checkNotNull(newLicenseKeys, "newLicenseKeys");
        if (licenseRolesAreEnabled())
        {
            if (Iterables.isEmpty(newLicenseKeys))
            {
                throw new IllegalArgumentException("You must store at least one license.");
            }

            if (any(newLicenseKeys, Predicates.isNull()))
            {
                throw new IllegalArgumentException("You cannot store null licenses - no changes have been made to licenses.");
            }

            entityEngine.delete(from(PRODUCT_LICENSE).all());

            for (String licenseKey : newLicenseKeys)
            {
                entityEngine.createValue(PRODUCT_LICENSE, new ProductLicense(licenseKey));
            }

            String singleLicense = trimToNull(jiraLicenseStore.retrieve());
            if (singleLicense != null)
            {
                jiraLicenseStore.remove();
            }
        }
        else
        {
            Iterator<String> iterator = newLicenseKeys.iterator();
            if(iterator.hasNext())
            {
                jiraLicenseStore.store(iterator.next());
            }
            else
            {
                jiraLicenseStore.store(null);
            }
        }
    }

    @Override
    public String retrieveServerId()
    {
        return jiraLicenseStore.retrieveServerId();
    }

    @Override
    public void storeServerId(String serverId)
    {
        jiraLicenseStore.storeServerId(serverId);
    }
}
