package com.atlassian.jira.entity;

import com.atlassian.beehive.db.spi.ClusterLockStatus;
import com.atlassian.jira.cluster.lock.ClusterNodeHeartbeatFactory;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.index.property.EntityPropertyIndexDocument;
import com.atlassian.jira.index.property.EntityPropertyIndexDocumentFactory;
import com.atlassian.jira.issue.MovedIssueKey;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.search.SearchRequestEntity;
import com.atlassian.jira.issue.search.SearchRequestEntityBuilder;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecurityLevelSchemeBuilder;
import com.atlassian.jira.issue.subscription.FilterSubscription;
import com.atlassian.jira.issue.subscription.FilterSubscriptionFactory;
import com.atlassian.jira.license.LicenseRoleGroupEntityFactory;
import com.atlassian.jira.license.ProductLicense;
import com.atlassian.jira.license.ProductLicenseEntityFactory;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.scheduler.OfBizRunDetails;
import com.atlassian.jira.scheduler.RunDetailsFactory;
import com.atlassian.jira.security.GlobalPermissionEntry;
import com.atlassian.jira.security.plugin.GlobalPermissionEntityFactory;

/**
 * Holds Entity Factory classes.
 *
 * @since v4.4
 */
public interface Entity
{
    public static final ApplicationUserEntityFactory APPLICATION_USER = new ApplicationUserEntityFactory();
    public static final EntityFactory<EntityProperty> ENTITY_PROPERTY = new EntityPropertyFactory();
    public static final EntityFactory<EntityPropertyIndexDocument> ENTITY_PROPERTY_INDEX_DOCUMENT = new EntityPropertyIndexDocumentFactory();
    public static final EntityFactory<FilterSubscription> FILTER_SUBSCRIPTION = new FilterSubscriptionFactory();
    public static final EntityFactory<IssueLink> ISSUE_LINK = new IssueLinkFactory();
    public static final AbstractEntityFactory<IssueSecurityLevel> ISSUE_SECURITY_LEVEL = new IssueSecurityLevelFactory();
    public static final NamedEntityBuilder<IssueSecurityLevelScheme> ISSUE_SECURITY_LEVEL_SCHEME = new IssueSecurityLevelSchemeBuilder();
    public static final IssueSecurityLevelPermissionFactory ISSUE_SECURITY_LEVEL_PERMISSION = new IssueSecurityLevelPermissionFactory();
    public static final EntityFactory<GlobalPermissionEntry> GLOBAL_PERMISSION_ENTRY = new GlobalPermissionEntityFactory();
    public static final LicenseRoleGroupEntityFactory LICENSE_ROLE_GROUP = new LicenseRoleGroupEntityFactory();
    public static final EntityFactory<MovedIssueKey> MOVED_ISSUE_KEY = new MovedIssueKeyFactory();
    public static final EntityFactory<ProductLicense> PRODUCT_LICENSE = new ProductLicenseEntityFactory();
    public static final EntityFactory<ProjectCategory> PROJECT_CATEGORY = new ProjectCategoryFactory();
    public static final EntityFactory<RemoteIssueLink> REMOTE_ISSUE_LINK = new RemoteIssueLinkFactory();
    public static final EntityFactory<OfBizRunDetails> RUN_DETAILS = new RunDetailsFactory();
    public static final NamedEntityBuilder<SearchRequestEntity> SEARCH_REQUEST = new SearchRequestEntityBuilder();
    /**
     * Entity Factory for ClusterLockStatus - used by the Beehive Cluster Lock library.
     *
     * @since 6.3
     */
    public static final EntityFactory<ClusterLockStatus> CLUSTER_LOCK_STATUS = new ClusterLockStatusEntity();
    public static final ClusterNodeHeartbeatFactory CLUSTER_NODE_HEARTBEAT = new ClusterNodeHeartbeatFactory();

    /**
     * Entity Names as defined in entitymodel.xml.
     */
    public class Name
    {
        /** An alias for the "jiraaction" table entity name ("Action") that is in fact used for Comments. */
        public static final String APPLICATION_USER = "ApplicationUser";
        public static final String COMMENT = "Action";
        public static final String COMPONENT = "Component";
        public static final String CHANGE_GROUP = "ChangeGroup";
        public static final String CHANGE_ITEM = "ChangeItem";
        public static final String ENTITY_PROPERTY = "EntityProperty";
        public static final String ENTITY_PROPERTY_INDEX_DOCUMENT = "EntityPropertyIndexDocument";
        public static final String EXTERNAL_ENTITY = "ExternalEntity";
        public static final String GLOBAL_PERMISSION_ENTRY = "GlobalPermissionEntry";
        public static final String ISSUE = "Issue";
        public static final String OS_PROPERTY_ENTRY = "OSPropertyEntry";
        public static final String PROJECT = "Project";
        public static final String RUN_DETAILS = "RunDetails";
        public static final String SCHEME_ISSUE_SECURITIES = "SchemeIssueSecurities";
        public static final String SERVICE_CONFIG = "ServiceConfig";
        public static final String USER = "User";
        public static final String VERSION = "Version";
    }
}
