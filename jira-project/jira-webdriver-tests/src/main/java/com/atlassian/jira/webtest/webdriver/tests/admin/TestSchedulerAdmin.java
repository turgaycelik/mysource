package com.atlassian.jira.webtest.webdriver.tests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.admin.SchedulerAdminPage;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;

import com.google.common.collect.Iterables;

import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION })
public class TestSchedulerAdmin extends BaseJiraWebTest
{
    @Before
    public void setUp()
    {
        backdoor.restoreBlankInstance();
    }

    @Test
    public void smokeTestForSchedulerAdmin()
    {
        SchedulerAdminPage schedulerAdmin = jira.quickLoginAsAdmin(SchedulerAdminPage.class);


        final TimedQuery<Iterable<SchedulerAdminPage.JobRunnerRow>> jobRunnersQuery = schedulerAdmin.getJobRunners();
        Poller.waitUntil(jobRunnersQuery, IsIterableWithSize.<SchedulerAdminPage.JobRunnerRow>iterableWithSize(3));

        final Iterable<SchedulerAdminPage.JobRunnerRow> jobRunners = jobRunnersQuery.now();

        final SchedulerAdminPage.JobRunnerRow jobRunnerRow = Iterables.get(jobRunners, 0);
        assertThat(jobRunnerRow.getJobRunnerKey(), equalTo("com.atlassian.jira.service.DefaultServiceManager"));
        assertThat(jobRunnerRow.getSchedule(), equalTo("interval"));
        assertThat(jobRunnerRow.getNumberOfJobs(), equalTo(2));

        jobRunnerRow.showDetails();

        final TimedQuery<Iterable<SchedulerAdminPage.JobDetailsRow>> jobDetailsQuery = schedulerAdmin.getJobDetails(jobRunnerRow.getId());
        Poller.waitUntil(jobDetailsQuery, IsIterableWithSize.<SchedulerAdminPage.JobDetailsRow>iterableWithSize(2));

        final SchedulerAdminPage.JobDetailsRow jobDetails = Iterables.get(jobDetailsQuery.now(), 0);
        assertThat(jobDetails.getType(), equalTo("Runnable"));
        assertThat(jobDetails.getParameters(), equalTo("{com.atlassian.jira.service.ServiceManager:serviceId=10000}"));
        assertThat(jobDetails.getRunMode(), equalTo("locally"));
        assertThat(jobDetails.getMessage(), equalTo(""));

        jobRunnerRow.hideDetails();
        Poller.waitUntilFalse(schedulerAdmin.isShowingJobDetails());

    }
}
