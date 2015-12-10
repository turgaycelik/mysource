package com.atlassian.jira.security.login;

import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.UrlBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Responsible for appending the XSRF token to the url before a redirect is sent.
 *
 * @see javax.servlet.http.HttpServletResponse
 * @since v4.1.1
 */
class XsrfTokenAppendingResponse extends HttpServletResponseWrapper
{
    private final HttpServletResponse baseResponse;

    private final HttpServletRequest baseRequest;

    XsrfTokenAppendingResponse(final HttpServletRequest baseRequest, HttpServletResponse baseResponse)
    {
        super(baseResponse);
        this.baseResponse = baseResponse;
        this.baseRequest = baseRequest;
    }

    /**
     * Appends the xsrf token value to the redirect url, if it is present, and then delegates to the
     * {@link javax.servlet.http.HttpServletResponse}}
     *
     * @param url the redirect location URL
     * @throws java.io.IOException If an input or output exception occurs
     */
    public void sendRedirect(final String url) throws IOException
    {
        String xsrfTokenValue = baseRequest.getParameter(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY);

        if (xsrfTokenValue == null)
        {
            baseResponse.sendRedirect(url);
        }
        else
        {
            UrlBuilder builder = createUrlBuilder(url);
            builder.addParameter(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY, xsrfTokenValue);
            String finalUrl = builder.asUrlString();
            baseResponse.sendRedirect(finalUrl);
        }
    }

    UrlBuilder createUrlBuilder(final String url) {return new UrlBuilder(url);}
}
