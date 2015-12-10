package com.atlassian.jira.issue.index;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.query.operator.Operator;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class DefaultIndexedChangeHistoryFieldManager implements IndexedChangeHistoryFieldManager
{

    private final LazyReference <Map<String,IndexedChangeHistoryField>> ref = new LazyReference<Map<String,IndexedChangeHistoryField>>()
    {
        @Override
        protected Map<String, IndexedChangeHistoryField> create() throws Exception
        {
            return loadChangeHistoryFields();
        }
    };

    private static final Logger log = Logger.getLogger(DefaultIndexedChangeHistoryFieldManager.class);
    private final ChangeHistoryFieldConfigurationManager changeHistoryFieldConfigurationManager;

    public DefaultIndexedChangeHistoryFieldManager(ChangeHistoryFieldConfigurationManager changeHistoryFieldConfigurationManager)
    {
        this.changeHistoryFieldConfigurationManager = changeHistoryFieldConfigurationManager;
    }

    @Override
    public Collection<IndexedChangeHistoryField> getIndexedChangeHistoryFields()
    {
        return Collections.unmodifiableCollection(ref.get().values());
    }

    @Override
    @ClusterSafe("LOCAL_CACHE")
    public synchronized void addIndexedChangeHistoryField(IndexedChangeHistoryField field)
    {
        ref.get().put(field.getFieldName(), field);
    }

    @Override
    @ClusterSafe("LOCAL_CACHE")
    public synchronized void deleteIndexedChangeHistoryField(IndexedChangeHistoryField field)
    {
         ref.get().remove(field.getFieldName());
    }

    @Override
    public Collection<String> getIndexedChangeHistoryFieldNames()
    {
        return ref.get().keySet();
    }

    @Override
    public Set<Operator> getSupportedOperators(String fieldName, Set<Operator> operators)
    {
        IndexedChangeHistoryField field = ref.get().get(fieldName);
        return field != null ? field.getSupportedOperators(operators) : operators;
    }

     private Map<String, IndexedChangeHistoryField> loadChangeHistoryFields()
    {
        final Map<String,IndexedChangeHistoryField> fields = new HashMap<String, IndexedChangeHistoryField>();
        for (final String fieldName : changeHistoryFieldConfigurationManager.getAllFieldNames())
        {
            fields.put(fieldName, new IndexedChangeHistoryField(fieldName, changeHistoryFieldConfigurationManager.getDateRangeBuilder(fieldName)));
        }
        return fields;
    }
}
