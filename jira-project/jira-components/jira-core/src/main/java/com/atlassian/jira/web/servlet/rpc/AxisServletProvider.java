package com.atlassian.jira.web.servlet.rpc;

import org.apache.axis.transport.http.AxisServlet;

import javax.servlet.ServletConfig;

/**
 * Component responsible for providing appropriately configured AXIS servlet to the JIRA web interface layer.
 *
 * @since v4.4
 */
public interface AxisServletProvider
{

    /**
     * Initialize this provider. Safe to call multiple times, only the first call is effective.
     *
     * @param config init servlet configuration that will be used to create the Axis servlet
     */
    void init(ServletConfig config);

    /**
     * Explicitly reset the maintained Axis servlet instance. If this component has not been initialized yet, this
     * call has no effect.
     */
    void reset();

    /**
     * Get configured AXIS servlet.
     *
     * @return axis servlet instance
     */
    AxisServlet getAxisServlet();
}
