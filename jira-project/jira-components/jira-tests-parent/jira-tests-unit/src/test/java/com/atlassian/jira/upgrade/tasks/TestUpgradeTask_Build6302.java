package com.atlassian.jira.upgrade.tasks;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.upgrade.UpgradeTask;
import com.atlassian.scheduler.quartz1.Quartz1Job;

import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import static com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6302.LOCK_NAMES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.matchers.NotNull.NOT_NULL;
import static org.mockito.internal.matchers.Null.NULL;


/**
 * Test the upgrade of the Quartz tables.
 *
 * This integration style test uses the Quartz scheduler to validate that the data converted
 * is in a format recognisable by Quartz
 *
 * @since v6.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUpgradeTask_Build6302
{
    private static DelegatorInterface getDelegator()
    {
        // Getting a new clean GenericDelegator sets up an in-memory database.
        GenericDelegator.removeGenericDelegator("default");
        return GenericDelegator.getGenericDelegator("default");
    }

    @Rule
    public MockComponentContainer mockComponentContainer = new MockComponentContainer(this);

    @Mock @AvailableInContainer
    private ApplicationProperties mockApplicationProperties;
    @Mock @AvailableInContainer
    private ClusterManager clusterManager;
    @Mock
    private ServiceManager serviceManager;

    @AvailableInContainer
    private DelegatorInterface delegatorInterface = getDelegator();
    @AvailableInContainer
    private OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(delegatorInterface);
    private Scheduler scheduler;

    @Before
    public void setupMocks() throws SchedulerException, ParseException
    {
        buildOldTables();
        final Properties props = getQuartzProperties();

        final StdSchedulerFactory schedulerFactory = new StdSchedulerFactory(props);

        scheduler = schedulerFactory.getScheduler();
        // Check we are using Quartz 1.x otherwise this test is meaningless
        assertThat(scheduler.getMetaData().getVersion().charAt(0), is('1'));
    }

    @After
    public void tearDown() throws SchedulerException
    {
        // Clear all
        for (final String group : scheduler.getJobGroupNames())
        {
            for (final String job : scheduler.getJobNames(group))
            {
                for (final Trigger trigger : scheduler.getTriggersOfJob(job, group))
                {
                    scheduler.unscheduleJob(trigger.getName(), trigger.getGroup());
                }
                scheduler.deleteJob(job, group);
            }
        }

        scheduler.shutdown(false);
    }

    @Test
    public void testUpgradedSchedule() throws Exception
    {
        UpgradeTask task = new UpgradeTask_Build6302(ofBizDelegator, serviceManager);
        task.doUpgrade(false);

        JobDetail jobDetail = scheduler.getJobDetail("SEND_SUBSCRIPTION", "SEND_SUBSCRIPTION");
        assertThat("Subscription job should get discarded", jobDetail, nullValue());

        checkJobDetail("ServicesJob", "Default", NULL, new JobDataMap(), Quartz1Job.class, true, true);
        checkJobDetail("RefreshActiveUserCount", "Default", NULL, new JobDataMap(), Quartz1Job.class, true, false);

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        checkSimpleTriggerDetails("ServicesTrigger", "ServicesJob", "ServicesJob", "Default",
                df.parse("2093-09-20 06:00:00"), Trigger.STATE_NORMAL, df.parse("2092-03-07 09:29:19"), null, "", 0, 1,
                1, 10);
        checkCronTriggerDetails("RefreshActiveUserCountTrigger", "RefreshActiveUserCount",  "RefreshActiveUserCount",
                "Default", df.parse("2093-09-20 06:00:00"), Trigger.STATE_NORMAL,
                df.parse("2092-03-07 09:29:19"), null, "", 0, "0 0 0/2 * * ?");

        Trigger trigger = scheduler.getTrigger("SUBSCRIPTION_10100", "SEND_SUBSCRIPTION");
        assertThat("Subscription trigger should get discarded", trigger, nullValue());
        trigger = scheduler.getTrigger("SUBSCRIPTION_10200", "SEND_SUBSCRIPTION");
        assertThat("Subscription trigger should get discarded", trigger, nullValue());
        verify(serviceManager).refreshAll();
    }

    private void checkJobDetail(final String jobname, final String jobgroup, final Null desc, final JobDataMap jobdata,
            final Class<? extends Job> jobClass, final boolean durable, final boolean stateful)
        throws SchedulerException
    {
        final JobDetail jobDetail = scheduler.getJobDetail(jobname, jobgroup);
        assertThat(jobDetail, is(NOT_NULL));
        assertThat(jobDetail.getKey().getName(), is(jobname));
        assertThat(jobDetail.getKey().getGroup(), is(jobgroup));
        assertThat(jobDetail.getDescription(), is(desc));
        assertThat(jobDetail.getJobDataMap(), is(jobdata));
        assertThat(jobDetail.isDurable(), is(durable));
//      Statefullness depends upon the job class and we never are and this test doesn't test the database content
//      so commented out but left visible here so its absence is not read as an oversight.
//        assertThat(jobDetail.isStateful(), is(stateful));
        assertEquals(jobDetail.getJobClass(), jobClass);
        assertEquals(jobDetail.getJobClass(), jobClass);
        checkLockEntitiesExist();
    }

    private void checkSimpleTriggerDetails(final String triggername, final String triggergroup, final String jobname,
            final String jobgroup, final Date nextFireTime, final int state, final Date startTime, final Date endTime,
            final String calendar, final int misfire, final int repeatCount, final long repeatInterval,
            final int timesTriggered)
        throws SchedulerException
    {
        final Trigger trigger = scheduler.getTrigger(triggername, triggergroup);
        assertThat(trigger, is(NOT_NULL));
        assertThat(trigger, IsInstanceOf.instanceOf(SimpleTrigger.class));

        checkCommonTriggerDetails(trigger, triggername, triggergroup, jobname, jobgroup, nextFireTime, state, startTime,
                endTime, calendar, misfire);

        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        assertThat(simpleTrigger.getRepeatCount(), is(repeatCount));
        assertThat(simpleTrigger.getRepeatInterval(), is(repeatInterval));
        assertThat(simpleTrigger.getTimesTriggered(), is(timesTriggered));
    }

    private void checkCronTriggerDetails(final String triggername, final String triggergroup, final String jobname,
            final String jobgroup, final Date nextFireTime, final int state, final Date startTime, final Date endTime,
            final String calendar, final int misfire, final String cron)
        throws SchedulerException
    {
        final Trigger trigger = scheduler.getTrigger(triggername, triggergroup);
        assertThat(trigger, is(NOT_NULL));
        assertThat(trigger, IsInstanceOf.instanceOf(CronTrigger.class));

        checkCommonTriggerDetails(trigger, triggername, triggergroup, jobname, jobgroup, nextFireTime, state, startTime,
                endTime, calendar, misfire);

        CronTrigger cronTrigger = (CronTrigger) trigger;
        assertThat(cronTrigger.getCronExpression(), is(cron));
    }

    private void checkCommonTriggerDetails(final Trigger trigger, final String triggername, final String triggergroup,
            final String jobname, final String jobgroup, final Date nextFireTime, final int state, final Date startTime,
            final Date endTime, final String calendar, final int misfire)
        throws SchedulerException
    {
        assertThat(trigger.getKey().getName(), is(triggername));
        assertThat(trigger.getKey().getGroup(), is(triggergroup));
        assertThat(trigger.getJobName(), is(jobname));
        assertThat(trigger.getJobGroup(), is(jobgroup));
        assertThat(scheduler.getTriggerState(trigger.getName(), trigger.getGroup()), is(state));
        assertThat(trigger.getNextFireTime(), is(nextFireTime));
        assertThat(trigger.getStartTime(), is(startTime));
        assertThat(trigger.getEndTime(), is(endTime));
        assertThat(trigger.getCalendarName(), is(calendar));
        assertThat(trigger.getMisfireInstruction(), is(misfire));
    }

    private void checkLockEntitiesExist()
    {
        final String[] expectedLocks = LOCK_NAMES.toArray(new String[LOCK_NAMES.size()]);
        final List<String> actualLocks = new ArrayList<String>(expectedLocks.length);
        for (GenericValue lock : ofBizDelegator.findAll("JQRTZLocks"))
        {
            actualLocks.add(lock.getString("lockName"));
        }
        assertThat(actualLocks, containsInAnyOrder(expectedLocks));
    }

    private Properties getQuartzProperties()
    {
        final Properties props = new Properties();
        props.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");

        props.setProperty("org.quartz.scheduler.instanceName", "JIRA_scheduler");

        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "4");
        props.setProperty("org.quartz.threadPool.threadPriority", "4");

        props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.setProperty("org.quartz.jobStore.dataSource", "JiraDataSource");
        props.setProperty("org.quartz.jobStore.tablePrefix", "JQUARTZ_");

        props.setProperty("org.quartz.dataSource.JiraDataSource.connectionProvider.class",
                "com.atlassian.jira.scheduler.Quartz1ConnectionProvider");
        return props;
    }

    private void buildOldTables() throws ParseException
    {
        // Typically these contain the couple of standard jobs for running services and the subscriptions
        addJobDetail(10000, "SEND_SUBSCRIPTION", "SEND_SUBSCRIPTION", "com.atlassian.scheduler.quartz1.Quartz1Job",
                "true", "false", "false", "X");
        addJobDetail(10021, "ServicesJob", "Default", "com.atlassian.scheduler.quartz1.Quartz1Job",
                "true", "true", "false", "");
        addJobDetail(10022, "RefreshActiveUserCount", "Default", "com.atlassian.scheduler.quartz1.Quartz1Job",
                "true", "false", "false", "X");

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        addSimpleTrigger(10000, "ServicesTrigger", "ServicesJob",  10021, df.parse("2093-09-20 06:00:00"),
                "WAITING", "SIMPLE", df.parse("2092-03-07 09:29:19"), null, "", 0, 1, 1, 10);
        addCronTrigger(10001, "RefreshActiveUserCountTrigger", "RefreshActiveUserCount",  10022, df.parse("2093-09-20 06:00:00"),
                "WAITING", "CRON", df.parse("2092-03-07 09:29:19"), null, "", 0, "0 0 0/2 * * ?");
        addCronTrigger(10100, "SUBSCRIPTION_10100", "SEND_SUBSCRIPTION", 10000, df.parse("2093-09-20 06:00:00"),
                "WAITING", "CRON", df.parse("2092-03-07 09:29:19"), null, "", 0, "0 0 6 ? * *");
        addCronTrigger(10101, "SUBSCRIPTION_10200", "SEND_SUBSCRIPTION", 10000, df.parse("2093-09-20 06:00:00"),
                "WAITING", "CRON", df.parse("2092-03-07 09:29:19"), null, "", 0, "0 0 7-15/3 ? * *");
    }

    private void addJobDetail(final int i, final String jobname, final String groupname, final String clazz,
            final String durable, final String stateful, final String reqRecovery, final String jobdata)
    {
        final FieldMap values = new FieldMap();
        values.put("id", i);
        values.put("jobName", jobname);
        values.put("jobGroup", groupname);
        values.put("className", clazz);
        values.put("isDurable", durable);
        values.put("isStateful", stateful);
        values.put("requestsRecovery", reqRecovery);
        values.put("jobData", jobdata);
        
        ofBizDelegator.createValue("QRTZJobDetails", values);
    }

    private void addSimpleTrigger(final int i, final String triggerName, final String triggerGroup, final int jobId,
            final Date nextFire, final String state, final String type, final Date startTime, final Date endTime,
            final String calendar, final int misfire, final int repeatCount, final int repeatInterval,
            final int timesTriggered)
    {
        addTrigger(i, triggerName, triggerGroup, jobId, nextFire, state, type, startTime, endTime, calendar, misfire);
        final FieldMap values = new FieldMap();
        values.put("id", i);
        values.put("trigger", i);
        values.put("repeatCount", repeatCount);
        values.put("repeatInterval", repeatInterval);
        values.put("timesTriggered", timesTriggered);

        ofBizDelegator.createValue("QRTZSimpleTriggers", values);

    }

    private void addCronTrigger(final int i, final String triggerName, final String triggerGroup, final int jobId,
            final Date nextFire, final String state, final String type, final Date startTime, final Date endTime,
            final String calendar, final int misfire, final String cron)
    {
        addTrigger(i, triggerName, triggerGroup, jobId, nextFire, state, type, startTime, endTime, calendar, misfire);
        final FieldMap values = new FieldMap();
        values.put("id", i);
        values.put("trigger", i);
        values.put("cronExpression", cron);

        ofBizDelegator.createValue("QRTZCronTriggers", values);
    }

    private void addTrigger(final int i, final String triggerName, final String triggerGroup, final int jobId,
            final Date nextFire, final String state, final String type, final Date startTime, final Date endTime,
            final String calendar, final int misfire)
    {
        final FieldMap values = new FieldMap();
        values.put("id", i);
        values.put("triggerName", triggerName);
        values.put("triggerGroup", triggerGroup);
        values.put("job", jobId);
        values.put("nextFire", nextFire == null ? null : new Timestamp(nextFire.getTime()));
        values.put("triggerState", state);
        values.put("triggerType", type);
        values.put("startTime", startTime == null ? null : new Timestamp(startTime.getTime()));
        values.put("endTime", endTime == null ? null : new Timestamp(endTime.getTime()));
        values.put("calendarName", calendar);
        values.put("misfireInstr", misfire);

        ofBizDelegator.createValue("QRTZTriggers", values);
    }
}
