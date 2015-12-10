/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;


/**
 * This action is used to present the user with a list of allowed bulk operations
 * on the selected issues
 * User: keithb
 * Date: Dec 3, 2003
 * Time: 12:26:25 PM
 * To change this template use Options | File Templates.
 */
public class BulkCancelWizard extends AbstractBulkOperationAction
{
    public BulkCancelWizard(final SearchService searchService, final BulkEditBeanSessionHelper bulkEditBeanSessionHelper)
    {
        super(searchService, bulkEditBeanSessionHelper);
    }

    protected String doExecute() throws Exception
    {
        // Cleanup the BulkEditBean
        return finishWizard();
    }
}
