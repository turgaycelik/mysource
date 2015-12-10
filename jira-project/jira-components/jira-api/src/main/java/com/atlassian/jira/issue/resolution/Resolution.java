/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.resolution;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.IssueConstant;

@PublicApi
public interface Resolution extends IssueConstant
{
    /**
     * We have a data space for Resolution Icon URL, but this value is not set or used.
     *
     * @return Nothing interesting.
     *
     * @deprecated Not used for resolutions. Since v5.0.
     */
    @Override
    String getIconUrl();
}
