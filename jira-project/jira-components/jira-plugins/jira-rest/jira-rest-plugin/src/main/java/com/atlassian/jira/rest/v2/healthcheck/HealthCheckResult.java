package com.atlassian.jira.rest.v2.healthcheck;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The result of JIRA health check.
 * @deprecated Use atlassian healthcheck plugin instead. Remove with 7.0
 */
@Deprecated
@SuppressWarnings ("UnusedDeclaration")
public class HealthCheckResult
{
    @JsonProperty
    public String name;

    @JsonProperty
    public String description;

    @JsonProperty
    public Boolean passed;

    public HealthCheckResult()
    {
    }

    public HealthCheckResult(String name, String description, boolean passed)
    {
        this.name = name;
        this.description = description;
        this.passed = passed;
    }
}
