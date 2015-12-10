package com.atlassian.jira.plugin.customfield;

import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.util.collect.MemoizingMap;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Holds the custom field display parameters that are passed to every custom field view (.vm file).
 *
 * @since v5.0.4
 */
public class CustomFieldDefaultVelocityParams
{
    private final Encoder encoder;

    public CustomFieldDefaultVelocityParams(Encoder encoder)
    {
        this.encoder = encoder;
    }

    /**
     * Combines the default velocity params with the given params.
     *
     * @param displayParams a Map containing display params
     * @return a Map containing the display params and the default params
     */
    public Map<String, ?> combine(@Nullable Map<String, ?> displayParams)
    {
        MemoizingMap.Master<String, Object> master = MemoizingMap.Master.<String, Object>builder()
                .add("cfValueEncoder", encoder)
                .master();

        return master.toMap(displayParams == null ? Collections.<String, Object>emptyMap() : displayParams);
    }
}
