package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.opensymphony.util.UrlUtils;

public class URLCFType extends GenericTextCFType
{
    public URLCFType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
    }

    public String getSingularObjectFromString(final String string) throws FieldValidationException
    {
        // JRA-14998 - trim URLs before validating. URLs will also be saved in trim form.
        final String uri = (string == null) ? null : string.trim();
        if (!UrlUtils.verifyHierachicalURI(uri))
        {
            throw new FieldValidationException("Not a valid URL");
        }
        return uri;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitURL(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitURL(URLCFType urlCustomFieldType);
    }
}
