package com.atlassian.jira.rest.v2.admin.auditing;

import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditRecord;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.ChangedValueImpl;
import com.atlassian.jira.rest.util.serializers.ISODateSerializer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Date;
import java.util.List;

/**
 * @since 6.3
 */
@SuppressWarnings("unused")
@JsonAutoDetect
public class AuditRecordBean {

    private Long id;
    private String summary;
    private String remoteAddress;
    private String authorKey;
    private DateTime created;
    private String category;
    private String eventSource;
    private AssociatedItemBean objectItem;
    private List<ChangedValueBean> changedValues;
    private List<AssociatedItemBean> associatedItems;

    public AuditRecordBean() {
    }

    public AuditRecordBean(final AuditRecord auditRecord, final String categoryName) {
        this.id = auditRecord.getId();
        this.summary = auditRecord.getSummary();
        this.remoteAddress = auditRecord.getRemoteAddr();
        this.authorKey = auditRecord.getAuthorKey();
        this.created = new DateTime(auditRecord.getCreated());
        this.category = categoryName;
        this.eventSource = auditRecord.getEventSource();
        this.objectItem = (auditRecord.getObjectItem() != null) ? new AssociatedItemBean(auditRecord.getObjectItem()) : null;
        this.changedValues = getChangedValues(auditRecord.getValues());
        this.associatedItems = getAssociatedItems(auditRecord.getAssociatedItems());
    }

    public Long getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public String getAuthorKey() {
        return authorKey;
    }

    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getCreated() {
        return created;
    }

    public AssociatedItemBean getObjectItem() {
        return objectItem;
    }

    public String getCategory()
    {
        return category;
    }

    public Iterable<AssociatedItemBean> getAssociatedItems()
    {
        return associatedItems;
    }

    public Iterable<ChangedValueBean> getChangedValues()
    {
        return changedValues;
    }

    public String getRemoteAddress()
    {
        return remoteAddress;
    }

    public String getEventSource()
    {
        return eventSource;
    }

    private List<AssociatedItemBean> getAssociatedItems(final Iterable<AssociatedItem> associatedItems)
    {
        final Iterable<AssociatedItemBean> itemBeans = Iterables.transform(associatedItems, new Function<AssociatedItem, AssociatedItemBean>()
        {
            @Override
            public AssociatedItemBean apply(final AssociatedItem item)
            {
                return new AssociatedItemBean(item);
            }
        });

        return !Iterables.isEmpty(itemBeans) ? ImmutableList.copyOf(itemBeans) : null;

    }

    private List<ChangedValueBean> getChangedValues(final Iterable<ChangedValue> values)
    {
        final Iterable<ChangedValueBean> valueBeans = Iterables.transform(values, new Function<ChangedValue, ChangedValueBean>()
        {
            @Override
            public ChangedValueBean apply(final ChangedValue value)
            {
                return new ChangedValueBean(value);
            }
        });

        return !Iterables.isEmpty(valueBeans) ? ImmutableList.copyOf(valueBeans) : null;
    }

    public static final AuditRecordBean DOC_EXAMPLE;

    static
    {
        //<editor-fold desc="Audit Record Definition">
        final AuditRecord auditRecord = new AuditRecord() {
            @Nonnull
            @Override
            public Long getId() {
                return 1L;
            }

            @Nonnull
            @Override
            public Date getCreated() {
                return new Date(1395254742967L);
            }

            @Nonnull
            @Override
            public AuditingCategory getCategory() {
                return AuditingCategory.USER_MANAGEMENT;
            }

            @Nonnull
            @Override
            public String getSummary() {
                return "User created";
            }

            @Nonnull
            @Override
            public String getEventSource()
            {
                return "JIRA Connect Plugin";
            }

            @Nullable
            @Override
            public String getRemoteAddr() {
                return "192.168.1.1";
            }

            @Nullable
            @Override
            public String getAuthorKey() {
                return "administrator";
            }

            @Nonnull
            @Override
            public Iterable<AssociatedItem> getAssociatedItems() {
                return ImmutableList.<AssociatedItem>of(new AssociatedItem() {
                    @Nonnull
                    @Override
                    public String getObjectName() {
                        return "jira-users";
                    }

                    @Nullable
                    @Override
                    public String getObjectId() {
                        return "jira-users";
                    }

                    @Nullable
                    @Override
                    public String getParentName() {
                        return "JIRA Internal Directory";
                    }

                    @Nullable
                    @Override
                    public String getParentId() {
                        return "1";
                    }

                    @Nonnull
                    @Override
                    public AssociatedItem.Type getObjectType() {
                        return AssociatedItem.Type.GROUP;
                    }
                });
            }

            @Nonnull
            @Override
            public Iterable<ChangedValue> getValues() {
                return ImmutableList.<ChangedValue>of(new ChangedValueImpl("email","user@atlassian.com", "newuser@atlassian.com"));
            }

            @Nullable
            @Override
            public AssociatedItem getObjectItem() {
                return new AssociatedItem() {

                    @Nonnull
                    @Override
                    public String getObjectName() {
                        return "user";
                    }

                    @Nullable
                    @Override
                    public String getObjectId() {
                        return "usr";
                    }

                    @Nullable
                    @Override
                    public String getParentName() {
                        return "JIRA Internal Directory";
                    }

                    @Nullable
                    @Override
                    public String getParentId() {
                        return "1";
                    }

                    @Nonnull
                    @Override
                    public Type getObjectType() {
                        return Type.USER;
                    }
                };
            }
        };
        //</editor-fold>
        DOC_EXAMPLE = new AuditRecordBean(auditRecord, "user management");
    }

    public static final AuditRecordBean POST_EXAMPLE;
    static
    {
        POST_EXAMPLE = new AuditRecordBean() {
            @Nonnull
            @Override
            public String getCategory() {
                return AuditingCategory.USER_MANAGEMENT.toString();
            }

            @Nonnull
            @Override
            public String getSummary() {
                return "User created";
            }

            @Nonnull
            @Override
            public Iterable<AssociatedItemBean> getAssociatedItems() {
                return ImmutableList.<AssociatedItemBean>of(new AssociatedItemBean() {
                    @Nonnull
                    @Override
                    public String getName() {
                        return "jira-users";
                    }

                    @Nullable
                    @Override
                    public String getId() {
                        return "jira-users";
                    }

                    @Nullable
                    @Override
                    public String getParentName() {
                        return "JIRA Internal Directory";
                    }

                    @Nullable
                    @Override
                    public String getParentId() {
                        return "1";
                    }

                    @Nonnull
                    @Override
                    public String getTypeName() {
                        return AssociatedItem.Type.GROUP.toString();
                    }
                });
            }

            @Nonnull
            @Override
            public Iterable<ChangedValueBean> getChangedValues() {
                return ImmutableList.of(new ChangedValueBean(new ChangedValueImpl("email", "user@atlassian.com", "newuser@atlassian.com")));
            }

            @Nullable
            @Override
            public AssociatedItemBean getObjectItem() {
                return new AssociatedItemBean() {
                    @Nonnull
                    @Override
                    public String getName() {
                        return "user";
                    }

                    @Nullable
                    @Override
                    public String getId() {
                        return "usr";
                    }

                    @Nullable
                    @Override
                    public String getParentName() {
                        return "JIRA Internal Directory";
                    }

                    @Nullable
                    @Override
                    public String getParentId() {
                        return "1";
                    }

                    @Nonnull
                    @Override
                    public String getTypeName() {
                        return AssociatedItem.Type.USER.toString();
                    }
                };
            }
        };
    }
}
