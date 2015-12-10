package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.web.filters.accesslog.AccessLogImprinter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;

/**
 * Provide some info to the selenium tests about the number of concurrent requests
 * This resource has the advantage that it doesn't trigger the loading of other resources (e.g. css)
 * like a big page such as "XmlRestore!default.jspa" would.
 *
 * Anonymous requests are allowed since this is a func test only plugin and <b>should never be enabled in production</b>
 *
 * @since v4.3
 */
@Path ("concurrentRequestInfo")
public class JiraConcurrentRequestsInfo
{
    @GET
    public Response getInfo(@Context HttpServletRequest request)
    {
        final AccessLogImprinter imprinter = new AccessLogImprinter(request);

        StringWriter html = new StringWriter();
        html.write("<html><body>");
        html.write("(view source)");
        html.write(imprinter.imprintHiddenHtml());
        html.write(imprinter.imprintHTMLComment());
        html.write("</body></body>");
        final Response.ResponseBuilder r = Response.ok(html.toString(), MediaType.TEXT_HTML_TYPE);
        return r.build();
    }
}
