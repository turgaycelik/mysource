package com.atlassian.jira.pageobjects.pages.admin;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;

import com.google.common.base.Supplier;

import org.openqa.selenium.By;

/**
 * @since v6.3
 */
public class SchedulerAdminPage extends AbstractJiraAdminPage
{
    @Inject
    protected ExtendedElementFinder extendedFinder;

    @ElementBy(className = "page-scheduler-admin")
    PageElement body;

    @ElementBy(cssSelector = "table.runners")
    PageElement runnersTable;

    @Override
    public String linkId()
    {
        return "scheduler_details";
    }

    @Override
    public TimedCondition isAt()
    {
        return body.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/SchedulerAdmin.jspa";
    }

    public TimedQuery<Iterable<JobRunnerRow>> getJobRunners()
    {
        return Queries.forSupplier(timeouts, new Supplier<Iterable<JobRunnerRow>>()
        {
            @Override
            public Iterable<JobRunnerRow> get()
            {
                return PageElements.transform(pageBinder, runnersTable.findAll(By.cssSelector("tr.runner")), JobRunnerRow.class);
            }
        });
    }

    public TimedQuery<Iterable<JobDetailsRow>> getJobDetails(final String runnerId)
    {
        return Queries.forSupplier(timeouts, new Supplier<Iterable<JobDetailsRow>>()
        {
            @Override
            public Iterable<JobDetailsRow> get()
            {
                return PageElements.transform(pageBinder, runnersTable.findAll(By.cssSelector("tr.job-details[data-runner-id='" + runnerId + "']")), JobDetailsRow.class);
            }
        });
    }

    public TimedQuery<Boolean> isShowingJobDetails()
    {
        return elementFinder.find(By.className("job-details")).timed().isVisible();
    }

    public static class JobDetailsRow
    {
        @Inject
        PageBinder pageBinder;

        private final PageElement row;
        private final PageElement details;

        public JobDetailsRow(final PageElement element)
        {
            this.row = element;
            this.details = element.find(By.className("details"));
        }

        public String getType()
        {
            return details.find(By.className("type")).getText().trim();
        }

        public String getParameters()
        {
            return details.find(By.className("parameters")).getText().trim();
        }

        public String getRunMode()
        {
            return details.find(By.className("run-mode")).getText().trim();
        }

        public String getMessage()
        {
            return details.find(By.className("last-run-message")).getText().trim();
        }
    }

    public static class JobRunnerRow
    {
        @Inject
        PageBinder pageBinder;

        private final PageElement row;

        public JobRunnerRow(final PageElement element)
        {
            this.row = element;
        }

        public String getId()
        {
            return row.getAttribute("data-runner-id");
        }

        public String getJobRunnerKey()
        {
            return row.find(By.cssSelector("td.runner-key p")).getText().trim();
        }

        public String getSchedule()
        {
            return row.find(By.cssSelector("td.schedule span")).getText().trim();
        }

        public int getNumberOfJobs()
        {
            return Integer.valueOf(row.find(By.cssSelector("td.number-of-jobs")).getAttribute("data-number-of-jobs"));
        }

        public JobRunnerRow showDetails()
        {
            row.find(By.className("show-details")).click();
            return this;
        }

        public JobRunnerRow hideDetails()
        {
            row.find(By.className("show-details")).click();
            return this;
        }
    }
}
