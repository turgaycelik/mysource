package com.atlassian.jira.memoryinspector;

import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMemoryInspector
{
    public static final String LOG_CAPTURE_APPENDER = "log-capture";
    private final Logger capturingLogger = Logger.getLogger(TestMemoryInspector.class);
    private final WriterAppender capturingAppender = new WriterAppender();
    private StringWriter logWriter;

    @Before
    public void setup() {
        logWriter = new StringWriter();

        capturingAppender.setName(LOG_CAPTURE_APPENDER);
        capturingAppender.setLayout(new SimpleLayout());
        capturingAppender.setWriter(logWriter);

        capturingLogger.setLevel(Level.ALL);
        capturingLogger.removeAllAppenders();
        capturingLogger.addAppender(capturingAppender);
    }

    @Test
    public void testMemoryInspectorShouldRunAllInspectorsAndPrintReportsForFailedInspections() throws Exception
    {
        final List<Thread> allThreadsList = ImmutableList.of(mock(Thread.class));

        final ThreadsProvider threadsProvider = mock(ThreadsProvider.class);
        when(threadsProvider.getAllThreads()).thenReturn(allThreadsList);

        final MockInspectionReport threadsInspectionReport = new MockInspectionReport("<THREADS REPORT>", false);

        final ThreadsInspector threadsInspector = mock(ThreadsInspector.class);
        when(threadsInspector.inspectThreads(eq(allThreadsList))).thenReturn(threadsInspectionReport);

        final MemoryInspector memoryInspector = new MemoryInspector(threadsProvider, threadsInspector);

        memoryInspector.inspectMemoryAfterJiraShutdownWithRetries(capturingLogger, 0, Period.ZERO);

        logWriter.flush();
        final String loggedMessages = logWriter.getBuffer().toString();
        final String expectedMessages = ""
                + "WARN - " + MemoryInspector.MEMORY_INSPECTION_FAILED_HEADER + "\n"
                + "WARN - " + threadsInspectionReport.getReportBody() + "\n"
                + "WARN - " + MemoryInspector.MEMORY_INSPECTION_FAILED_FOOTER + "\n";

        assertEquals(expectedMessages, loggedMessages);
    }

    public static class MockInspectionReport implements InspectionReport {

        private final String reportBody;
        private final Boolean inspectionResult;

        public MockInspectionReport(final String reportBody, final Boolean inspectionResult) {
            this.reportBody = reportBody;
            this.inspectionResult = inspectionResult;
        }

        @Override
        public void printReport(final Logger log)
        {
            log.warn(reportBody);
        }

        @Override
        public boolean inspectionPassed()
        {
            return inspectionResult;
        }

        public String getReportBody()
        {
            return reportBody;
        }
    }
}