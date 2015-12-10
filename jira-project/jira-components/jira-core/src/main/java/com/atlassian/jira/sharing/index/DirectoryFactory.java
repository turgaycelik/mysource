package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.util.Function;
import org.apache.lucene.store.Directory;

/**
 * The DirectoryFactory allows a different Lucene Directory to be used
 */
public interface DirectoryFactory extends Function<SharedEntity.TypeDescriptor<?>, Directory> {
}
