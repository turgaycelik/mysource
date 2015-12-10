package com.atlassian.jira.sharing.index;

import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharedEntity;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.document.Field;

import java.util.Collection;

import static java.lang.String.valueOf;

/**
* Responsible for building a {@link Field} that allows searching whether {@link SharedEntity} has been shared or not.
*
* @see SharedEntityDocumentFactory
* @since v4.4
*/
public class IsSharedFieldFactory implements SharedEntityFieldFactory
{
    private static final String IS_SHARED_FIELD_NAME = "isShared";

    @Override
    public String getFieldName()
    {
        return IS_SHARED_FIELD_NAME;
    }

    @Override
    public Collection<Field> getField(final SharedEntity entity)
    {
        return ImmutableList.of
                (
                        new Field(IS_SHARED_FIELD_NAME, valueOf(isShared(entity)), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS)
                );
    }

    private boolean isShared(final SharedEntity entity)
    {
        boolean isSystemDashboard = false;
        //JRADEV-6820 : Exclude the System dashboard
        if (entity.getEntityType().equals(PortalPage.ENTITY_TYPE)) {
            isSystemDashboard =  ((PortalPage)entity).isSystemDefaultPortalPage();
        }
        return !entity.getPermissions().isPrivate() && !isSystemDashboard;
    }
}
