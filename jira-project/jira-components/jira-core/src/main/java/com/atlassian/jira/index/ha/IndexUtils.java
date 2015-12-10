package com.atlassian.jira.index.ha;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.util.LuceneDirectoryUtils;
import com.atlassian.jira.util.PathUtils;
import com.atlassian.jira.util.TempDirectoryUtil;
import com.atlassian.jira.util.ZipUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.PatternFilenameFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.NoSuchDirectoryException;

/**
 * Helper methods to work with LuceneIndexes.
 *
 * @since v6.1
 */
public class IndexUtils
{
    private static final Logger log = Logger.getLogger(IndexUtils.class);
    private static final String INDEX_SNAPSHOT_PREFIX = "IndexSnapshot_";
    private static final String INDEX_SNAPSHOT_EXT = ".zip";
    private static final Pattern INDEX_SNAPSHOT_PATTERN =
            Pattern.compile(Pattern.quote(INDEX_SNAPSHOT_PREFIX) + ".*" + Pattern.quote(INDEX_SNAPSHOT_EXT));

    public static final PatternFilenameFilter INDEX_SNAPSHOT_FILTER =
            new PatternFilenameFilter(INDEX_SNAPSHOT_PATTERN);

    private final IssueIndexManager issueIndexManager;
    private final LuceneDirectoryUtils luceneDirectoryUtils;

    /**
     *  Represents the paths in jira home where indexes are stored
     */
    public enum IndexPath
    {
        ISSUES(IndexPathManager.Directory.ISSUES_SUBDIR),
        COMMENTS(IndexPathManager.Directory.COMMENTS_SUBDIR),
        CHANGE_HISTORY(IndexPathManager.Directory.CHANGE_HISTORY_SUBDIR),
        SEARCH_REQUESTS(PathUtils.joinPaths(IndexPathManager.Directory.ENTITIES_SUBDIR, "searchrequest")),
        PORTAL_PAGES(PathUtils.joinPaths(IndexPathManager.Directory.ENTITIES_SUBDIR, "portalpage"));

        private final String path;

        private IndexPath(String path)
        {
            this.path = path;
        }

        public String getPath()
        {
            return path;
        }
    }

    public IndexUtils(final IssueIndexManager issueIndexManager, final LuceneDirectoryUtils luceneDirectoryUtils)
    {
        this.issueIndexManager = issueIndexManager;
        this.luceneDirectoryUtils = luceneDirectoryUtils;
    }

    /**
     *  This clasth maps the local index file to the shared index file for each type of index
     */
    static class IndexPathMapping
    {
        private final String sourcePath;
        private final String destinationPath;

        IndexPathMapping(final String sourcePath, final String destinationPath) {
            this.sourcePath = sourcePath;
            this.destinationPath = destinationPath;
        }

        String getSourcePath()
        {
            return sourcePath;
        }

        String getDestinationPath()
        {
            return destinationPath;
        }
    }

    public String takeIndexSnapshot(@Nonnull String sourcePath, @Nonnull String destinationPath,
            @Nonnull String snapshotId, int maxSnapshots)
    {
        File workDir = null;
        try
        {
            workDir = TempDirectoryUtil.createTempDirectory("JIRAIndexBackup");
            copyIndexes(sourcePath, workDir.getCanonicalPath());

            File destination = new File(destinationPath);
            if (!destination.exists())
            {
                destination.mkdir();
            }

            String filename = INDEX_SNAPSHOT_PREFIX + snapshotId + INDEX_SNAPSHOT_EXT;
            File snapshot = new File(destination, filename);
            ZipUtils.zip(workDir, snapshot);

            deleteOldSnapshots(destination, maxSnapshots);

            return filename;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            FileUtils.deleteQuietly(workDir);
        }
    }

    /**
     * Copies all indexes from a specified directory to another specified directory.  Any indexes on the destination directory will
     * be deleted.
     * @param sourcePath     source of index files
     * @param destinationPath   destination of index files
     */
    public void copyIndexes(@Nonnull String sourcePath, @Nonnull String destinationPath)
    {
        final Map<IndexPath, IndexPathMapping> indexPathMappings = buildIndexPathMappings(sourcePath, destinationPath);
        for(IndexPathMapping indexPathMapping : indexPathMappings.values())
        {
            File sourceDirectory = new File(indexPathMapping.getSourcePath());
            File destDirectory = new File(indexPathMapping.getDestinationPath());
            copySpecificIndex(sourceDirectory, destDirectory);
        }
    }

    /**
     * Builds a mapping from the source index filepath to the destination filepath - keyed by the type of index
     *
     * @param sourcePath  the root of the source index
     * @param destinationPath  the root of the destination index
     * @return
     */
    public Map<IndexPath, IndexPathMapping> buildIndexPathMappings(@Nonnull final String sourcePath, @Nonnull final String destinationPath)
    {
        Map<IndexPath, IndexPathMapping> indexPathMappings = Maps.newHashMap();
        for (IndexPath path: IndexPath.values())
        {
            indexPathMappings.put(path, new IndexPathMapping(PathUtils.joinPaths(sourcePath, path.getPath()),
                    PathUtils.joinPaths(destinationPath, path.getPath())));
        }
        return indexPathMappings;
    }

    /**
     * This deletes all documents in all indexes (if they exist)
     *
     * @param path  the root of the index to clear
     */
    public void clearIndex(@Nonnull final String path)
    {
        final List<String> indexPaths = buildIndexPaths(path);
        for(String indexPath : indexPaths)
        {
            clearIndex(new File(indexPath));
        }

    }

    private void clearIndex(final File directory)
    {
        IndexWriter writer =null;
        try
        {
            writer = getWriter(directory);
            writer.deleteAll();
        }
        catch (Exception e)
        {
            log.error("Error occured while copying index", e);
            throw new RuntimeException(e);
        }
        finally
        {
            closeQuietly(writer);
        }
    }

    private List<String> buildIndexPaths(final String path)
    {
        List<String> paths = Lists.newArrayList();
        for(IndexPath indexPath : IndexPath.values())
        {
            paths.add(PathUtils.joinPaths(path, indexPath.getPath()));
        }
        return paths;
    }

    private void copySpecificIndex(final File sourceDirectory, final File destDirectory)
    {
        IndexReader reader = null;
        IndexWriter writer = null;
        try
        {
            writer = getWriter(destDirectory);
            reader = IndexReader.open(luceneDirectoryUtils.getDirectory(sourceDirectory));
            writer.addIndexes(reader);
        }
        catch (NoSuchDirectoryException e)
        {
            log.debug("Cannot copy index; " + e.getMessage());
        }
        catch (Exception e)
        {
            log.error("Error occured while copying index", e);
            throw new RuntimeException(e);
        }
        finally
        {
            closeQuietly(reader);
            closeQuietly(writer);
        }
    }

    private void closeQuietly(final IndexWriter writer)
    {
        if (writer != null)
        {
            try
            {
                writer.close();
            }
            catch (IOException e)
            {
                log.debug("Exception thrown while closing writer, ignored");
            }
        }
    }

    private void closeQuietly(final IndexReader reader)
    {
        if (reader != null)
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                log.debug("Exception thrown while closing reader, ignored");
            }
        }
    }

    private IndexWriter getWriter(final File directory) throws Exception
    {
        final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(IssueIndexManager.LUCENE_VERSION, issueIndexManager.getAnalyzerForIndexing());
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        return new IndexWriter(luceneDirectoryUtils.getDirectory(directory), indexWriterConfig);
    }

    @VisibleForTesting
    protected int deleteOldSnapshots(File directory, int numToKeep)
    {
        final File[] snapshots = directory.listFiles(INDEX_SNAPSHOT_FILTER);
        Arrays.sort(snapshots, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        int numKept = 0;
        int numDeleted = 0;
        for (File snapshot : snapshots)
        {
            if (numKept < numToKeep)
            {
                numKept++;
            }
            else if (snapshot.delete())
            {
                numDeleted++;
            }
        }
        return numDeleted;
    }
}
