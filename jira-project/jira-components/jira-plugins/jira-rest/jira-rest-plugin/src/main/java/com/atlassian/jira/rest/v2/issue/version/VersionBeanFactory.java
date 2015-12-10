package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.project.version.Version;

import java.util.Collection;
import java.util.List;

/**
 * Simple factory used to create version beans from versions.
 *
 * @since v4.4
 */
public interface VersionBeanFactory
{
    String VERSION_OPERATIONS_WEB_LOCATION = "atl.jira.version.admin.operations";

    /**
     * Create a VersionBean given the passed Version.
     * Will not include available operations or remote links.
     *
     * @param version the version to convert.
     * @return the VersionBean from the passed Version.
     */
    VersionBean createVersionBean(Version version);

    /**
     * Create a VersionBean given the passed Version and whether or not to include available operations,
     * but will not contain remote links.
     *
     * @param version the version to convert.
     * @param expandOps whether or not to include the operations
     * @return the VersionBean from the passed Version.
     * @deprecated Use {@link #createVersionBean(com.atlassian.jira.project.version.Version, boolean, boolean)} instead.
     *      Since JIRA REST v6.5.1 (JIRA v6.1.1)
     */
    VersionBean createVersionBean(Version version, boolean expandOps);

    /**
     * Create a VersionBean given the passed Version and whether or not to include available operations,
     * but will not contain remote links.
     *
     * @param version the version to convert.
     * @param expandOps whether or not to include the operations
     * @param expandRemoteLinks whether or not to include the remote entity links
     * @return the VersionBean from the passed Version.
     * @since JIRA REST v6.5.1 (JIRA v6.1.1)
     */
    VersionBean createVersionBean(Version version, boolean expandOps, boolean expandRemoteLinks);

    /**
     * Create a list of VersionBeans given the passed Versions.
     * This will not include available operations
     *
     * @param versions the versions to convert.
     * @return the VersionBeans for the passed Versions.
     */
    List<VersionBean> createVersionBeans(Collection<? extends Version> versions);

    /**
     * Create a list of VersionBeans given the passed Versions.
     *
     * @param versions the versions to convert.
     * @param expandOps whether or not to include available operations.
     * @return the VersionBeans for the passed Versions.
     * @deprecated Use {@link #createVersionBeans(Collection, boolean, boolean)} instead.
     *      Since JIRA REST v6.5.1 (JIRA v6.1.1)
     */
    List<VersionBean> createVersionBeans(Collection<? extends Version> versions, boolean expandOps);

    /**
     * Create a list of VersionBeans given the passed Versions.
     *
     * @param versions the versions to convert.
     * @param expandOps whether or not to include available operations.
     * @param expandRemoteLinks whether or not to include the remote entity links
     * @return the VersionBeans for the passed Versions.
     * @since JIRA REST v6.5.1 (JIRA v6.1.1)
     */
    List<VersionBean> createVersionBeans(Collection<? extends Version> versions, boolean expandOps, boolean expandRemoteLinks);
}
