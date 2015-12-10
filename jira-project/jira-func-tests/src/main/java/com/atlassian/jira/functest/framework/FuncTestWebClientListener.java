package com.atlassian.jira.functest.framework;

import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebClientListener;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This counts and execution times the web requests
 *
 * @since v4.0
 */
public class FuncTestWebClientListener implements WebClientListener
{
    private static final int MAX_PREV_PAGES_KEPT = 5;
    private static final String REQUEST_START_TIME = "request.start.time";

    long requestCount = 0;
    long requestTime = 0;
    long parseCount = 0;
    long parseTimeNanos = 0;
    List<Long> requestTimeList = new ArrayList<Long>(100);
    final LifoList<String> previousPages = new LifoList<String>(MAX_PREV_PAGES_KEPT);

    public void requestSent(final WebClient src, final WebRequest req)
    {
        requestCount++;
        src.setAttribute(REQUEST_START_TIME, new Date());
    }

    public void responseReceived(final WebClient src, final WebResponse resp)
    {
        Date then = (Date) src.getAttribute(REQUEST_START_TIME);
        if (then != null)
        {
            src.setAttribute(REQUEST_START_TIME, null);

            long timeMS = new Date().getTime() - then.getTime();
            requestTime += timeMS;
            requestTimeList.add(timeMS);
        }
        squirrelAwayTheResponse(resp);
    }

    public void responseParsed(WebClient src, WebResponse resp, long parseNanos)
    {
        // has any parsing gone on before we got here
        if (parseCount == 0)
        {
            parseTimeNanos = src.getParseTimeNanos();
            parseCount = src.getParseCount();
        }
        parseTimeNanos += parseNanos;
        parseCount++;
    }

    private void squirrelAwayTheResponse(final WebResponse resp)
    {
        if (!isError(resp))
        {
            try
            {
                synchronized (previousPages)
                {
                    previousPages.offer(resp.getText());
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to read the web response text");
            }
        }
    }

    private boolean isError(final WebResponse resp)
    {
        return resp.getResponseCode() >= 400;
    }

    /**
     * We keep the previous response around in memory to help with problem diagnostics
     *
     * @return a list of previous response if there are any other wise an EMPTY LIST
     */
    public List<String> getLastResponses()
    {
        List<String> list = new ArrayList<String>();
        synchronized (previousPages)
        {
            for (String previousPage : previousPages)
            {
                list.add(previousPage);
            }
        }
        return list;
    }

    /**
     * @return The number of HTTP web requests that have been made
     */
    public long getRequestCount()
    {
        return requestCount;
    }

    /**
     * @return the time take in web requests
     */

    public long getRequestTime()
    {
        return requestTime;
    }

    /**
     * @return the number of parse invocations
     */
    public long getParseCount()
    {
        return parseCount;
    }

    /**
     * @return the time of parse in nanoseconds
     */
    public long getParseTimeNanos()
    {
        return parseTimeNanos;
    }

    public long getPercentileRequestTime(final int percentile)
    {
        if (requestTimeList.size() == 0)
        {
            return -1;
        }
        Collections.sort(requestTimeList);

        if (percentile == 100)
        {
            return requestTimeList.get(requestTimeList.size() - 1);
        }

        int actualPercentile = Math.max(0, Math.min(100, percentile));

        final int size = requestTimeList.size();
        final double dPercentile = (double) size * (double) actualPercentile / 100.0;
        int index = Math.max(0, Math.min(size, (int) dPercentile));
        return requestTimeList.get(index);
    }

    private static class LifoList<E> implements Iterable<E>
    {
        private LinkedList<E> linkList = new LinkedList<E>();
        private final int size;

        private LifoList(final int size)
        {
            this.size = size;
            linkList = new LinkedList<E>();
        }

        public void offer(E element)
        {
            if (linkList.size() >= size)
            {
                linkList.removeLast();
            }
            linkList.addFirst(element);
        }

        public Iterator<E> iterator()
        {
            return linkList.iterator();
        }
    }
}
