package com.atlassian.jira.ofbiz;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.security.xsrf.XsrfVulnerabilityDetectionSQLInterceptor;
import org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptor;
import org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptorFactory;

/**
 * An {@link org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptorFactory} that will chained together one or more
 * {@link org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptor}s
 *
 * @since v4.0
 */
public class JiraSQLInterceptorFactory implements SQLInterceptorFactory
{
    private final boolean showPerformanceMonitor;

    public JiraSQLInterceptorFactory()
    {
        showPerformanceMonitor = JiraSystemProperties.showPerformanceMonitor();
    }

    public SQLInterceptor newSQLInterceptor(final String ofbizHelperName)
    {
        final ChainedSQLInterceptor.Builder builder = new ChainedSQLInterceptor.Builder();
        builder
                .add(new LoggingSQLInterceptor())
                .add(new InstrumentedSQLInterceptor())
                .add(new XsrfVulnerabilityDetectionSQLInterceptor());
        if (showPerformanceMonitor)
        {
            builder.add(new PerformanceSQLInterceptor());
        }
        return builder.build();

    }
}