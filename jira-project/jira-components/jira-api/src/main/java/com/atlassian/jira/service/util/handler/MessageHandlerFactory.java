/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util.handler;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;

import javax.annotation.Nullable;

/**
 * This call is going to be moved away from API into JIRA Mail Plugin.
 * The only reason it's here are that a few dependencies from API need it and these dependencies
 * cannot easily be moved to JIRA Mail Plugin just becasuse ... unit tests which depend
 * on test infrastructure (ugly) unavailable to plugins (i.e. this stuff is in test scope of jira-tests).
 */
@Internal
public interface MessageHandlerFactory
{
    @Nullable
    public MessageHandler getHandler(String clazz);

    @ExperimentalApi
    @Nullable
    String getCorrespondingModuleDescriptorKey(String clazz);
}
