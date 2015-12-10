package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.SharedEntity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * An implementation of {@link DirectoryFactory} that uses a {@link org.apache.lucene.store.RAMDirectory}
 *
 * This is for testing only
 */
public class MemoryDirectoryFactory implements DirectoryFactory {
    public Directory get(SharedEntity.TypeDescriptor<?> input) {
        return new RAMDirectory();
    }
}
