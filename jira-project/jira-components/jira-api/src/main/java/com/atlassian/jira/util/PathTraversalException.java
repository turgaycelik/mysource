package com.atlassian.jira.util;

/**
 * Indicates that a potential path traversal was prevented.
 */
public class PathTraversalException extends Exception
{
    public PathTraversalException()
    {
    }
}
