package com.atlassian.jira.web.filters.accesslog;

import com.atlassian.jira.util.http.request.CapturingRequestWrapper;
import com.atlassian.jira.util.http.response.CapturingResponseWrapper;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.Cookie;

/**
 * This is a help class that will help dump the contents of HTTP requests and responses
 * <p/>
 * This can only really be used in conjunction with AccessLogDumpUtil filter
 *
 * @since v3.13.2
 */
class AccessLogDumpUtil
{
    /**
     * This will return a access log message that consists of the one passed in (logMsg) and then followed by a large
     * dump of request/response information.
     *
     * @param request an instance of CapturingRequestWrapper that has captured the request
     * @param response an instance of CapturingResponseWrapper that has captured the response
     * @param logMsg the access log message to prepend first
     * @param responseContentLen the length of the response content
     * @param requestException a possible exception that may have happended during the request processing
     * @return the logMsg with the dumped information appended
     */
    static String dumpRequestResponse(CapturingRequestWrapper request, CapturingResponseWrapper response, String logMsg, long responseContentLen, final Exception requestException)
    {
        // ==============================================
        // EXCEPTION PROCESSING
        // ==============================================
        StringBuilder sb = new StringBuilder();
        if (requestException != null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            requestException.printStackTrace(pw);

            sb.append("\t___ Exception ___________________________________________\n");
            sb.append(indentVal(sw.toString()));
        }

        // ==============================================
        // REQUEST PROCESSING
        // ==============================================
        sb.append("\t___ Request _____________________________________________________\n");
        final Enumeration parameterNames = request.getParameterNames();
        int i = 0;
        if (parameterNames != null)
        {
            while (parameterNames.hasMoreElements())
            {
                if (i == 0)
                {
                    sb.append("\tRequest URL Parameters : \n");
                }
                String name = (String) parameterNames.nextElement();
                final String[] values = request.getParameterValues(name);

                for (String value : values)
                {
                    printNameValue(sb, name, value);
                }
                i++;
            }
        }

        i = 0;
        final Enumeration headerNames = request.getHeaderNames();
        if (headerNames != null)
        {
            while (headerNames.hasMoreElements())
            {
                if (i == 0)
                {
                    sb.append("\tRequest HTTP Headers : \n");
                }
                String name = (String) headerNames.nextElement();
                final Enumeration headerValues = request.getHeaders(name);
                while (headerValues.hasMoreElements())
                {
                    String value = (String) headerValues.nextElement();
                    printNameValue(sb, name, indentVal(value));
                }
                i++;
            }
        }

        i = 0;
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (i == 0)
                {
                    sb.append("\tRequest Cookies : \n");
                }
                String name = cookie.getName();
                String value = formatCookieValue(cookie);
                printNameValue(sb, name, value);
                i++;
            }
        }

        i = 0;
        final Enumeration attributeNames = request.getAttributeNames();
        if (attributeNames != null)
        {
            while (attributeNames.hasMoreElements())
            {
                if (i == 0)
                {
                    sb.append("\tRequest Attributes : \n");
                }
                String name = (String) attributeNames.nextElement();
                String value = String.valueOf(request.getAttribute(name));
                printNameValue(sb, name, value);
                i++;
            }
        }
        appendByteDump(sb, "Request Data", request.getBytes(), request.getCharacterEncoding(), request.getContentLength());

        // ==============================================
        // RESPONSE PROCESSING
        // ==============================================
        sb.append("\t___ Response ____________________________________________________\n");
        i = 0;
        final List cookieList = response.getCookieList();
        if (cookieList != null)
        {
            final Iterator cookiesIterator = cookieList.iterator();
            while (cookiesIterator.hasNext())
            {
                Cookie cookie = (Cookie) cookiesIterator.next();
                if (i == 0)
                {
                    sb.append("\tResponse Cookies : \n");
                }
                String name = cookie.getName();
                String value = formatCookieValue(cookie);
                printNameValue(sb, name, value);
                i++;
            }
        }

        i = 0;
        final List headerList = response.getHeaderList();
        if (headerList != null)
        {
            final Iterator httpHeaderIterator = headerList.iterator();
            while (httpHeaderIterator.hasNext())
            {
                CapturingResponseWrapper.HttpHeader httpHeader = (CapturingResponseWrapper.HttpHeader) httpHeaderIterator.next();
                if (i == 0)
                {
                    sb.append("\tResponse HTTP Headers : \n");
                }
                String name = httpHeader.getName();
                String value = String.valueOf(httpHeader.getValue());
                printNameValue(sb, name, value);
                i++;
            }
        }
        appendByteDump(sb, "Response Data", response.getBytes(), response.getCharacterEncoding(), responseContentLen);

        return new StringBuilder(logMsg).append("\n\t").append(sb.toString().trim()).toString();
    }

    private static void appendByteDump(StringBuilder sb, String desc, byte[] bytes, String characterEncoding, long contentLen)
    {
        if (bytes.length > 0)
        {
            sb.append("\t").append(desc);
            if (bytes.length < contentLen)
            {
                sb.append(" (first ").append(bytes.length).append(" of ").append(contentLen).append(" bytes) : \n");
            }
            else
            {
                sb.append(" (total ").append(bytes.length).append(" bytes) : \n");
            }
            String capturedOutput = "";
            try
            {
                capturedOutput = new String(bytes, characterEncoding);
            }
            catch (UnsupportedEncodingException ignore)
            {
                // we really dont care.  Its not that important
            }

            sb.append("\n").append(indentVal(capturedOutput));
        }
        else
        {
            sb.append("\t").append(desc).append(" (0 bytes).\n");
        }
    }

    private static String formatCookieValue(Cookie cookie)
    {
        return new StringBuilder().append(cookie.getValue())
                .append(" path:").append(cookie.getPath())
                .append(" domain:").append(cookie.getDomain())
                .append(" version:").append(cookie.getVersion())
                .append(" maxAge:").append(cookie.getMaxAge())
                .toString();
    }

    private static void printNameValue(StringBuilder sb, String name, String value)
    {
        sb.append("\t\t");
        sb.append(name).append("=").append(indentVal(value)).append("\n");
    }

    private static String indentVal(String s)
    {
        return StringUtils.replace(s, "\n", "\n\t\t\t");
    }
}
