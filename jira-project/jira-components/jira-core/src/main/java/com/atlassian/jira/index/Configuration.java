package com.atlassian.jira.index;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.util.IndexWriterConfiguration;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import net.jcip.annotations.Immutable;

/**
 * The configuration for a particular index and how it should be written.
 *
 * @since v4.0
 */
@Immutable
public interface Configuration
{
    @Nonnull
    Directory getDirectory();

    @Nonnull
    Analyzer getAnalyzer();

    @Nonnull
    IndexWriterConfiguration.WriterSettings getWriterSettings(Index.UpdateMode mode);
}
