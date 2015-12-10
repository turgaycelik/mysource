package com.atlassian.jira.issue.customfields.view;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldTransportParams;
import org.apache.commons.collections.Transformer;

import java.util.Map;

@PublicApi
public interface CustomFieldParams extends FieldTransportParams
{
    CustomField getCustomField();
    void setCustomField(CustomField customField);

    void transform(Transformer transformer);
    void transformObjectsToStrings();
    void transformStringsToObjects();

    boolean contains(String key, String value);

    String getQueryString();
    Map getKeysAndValues();

    void remove(String key);
}
