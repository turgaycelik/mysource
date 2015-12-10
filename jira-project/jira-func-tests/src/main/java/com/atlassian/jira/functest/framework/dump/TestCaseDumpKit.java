package com.atlassian.jira.functest.framework.dump;

import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.meterware.httpunit.CurrentHttpInformation;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;

import java.util.Date;
import java.util.Map;

/**
 * A class that can dump information about a failed test case
 *
 * @since v4.0
 */
public class TestCaseDumpKit
{
    public static void dumpTestInformation(TestCase testCase, Date when, Throwable thrownException)
    {
        dumpTestInformation(testCase, when, thrownException, true);
    }

    public static void dumpTestInformation(TestCase testCase, Date when, Throwable thrownException, boolean dumpHtml)
    {
        dumpTestInformation(testCase, when, thrownException, dumpHtml, ArtifactDumper.getTester(testCase));
    }

    public static void dumpTestInformation(TestCase testCase, Date when, Throwable thrownException, boolean dumpHtml, WebTester tester)
    {
        FuncTestLoggerImpl log = new FuncTestLoggerImpl();
        log.log("\n______ TEST FAILURE ______ \n");

        String responseText = null;
        if (tester == null || tester.getDialog() == null)
        {
            log.log("The WebTester does not have a dialog associated with it?");
        }
        else
        {
            responseText = tester.getDialog().getResponseText();
        }

        log.log(thrownException);
        if (dumpHtml)
        {
            log.log("______________________________ Starting HTML dump");

            final CurrentHttpInformation.Info httpInfo = CurrentHttpInformation.getInfo();
            if (httpInfo != null)
            {
                log.log("URL              : " + httpInfo.getUrl());
                log.log("HTTP METHOD      : " + httpInfo.getRequestMethod());
                log.log("HTTP SC          : " + httpInfo.getStatusCode());
                log.log("HTTP MSG         : " + httpInfo.getStatusMessage());

                dumpHeaders(log, "Request", httpInfo.getRequestHeaders());
                dumpHeaders(log, "Response", httpInfo.getResponseHeaders());
            }
            dumpBody(log,responseText);

            log.log("______________________________ Ending HTML dump \n");

            // dump an artifact for later examination
            new ArtifactDumper(testCase, thrownException, when, log);
        }
    }

    private static void dumpHeaders(final FuncTestLoggerImpl log, String typeOfHeaders, final Map<String, String[]> headers)
    {
        log.log("" + typeOfHeaders + " Headers : ");
        log.log("________________");
        for (String headerName : headers.keySet())
        {
            StringBuilder sb = new StringBuilder();
            String[] values = headers.get(headerName);
            for (String value : values)
            {
                sb.append("   ").append(headerName).append(" :").append(" ").append(value);
            }
            log.log(sb);
        }
    }

    private static void dumpBody(final FuncTestLoggerImpl log, final String responseText)
    {
        log.log("Last Successful Response Body : ");
        log.log("________________");
        log.log(responseText.trim());
    }
}
