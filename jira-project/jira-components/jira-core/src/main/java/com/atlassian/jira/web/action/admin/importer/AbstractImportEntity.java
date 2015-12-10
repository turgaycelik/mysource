/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.importer;

import com.atlassian.jira.web.action.ProjectActionSupport;

public abstract class AbstractImportEntity extends ProjectActionSupport implements ImportEntity
{
    public int compareTo(Object o)
    {
        if (!(o instanceof AbstractImportEntity))
            return 0;

        AbstractImportEntity that = (AbstractImportEntity) o;
        return this.getActionOrder() - that.getActionOrder();
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof AbstractImportEntity))
            return false;

        final AbstractImportEntity abstractImportEntity = (AbstractImportEntity) o;

        if (getActionOrder() != abstractImportEntity.getActionOrder())
            return false;

        return true;
    }

    public int hashCode()
    {
        return getActionOrder();
    }
}
