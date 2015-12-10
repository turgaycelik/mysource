package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents the move subtask choose operation page (step 1 of 4).
 *
 * @since v5.2
 */
public class MoveSubtaskChooseOperation extends AbstractJiraPage
{
    private static final String URI = "/secure/MoveSubTaskChooseOperation!default.jspa";

    @ElementBy (id = "move.subtask.parent.operation.name_id")
    protected PageElement changeParentRadio;

    @ElementBy (id = "move.subtask.type.operation.name")
    protected PageElement changeTypeRadio;

    @ElementBy (id = "next_submit")
    protected PageElement nextSubmitButton;

    private final long issueId;

    public MoveSubtaskChooseOperation (long issueId)
    {
        this.issueId = issueId;
    }

    @Override
    public TimedCondition isAt()
    {
        return nextSubmitButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI + "?id=" + issueId ;
    }

    public MoveSubtaskChooseOperation selectChangeTypeRadio()
    {
        changeTypeRadio.click();
        return this;
    }

    public MoveSubtaskChooseOperation selectChangeParentRadio()
    {
        changeParentRadio.click();
        return this;
    }

    public MoveSubtaskParentPage next()
    {
        nextSubmitButton.click();
        return pageBinder.bind(MoveSubtaskParentPage.class, issueId);
    }
}
