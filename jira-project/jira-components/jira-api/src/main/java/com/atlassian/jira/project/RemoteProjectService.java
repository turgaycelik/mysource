package com.atlassian.jira.project;

import java.util.Collection;
import java.util.List;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.remote.UnauthorisedAppLink;
import com.atlassian.sal.api.net.ResponseException;

/**
 * Offers access to projects in remote JIRA servers based on App Links.
 * <p/>
 * This service is marked as experimental because it is still being actively developed.
 */
@ExperimentalApi
public interface RemoteProjectService
{
    RemoteProject getRemoteProject(final ApplicationLink applicationLink, final String key)
            throws CredentialsRequiredException, ResponseException;

    /**
     * Finds all the projects that the currently logged in user can see in all the linked JIRA instances.
     *
     * @return RemoteProjectsResult object that contains the projects found as well as extra information about servers
     *          that we couldn't reach.
     */
    RemoteProjectsResult findAllRemoteProjects();

    static final class RemoteProjectsResult
    {
        private final List<RemoteProject> projects;
        private final Collection<UnauthorisedAppLink> unauthorisedAppLinks;
        private final Collection<String> connectionErrors;

        public RemoteProjectsResult(final List<RemoteProject> projects, final Collection<UnauthorisedAppLink> unauthorisedAppLinks, final Collection<String> connectionErrors)
        {
            this.projects = projects;
            this.unauthorisedAppLinks = unauthorisedAppLinks;
            this.connectionErrors = connectionErrors;
        }

        /**
         * Returns the remote projects that were successfully retrieved.
         *
         * @return the remote projects that were successfully retrieved.
         */
        public List<RemoteProject> getProjects()
        {
            return projects;
        }

        /**
         * Returns any app links that the user has to authenticate with before we can make requests.
         *
         * @return any app links that the user has to authenticate with before we can make requests.
         */
        public Collection<UnauthorisedAppLink> getUnauthorisedAppLinks()
        {
            return unauthorisedAppLinks;
        }

        /**
         * If any errors occurred during connection to remote servers, these are included here.
         *
         * @return a list of error messages for any server connection problems.
         */
        public Collection<String> getConnectionErrors()
        {
            return connectionErrors;
        }
    }
}
