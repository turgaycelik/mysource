package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.util.BrowserUtils;

public class WordViewUtils
{
    private WordViewUtils()
    {
    }

    public static void writeGenericNoCacheHeaders(RequestHeaders requestHeaders)
    {
        //these headers need to be set to get around an internet explorer 6 SP1 bug.  See JRA-1738
        //we need to override the headers that are set either by the server, or the encoding filter
        // Add private and must-revalidate to close information leaks.
        requestHeaders.setHeader("Cache-Control", "private, must-revalidate, max-age=5"); // http 1.1
        requestHeaders.setHeader("Pragma", ""); // http 1.0
        requestHeaders.setDateHeader("Expires", System.currentTimeMillis() + 300); // prevent proxy caching
    }

    /**
     * JRA-15545: when sending an attachment in a response with a filename that has non-ASCII characters in it, you must
     * modify your headers slightly so that browsers can recognise the filename. Mozilla and Opera seem to deal with
     * the headers in the same way; IE uses a "broken" way; Safari is broken all together and will not work unless the
     * URL matches the filename.
     * 
     * @see http://www.faqs.org/rfcs/rfc2231.html, https://bugs.webkit.org/show_bug.cgi?id=15287
     * @param requestHeaders the headers of the response
     * @param filename the filename to send to the client; should already be URL Encoded!
     * @param userAgent the user agent string from the client's request
     * @param encoding the encoding used by JIRA (e.g. UTF-8)
     */
    public static void writeEncodedAttachmentFilenameHeader(RequestHeaders requestHeaders, String filename, String userAgent, String encoding)
    {
        StringBuilder header = new StringBuilder("attachment; filename");
        if (BrowserUtils.isIe456Or7(userAgent))
        {
            header.append("=\"").append(filename).append("\";");
        }
        else
        {
            // encoded in accordance with RFC-2231
            header.append("*=").append(encoding).append("''").append(filename).append(";");
        }
        requestHeaders.addHeader("content-disposition", header.toString());
    }
}
