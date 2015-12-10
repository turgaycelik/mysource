package com.atlassian.jira.functest.framework.dashboard;

import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A representation of PortalPage for testing. 
 *
 * @since v3.13
 */
public class DashboardPageInfo extends SharedEntityInfo
{
    public static class Operation
    {
        public static Operation EDIT = new Operation("Edit", "EditPortalPage!default.jspa?pageId=");
        public static Operation DELETE = new Operation("Delete", "DeletePortalPage!default.jspa?pageId=");
        public static Operation COPY = new Operation("Copy", "AddPortalPage!default.jspa?clonePageId=");

        public final static List <Operation> NONE = Collections.emptyList();
        public final static List <Operation> ALL = Arrays.asList(EDIT, DELETE, COPY);
        public final static List <Operation> COPY_ONLY = Collections.singletonList(COPY);

        private final String linkName;
        private final String linkPrefix;

        public Operation(final String linkName, final String linkPrefix)
        {
            this.linkName = linkName;
            this.linkPrefix = linkPrefix;
        }

        public String getUrl(long id)
        {
            return linkPrefix + id;
        }

        public String getLinkName()
        {
            return linkName;
        }

        public String toString()
        {
            return linkName;
        }
    }

    private List<Operation> operations = null;

    public DashboardPageInfo(final long id, final String name, final String description, final boolean favourite, 
            final Set<TestSharingPermission> permissions,
            final String owner, final int favCount, final List<Operation> operations)
    {
        this(new Long(id), name, description, favourite, permissions, owner, new Integer(favCount), operations);
    }

    public DashboardPageInfo(final Long id, final String name, final String description, final boolean favourite,
            final Set<TestSharingPermission> sharingPermissions, final String owner, final Integer favCount,
            final List<Operation> operations)
    {
        super(id, name, description, favourite, sharingPermissions, owner, favCount);
        
        this.operations = operations;
    }

    public DashboardPageInfo(final Long id, final String name, final String description, final boolean favourite,
            final Set<TestSharingPermission> sharingPermissions)
    {
        super(id, name, description, favourite, sharingPermissions, null, null);

        this.operations = null;

    }

    public DashboardPageInfo(final DashboardPageInfo dashboardInfo)
    {
        super(dashboardInfo);
        this.operations = dashboardInfo.getOperations();
    }

    public List<Operation> getOperations()
    {
        return operations;
    }

    public DashboardPageInfo setOperations(final List<Operation> operations)
    {
        this.operations = operations;
        return this;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public DashboardPageInfo copy()
    {
        return new DashboardPageInfo(this);
    }
}
