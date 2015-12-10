package com.atlassian.jira.memoryinspector;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;
import org.joda.time.Period;

/**
 * This class tries to find known memory issues (e.g. memory leaks).
 *
 * @since v6.3
 */
public class MemoryInspector
{
    @VisibleForTesting
    static final String MEMORY_INSPECTION_FAILED_HEADER = "----------------------- MEMORY INSPECTION FAILED -------------------------------------";
    @VisibleForTesting
    static final String MEMORY_INSPECTION_FAILED_FOOTER = "______________________________________________________________________________________";
    private static final Logger log = Logger.getLogger(MemoryInspector.class);

    private static final Predicate<InspectionReport> REPORT_PASSED_PREDICATE = new Predicate<InspectionReport>()
    {
        @Override
        public boolean apply(final InspectionReport report)
        {
            return report.inspectionPassed();
        }
    };

    private final ThreadsProvider threadsProvider;
    private final ThreadsInspector threadsInspector;

    public MemoryInspector()
    {
        this(new ThreadsProviderImpl(), new ThreadsInspectorImpl());
    }

    public MemoryInspector(final ThreadsProvider threadsProvider, final ThreadsInspector threadsInspector)
    {
        this.threadsProvider = threadsProvider;
        this.threadsInspector = threadsInspector;
    }

    public void inspectMemoryAfterJiraShutdown() {
        // This method should only delegate call to method that can be unit-tested
        inspectMemoryAfterJiraShutdownWithRetries(log, 100, Period.millis(100));
    }

    @VisibleForTesting
    void inspectMemoryAfterJiraShutdownWithRetries(final Logger log, final int retries, final Period waitTime)
    {
        final List<InspectionReport> inspectionReports = executeAllInspectionsWithTimeout(retries, waitTime);

        if (!allInspectionsPassed(inspectionReports)) {
            // some inspection failed, print report
            log.warn(MEMORY_INSPECTION_FAILED_HEADER);
            for (final InspectionReport inspectionReport : inspectionReports)
            {
                if (!inspectionReport.inspectionPassed())
                {
                    inspectionReport.printReport(log);
                }
            }
            log.warn(MEMORY_INSPECTION_FAILED_FOOTER);
        }
    }

    private List<InspectionReport> executeAllInspectionsWithTimeout(final int retries, final Period waitTime)
    {
        Preconditions.checkArgument(retries >= 0, "Retries cannot be negative! Given value: %s", retries);

        int remainingRetires = retries;
        while (remainingRetires-- >= 0)
        {
            final List<InspectionReport> inspectionReports = ImmutableList.of(threadsInspector.inspectThreads(threadsProvider.getAllThreads()));
            if (allInspectionsPassed(inspectionReports) || (remainingRetires <= 0))
            {
                // passed / no more retries
                return inspectionReports;
            }
            sleep(waitTime);
        }

        throw new RuntimeException("I did not expected to end here.");
    }

    private void sleep(final Period sleepTime)
    {
        try
        {
            Thread.sleep(sleepTime.getMillis());
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private boolean allInspectionsPassed(final List<InspectionReport> inspectionReports)
    {
        return Iterables.all(inspectionReports, REPORT_PASSED_PREDICATE);
    }
}
