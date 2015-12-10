package com.atlassian.jira.bc.project.version.remotelink;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.remotelink.RemoteEntityLink;
import com.atlassian.jira.project.version.Version;

/**
 * @since v6.1.1
 */
@ExperimentalApi
public interface RemoteVersionLink extends RemoteEntityLink<Version> {}

