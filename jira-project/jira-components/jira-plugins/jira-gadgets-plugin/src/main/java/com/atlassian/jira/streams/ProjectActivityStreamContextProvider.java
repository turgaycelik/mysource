package com.atlassian.jira.streams;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.view.GadgetViewFactory;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.streams.spi.ActivityProviderModuleDescriptor;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Context provider to render the activity stream for a JIRA project. The activity stream will contain only activity
 * from the local JIRA instance, and will exclude activity from linked applications (e.g. Bamboo, Confluence etc.).
 *
 * @since v5.2
 */
public class ProjectActivityStreamContextProvider implements ContextProvider
{
    private static final Logger log = Logger.getLogger(ProjectActivityStreamContextProvider.class);

    private static final String PREF_IS_CONFIGURED = "isConfigured";
    private static final String PREF_RULES = "rules";
    private static final String GADGET_URI = "rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin/gadgets/activitystream-gadget.xml";
    private static final String PREF_NUMOFENTRIES = "numofentries";
    private static final String NUMOFENTRIES = "10";
    private static final String PREFS_IS_CONFIGURABLE = "isConfigurable";
    private static final String PREF_TITLE_REQUIRED = "titleRequired";

    private final PluginAccessor pluginAccessor;
    private final GadgetViewFactory gadgetViewFactory;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;

    public ProjectActivityStreamContextProvider(final PluginAccessor pluginAccessor, final GadgetViewFactory gadgetViewFactory,
            final GadgetRequestContextFactory gadgetRequestContextFactory)
    {
        this.pluginAccessor = pluginAccessor;
        this.gadgetViewFactory = gadgetViewFactory;
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final String projectKey = (String) context.get("projectKey");

        return MapBuilder.newBuilder(context)
                .add("gadgetHtml", getGadgetHtml(projectKey))
                .toMap();
    }

    private String getGadgetHtml(final String projectKey)
    {
        final MapBuilder<String, String> prefsBuilder = MapBuilder.newBuilder();
        prefsBuilder.add(PREF_IS_CONFIGURED, Boolean.TRUE.toString());
        prefsBuilder.add(PREFS_IS_CONFIGURABLE, Boolean.FALSE.toString());
        prefsBuilder.add(PREF_RULES, getJiraOnlyRulesJson(projectKey));
        prefsBuilder.add(PREF_NUMOFENTRIES, NUMOFENTRIES);
        prefsBuilder.add(PREF_TITLE_REQUIRED, Boolean.FALSE.toString());

        final GadgetState gadget = GadgetState.gadget(GadgetId.valueOf("1")).specUri(URI.create(GADGET_URI)).userPrefs(prefsBuilder.toMap()).build();
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Writer gadgetWriter = new OutputStreamWriter(baos);
            final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(ExecutingHttpRequest.get());
            final View settings = new View.Builder().viewType(ViewType.DEFAULT).writable(false).build();
            gadgetViewFactory.createGadgetView(gadget, ModuleId.valueOf(1L), settings, requestContext).writeTo(gadgetWriter);
            gadgetWriter.flush();

            return baos.toString();
        }
        catch (IOException e)
        {
            log.error("Error rendering activity stream gadget.", e);
        }
        catch (RuntimeException e)
        {
            log.error("Runtime error rendering activity stream gadget.", e);
        }

        return "";
    }

    private String getJiraOnlyRulesJson(final String projectKey)
    {
        List<ActivityProviderModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(ActivityProviderModuleDescriptor.class);

        final List<Provider> providers = new ArrayList<Provider>();
        providers.add(new Provider("streams", new Rule("key", "is", Arrays.asList(projectKey), "select")));

        // To make only JIRA activity items appear, we need to add a dud provider which is disabled, so that the
        // activity streams plugin will generate a list of enabled providers
        providers.add(new DisabledProvider("dud"));

        for (final ActivityProviderModuleDescriptor descriptor : descriptors)
        {
            providers.add(new Provider(descriptor.getKey()));
        }

        final Rules rules = new Rules(providers);
        return rules.toJSONObject().toString();
    }

    private interface JsonObjectable
    {
        JSONObject toJSONObject();
    }

    private static class Rules implements JsonObjectable
    {
        private final List<Provider> providers;

        private Rules(List<Provider> providers)
        {
            this.providers = providers;
        }

        @Override
        public JSONObject toJSONObject()
        {
            final Map<String, Object> map = MapBuilder.<String, Object>newBuilder()
                    .add("providers", listToJSONArray(providers))
                    .toMap();
            return new JSONObject(map);
        }
    }

    private static class Provider implements JsonObjectable
    {
        protected final String provider;
        protected final List<Rule> rules;

        public Provider(String provider, Rule ... rules)
        {
            this.provider = provider;
            this.rules = Arrays.asList(rules);
        }

        public Map<String, Object> toMap()
        {
            return MapBuilder.<String, Object>newBuilder()
                    .add("provider", provider)
                    .add("rules", listToJSONArray(rules))
                    .toMap();
        }

        public JSONObject toJSONObject()
        {
            return new JSONObject(toMap());
        }
    }

    private static class DisabledProvider extends Provider
    {
        private DisabledProvider(String provider, Rule ... rules)
        {
            super(provider, rules);
        }

        public JSONObject toJSONObject()
        {
            final Map<String, Object> map = MapBuilder.<String, Object>newBuilder(toMap())
                    .add("disabled", true)
                    .toMap();
            return new JSONObject(map);
        }
    }

    private static class Rule implements JsonObjectable
    {
        private final String rule;
        private final String operator;
        private final List<String> value;
        private final String type;

        public Rule(String rule, String operator, List<String> value, String type)
        {
            this.rule = rule;
            this.operator = operator;
            this.value = value;
            this.type = type;
        }

        public JSONObject toJSONObject()
        {
            final Map<String, Object> map = MapBuilder.<String, Object>newBuilder()
                    .add("rule", rule)
                    .add("operator", operator)
                    .add("value", new JSONArray(value))
                    .add("type", type)
                    .toMap();
            return new JSONObject(map);
        }
    }

    private static JSONArray listToJSONArray(final List<? extends JsonObjectable> list)
    {
        final List<JSONObject> jsonObjects = new ArrayList<JSONObject>(list.size());
        for (final JsonObjectable obj : list)
        {
            jsonObjects.add(obj.toJSONObject());
        }

        return new JSONArray(jsonObjects);
    }
}
