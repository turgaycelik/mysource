package com.atlassian.jira.rest.v2.admin.auditing;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import java.util.Collection;

/**
 * Representation of auditing log containing audit records and meta information.
 * @since 6.3
 */
@SuppressWarnings("unused")
@JsonAutoDetect
public class AuditingResponseBean
{
    private final Integer offset;
    private final Integer limit;
    private final Long total;
    private final Collection<AuditRecordBean> records;

    public AuditingResponseBean(final Collection<AuditRecordBean> records, final Integer offset, final Integer limit, final Long total)
    {
        this.records = records;
        this.offset = offset;
        this.limit = limit;
        this.total = total;
    }

    public Iterable<AuditRecordBean> getRecords()
    {
        return records;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public Long getTotal()
    {
        return total;
    }
}
