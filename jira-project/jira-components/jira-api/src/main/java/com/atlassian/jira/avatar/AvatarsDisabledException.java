package com.atlassian.jira.avatar;

import com.atlassian.annotations.PublicApi;

/**
 * This exception indicates that an operation has failed because avatars are disabled.
 *
 * @since v4.3
 */
@PublicApi
public class AvatarsDisabledException extends RuntimeException
{
}
