package com.atlassian.jira.functest.framework.dump;

import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Listens for suite events and dumps timings to a file
 *
 * @since v4.3
 */
public class FuncTestTimingsListener implements WebTestListener
{
    @Override
    public void suiteStarted(final WebTestDescription suiteDescription)
    {
    }

    @Override
    public void suiteFinished(final WebTestDescription suiteDescription)
    {
        List<TestInformationKit.TestCaseInfo> allTestsRun = TestInformationKit.getAllTestsRun();
        Collections.sort(allTestsRun);

        File parent = new File("target/test-reports");
        parent.mkdirs();

        File report = new File(parent, "timings-report.xml");
        System.out.printf("Writing timings file to '%s'\n", report.getAbsolutePath());
        try
        {
            PrintStream reportOut = printReportPreamble(report);
            printTestCaseInfo(reportOut, TestInformationKit.getTotals(), Collections.<TestInformationKit.TestCaseCounter>emptyList());
            for (TestInformationKit.TestCaseInfo testCaseInfo : allTestsRun)
            {
                List<TestInformationKit.TestCaseCounter> countersForTest = TestInformationKit.getCountersForTest(testCaseInfo.getTestName());
                printTestCaseInfo(reportOut, testCaseInfo, countersForTest);
            }

            printReportFinalyArguments(reportOut);
        }
        catch (IOException e)
        {
            System.out.printf("Cant open or write to timing report file :" + report + "\n");
        }
    }

    private void printTestCaseInfo(PrintStream reportOut, TestInformationKit.TestCaseInfo testCaseInfo, final List<TestInformationKit.TestCaseCounter> countersForTest)
    {
        reportOut.printf("\t\t<testCase>\n");
        reportOut.printf("\t\t\t<name>%s</name>\n", testCaseInfo.getTestName());
        reportOut.printf("\t\t\t<httprequestcount>%d</httprequestcount>\n", testCaseInfo.getHttpRequestCount());
        reportOut.printf("\t\t\t<runtimeMS>%d</runtimeMS>\n", testCaseInfo.getRuntimeMS());
        reportOut.printf("\t\t\t<httptimeMS>%d</httptimeMS>\n", testCaseInfo.getHttpTimeMS());
        reportOut.printf("\t\t\t<http50thMS>%d</http50thMS>\n", testCaseInfo.getHttp50thMS());
        reportOut.printf("\t\t\t<http90thMS>%d</http90thMS>\n", testCaseInfo.getHttp90thMS());
        reportOut.printf("\t\t\t<http100thMS>%d</http100thMS>\n", testCaseInfo.getHttp100thMS());
        reportOut.printf("\t\t\t<msperrequest>%f</msperrequest>\n", testCaseInfo.getMsPerRequest());
        reportOut.printf("\t\t\t<parseTimeMS>%d</parseTimeMS>\n", testCaseInfo.getParseTimeMS());
        reportOut.printf("\t\t\t<parseCount>%d</parseCount>\n", testCaseInfo.getParseCount());
        reportOut.printf("\t\t\t<counters>\n");
        for (TestInformationKit.TestCaseCounter counter : countersForTest)
        {
            reportOut.printf("\t\t\t\t<counter>\n");
            reportOut.printf("\t\t\t\t\t<name>%s</name>\n", counter.getName());
            reportOut.printf("\t\t\t\t\t<value>%f</value>\n", counter.getValue());
            reportOut.printf("\t\t\t\t</counter>\n");
        }
        reportOut.printf("\t\t\t</counters>\n");
        reportOut.printf("\t\t</testCase>\n");
        reportOut.flush();
    }

    private PrintStream printReportPreamble(File report) throws IOException
    {
        PrintStream reportOut = new PrintStream(new FileOutputStream(report));
        reportOut.printf("<!-- Timings produced on %1$td %1$tb %1$ty-->\n", new Date());
        reportOut.printf("<timings>\n");
        reportOut.printf("\t<testCases>\n");
        return reportOut;
    }


    private void printReportFinalyArguments(PrintStream reportOut)
    {
        reportOut.printf("\t</testCases>\n");
        reportOut.printf("</timings>\n");
        reportOut.close();
    }

    @Override
    public void testError(WebTestDescription test, Throwable t)
    {
    }

    @Override
    public void testFailure(WebTestDescription test, Throwable t)
    {
    }

    @Override
    public void testFinished(WebTestDescription test)
    {
    }

    @Override
    public void testStarted(WebTestDescription test)
    {
    }
}
