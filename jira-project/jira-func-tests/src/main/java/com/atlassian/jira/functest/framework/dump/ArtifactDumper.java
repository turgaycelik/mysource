package com.atlassian.jira.functest.framework.dump;

import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.atlassian.jira.testkit.client.log.MavenEnvironment;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FuncTestWebClientListener;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.CurrentHttpInformation;
import com.opensymphony.util.TextUtils;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ArtifactDumper is used to generate "artifact" HTML files which show the state of a web page at the time a test
 * fails.  If the browser is known to the
 *
 * @since v3.13
 */
public class ArtifactDumper
{
    private final Throwable failureCause;
    private final JIRAEnvironmentData environmentData;
    private final FuncTestLogger logger;
    private static AtomicInteger dumpCount = new AtomicInteger(0);
    private static final int DEFAULT_MAX_DUMPCOUNT = 5;

    /**
     * This will dump artifact files out to a well know location
     *
     * @param testCase     the test case in play
     * @param failureCause the Throwable that caused the test to fail
     * @param date         the date the event happened
     * @param logger       the FuncTestLogger to use
     */
    public ArtifactDumper(TestCase testCase, Throwable failureCause, Date date, FuncTestLogger logger)
    {
        this.failureCause = failureCause;
        this.environmentData = getEnviromentData(testCase);
        this.logger = logger == null ? new FuncTestLoggerImpl() : logger;
        dumpToFileAndLaunchBrowserMaybe(testCase, date);
    }

    public static WebTester getTester(final TestCase testCase)
    {
        if (testCase instanceof JIRAWebTest)
        {
            return ((JIRAWebTest) testCase).getTester();
        }
        if (testCase instanceof FuncTestCase)
        {
            return ((FuncTestCase) testCase).getTester();
        }
        throw new UnsupportedOperationException("We dont supported this class of TestCase " + testCase.getClass().getName() + " : " + testCase.getName());
    }

    public static JIRAEnvironmentData getEnviromentData(final TestCase testCase)
    {
        if (testCase instanceof JIRAWebTest)
        {
            return ((JIRAWebTest) testCase).getEnvironmentData();
        }
        if (testCase instanceof FuncTestCase)
        {
            return ((FuncTestCase) testCase).getEnvironmentData();
        }
        throw new UnsupportedOperationException("We dont supported this class of TestCase " + testCase.getClass().getName() + " : " + testCase.getName());
    }

    public static FuncTestWebClientListener getFuncTestWebClientListener(final TestCase testCase)
    {
        if (testCase instanceof JIRAWebTest)
        {
            return ((JIRAWebTest) testCase).getWebClientListener();
        }
        if (testCase instanceof FuncTestCase)
        {
            return ((FuncTestCase) testCase).getWebClientListener();
        }
        throw new UnsupportedOperationException("We dont supported this class of TestCase " + testCase.getClass().getName() + " : " + testCase.getName());
    }


    /**
     * Looks in localtest.properties for "browser.path" and if its present it dumps the response data to a html file and
     * launches a browser on it. This doesn't do anything if it's not set.
     *
     * @param testCase    the TestCase in play
     * @param currentDate the time the event happened
     */
    private void dumpToFileAndLaunchBrowserMaybe(TestCase testCase, Date currentDate)
    {
        String printedStackTrace = "";
        if (failureCause != null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            failureCause.printStackTrace(pw);
            printedStackTrace = sw.toString();
        }

        String captureDirectory = MavenEnvironment.getMavenAwareOutputDir();
        File captureDir = new File(captureDirectory);
        if (!captureDir.exists())
        {
            if (!captureDir.mkdirs())
            {
                logger.log("Couldnt capture test artifacts to : " + captureDir.getAbsolutePath());
                return;
            }
        }

        List<String> lastResponses = getFuncTestWebClientListener(testCase).getLastResponses();
        File captureFile = null;
        int i = 0;
        String captureFileNamePrefix = getFailureFileName(currentDate, testCase);
        for (String lastResponse : lastResponses)
        {
            File prevFileName = null;
            File nextFileName = null;

            String captureFileName = captureFileNamePrefix + "_" + i + ".html";
            if (i > 0)
            {
                prevFileName = new File(captureDir, captureFileNamePrefix + "_" + (i-1) + ".html");
            }
            if (i < lastResponses.size()-1)
            {
                nextFileName = new File(captureDir, captureFileNamePrefix + "_" + (i+1) + ".html");
            }

            File captureFileN = new File(captureDir, captureFileName);

            StringBuilder sb = new StringBuilder();
            int bodyCloseIndex = lastResponse.toLowerCase().indexOf("</body");
            if (bodyCloseIndex == -1)
            {
                lastResponse = "<!--"
                        + "Original Response Text Follows:\n" 
                        + "________________\n"
                        + lastResponse
                        + "________________\n"
                        + "-->\n" +
                        "<html><body>The response does not seem to HTML.  This is a generated message to tell you that</body></html>";
            }

            bodyCloseIndex = lastResponse.toLowerCase().indexOf("</body");
            if (bodyCloseIndex != -1)
            {

                sb.append(lastResponse.substring(0, bodyCloseIndex));
                sb.append(buildPageNavigation(printedStackTrace, i, prevFileName, nextFileName));
                sb.append(lastResponse.substring(bodyCloseIndex));

                lastResponse = sb.toString();
            }
            writeToCaptureFile(lastResponse, captureFileN);

            if (i == 0)
            {
                captureFile = captureFileN;
            }
            i++;
        }

        if (captureFile != null)
        {
            openInBrowser(captureFile);
        }
    }

    private void writeToCaptureFile(final String htmlResponse, final File captureFile)
    {
        //
        // write out the htmlResponse
        PrintWriter pw;
        try
        {
            pw = new PrintWriter(new FileWriter(captureFile));
            pw.print(htmlResponse);
            pw.close();
        }
        catch (IOException e)
        {
            logger.log("Couldnt capture test artifact to : " + captureFile.getAbsolutePath());
        }
    }

    private void openInBrowser(final File captureFile)
    {
        final int currentCount = dumpCount.incrementAndGet();

        int maxCount;
        try
        {
            maxCount = Integer.parseInt(environmentData.getProperty("browser.maxopen"));
        }
        catch (NumberFormatException e)
        {
            maxCount = DEFAULT_MAX_DUMPCOUNT;
        }

        if ((maxCount < 0) || (currentCount <= maxCount))
        {
            openBrowser(environmentData.getProperty("browser.path"), captureFile, logger);
        }
    }

    public static void openBrowser(String command, final File captureFile, FuncTestLogger logger)
    {
        // if the command is empty don't do anything.
        // on Bamboo it wont launch anything for example.
        if (command == null || command.trim().length() == 0)
        {
            logger.log("A test artifact file has been created here : " + captureFile.getAbsolutePath());
            return;
        }

        // run command
        String[] commandWithArgs;
        if (command.indexOf(" ") != -1)
        {
            String[] bcmd = command.split(" ");
            commandWithArgs = new String[bcmd.length + 1];
            //copy all the args from the command string and combine them with the capturefile as path.
            System.arraycopy(bcmd, 0, commandWithArgs, 0, bcmd.length);
        }
        else
        {
            commandWithArgs = new String[2];
            commandWithArgs[0] = command;
        }

        commandWithArgs[commandWithArgs.length - 1] = captureFile.getAbsolutePath();
        try
        {
            logger.log("Executing command for failing web response - command : " + command + " - captured test artifact : file://" + captureFile.getAbsolutePath());
            Runtime.getRuntime().exec(commandWithArgs);
        }
        catch (IOException e)
        {
            logger.log(String.format("Failed to execute command : %s with captured test artifact 'file://%s': %s", command, captureFile.getAbsolutePath(), e.getMessage()));
        }
    }

    private String buildPageNavigation(String printedStackTrace, final int fileIndex, final File prevFile, final File nextFile)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<script>\n");
        sb.append("function setText(el, text) { while (el.firstChild) { el.removeChild(el.firstChild) } el.appendChild(document.createTextNode(text));  }\n");
        sb.append("function stackTraceShrink() { eShrinkButton = document.getElementById('shrinkButton'); el = document.getElementById('stackTraceDiv'); if (el.className == 'stackTraceDiv') { el.className = 'stackTraceDivShrink'; setText(eShrinkButton,'Expand');  } else { el.className = 'stackTraceDiv'; setText(eShrinkButton,'Shrink'); } }\n");
        sb.append("function stackTraceHide() { el = document.getElementById('stackTraceDiv'); el.style.display = 'none'; }\n");
        sb.append("</script>");

        sb.append("<style>");
        sb.append(".stackTraceDiv {");
        sb.append("width:90%;height:95%;position:absolute;right:5px;top:30px;overflow:scroll;border-style:solid;border-width:1px;border-color:black;background-color:#FFFFE5;");
        sb.append("} \n");

        sb.append(".stackTraceDivShrink {");
        sb.append("width:40%;height:80px;position:absolute;right:5px;top:30px;overflow:scroll;border-style:solid;border-width:1px;border-color:black;background-color:#FFFFE5;");
        sb.append("} \n");

        sb.append(".stackTraceButtonDiv {");
        sb.append("position:absolute; right : 5px; top : 5px;");
        sb.append("} \n");

        sb.append(".stackTraceButton {");
        sb.append("border : 1px orange solid; margin : 1px; padding : 1px; cursor : pointer; background-color : blue; color : white; ");
        sb.append("} \n");

        sb.append(".stackTraceButton a {");
        sb.append("background-color : blue; color : white; ");
        sb.append("} \n");

        sb.append("</style>");

        // outer containing div
        String shrinkText = "Shrink";
        if (fileIndex == 0)
        {
            sb.append("<div id=\"stackTraceDiv\" class=\"stackTraceDiv\"> \n");
        } else {
            sb.append("<div id=\"stackTraceDiv\" class=\"stackTraceDivShrink\"> \n");
            shrinkText = "Expand";
        }
        if (fileIndex == 0)
        {
            sb.append("<h1 style=\"color:red\">Tester Response : " + fileIndex + "</h1> \n");
        } else {
            sb.append("<h3>Tester Response : " + (-fileIndex) + "</h3> \n");

        }

        CurrentHttpInformation.Info httpInfo = CurrentHttpInformation.getInfo();
        if (httpInfo != null)
        {
            sb.append("<table><tr><td><a target=\"_newWindow\"href=\"").append(httpInfo.getUrl()).append("\">").append(httpInfo.getUrl()).append("</a></td></tr></table>\n");
        }

        // buttons
        sb.append("<div class=\"stackTraceButtonDiv\"> \n");
        sb.append("<table>\n");
        sb.append("<tr> \n");
        sb.append("<td><div id=\"shrinkButton\" class=\"stackTraceButton\" onclick=\"stackTraceShrink(this)\">" + shrinkText + "</div></td> \n");
        sb.append("<td><div class=\"stackTraceButton\" onclick=\"stackTraceHide(this)\">Hide</div></td> \n");
        sb.append("</tr>\n");
        sb.append("<tr> \n");

        // the list is in reverse so we we call next file is in fact backwards in time
        sb.append("<td>");
        if (nextFile != null)
        {
            sb.append("<div class=\"stackTraceButton\"><a href=\"file://" + nextFile.getAbsolutePath() + "\" >Prev</a></div>");
        }
        sb.append("</td>\n");
        sb.append("<td>");
        if (prevFile != null)
        {
            sb.append("<div class=\"stackTraceButton\"><a href=\"file://" + prevFile.getAbsolutePath() + "\" >Next</a></div>");
        }
        sb.append("</td>\n");

        sb.append("</tr>\n");
        sb.append("</table> \n");
        sb.append("</div> \n");

        // stack trace pre
        sb.append("<pre>");
        sb.append(higlightStackTrace(printedStackTrace));

        if (httpInfo != null)
        {
            sb.append("\n");
            sb.append("HTTP URL    : ").append(httpInfo.getUrl()).append("\n");
            sb.append("HTTP METHOD : ").append(httpInfo.getRequestMethod()).append("\n");
            sb.append("HTTP SC     : ").append(httpInfo.getStatusCode()).append("\n");
            sb.append("HTTP MSG    : ").append(httpInfo.getStatusMessage()).append("\n");

            sb.append(printHeaders("Request", httpInfo.getRequestHeaders()));
            sb.append(printHeaders("Response", httpInfo.getResponseHeaders()));
        }

        sb.append("</pre>");
        sb.append("</div>");


        return sb.toString();
    }

    private Object printHeaders(final String headerTypes, final Map<String, String[]> responseHeaders)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(headerTypes).append(" Headers\n");
        for (String headerName : responseHeaders.keySet())
        {
            String[] values = responseHeaders.get(headerName);
            for (String value : values)
            {
                sb.append("   ").append(headerName).append(":").append(value).append("\n");
            }
        }
        return sb;
    }

    private String getFailureFileName(Date currentDate, TestCase testCase)
    {
        String testName;
        if (testCase == null)
        {
            testName = "UnknownTestName.cantFindInStackTrace";
        }
        else
        {
            testName = testCase.getClass().getName() + "." + testCase.getName();
        }

        // try and work out the name of the test that failed
        return testName + "_" + new SimpleDateFormat("yyyy-MM-dd--hh-mm-ss-SSSS").format(currentDate);
    }

    private String higlightStackTrace(String printedStackTrace)
    {
        StringBuilder sb = new StringBuilder();
        // go back into the stack trace util we find a line that starts with "testXXX"
        StringReader sr = new StringReader(printedStackTrace);
        BufferedReader br = new BufferedReader(sr);
        String line;
        String testName = null;
        try
        {
            while ((line = br.readLine()) != null)
            {
                // make sure its html kosher
                line = TextUtils.htmlEncode(line);
                boolean append = true;
                // hi-light only once
                if (testName == null)
                {
                    testName = getTestAndClassName(line);
                    if (testName != null)
                    {
                        sb.append("<strong>");
                        sb.append(line);
                        sb.append("</strong>");
                        append = false;
                    }
                }
                if (append)
                {
                    sb.append(line);
                }
                sb.append("\n");
            }
        }
        catch (IOException e)
        {
            // cant happen
        }
        return sb.toString();
    }


    /**
     * Returns null if it can find the name of a test in an exception stack tarce line or non null if it can
     *
     * @param line the line of strack tarce to look in
     *
     * @return the test class and test method name
     */
    private String getTestAndClassName(String line)
    {
        String testName = null;
        line = line.trim();
        if (line.startsWith("at"))
        {
            // find the first (
            int openingBracket = line.indexOf('(');
            if (openingBracket != -1)
            {
                // jump back to previous .
                int dotIndex = line.lastIndexOf('.', openingBracket);
                if (dotIndex != -1)
                {
                    String methodName = line.substring(dotIndex + 1, openingBracket);
                    if (methodName.indexOf("test") == 0)
                    {
                        testName = methodName;
                        // now try and find the class.  dont be require it however
                        int classDotIndex = line.lastIndexOf('.', dotIndex - 1);
                        if (classDotIndex != -1)
                        {
                            String className = line.substring(classDotIndex + 1, dotIndex);
                            testName = className + "." + testName;
                        }
                    }
                }
            }
        }
        return testName;
    }

}
