package com.atlassian.jira.index;

import com.google.common.collect.ImmutableList;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Test;
import org.mockito.Mockito;

import static com.atlassian.jira.index.IndexDocumentConfiguration.ConfigurationElement;
import static com.atlassian.jira.index.IndexDocumentConfiguration.ExtractConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfiguration.KeyConfiguration;
import static org.junit.Assert.assertThat;

/**
 * @since v6.2
 */
public class IndexDocumentConfigurationTest
{
    @Test
    public void shouldReturnTrueForEqualIndexDocumentConfigurations()
    {
        final KeyConfiguration mock = Mockito.mock(KeyConfiguration.class);
        final IndexDocumentConfiguration idc1 = new IndexDocumentConfiguration("key1", ImmutableList.of(mock));
        final IndexDocumentConfiguration idc2 = new IndexDocumentConfiguration("key1", ImmutableList.of(mock));

        assertThat(idc1, Matchers.equalTo(idc2));
    }

    @Test
    public void shouldReturnFalseForNonEqualIndexDocumentConfigurations()
    {
        final IndexDocumentConfiguration idc1 = new IndexDocumentConfiguration("key1", ImmutableList.of(Mockito.mock(KeyConfiguration.class)));
        final IndexDocumentConfiguration idc2 = new IndexDocumentConfiguration("key2", ImmutableList.of(Mockito.mock(KeyConfiguration.class)));
        assertThat(idc1, Matchers.not(Matchers.equalTo(idc2)));
    }

    @Test
    public void shouldReturnTrueForEqualKeyConfigurations()
    {
        final ExtractConfiguration mock = Mockito.mock(ExtractConfiguration.class);
        final KeyConfiguration keyConfiguration1 = new KeyConfiguration("propKey", ImmutableList.of(mock));
        final KeyConfiguration keyConfiguration2 = new KeyConfiguration("propKey", ImmutableList.of(mock));

        assertThat(keyConfiguration1, Matchers.equalTo(keyConfiguration2));
    }

    @Test
    public void shouldReturnFalseForNonEqualKeyConfigurations()
    {
        final KeyConfiguration keyConfiguration1 = new KeyConfiguration("propKey1", ImmutableList.of(Mockito.mock(ExtractConfiguration.class)));
        final KeyConfiguration KeyConfiguration2 = new KeyConfiguration("propKey2", ImmutableList.of(Mockito.mock(ExtractConfiguration.class)));

        assertThat(keyConfiguration1, Matchers.not(Matchers.equalTo(KeyConfiguration2)));
    }

    @Test
    public void shouldReturnTrueForEqualExtractConfigurations()
    {
        final ExtractConfiguration extractConfiguration1 = new ExtractConfiguration("path1", IndexDocumentConfiguration.Type.DATE);
        final ExtractConfiguration extractConfiguration2 = new ExtractConfiguration("path1", IndexDocumentConfiguration.Type.DATE);

        assertThat(extractConfiguration1, Matchers.equalTo(extractConfiguration2));
    }

    @Test
    public void shouldReturnFalseForNonEqualExtractConfigurations()
    {
        final ExtractConfiguration extractConfiguration1 = new ExtractConfiguration("path1", IndexDocumentConfiguration.Type.DATE);
        final ExtractConfiguration extractConfiguration2 = new ExtractConfiguration("path2", IndexDocumentConfiguration.Type.NUMBER);

        assertThat(extractConfiguration1, Matchers.not(Matchers.equalTo(extractConfiguration2)));
    }

    @Test
    public void shoudIterateOverAllConfigurations(){
        final IndexDocumentConfiguration idc = new IndexDocumentConfiguration(
                "entityKey",
                ImmutableList.of(
                        new KeyConfiguration("propKey1",ImmutableList.of(
                                new ExtractConfiguration("path1k1", IndexDocumentConfiguration.Type.DATE),
                                new ExtractConfiguration("path2k1", IndexDocumentConfiguration.Type.NUMBER)
                                )),
                new KeyConfiguration("propKey2",ImmutableList.of(
                                new ExtractConfiguration("path1k2", IndexDocumentConfiguration.Type.DATE),
                                new ExtractConfiguration("path2k2", IndexDocumentConfiguration.Type.NUMBER)
                                )))
        );
        final Iterable<ConfigurationElement> configurationElements = idc.getConfigurationElements();
        assertThat(configurationElements, IsIterableWithSize.<ConfigurationElement>iterableWithSize(4));
    }
}
