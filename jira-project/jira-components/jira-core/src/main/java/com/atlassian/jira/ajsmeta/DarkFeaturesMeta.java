package com.atlassian.jira.ajsmeta;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.util.json.JSONArray;

import java.util.Set;

/**
 * Dark feature-related metadata.
 *
 * @since v5.2
 */
class DarkFeaturesMeta
{
    private final FeatureManager featureManager;

    public DarkFeaturesMeta(FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    /**
     * Returns the content of the DarkFeatures &lt;meta&gt; tag. This will be a JSON array literal,
     * e.g. <code>[ "feat1", "feat2" ]</code>.
     *
     * @return a String containing a Javscript array literal with the enabled dark feature names
     */
    String getContent()
    {
        Set<String> keys = featureManager.getDarkFeatures().getAllEnabledFeatures();

        return new JSONArray(keys).toString();
    }
}
