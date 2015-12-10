package com.atlassian.jira.appconsistency.integrity.transformer;

import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import org.apache.commons.collections.Transformer;
import org.ofbiz.core.entity.GenericValue;

/**
 * Transforms a {@link GenericValue} into a {@link DeleteEntityAmendment} 
 */
public class DeleteEntityAmendmentTransformer implements Transformer
{
    int amendmentType;
    String amendmentMessage;

    public DeleteEntityAmendmentTransformer(int amendmentType, String amendmentMessage)
    {
        this.amendmentType = amendmentType;
        this.amendmentMessage = amendmentMessage;
    }

    public Object transform(Object object)
    {
        return new DeleteEntityAmendment(amendmentType, amendmentMessage, (GenericValue) object);
    }
}
