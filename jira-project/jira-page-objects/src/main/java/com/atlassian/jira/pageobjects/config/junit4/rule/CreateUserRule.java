package com.atlassian.jira.pageobjects.config.junit4.rule;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import com.atlassian.jira.functest.framework.util.junit.AnnotatedDescription;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.CreateUser;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.Sets;
import com.sun.jersey.api.client.UniformInterfaceException;

import org.json.JSONObject;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Creates new user via JIRA REST API.
 *
 * @see CreateUser
 */
public class CreateUserRule extends TestWatcher
{
    private boolean userCreated;

    @Inject
    private JiraTestedProduct jira;

    @Override
    protected void starting(final Description description)
    {
        final AnnotatedDescription annotatedDescription = new AnnotatedDescription(description);

        if (annotatedDescription.isMethodAnnotated(CreateUser.class))
        {
            doCreateUser(annotatedDescription.getAnnotation(CreateUser.class));
        }
    }

    @Override
    protected void finished(final Description description)
    {
        final AnnotatedDescription annotatedDescription = new AnnotatedDescription(description);

        if (annotatedDescription.isMethodAnnotated(CreateUser.class))
        {
            doDeleteUser(annotatedDescription.getAnnotation(CreateUser.class));
        }
    }

    private void doCreateUser(final CreateUser createUser)
    {
        final String username = createUser.username();
        final String password = createUser.password();
        final String[] groupnames = createUser.groupnames();

        if (!userExists(username))
        {
            createUser(username, password);
            // later, allow to delete just users created by this
            userCreated = true;
        }

        final Set<String> groups = Sets.newHashSet(groupnames);
        if (createUser.admin())
        {
            groups.add(administratorsGroup());
        }
        if (createUser.developer())
        {
            groups.add(developersGroup());
        }

        // add user to all determined groups
        if (!groups.isEmpty())
        {
            addUserToGroups(username, groups);
        }

        // user is by default in "users" group, so we remove him, if needed
        if (!createUser.user())
        {
            removeUserFromGroups(username, Collections.singletonList(usersGroup()));
        }
    }

    private void doDeleteUser(final CreateUser createUser)
    {
        final String username = createUser.username();

        if (userExists(username) && userCreated)
        {
            deleteUser(username);
        }
    }

    private boolean userExists(final String username)
    {
        try
        {
            jira.backdoor().getTestkit().rawRestApiControl().rootResource().path("user")
                    .queryParam("username", username).get(String.class);
            return true;
        }
        catch (final UniformInterfaceException e)
        {
            return false;
        }
    }

    private void createUser(final String username, final String password)
    {
        final JSONObject json = new JSONObject(MapBuilder.newBuilder().add("name", username).add("password", password)
                .add("emailAddress", username + "@example.com").add("displayName", "Test User " + username).toMap());
        jira.backdoor().getTestkit().rawRestApiControl().rootResource().path("user").post(json.toString());
    }

    private void deleteUser(final String username)
    {
        jira.backdoor().getTestkit().rawRestApiControl().rootResource().path("user").queryParam("username", username)
                .delete();
    }

    private void addUserToGroups(final String username, final Iterable<String> groupnames)
    {
        for (final String groupname : groupnames)
        {
            final JSONObject json = new JSONObject(MapBuilder.newBuilder().add("name", username).toMap());
            jira.backdoor().getTestkit().rawRestApiControl().rootResource().path("group").path("user")
                    .queryParam("groupname", groupname).post(json.toString());
        }
    }

    private void removeUserFromGroups(final String username, final Iterable<String> groupnames)
    {
        for (final String groupname : groupnames)
        {
            jira.backdoor().getTestkit().rawRestApiControl().rootResource().path("group").path("user")
                    .queryParam("groupname", groupname).queryParam("username", username).delete();
        }
    }

    private String administratorsGroup()
    {
        return jira.isOnDemand() ? "administrators" : "jira-administrators";
    }

    private String developersGroup()
    {
        return jira.isOnDemand() ? "developers" : "jira-developers";
    }

    private String usersGroup()
    {
        return "jira-users";
    }

}
