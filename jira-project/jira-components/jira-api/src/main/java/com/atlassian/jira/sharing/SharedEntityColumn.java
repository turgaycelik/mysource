package com.atlassian.jira.sharing;

import javax.annotation.concurrent.Immutable;

/**
 * Simple enumeration that represents the standard columns in a {@link SharedEntity}.
 *
 * @since v3.13
 */
@Immutable
public enum SharedEntityColumn
{
    ID, NAME, DESCRIPTION, OWNER, FAVOURITE_COUNT, IS_SHARED
}
