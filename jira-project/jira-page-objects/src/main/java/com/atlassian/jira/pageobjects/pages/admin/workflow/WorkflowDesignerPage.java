package com.atlassian.jira.pageobjects.pages.admin.workflow;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

/**
 * Represents the workflow designer page for a particular workflow!
 *
 * @since v4.4
 */
public class WorkflowDesignerPage extends AbstractWorkflowHeaderPage
{
    private static final String URI_TEMPLATE = "/secure/admin/WorkflowDesigner.jspa?wfName=%s&workflowMode=%s";

    private final String uri;
    private final String workflowName;

    @ElementBy(id = "jwd", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement jwdElement;

    public WorkflowDesignerPage(final String workflowName, final boolean isDraft)
    {
        this.workflowName = workflowName;
        this.uri = String.format(URI_TEMPLATE, encodeUrl(workflowName), isDraft ? "draft" : "live");
    }

    //Must be overwritten so that WD will find and execute it.
    @Init
    public void init()
    {
        super.init();
    }


    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(jwdElement.timed().isPresent(),
                getWorkflowHeader().isPresentCondition(workflowName));
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    private static String encodeUrl(String param)
    {
        try
        {
            return URLEncoder.encode(param, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
