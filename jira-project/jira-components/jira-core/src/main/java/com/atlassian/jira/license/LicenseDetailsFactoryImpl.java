package com.atlassian.jira.license;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.core.util.Clock;
import com.atlassian.extras.api.LicenseManager;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.extras.common.LicenseException;
import com.atlassian.extras.decoder.api.LicenseDecoder;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.ExternalLinkUtil;

import static com.atlassian.jira.license.DefaultLicenseDetails.isEnterpriseSubscriptionLicense;


public class LicenseDetailsFactoryImpl implements LicenseDetailsFactory
{
    private final ApplicationProperties applicationProperties;
    private final ExternalLinkUtil externalLinkUtil;
    private final BuildUtilsInfo buildUtilsInfo;
    private final I18nHelper.BeanFactory i18nFactory;
    private final DateTimeFormatter dateTimeFormatter;
    private final LicenseDecoder licenseDecoder;
    private final LicenseManager licenseManager;
    private final ClusterManager clusterManager;
    private final Clock clock;


    public LicenseDetailsFactoryImpl(
        ApplicationProperties applicationProperties,
        ExternalLinkUtil externalLinkUtil,
        BuildUtilsInfo buildUtilsInfo,
        I18nBean.BeanFactory i18Factory,
        DateTimeFormatter dateTimeFormatter,
        LicenseDecoder decoder,
        LicenseManager licenseManager,
        ClusterManager clusterManager,
        Clock clock)
    {
        this.applicationProperties = applicationProperties;
        this.externalLinkUtil = externalLinkUtil;
        this.buildUtilsInfo = buildUtilsInfo;
        this.i18nFactory = i18Factory;
        this.dateTimeFormatter = dateTimeFormatter;
        this.licenseDecoder = decoder;
        this.licenseManager = licenseManager;
        this.clusterManager = clusterManager;
        this.clock = clock;
    }

    @Nonnull @Override
    public LicenseDetails getLicense(@Nullable final String licenseString)
    {
        final JiraLicense jiraLicense = getLicenseInternal(licenseString);
        if (jiraLicense == null)
        {
            return NullLicenseDetails.NULL_LICENSE_DETAILS;
        }

        if (isEnterpriseSubscriptionLicense(jiraLicense))
        {
            return new SubscriptionLicenseDetails(
                jiraLicense, licenseString, applicationProperties, externalLinkUtil,
                buildUtilsInfo, i18nFactory, dateTimeFormatter, licenseDecoder, clusterManager, clock);
        }
        else
        {
            return new DefaultLicenseDetails(
                jiraLicense, licenseString, applicationProperties, externalLinkUtil,
                buildUtilsInfo, i18nFactory, dateTimeFormatter, licenseDecoder, clusterManager, clock);
        }
    }

    @Override
    public boolean isDecodeable(final String licenseString)
    {
        try
        {
            return getLicenseInternal(licenseString) != null;
        }
        catch (LicenseException e)
        {
            return false;
        }
    }

    private JiraLicense getLicenseInternal(String licenseString)
    {
        if (StringUtils.isBlank(licenseString))
        {
            return null;
        }
        return (JiraLicense) licenseManager.getLicense(licenseString).getProductLicense(Product.JIRA);
    }
}
