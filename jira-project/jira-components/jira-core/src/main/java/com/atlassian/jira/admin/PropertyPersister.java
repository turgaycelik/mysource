package com.atlassian.jira.admin;

import com.atlassian.annotations.Internal;

/**
 * SPI interface used by clients of {@link RenderablePropertyImpl} that wish to have persistence back ends other
 * than the JIRA application properties.
 *
 * @since v5.0.7
 */
@Internal
public interface PropertyPersister
{
    /**
     * Returns the saved value.
     *
     * @return a String containing the saved value
     * @since v5.0.7
     */
    String load();

    /**
     * Saves a new value.
     *
     * @param value a String containing the value to save
     * @since v5.0.7
     */
    void save(String value);
}
