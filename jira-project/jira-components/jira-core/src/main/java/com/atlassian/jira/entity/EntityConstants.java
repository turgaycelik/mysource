package com.atlassian.jira.entity;

/**
 * @since v6.2
 */
public interface EntityConstants
{
    /**
     * The maximum number of bytes which fits in fields with extremely-long type.
     */
    int EXTREMELY_LONG_MAXIMUM_LENGTH = 32768;

    /**
     * The maximum number of bytes which fites in the fields with long-varchar type.
     */
    int LONG_VARCHAR_MAXIMUM_LENGTH = 255;
}
