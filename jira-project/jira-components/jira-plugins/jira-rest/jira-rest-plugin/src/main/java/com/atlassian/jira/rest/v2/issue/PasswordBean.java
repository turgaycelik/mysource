package com.atlassian.jira.rest.v2.issue;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Used to change user password
 *
 * @since v6.1
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class PasswordBean
{
    /**
     * Password bean example used in auto-generated documentation.
     */
    private static final PasswordBean DOC_EXAMPLE = new PasswordBean("abracadabra");

    @JsonProperty
    private String password;

    private PasswordBean() {};

    public PasswordBean(final String password)
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }
}
