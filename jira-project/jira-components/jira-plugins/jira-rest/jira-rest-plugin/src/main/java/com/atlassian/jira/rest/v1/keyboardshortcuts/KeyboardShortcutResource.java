package com.atlassian.jira.rest.v1.keyboardshortcuts;

import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcut;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Context;
import com.atlassian.jira.plugin.webfragment.DefaultWebFragmentContext;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides access to the keyboard shortcuts currently registered in the plugin system.
 *
 * @since v4.1
 */
@Path ("shortcuts")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@CorsAllowed
public class KeyboardShortcutResource
{
    private final KeyboardShortcutManager keyboardShortcutManager;
    private final JaxbJsonMarshaller jaxbJsonMarshaller;

    public KeyboardShortcutResource(final KeyboardShortcutManager keyboardShortcutManager)
    {
        this.keyboardShortcutManager = keyboardShortcutManager;
        this.jaxbJsonMarshaller = new DefaultJaxbJsonMarshaller();
    }

    /**
     * Returns a JSON representation of the keyboard shortcuts requested (which always
     * includes the global context shortcuts).
     *
     * @param contextNames keyboard shortcut contexts to include along with the global context
     * @return a JSON representation of the keyboard shortcuts requested (always includes global shortcuts)
     */
    @GET
    @Path ("{buildnumber}/{hashcode}/shortcuts.js")
    @Produces ({ "text/javascript" })
    public Response getShortCuts(@QueryParam ("context") final Set<String> contextNames)
    {
        List<KeyboardShortcut> sortedShortcuts = getKeyboardShortcutsFor(contextNames);
        try
        {
            return Response.ok("AJS.keys = " + jaxbJsonMarshaller.marshal(new Shortcuts(sortedShortcuts),
                    Shortcuts.class, Shortcut.class) + ";").cacheControl(CacheControl.CACHE_FOREVER).build();
        }
        catch (JAXBException e)
        {
            return Response.noContent().cacheControl(CacheControl.NO_CACHE).build();
        }
    }

    protected List<KeyboardShortcut> getKeyboardShortcutsFor(Set<String> contextNames)
    {
        final Set<Context> contexts = retrieveSelectedContexts(contextNames);
        final List<KeyboardShortcut> activeShortcutsUniquePerContext = getActiveShortcutsUniquePerContext();
        final List<KeyboardShortcut> result = filterShortcutsBySelectedContexts(activeShortcutsUniquePerContext, contexts);
        return result;
    }

    @Nonnull
    private Set<Context> retrieveSelectedContexts(@Nullable final Set<String> contextNames)
    {
        final Set<Context> contexts = EnumSet.of(Context.global);
        if (contextNames != null)
        {
            for (String name : contextNames)
            {
                final Context context = Context.fromString(name);
                if (context != null)
                {
                    contexts.add(context);
                }
            }
        }
        return contexts;
    }

    @Nonnull
    private List<KeyboardShortcut> getActiveShortcutsUniquePerContext()
    {
        final Map<String, Object> context = DefaultWebFragmentContext.get();
        return keyboardShortcutManager.listActiveShortcutsUniquePerContext(context);
    }

    @Nonnull
    private List<KeyboardShortcut> filterShortcutsBySelectedContexts(@Nonnull final List<KeyboardShortcut> activeShortcutsUniquePerContext, @Nonnull final Set<Context> contexts)
    {
        final Map<Set<List<String>>, KeyboardShortcut> ret = new HashMap<Set<List<String>>, KeyboardShortcut>();

        for (KeyboardShortcut shortcut : activeShortcutsUniquePerContext)
        {
            if(contexts.contains(shortcut.getContext()))
            {
                //putting shortcuts into a map keyed by the shortcut keys to basically override
                //shortcuts with the same shortcut keys.  Shortcuts with a higher order attribute will
                //override shortcuts with the same keys with a lower order attribute.
                ret.put(shortcut.getShortcuts(), shortcut);
            }
        }

        List<KeyboardShortcut> sortedShortcuts = new ArrayList<KeyboardShortcut>(ret.values());
        Collections.sort(sortedShortcuts);
        return sortedShortcuts;
    }

    @XmlRootElement
    public static class Shortcuts
    {
        @XmlElement
        final List<Shortcut> shortcuts = new ArrayList<Shortcut>();

        private Shortcuts() {}

        public Shortcuts(Collection<KeyboardShortcut> origShortcuts)
        {
            for (KeyboardShortcut origShortcut : origShortcuts)
            {
                shortcuts.add(new Shortcut(origShortcut));
            }
        }
    }

    @XmlRootElement
    public static class Shortcut
    {
        @XmlElement
        private String moduleKey;
        @XmlElement
        private Set<List<String>> keys;
        @XmlElement
        private String context;
        @XmlElement
        private String op;
        @XmlElement
        private String param;

        private Shortcut() {}

        public Shortcut(KeyboardShortcut shortcut)
        {
            this.moduleKey = shortcut.getModuleKey();
            this.keys = new LinkedHashSet<List<String>>(shortcut.getShortcuts());
            this.context = shortcut.getContext().toString();
            this.op = shortcut.getOperation().toString();
            this.param = shortcut.getParameter();
        }
    }
}
