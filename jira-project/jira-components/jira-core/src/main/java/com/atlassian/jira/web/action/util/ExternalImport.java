package com.atlassian.jira.web.action.util;

import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionSupport;

/**
 * Dummy action to allow the WebSudoRequired annotation
 */
@WebSudoRequired
public class ExternalImport extends ActionSupport
{
}
