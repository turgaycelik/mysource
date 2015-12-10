package com.atlassian.jira.web.filters;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.seraph.RequestParameterConstants;
import com.atlassian.seraph.filter.HttpAuthFilter;
import com.atlassian.seraph.filter.LoginFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * This {@link javax.servlet.Filter} implementation is a composite of the Seraph provided {@link com.atlassian.seraph.filter.LoginFilter}
 * and {@link com.atlassian.seraph.filter.HttpAuthFilter}, which will allow HTTP basic Auth and os_username based login.
 * <p/>
 * It can be derived because of the fragile base class pattern in Java but really it because Serpah was never designed to
 * be extended and has package protected classes and other shenanigans that prevent proper derivation.  Buts that ok, because this
 * pattern is more unit testable and less fragile!  Long live interfaces!
 * <p/>
 * Also as it turns out this class is logically the same as having a {@link com.atlassian.seraph.filter.HttpAuthFilter}
 * follow a {@link com.atlassian.seraph.filter.LoginFilter} in the web.xml.  But by making a composite we dont have to rely
 * on one failign to work followed by one working.  Instead we make a decision as to which one to call and thn invoke it.
 * <p/>
 * And if we change the login rules in the future, we are in a better position this way in terms of coupling and encapsulation.
 */
public class JiraLoginFilter implements Filter
{
    private static final String OS_AUTH_TYPE = "os_authType";
    
    private final Filter seraphLoginFilter;
    private final Filter seraphHttpAuthFilter;

    /**
     * This is the production constructor that will be called by the servlet container.  It is the one that
     * sets up the {@link com.atlassian.seraph.filter.LoginFilter} and {@link com.atlassian.seraph.filter.HttpAuthFilter}
     */
    public JiraLoginFilter()
    {
        this(new LoginFilter(), new HttpAuthFilter());
    }

    /**
     * Done this way for unit testing so we can swap in a different implementation
     * without having to call back to the actual Serpah code, which is not particularily unit testable.
     *
     * @param seraphLoginFilter    in production this is an instance of {@link com.atlassian.seraph.filter.LoginFilter}
     * @param seraphHttpAuthFilter in production this is an instance of {@link com.atlassian.seraph.filter.HttpAuthFilter}
     */
    JiraLoginFilter(final Filter seraphLoginFilter, final Filter seraphHttpAuthFilter)
    {
        this.seraphLoginFilter = Assertions.notNull("seraphLoginFilter", seraphLoginFilter);
        this.seraphHttpAuthFilter = Assertions.notNull("seraphHttpAuthFilter", seraphHttpAuthFilter);
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        //
        // a standard design pattern in a filter is to put a if ALREADY_FILTERED statement in at the start and only run if you havent been there
        // before.  BUT in this case because we are delegating to EITHER the LoginFilter or the HttpAuthFilter then we also delegate this responsiblity
        // those enclosed fitlers to do this.  As it turns out they do indeed do this pattern since they are BOTH derived
        // from PasswordBasedLoginFilter.
        //
        // Young filters today... they have to learn responsibility!
        //
        Filter seraphFilterToRun = chooseFilterStrategy(servletRequest);
        seraphFilterToRun.doFilter(servletRequest, servletResponse, filterChain);
    }

    private Filter chooseFilterStrategy(ServletRequest servletRequest)
    {
        if (hasOpenSymphonyParameters(servletRequest))
        {
            return seraphLoginFilter;
        }
        else
        {
            return seraphHttpAuthFilter;
        }
    }

    private boolean hasOpenSymphonyParameters(ServletRequest servletRequest)
    {
        /*
         * We end up running the LoginFilter, as JIRA did before, when we detect the os_username
         * or the os_authType parameter.  This way it works just like the old code did
         * but ends up calling the HttpAuthFilter if the parameters that drove the LoginFilter
         * are missing. 
         */
        return servletRequest.getParameter(RequestParameterConstants.OS_USERNAME) != null ||
                servletRequest.getParameter(OS_AUTH_TYPE) != null;
    }


    /**
     * Delegates the initialisation to both filters.
     *
     * @param filterConfig the servlet filter config in play
     * @throws ServletException if stuff goes wrong
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        seraphLoginFilter.init(filterConfig);
        seraphHttpAuthFilter.init(filterConfig);
    }

    /**
     * Delegates the filter destroy to both filters.
     */
    public void destroy()
    {
        seraphLoginFilter.destroy();
        seraphHttpAuthFilter.destroy();
    }
}
