/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.xmlrpc;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;

public class RpcTestClient
{
    public static void main(String[] args) throws Exception
    {
        try
        {
            XmlRpcClient xmlrpc = new XmlRpcClient("http://localhost:8080/rpc/xmlrpc");

            System.out.println("\n\n\nFirst let's be naughty and try to call a method without logging in");
            try
            {
                xmlrpc.execute("jira1.getProjects", makeParams("foo"));
                System.out.println("Should have blown up - what happened!");
                System.exit(-12);
            }
            catch (Exception e)
            {
                System.out.println("Good - we failed!");
                e.printStackTrace();
            }

            System.out.println("\n\n\nNow logging in properly... ");

            String token = (String) xmlrpc.execute("jira1.login", makeParams("mike@atlassian.com", "cube"));
            System.out.println("login token = " + token);

            System.out.println("\n\n\nNow let's get some projects... ");

            Vector spaces = (Vector) xmlrpc.execute("jira1.getProjects", makeParams(token));
            System.out.println("projects = " + spaces);

            Hashtable project = (Hashtable) spaces.get(2);
            System.out.println("project = " + project);

            String projectKey = (String) project.get("key");

            System.out.println("\n\n\nNow let's get more the versions and components...");

            Vector versions = (Vector) xmlrpc.execute("jira1.getVersions", makeParams(token, projectKey));
            System.out.println("versions = " + versions);

            Vector components = (Vector) xmlrpc.execute("jira1.getComponents", makeParams(token, projectKey));
            System.out.println("components = " + components);

            System.out.println("\n\n\nNow let's get some constants... ");
            System.out.println("issue types = " + xmlrpc.execute("jira1.getIssueTypes", makeParams(token)));
            System.out.println("priorities = " + xmlrpc.execute("jira1.getPriorities", makeParams(token)));
            System.out.println("statuses  = " + xmlrpc.execute("jira1.getStatuses", makeParams(token)));
            System.out.println("resolutions = " + xmlrpc.execute("jira1.getResolutions", makeParams(token)));

            System.out.println("\n\n\nCan we retrieve a user?");
            System.out.println("user = " + xmlrpc.execute("jira1.getUser", makeParams(token, "mike@atlassian.com")));
            System.out.println("user = " + xmlrpc.execute("jira1.getUser", makeParams(token, "peduso")));

            System.out.println("\n\n\nCan we retrieve our search requests?");
            System.out.println("filters = " + xmlrpc.execute("jira1.getSavedFilters", makeParams(token)));

            System.out.println("\n\n\nNow we should be nice and logout... ");

            Boolean loggedOut = (Boolean) xmlrpc.execute("jira1.logout", makeParams(token));
            System.out.println("loggedOut = " + loggedOut);

            System.out.println("\n\n\nOops, can't logout twice!");
            loggedOut = (Boolean) xmlrpc.execute("jira1.logout", makeParams(token));
            System.out.println("loggedOut = " + loggedOut);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(-1);
        }
    }

    private static Vector makeParams(Object p1)
    {
        Vector params;
        params = new Vector();
        params.add(p1);

        return params;
    }

    private static Vector makeParams(Object p1, Object p2)
    {
        Vector params = makeParams(p1);
        params.add(p2);
        return params;
    }

    private static Vector makeParams(Object p1, Object p2, Object p3)
    {
        Vector params = makeParams(p1, p2);
        params.add(p3);
        return params;
    }

    private static Vector makeParams(Object p1, Object p2, Object p3, Object p4)
    {
        Vector params = makeParams(p1, p2, p3);
        params.add(p4);
        return params;
    }

    private static Vector makeParams(Object p1, Object p2, Object p3, Object p4, Object p5)
    {
        Vector params = makeParams(p1, p2, p3, p4);
        params.add(p5);
        return params;
    }
}
