package com.atlassian.jira.bc.customfield;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class TestCustomFieldDefinition
{
    @Test
    public void usingDefaultSearcherFlag()
    {
        final String key = "key";

        final CustomFieldDefinition.Builder builder = CustomFieldDefinition.builder();

        //Initial state of key.
        CustomFieldDefinition build = builder.build();
        assertThat(build.isUseDefaultSearcher(), Matchers.is(false));
        assertThat(build.getSearcherKey(), Matchers.nullValue());

        //Set a searcher key.
        builder.searcherKey(key);
        build = builder.build();
        assertThat(build.isUseDefaultSearcher(), Matchers.is(false));
        assertThat(build.getSearcherKey(), Matchers.is(key));

        //Use default searcher.
        build = builder.defaultSearcher().build();
        assertThat(build.isUseDefaultSearcher(), Matchers.is(true));
        assertThat(build.getSearcherKey(), Matchers.is(key));

        //Use set searcher again.
        build = builder.searcherKey(key).build();
        assertThat(build.isUseDefaultSearcher(), Matchers.is(false));
        assertThat(build.getSearcherKey(), Matchers.is(key));
    }
}
