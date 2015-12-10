package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.component.AppPropertiesComponentAdaptor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.auditing.AuditLogCleaningService;
import com.atlassian.jira.util.I18nHelper;

/**
 * Add Audit Log Cleaning Service
 *
 * @since 6.3
 */
public class UpgradeTask_Build6318 extends DropIndexTask
{
    public static final long ONE_DAY_IN_MILLIS = 24 * 3600 * 1000L;
    private final ServiceManager serviceManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build6318(ServiceManager serviceManager, I18nHelper.BeanFactory beanFactory, ApplicationProperties applicationProperties)
    {
        super(false);
        this.serviceManager = serviceManager;
        this.beanFactory = beanFactory;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String getShortDescription()
    {
        return "Add Audit Log Cleaning Service";
    }

    @Override
    public String getBuildNumber()
    {
        return "6318";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        serviceManager.addService(
                beanFactory.getInstance(applicationProperties.getDefaultLocale()).getText("admin.services.auditing.auditlogcleaner.service"),
                AuditLogCleaningService.class,
                ONE_DAY_IN_MILLIS);
    }
}
