package com.atlassian.configurable;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;

/**
 * Determines whether a property should be displayed and configured. Can be used as a condition for a {@link ObjectConfigurationProperty}.
 *
 * @since v6.3
 */
@PublicSpi
public interface EnabledCondition
{
    /**
     * A condition that means <strong>always enabled</strong>.
     */
    public static final EnabledCondition TRUE = new EnabledCondition()
    {
        public boolean isEnabled()
        {
            return true;
        }
    };

    /**
     * Whether or not to display and use a property.
     *
     * @return true only if the property is enabled in the current context.
     */
    boolean isEnabled();

}
