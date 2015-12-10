package com.atlassian.jira.bc.security.login;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

/**
 * This class is used to indicate the reason why authentication has been denied.
 *
 * @see com.atlassian.jira.bc.security.login.LoginReason#AUTHENTICATION_DENIED
 * @since v4.3
 */
@Immutable
public class DeniedReason
{
    /**
     * The HTTP header that is used to
     */
    public static final String X_DENIED_HEADER = "X-Authentication-Denied-Reason";

    /**
     * A string representation of the reason why authentication has been denied.
     */
    private final String reasonCode;

    /**
     * Immutable map of properties for this DenialReason.
     */
    private final ImmutableMap<String, String> reasonSpecificProperties;

    /**
     * Creates a new DenialReason with no additional properties.
     *
     * @param reasonCode the reason code for the denial
     */
    public DeniedReason(String reasonCode)
    {
        this(reasonCode, ImmutableMap.<String, String>of());
    }

    /**
     * Creates a new DenialReason with properties.
     *
     * @param reasonCode the reason code for the denial
     * @param reasonSpecificProperties the reason-specific properties
     */
    public DeniedReason(String reasonCode, Map<String, String> reasonSpecificProperties)
    {
        if (reasonCode == null) { throw new NullPointerException("reasonCode"); }
        if (reasonSpecificProperties == null) { throw new NullPointerException("properties"); }

        this.reasonCode = reasonCode;
        this.reasonSpecificProperties = ImmutableMap.copyOf(reasonSpecificProperties);
    }

    /**
     * Returns a string representation of this DenialReason, which is suitable for inclusion in a HTTP response header.
     *
     * @return a string representation of this DenialReason.
     */
    public String asString()
    {
        if (reasonSpecificProperties.isEmpty())
        {
            return reasonCode;
        }

        return String.format("%s; %s", reasonCode, StringUtils.join(reasonSpecificProperties.entrySet(), ","));
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
