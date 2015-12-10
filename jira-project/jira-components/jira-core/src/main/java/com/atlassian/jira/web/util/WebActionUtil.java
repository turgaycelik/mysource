/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.atlassian.jira.util.ErrorCollection;

import java.util.List;

public class WebActionUtil
{
    public static void addDependentVersionErrors(ErrorCollection action, List versionIds, String fieldname)
    {
        addDependentEntityInvalidErrors(action, versionIds, fieldname, "You cannot specify \"Unreleased\" or \"Released\".");
        addDependentEntityUnknownErrors(action, versionIds, fieldname, "You cannot specify \"Unknown\" with a specific version.");
    }

    public static void addDependentComponentErrors(ErrorCollection errorCollection, List componentIds, String fieldname)
    {
        addDependentEntityUnknownErrors(errorCollection, componentIds, fieldname, "You cannot select \"Unknown\" with a specific component.");
    }

    /**
     * Catches invalid versions.
     * @param errorCollection
     * @param entityIds
     * @param fieldname
     * @param errorMessage
     */
    public static void addDependentEntityInvalidErrors(ErrorCollection errorCollection, List entityIds, String fieldname, String errorMessage)
    {
        for (final Object entityId : entityIds)
        {
            Long l = (Long) entityId;

            if (l.longValue() < -1)
            {
                errorCollection.addError(fieldname, errorMessage);
            }
        }
    }

    /**
     * Cannot select unknown with another Version.
     * @param errorCollection
     * @param entityIds
     * @param fieldname
     * @param errorMessage
     */
    public static void addDependentEntityUnknownErrors(ErrorCollection errorCollection, List entityIds, String fieldname, String errorMessage)
    {
        if (entityIds.size() > 1)
        {
            for (final Object entityId : entityIds)
            {
                Long l = (Long) entityId;

                if (l.longValue() == -1)
                {
                    errorCollection.addError(fieldname, errorMessage);
                }
            }
        }
    }
}
