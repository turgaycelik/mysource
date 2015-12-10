/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.issue.subscription;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.mail.MailingListCompiler;
import com.atlassian.jira.mail.SubscriptionMailQueueItemFactory;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheduler.cron.SimpleToCronTriggerConverter;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.CronScheduleInfo;
import com.atlassian.scheduler.config.IntervalScheduleInfo;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.CronTrigger;
import org.quartz.Trigger;

import static com.atlassian.jira.issue.subscription.FilterSubscriptionFactory.EMAIL_ON_EMPTY;
import static com.atlassian.jira.issue.subscription.FilterSubscriptionFactory.FILTER_ID;
import static com.atlassian.jira.issue.subscription.FilterSubscriptionFactory.GROUP;
import static com.atlassian.jira.issue.subscription.FilterSubscriptionFactory.ID;
import static com.atlassian.jira.issue.subscription.FilterSubscriptionFactory.LAST_RUN_TIME;
import static com.atlassian.jira.issue.subscription.FilterSubscriptionFactory.USER_KEY;
import static com.atlassian.jira.user.ApplicationUsers.from;
import static com.atlassian.jira.user.ApplicationUsers.getKeyFor;
import static com.atlassian.jira.user.ApplicationUsers.toDirectoryUser;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultSubscriptionManager extends MailingListCompiler implements SubscriptionManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSubscriptionManager.class);
    public static final String SUBSCRIPTION_PREFIX = DefaultSubscriptionManager.class.getName();
    public static final String JOB_RUNNER_KEY = DefaultSubscriptionManager.class.getName();

    private static final String ENTITY_NAME = "FilterSubscription";

    private final OfBizDelegator delegator;
    private final MailQueue mailQueue;
    private final SubscriptionMailQueueItemFactory subscriptionMailQueueItemFactory;
    private final GroupManager groupManager;
    private final SchedulerService schedulerService;

    public DefaultSubscriptionManager(final OfBizDelegator delegator, final MailQueue mailQueue,
            final TemplateManager templateManager, final SubscriptionMailQueueItemFactory subscriptionMailQueueItemFactory,
            final ProjectRoleManager projectRoleManager, final GroupManager groupManager, SchedulerService schedulerService)
    {
        super(templateManager, projectRoleManager);
        this.delegator = delegator;
        this.mailQueue = mailQueue;
        this.subscriptionMailQueueItemFactory = subscriptionMailQueueItemFactory;
        this.groupManager = groupManager;
        this.schedulerService = schedulerService;
        schedulerService.registerJobRunner(JobRunnerKey.of(JOB_RUNNER_KEY), new SendFilterJob());
    }

    @Override
    public boolean hasSubscription(final ApplicationUser user, final Long filterId) throws GenericEntityException
    {
        return !getSubscriptions(user, filterId).isEmpty();
    }

    @Override
    public boolean hasSubscription(User user, Long filterId) throws GenericEntityException
    {
        return hasSubscription(from(user), filterId);
    }

    @Override
    public FilterSubscription getFilterSubscription(final Long subId) throws GenericEntityException
    {
        return Select.from(Entity.FILTER_SUBSCRIPTION).byId(subId).runWith(delegator).singleValue();
    }

    private GenericValue getSubscription(final Long subId) throws GenericEntityException
    {
        return Select.from(Entity.FILTER_SUBSCRIPTION.getEntityName()).byId(subId).runWith(delegator).singleValue();
    }

    @Override
    public GenericValue getSubscription(final ApplicationUser user, final Long subId) throws GenericEntityException
    {
        return Select.from(Entity.FILTER_SUBSCRIPTION.getEntityName())
                .byId(subId).whereEqual(USER_KEY, getKeyFor(user))
                .runWith(delegator).singleValue();
    }

    @Override
    public FilterSubscription getFilterSubscription(final ApplicationUser user, final Long subId)
            throws GenericEntityException
    {
        return Select.from(Entity.FILTER_SUBSCRIPTION)
                .byId(subId).whereEqual(USER_KEY, getKeyFor(user))
                .runWith(delegator).singleValue();
    }

    @Override
    public GenericValue getSubscription(User user, Long subId) throws GenericEntityException
    {
        return getSubscription(from(user), subId);
    }

    @Nullable
    @Override
    public GenericValue getSubscriptionFromTriggerName(final String triggerName) throws GenericEntityException
    {
        if (triggerName == null || triggerName.length() <= SUBSCRIPTION_PREFIX.length() || !triggerName.startsWith(SUBSCRIPTION_PREFIX))
        {
            return null;
        }
        final Long subscriptionId = Long.valueOf(triggerName.substring(SUBSCRIPTION_PREFIX.length()));
        return getSubscription(subscriptionId);
    }

    @Override
    public List<GenericValue> getSubscriptions(final ApplicationUser user, final Long filterId) throws GenericEntityException
    {
        final EntityCondition filterCondition = getFilterByUserCondition(user, filterId);

        // Return the results
        return Select.from(Entity.FILTER_SUBSCRIPTION.getEntityName()).whereCondition(filterCondition).runWith(delegator).asList();
    }

    private EntityCondition getFilterByUserCondition(final ApplicationUser user, final Long filterId)
    {
        final List<EntityExpr> entityExpressions = Lists.newArrayList();
        // Retrieve all subscriptions created by the user
        entityExpressions.add(new EntityExpr(USER_KEY, EntityOperator.EQUALS, getKeyFor(user)));

        // Group shared subscriptions
        final Iterable<String> groups = groupManager.getGroupNamesForUser(toDirectoryUser(user));
        for (final String group : groups)
        {
            // That's the whole point...
            //noinspection ObjectAllocationInLoop
            entityExpressions.add(new EntityExpr(GROUP, EntityOperator.EQUALS, group));
        }

        // Get the expression which will return everything owned by the user or shared to one of user's groups
        final EntityCondition ownershipCondition = new EntityConditionList(entityExpressions, EntityOperator.OR);

        final EntityCondition filterCondition = new EntityExpr(FILTER_ID, EntityOperator.EQUALS, filterId);

        return new EntityConditionList(ImmutableList.of(ownershipCondition, filterCondition), EntityOperator.AND);
    }

    @Override
    public List<FilterSubscription> getFilterSubscriptions(final ApplicationUser user, final Long filterId)
            throws GenericEntityException
    {
        final EntityCondition filterCondition = getFilterByUserCondition(user, filterId);

        return Select.from(Entity.FILTER_SUBSCRIPTION).whereCondition(filterCondition).runWith(delegator).asList();
    }

    @Override
    public List<GenericValue> getSubscriptions(User user, Long filterId) throws GenericEntityException
    {
        return getSubscriptions(from(user), filterId);
    }

    @Override
    public Trigger getTriggerFromSubscription(final GenericValue subscription)
    {
        String expression = getCronExpression(subscription.getLong(ID));
        try
        {
            return new CronTrigger(SUBSCRIPTION_PREFIX + subscription.getLong(ID), null, expression);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public String getCronExpressionForSubscription(final FilterSubscription subscription)
    {
        return getCronExpression(subscription.getId());
    }

    @Nullable
    private String getCronExpression(final long subscriptionId)
    {
        final JobDetails jobDetails = schedulerService.getJobDetails(toJobId(subscriptionId));
        if (jobDetails == null)
        {
            return null;
        }
        return getCronExpression(jobDetails.getSchedule());
    }

    @Nullable
    private static String getCronExpression(final Schedule schedule)
    {
        final String cronExpression;
        if (schedule.getCronScheduleInfo() != null)
        {
            cronExpression = schedule.getCronScheduleInfo().getCronExpression();
        }
        else if (schedule.getIntervalScheduleInfo() != null)
        {
            final long interval = schedule.getIntervalScheduleInfo().getIntervalInMillis();
            cronExpression = new SimpleToCronTriggerConverter().convertToCronString(new Date(), interval).cronString;
        }
        else
        {
            cronExpression = null;
        }
        return cronExpression;
    }

    @Override
    public void updateSubscription(final ApplicationUser user, final Long subId, final String groupName, final Trigger trigger, final Boolean emailOnEmpty) throws DataAccessException
    {
        updateSubscription(user, subId, groupName, ((CronTrigger) trigger).getCronExpression(), emailOnEmpty);
    }

    @Override
    public void updateSubscription(final ApplicationUser user, final Long subId, final String groupName, final String cronExpression, final Boolean emailOnEmpty) throws DataAccessException
    {
        Schedule schedule = Schedule.forCronExpression(cronExpression);
        try
        {
            schedulerService.unscheduleJob(toJobId(subId));

            final GenericValue subscriptionGV = getSubscription(user, subId);
            final Map<String, String> fields = MapBuilder.build(USER_KEY, getKeyFor(user), GROUP, groupName, EMAIL_ON_EMPTY, emailOnEmpty.toString());
            subscriptionGV.setFields(fields);
            subscriptionGV.store();

            JobConfig config = getJobConfig(subscriptionGV.getLong(ID), schedule);
            schedulerService.scheduleJob(toJobId(subscriptionGV.getLong(ID)), config);
        }
        catch (final SchedulerServiceException e)
        {
            throw new DataAccessException(e);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private static JobId toJobId(final Long subId)
    {
        return JobId.of(SUBSCRIPTION_PREFIX + ':' + subId);
    }

    @Override
    public void updateSubscription(User user, Long subscriptionId, String groupName, Trigger trigger, Boolean emailOnEmpty)
            throws DataAccessException
    {
        updateSubscription(from(user), subscriptionId, groupName, trigger, emailOnEmpty);
    }

    private static JobConfig getJobConfig(final Long subscriptionId, final Schedule schedule)
    {
        return JobConfig.forJobRunnerKey(JobRunnerKey.of(JOB_RUNNER_KEY))
                .withSchedule(schedule)
                .withParameters(ImmutableMap.<String, Serializable>of(SUBSCRIPTION_IDENTIFIER, subscriptionId));
    }

    @Override
    public List<FilterSubscription> getAllFilterSubscriptions(final Long filterId)
    {
        return Select.from(Entity.FILTER_SUBSCRIPTION).whereEqual(FILTER_ID, filterId).runWith(delegator).asList();
    }

    @Override
    public List<FilterSubscription> getAllFilterSubscriptions()
    {
        return Select.from(Entity.FILTER_SUBSCRIPTION).runWith(delegator).asList();
    }

    @Override
    public List<GenericValue> getAllSubscriptions(final Long filterId)
    {
        return Select.from(Entity.FILTER_SUBSCRIPTION.getEntityName()).whereEqual(FILTER_ID, filterId).runWith(delegator).asList();
    }

    @Override
    public List<GenericValue> getAllSubscriptions()
    {
        return Select.from(Entity.FILTER_SUBSCRIPTION.getEntityName()).runWith(delegator).asList();
    }

    @Override
    public GenericValue createSubscription(final ApplicationUser user, final Long filterId, final String groupName, final Long period, final Boolean emailOnEmpty)
    {
        Schedule schedule = Schedule.forInterval(period, null);
        return createSubscription(user, filterId, groupName, emailOnEmpty, schedule);
    }

    @Override
    public GenericValue createSubscription(User user, Long filterId, String groupName, Long period, Boolean emailOnEmpty)
    {
        return createSubscription(from(user), filterId, groupName, period, emailOnEmpty);
    }

    @Override
    public GenericValue createSubscription(final ApplicationUser user, final Long filterId, String groupName, final Trigger trigger, final Boolean emailOnEmpty)
    {
        Schedule schedule = Schedule.forCronExpression(((CronTrigger) trigger).getCronExpression());
        return createSubscription(user, filterId, groupName, emailOnEmpty, schedule);
    }

    @Override
    public FilterSubscription createSubscription(final ApplicationUser user, final Long filterId, String groupName, final String cronExpression, final Boolean emailOnEmpty)
    {
        Schedule schedule = Schedule.forCronExpression(cronExpression);
        return Entity.FILTER_SUBSCRIPTION.build(createSubscription(user, filterId, groupName, emailOnEmpty, schedule));
    }

    @Nullable
    @Override
    public Date getNextSendTime(@Nonnull final FilterSubscription sub)
    {
        final JobDetails jobDetails = schedulerService.getJobDetails(toJobId(sub.getId()));
        return jobDetails == null ? null : jobDetails.getNextRunTime();
    }

    private GenericValue createSubscription(final ApplicationUser user, final Long filterId, String groupName, final Boolean emailOnEmpty, final Schedule schedule)
    {
        final FieldMap columns = FieldMap.build(
                FILTER_ID, filterId,
                USER_KEY, getKeyFor(user),
                GROUP, TextUtils.stringSet(groupName) ? groupName : null,
                LAST_RUN_TIME, null,
                EMAIL_ON_EMPTY, emailOnEmpty.toString());

        final GenericValue subscriptionGV;
        try
        {
            subscriptionGV = EntityUtils.createValue(ENTITY_NAME, columns);
            JobConfig config = getJobConfig(subscriptionGV.getLong(ID), schedule);
            schedulerService.scheduleJob(toJobId(subscriptionGV.getLong(ID)), config);
            return subscriptionGV;
        }
        catch (final SchedulerServiceException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public GenericValue createSubscription(User user, Long filterId, String groupName, Trigger trigger, Boolean emailOnEmpty)
    {
        return createSubscription(from(user), filterId, groupName, trigger, emailOnEmpty);
    }

    @Override
    public void deleteSubscription(final Long subId) throws GenericEntityException
    {
        final GenericValue subscriptionGV = getSubscription(subId);
        if (schedulerService.getJobDetails(toJobId(subscriptionGV.getLong(ID))) != null)
        {
            schedulerService.unscheduleJob(toJobId(subscriptionGV.getLong(ID)));
        }
        else
        {
            LOG.debug("Unable to find a scheduled job for the subscription: " + subscriptionGV.getLong(ID) + "; removing the subscription anyway.");
        }
        subscriptionGV.remove();
    }

    @Override
    public void deleteSubscriptionsForUser(@Nonnull final ApplicationUser user)
            throws GenericEntityException
    {
        notNull("user", user);
        final List<GenericValue> subscriptionGvs = delegator.findByAnd(ENTITY_NAME, ImmutableMap.of(USER_KEY, getKeyFor(user)));
        for (final GenericValue subscription : subscriptionGvs)
        {
            deleteSubscription(subscription.getLong(ID));
        }
    }

    @Override
    public void deleteSubscriptionsForUser(User user) throws GenericEntityException
    {
        deleteSubscriptionsForUser(from(user));
    }

    @Override
    public void runSubscription(final GenericValue sub) throws GenericEntityException
    {
        //Update the timestamps for the subscription so if it fails it won't get run every minute
        final Timestamp ts = new Timestamp(new Date().getTime());
        sub.set(LAST_RUN_TIME, ts);
        sub.store();

        final MailQueueItem item = subscriptionMailQueueItemFactory.getSubscriptionMailQueueItem(sub);
        mailQueue.addItem(item);
    }

    @Override
    public void runSubscription(final Long subId) throws GenericEntityException
    {
        runSubscription(getSubscription(subId));
    }

    @Override
    public void runSubscription(final ApplicationUser user, final Long subId) throws GenericEntityException
    {
        runSubscription(getSubscription(user, subId));
    }

    @Override
    public void runSubscription(User user, Long subId) throws GenericEntityException
    {
        runSubscription(from(user), subId);
    }

    @Override
    public void deleteSubscriptionsForGroup(final Group group) throws GenericEntityException
    {
        notNull("group", group);
        final List<GenericValue> subscriptionGvs = delegator.findByAnd(ENTITY_NAME, ImmutableMap.of(GROUP, group.getName()));
        for (final GenericValue subscription : subscriptionGvs)
        {
            deleteSubscription(subscription.getLong(ID));
        }

    }

}
