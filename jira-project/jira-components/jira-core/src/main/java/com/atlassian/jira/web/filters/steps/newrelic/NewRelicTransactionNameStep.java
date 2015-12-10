package com.atlassian.jira.web.filters.steps.newrelic;

import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterStep;
import com.google.common.base.Function;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Sets the New Relic transaction name for this request,
 * as per http://newrelic.com/docs/java/java-agent-component-based-web-transaction-naming
 *
 * @since v5.0
 */
public class NewRelicTransactionNameStep implements FilterStep
{

    private static Function<String, String> startsWith(final String prefix, final boolean captureNext) {
        return new Function<String, String>() {
            @Override
            public String apply(String uri) {
                if (uri.startsWith(prefix))
                {
                    if (!captureNext) {
                        return prefix + "*";
                    }

                    String rest = uri.substring(prefix.length());
                    int i = rest.indexOf('/');
                    if (i > 0) {
                        return prefix + rest.substring(0, i) + "/*";
                    }
                    else {
                        return prefix + "*";
                    }
                }
                else
                {
                    return null; 
                }
            }
        };
    }

    private static Function<String, String> endsWith(final String suffix) {
        final String name = "/*" + suffix;
        return new Function<String, String>() {
            @Override
            public String apply(String uri) {
                return uri.endsWith(suffix)? name : null;
            }
        };
    }

    private static List<Function<String, String>> RULES = Arrays.asList(
            startsWith("/secure/admin/", false),
            endsWith(".jspa"),
            startsWith("/secure/", false),
            startsWith("/browse/", false),

            startsWith("/plugins/servlet/", true),
            startsWith("/rest/", true)
    );

    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext callContext)
    {
        HttpServletRequest req = callContext.getHttpServletRequest();

        final String cp = req.getContextPath();
        String requestURI = req.getRequestURI();
        if (requestURI.startsWith(cp)) {
            requestURI = requestURI.substring(cp.length());
        }
        if (!requestURI.startsWith("/")) {
            requestURI = "/" + requestURI;
        }

        String result = calculateName(requestURI);
        if (result != null) {
            req.setAttribute("com.newrelic.agent.TRANSACTION_NAME", result);
//            System.out.println("NAME = " + result);
        }

        return callContext;
    }

    static String calculateName(String requestURI)
    {
        for (Function<String, String> rule : RULES) {
            String name = rule.apply(requestURI);
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
    {
        return callContext;
    }
}
