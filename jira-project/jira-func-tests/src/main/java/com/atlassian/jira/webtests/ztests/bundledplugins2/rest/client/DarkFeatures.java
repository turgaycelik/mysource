package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.testkit.client.restclient.DarkFeature;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Map;

@JsonSerialize
public class DarkFeatures
{
    public Map<String, DarkFeature> siteFeatures;
    public Map<String, DarkFeature> systemFeatures;
}
