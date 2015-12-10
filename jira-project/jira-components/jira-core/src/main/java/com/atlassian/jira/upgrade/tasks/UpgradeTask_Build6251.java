package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.auditing.SearchTokenizer;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Visitor;
import com.google.common.collect.ImmutableMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Fill in search field in Audit Log table
 *
 * @since v6.2
 */
public class UpgradeTask_Build6251 extends AbstractUpgradeTask
{
    public static final String ID = "id";
    public static final String AUTHOR_KEY = "authorKey";
    public static final String REMOTE_ADDR = "remoteAddress";
    public static final String CATEGORY = "category";
    public static final String CREATED = "created";
    public static final String SUMMARY = "summary";
    public static final String OBJECT_NAME = "objectName";
    public static final String OBJECT_PARENT_NAME = "objectParentName";
    public static final String SEARCH_FIELD = "searchField";
    public static final String DELTA_FROM = "deltaFrom";
    public static final String DELTA_TO = "deltaTo";
    public static final String LOG_ID = "logId";

    private static String ENTITY_NAME = "AuditLog";
    private static String ITEMS_ENTITY_NAME = "AuditItem";
    private static String CHANGED_VALUES_ENTITY_NAME = "AuditChangedValue";

    public UpgradeTask_Build6251()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6251";
    }

    @Override
    public String getShortDescription()
    {
        return "Fill in search field in Audit Log table";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        Select.from(ENTITY_NAME).runWith(getOfBizDelegator()).visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(GenericValue gv)
            {
                try
                {
                    final Iterable<GenericValue> items =
                            gv.getRelatedByAnd("Child" + ITEMS_ENTITY_NAME, ImmutableMap.of(LOG_ID, gv.getLong("id")));

                    final Iterable<GenericValue> changedValues = Select.from(CHANGED_VALUES_ENTITY_NAME)
                            .whereEqual(LOG_ID, gv.getLong("id")).orderBy("id asc").runWith(getOfBizDelegator()).asList();

                    SearchTokenizer tokenizer = new SearchTokenizer();
                    final ApplicationUser author = ComponentAccessor.getUserManager().getUserByKey(gv.getString(AUTHOR_KEY));
                    if (author != null)
                    {
                        tokenizer.put(author.getName());
                        tokenizer.put(author.getDisplayName());
                    }
                    tokenizer.put(gv.getString(REMOTE_ADDR));
                    tokenizer.put(gv.getString(SUMMARY));
                    tokenizer.put(gv.getString(CATEGORY));
                    tokenizer.put(gv.getString(OBJECT_NAME));
                    tokenizer.put(gv.getString(OBJECT_PARENT_NAME));
                    for (GenericValue item : items)
                    {
                        tokenizer.put(item.getString(OBJECT_NAME));
                        tokenizer.put(item.getString(OBJECT_PARENT_NAME));
                    }
                    for (GenericValue changedValue : changedValues)
                    {
                        tokenizer.put(changedValue.getString(DELTA_FROM));
                        tokenizer.put(changedValue.getString(DELTA_TO));
                    }

                    gv.set(SEARCH_FIELD, tokenizer.getTokenizedString());
                    gv.store();
                }
                catch (GenericEntityException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
