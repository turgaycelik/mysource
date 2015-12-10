package com.atlassian.jira.functest.framework.parser;

import com.atlassian.jira.functest.framework.parser.filter.WebTestSharePermission;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Base interface for SharedEntityItem and DashboardItem
 *
 * @since v4.4.1
 */
public interface SharedEntityItem
{
     String getName();
 
     String getDescription();
  
     String getAuthor();
 
     Long getIssues();
 
     List getSharing();
 
     Boolean isFav();
 
     Long getSubscriptions();
 
     List getOperations();
 
     Long getFavCount();
 
     Long getId();
}
