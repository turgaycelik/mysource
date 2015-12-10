package com.atlassian.jira.ajsmeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.plugin.profile.DarkFeatures;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestDarkFeaturesMeta
{
    @Mock
    FeatureManager featureManager;

    @Test
    public void checkJavascriptSyntaxWithNoFeaturesEnabled() throws Exception
    {
        mockDarkFeatures(Collections.<String>emptySet());
        assertThat(createDarkFeaturesMeta().getContent(), equalTo("[]"));
    }

    @Test
    public void checkJavascriptSyntaxWithOneFeatureEnabled() throws Exception
    {
        mockDarkFeatures(Sets.newHashSet("feat_1"));
        assertThat(createDarkFeaturesMeta().getContent(), equalTo("[\"feat_1\"]"));
    }

    @Test
    public void checkJavascriptSyntaxWithSeveralFeaturesEnabled() throws Exception
    {
        mockDarkFeatures(Sets.newLinkedHashSet(Arrays.asList("feat_1", "feat_2")));
        assertThat(createDarkFeaturesMeta().getContent(), equalTo("[\"feat_1\",\"feat_2\"]"));
    }

    private void mockDarkFeatures(Set<String> stringSet)
    {
        DarkFeatures darkFeatures = new DarkFeatures(stringSet, Collections.<String>emptySet(), Collections.<String>emptySet());
        when(featureManager.getDarkFeatures()).thenReturn(darkFeatures);
    }

    private DarkFeaturesMeta createDarkFeaturesMeta()
    {
        return new DarkFeaturesMeta(featureManager);
    }
}
