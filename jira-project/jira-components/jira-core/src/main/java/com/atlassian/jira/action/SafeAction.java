package com.atlassian.jira.action;

/**
 * This is a simple marker interface that indicates that an {@ink webwork.action.Action} is a "safe" action and hence is
 * safe to receive any map of parameters.
 * <p/>
 * Actions that dont have this marker interface will be deemed "unsafe" front end web actions and the input of
 * parameters will be done in a controlled and safe manner (see JRA-15664)
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public interface SafeAction
{
}
