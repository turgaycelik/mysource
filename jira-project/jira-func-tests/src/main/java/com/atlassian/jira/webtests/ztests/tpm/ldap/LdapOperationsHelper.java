package com.atlassian.jira.webtests.ztests.tpm.ldap;

import java.util.LinkedList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.springframework.ldap.core.support.LdapContextSource;

/**
 *  LDAP helper class to simplify common operations like
 *  creating, removing and renaming users.
 *
 * @since v6.1
 */
public class LdapOperationsHelper
{

    private String server;
    private String userDn;
    private String password;
    private String baseDn;
    private boolean isActiveDirectory;

    public LdapOperationsHelper(final String server, final String userDn, final String password, final String baseDn, final boolean activeDirectory)
    {
        this.server = server;
        this.userDn = userDn;
        this.password = password;
        this.baseDn = baseDn;
        this.isActiveDirectory = activeDirectory;
    }

    public DirContext setupLdapContext() {

        final LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl((isActiveDirectory ?"ldaps://":"ldap://") + server + ":" + (isActiveDirectory ?"636": "389"));
        contextSource.setBase(baseDn);
        contextSource.setUserDn(userDn);
        contextSource.setPassword(password);

        // create a pool for when doing multiple calls.
        contextSource.setPooled(true);

        try
        {
            // we need to tell the context source to configure up our ldap server
            contextSource.afterPropertiesSet();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return contextSource.getContext(userDn, password);
    }

    public void renameUser(final String oldName, final String newName) throws NamingException
    {
        final DirContext ctx = setupLdapContext();
        ctx.rename("cn="+oldName, "cn="+newName);
        if (isActiveDirectory)
        {
            final Attributes attributes = new BasicAttributes();
            attributes.put("sAMAccountName", newName);
            ctx.modifyAttributes("cn="+newName, DirContext.REPLACE_ATTRIBUTE, attributes);
        }
    }


    public void removeUser(final String username) throws NamingException
    {
        final DirContext ctx = setupLdapContext();
        ctx.unbind("cn=" + username);
    }

    public List<String> getListOfUsers()
    {
        final DirContext ctx = setupLdapContext();

        LinkedList<String> list = new LinkedList();
        NamingEnumeration results = null;
        try
        {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = ctx.search("", "(objectclass=person)", controls);

            while (results.hasMore())
            {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                Attribute attr = attributes.get("cn");
                String cn = (String) attr.get();
                list.add(cn);
            }
        }
        catch (NameNotFoundException e)
        {
            // The base context was not found.
            // Just clean up and exit.
        }
        catch (NamingException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (results != null)
            {
                try
                {
                    results.close();
                }
                catch (Exception e)
                {
                    // Never mind this.
                }
            }
            if (ctx != null)
            {
                try
                {
                    ctx.close();
                }
                catch (Exception e)
                {
                    // Never mind this.
                }
            }
        }


        return list;
    }

}
