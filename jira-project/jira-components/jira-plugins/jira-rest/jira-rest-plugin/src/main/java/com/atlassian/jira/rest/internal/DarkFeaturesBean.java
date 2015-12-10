package com.atlassian.jira.rest.internal;

import com.google.common.collect.Maps;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@JsonSerialize
public class DarkFeaturesBean
{
    public Map<String, DarkFeaturePropertyBean> systemFeatures = Collections.emptyMap();
    public Map<String, DarkFeaturePropertyBean> siteFeatures = Collections.emptyMap();

    public DarkFeaturesBean systemFeatures(Collection<String> systemFeatures)
    {
        this.systemFeatures = toDarkFeaturePropertyBean(systemFeatures);
        return this;
    }

    public DarkFeaturesBean siteFeatures(Collection<String> siteFeatures)
    {
        this.siteFeatures = toDarkFeaturePropertyBean(siteFeatures);
        return this;
    }

    private Map<String, DarkFeaturePropertyBean> toDarkFeaturePropertyBean(Collection<String> featureKeys)
    {
        Map<String, DarkFeaturePropertyBean> beans = Maps.newHashMap();
        for (String feature : featureKeys)
        {
            beans.put(feature, new DarkFeaturePropertyBean(true));
        }

        return beans;
    }
}
