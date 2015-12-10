/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

/**
 * Do not use this class. Its only purpose is to maintain binary compatibility with JIRA plugins.
 *
 * @deprecated Since 6.3.6 use {@link com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation}
 */
public abstract class AbstractBulkOperation implements BulkOperation {
    // Convenience class - it is not used in JIRA, nor it should be. It is left here to maintain binary compatibility
    // with JIRA plugins (especially from 3rd party vendors). DO NOT DELETE (unless you are sure that there is no JIRA plugin that is using it)
}