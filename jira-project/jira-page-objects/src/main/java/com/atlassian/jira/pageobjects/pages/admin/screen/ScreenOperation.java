package com.atlassian.jira.pageobjects.pages.admin.screen;

/**
* @since v5.0.2
*/
public enum ScreenOperation
{
    CREATE("Create Issue", 0),
    EDIT("Edit Issue", 1),
    VIEW("View Issue", 2),
    DEFAULT("Default", -1);

    private final String operationName;
    private final int operationId;

    ScreenOperation(String operation, int operationId)
    {
        this.operationName = operation;
        this.operationId = operationId;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public int getOperationId()
    {
        return operationId;
    }

    static ScreenOperation fromOperationName(String operationName)
    {
        for (ScreenOperation operation : ScreenOperation.values())
        {
            if (operation.getOperationName().equals(operationName))
            {
                return operation;
            }
        }
        return null;
    }

    static ScreenOperation fromOperationId(long id)
    {
        for (ScreenOperation operation : ScreenOperation.values())
        {
            if (operation.operationId == id)
            {
                return operation;
            }
        }
        return null;
    }
}
