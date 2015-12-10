package com.atlassian.jira.cache;

import com.atlassian.modzdetector.Modifications;
import com.atlassian.modzdetector.ModzRegistryException;

/**
 * @since v3.13
 */
public interface HashRegistryCache
{
    Modifications getModifications() throws ModzRegistryException;
}
