package com.atlassian.jira.util;

import org.apache.lucene.store.Directory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @since v5.0
 */
@InjectableComponent
public interface LuceneDirectoryUtils
{

    /**
     * Creates an index directory for the given path on the filesystem.
     *
     *
     * @param path to the index directory.
     * @throws IOException if there is a problem when creating the index directory for the given path.
     *
     * @return an implementation of {@link Directory}
     */
    Directory getDirectory(File path);

    /**
     * Creates a directory (robustly) or throws appropriate Exception
     *
     * @param path Lucene index directory path
     *
     * @throws java.io.IOException if cannot create directory, write to the directory, or not a directory
     */
    void createDirRobust(String path) throws IOException;


    /**
     * Given a {@link java.util.Collection} of paths that represent index directories checks if there are any existing
     * Lucene lock files for the passed paths. This method returns a {@link java.util.Collection} of file paths of any existing
     * Lucene lock files. If no lock files are found an empty collection is returned.
     * <p/>
     * A common usage of this methdo would be:
     * <pre>
     * Collection existingLockFilepaths = LuceneUtils.getStaleLockPaths(indexManager.getAllIndexPaths());
     * </pre>
     * </p>
     *
     * @param indexDirectoryPaths collection of index directory paths
     * @return collection of file paths of any existing Lucene lock files
     */
    Collection<String> getStaleLockPaths(Collection<String> indexDirectoryPaths);
}
