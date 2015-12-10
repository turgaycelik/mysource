package com.atlassian.jira.issue.index;

import com.atlassian.jira.concurrent.BarrierFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesUtil;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.EntityCondition;

/**
 * @since v6.1
 */
public class DefaultIssueBatcherFactory implements IssueBatcherFactory
{
    /**
     * The name of the JIRA property used to set the batch size.
     */
    private static final String BATCH_SIZE_PROPERTY_NAME = "jira.index.background.batch.size";

    /**
     * The default batch size, if not explicitly configured.
     */
    private static final int BATCH_SIZE_DEFAULT_VALUE = 1000;

    private final OfBizDelegator delegator;
    private final IssueFactory issueFactory;
    private final BarrierFactory barrierFactory;
    private final ApplicationProperties applicationProperties;

    public DefaultIssueBatcherFactory(final OfBizDelegator delegator,
            final IssueFactory issueFactory,
            final BarrierFactory barrierFactory,
            final ApplicationProperties applicationProperties)
    {
        this.delegator = delegator;
        this.issueFactory = issueFactory;
        this.barrierFactory = barrierFactory;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public IssuesBatcher getBatcher()
    {
        return getBatcher(null, null);
    }

    @Override
    public IssuesBatcher getBatcher(final IssueIdBatcher.Spy spy)
    {
        return getBatcher(null, spy);
    }

    @Override
    public IssuesBatcher getBatcher(final EntityCondition condition)
    {
        return getBatcher(condition, null);
    }

    @Override
    public IssuesBatcher getBatcher(final EntityCondition condition, final IssueIdBatcher.Spy spy)
    {
        return new IssueIdBatcher(delegator, issueFactory, barrierFactory, getConfiguredBatchSize(), condition, spy);
    }

    @Override
    public IssuesBatcher getBatcher(final EntityCondition condition, final IssueIdBatcher.Spy spy, final int batchSize)
    {
        return new IssueIdBatcher(delegator, issueFactory, barrierFactory, batchSize, condition, spy);
    }

    /**
     * Returns the batch size that is configured using the '{@value #BATCH_SIZE_PROPERTY_NAME}' advanced property, or
     * {@value #BATCH_SIZE_DEFAULT_VALUE} if the property is not defined.
     *
     * @return the batch size to use for background indexing
     */
    private int getConfiguredBatchSize()
    {
        return PropertiesUtil.getIntProperty(applicationProperties, BATCH_SIZE_PROPERTY_NAME, BATCH_SIZE_DEFAULT_VALUE);
    }
}
