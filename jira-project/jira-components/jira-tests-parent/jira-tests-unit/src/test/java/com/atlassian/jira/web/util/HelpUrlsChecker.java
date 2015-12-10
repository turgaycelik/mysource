package com.atlassian.jira.web.util;

import com.atlassian.jira.help.HelpUrl;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.StaticHelpUrls;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utility class for checking that the JIRA help links are not broken.
 */
public class HelpUrlsChecker
{
    // run this from IDEA to check all help links
    public static void main(String[] args) throws Exception
    {
        new HelpUrlsChecker().checkHelpUtilLinks();
    }

    /**
     * Thread local HttpClient.
     */
    private final ThreadLocal<HttpClient> httpClient = new ThreadLocal<HttpClient>()
    {
        @Override
        protected HttpClient initialValue()
        {
            return new HttpClient();
        }
    };

    /**
     * Number of threads to use for checking links. Y U NO ASYNC IO?!
     */
    private final int nThreads = 20;

    /**
     * Thread pool to use for link checking.
     */
    private ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);

    @Test
    @Ignore
    public void checkHelpUtilLinks() throws Exception
    {
        final long start = System.currentTimeMillis();

        // check BTF
        HelpUrls helpUtil = load();
        List<CheckResult> allLinks = followLinksAndReportErrors(helpUtil);
        Collection<CheckResult> brokenLinks = Collections2.filter(allLinks, new FailedCheck());

        // kill threads before exiting
        threadPool.shutdown();
        System.err.flush();
        System.out.flush();

        Period elapsed = new Period(System.currentTimeMillis() - start);
        System.out.format("Found %d broken links in %s (checked %d in total).\n", brokenLinks.size(), formatter().print(elapsed), allLinks.size());
        System.exit(brokenLinks.isEmpty() ? 0 : 1);
    }

    private List<CheckResult> followLinksAndReportErrors(final HelpUrls helpUtil) throws Exception
    {
        List<CheckResult> followedLinks = Lists.newArrayList();
        List<Future<CheckResult>> urlChecks = checkLinksInThreadPool(helpUtil);
        for (Future<CheckResult> urlCheck : urlChecks)
        {
            CheckResult result = urlCheck.get();
            if (result.status != 200)
            {
                System.err.format("%s returned %d -> [%s]\n", result.helpPath.getKey(), result.status, result.helpPath.getUrl());
            }

            followedLinks.add(result);
        }

        return followedLinks;
    }

    private List<Future<CheckResult>> checkLinksInThreadPool(final HelpUrls urls)
    {
        final List<Future<CheckResult>> futures = Lists.newArrayList();

        // schedule a job for each link
        for (final String key : urls.getUrlKeys())
        {
            futures.add(threadPool.submit(new Callable<CheckResult>()
            {
                @Override
                public CheckResult call() throws Exception
                {
                    HelpUrl helpLink = urls.getUrl(key);
                    GetMethod get = new GetMethod(helpLink.getUrl());
                    try
                    {
                        int resp = httpClient.get().executeMethod(get);
                        IOUtils.copy(get.getResponseBodyAsStream(), NullOutputStream.NULL_OUTPUT_STREAM);

                        return new CheckResult(resp, helpLink);
                    }
                    catch (Exception e)
                    {
                        return new CheckResult(-1, helpLink);
                    }
                    finally
                    {
                        get.releaseConnection();
                    }
                }
            }));
        }

        return futures;
    }

    private HelpUrls load()
    {
        return StaticHelpUrls.getInstance();
    }

    private PeriodFormatter formatter()
    {
        return new PeriodFormatterBuilder()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .appendSeconds().appendSuffix("s")
                .toFormatter();
    }

    private static class CheckResult
    {
        public final int status;
        public final HelpUrl helpPath;

        private CheckResult(final int status, final HelpUrl helpPath)
        {
            this.status = status;
            this.helpPath = helpPath;
        }
    }

    private static class FailedCheck implements Predicate<CheckResult>
    {
        @Override
        public boolean apply(CheckResult result)
        {
            return result.status != 200;
        }
    }
}
