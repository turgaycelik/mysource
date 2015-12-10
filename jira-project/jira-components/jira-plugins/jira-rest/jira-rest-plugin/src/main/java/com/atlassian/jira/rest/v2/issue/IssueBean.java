package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.rest.api.issue.JsonTypeBean;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.SelfExpanding;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v4.2
 */
@SuppressWarnings ({ "UnusedDeclaration", "FieldCanBeLocal" })
@XmlRootElement (name = "issue")
public class IssueBean
{
    @XmlAttribute
    private String expand;

    @XmlElement
    private String id;

    @XmlElement
    private URI self;

    @XmlElement
    private String key;

    /*
     * Issue fields. The values in the map will always be a FieldBean.
     */
    @XmlElement
    private HashMap<String, Object> fields;

    @XmlElement
    private HashMap<String, Object> renderedFields;

    @XmlTransient
    private HashMap<String, Object> renderedFields_ = Maps.newHashMap();

    @XmlTransient
    @Expandable ("renderedFields")
    private SelfExpanding renderedFieldExpander = new SelfExpanding()
    {
        public void expand()
        {
            renderedFields = renderedFields_;
        }
    };

    @XmlElement
    private HashMap<String, String> names;

    @XmlTransient
    private HashMap<String, String> names_ = Maps.newHashMap();

    @XmlTransient
    @Expandable ("names")
    private SelfExpanding namesExpander = new SelfExpanding()
    {
        public void expand()
        {
            names = names_;
        }
    };

    @XmlElement
    private Map<String, JsonTypeBean> schema;

    @XmlTransient
    private final Map<String, JsonTypeBean> schema_ = Maps.newHashMap();

    @XmlTransient
    @Expandable ("schema")
    private SelfExpanding schemaExpander = new SelfExpanding()
    {
        public void expand()
        {
            schema = schema_;
        }
    };

    @XmlElement
    private List<TransitionBean> transitions;

    @XmlTransient
    private final List<TransitionBean> transitions_ = Lists.newArrayList();

    @XmlTransient
    @Expandable ("transitions")
    private SelfExpanding transitionExpander = new SelfExpanding()
    {
        public void expand()
        {
            transitions = transitions_;
        }
    };

    @XmlElement
    private OpsbarBean operations;

    @XmlTransient
    private OpsbarBean operations_ = null;

    @XmlTransient
    @Expandable ("operations")
    private SelfExpanding operationsExpander = new SelfExpanding()
    {
        public void expand()
        {
            operations = operations_;
        }
    };

    @XmlTransient
    @Expandable ("editmeta")
    private SelfExpanding editmetaExpander = new SelfExpanding()
    {
        public void expand()
        {
            editmeta = editmeta_;
        }
    };

    @XmlElement
    private EditMetaBean editmeta;
    @XmlTransient
    private EditMetaBean editmeta_;

    @XmlTransient
    @Expandable ("changelog")
    private SelfExpanding changelogExpander = new SelfExpanding()
    {
        public void expand()
        {
            changelog = changelog_;
        }
    };

    @XmlElement
    private ChangelogBean changelog;
    @XmlTransient
    private ChangelogBean changelog_;


    /**
     * The list of fields to include in the bean. If null, include all fields.
     */
    private IncludedFields fieldsToInclude;

    public IssueBean() {}

    public IssueBean(final Long id, final String key, URI selfUri)
    {
        this(id == null ? null : id.toString(), key, selfUri);
    }

    public IssueBean(final String id, final String key, URI selfUri)
    {
        this.id = id;
        this.self = selfUri;
        this.key = key;
    }

    public IssueBean fieldsToInclude(IncludedFields fieldsToInclude)
    {
        this.fieldsToInclude = fieldsToInclude;
        return this;
    }

    public IssueBean fields(Map<String, Object> fields)
    {
        this.fields = Maps.newHashMap(fields);
        return this;
    }

    public void addField(Field field, FieldJsonRepresentation data, boolean includeRenderedVersion)
    {
        JsonType schema;
        if (field instanceof RestAwareField)
        {
            schema = ((RestAwareField) field).getJsonSchema();
        }
        else
        {
            schema = null;
        }
        String fieldId = field.getId();

        if (fieldsToInclude == null || fieldsToInclude.included(field))
        {
            String displayName = field.getName();
            addRawField(fieldId, displayName, schema, data.getStandardData().getData());
            if (includeRenderedVersion && data.getRenderedData() != null)
            {
                renderedFields_.put(fieldId, data.getRenderedData().getData());
            }
        }
    }

    public void addParentField(IssueRefJsonBean value, String displayName)
    {
        String fieldId = "parent";
        JsonType schema = JsonTypeBuilder.systemArray(JsonType.ISSUELINKS_TYPE, "parent");
        if (fieldsToInclude == null || fieldsToInclude.included(fieldId, true))
        {
            addRawField(fieldId, displayName, schema, value);
        }

    }

    /**
     * package scope, so it can be called from Examples
     */
    void addRawField(String fieldId, String displayName, JsonType schema, Object value)
    {
        if (fields == null) { fields = Maps.newHashMap(); }
        names_.put(fieldId, displayName);
        if (schema != null)
        {
            schema_.put(fieldId, new JsonTypeBean(schema.getType(), schema.getItems(), schema.getSystem(), schema.getCustom(), schema.getCustomId()));
        }
        fields.put(fieldId, value);
    }

    public boolean hasField(String fieldId)
    {
        return fields != null && fields.containsKey(fieldId);
    }

//    public void addRenderedField(final String fieldId, final Object value)
//    {
//        if (fieldsToInclude == null || fieldsToInclude.contains(fieldId))
//        {
//            renderedFields_.put(fieldId, value);
//        }
//    }

    public String getKey()
    {
        return key;
    }

    public String getId()
    {
        return id;
    }

    public List<String> expand()
    {
        return StringList.fromQueryParam(expand).asList();
    }

    public IssueBean expand(Iterable<String> expand)
    {
        this.expand = expand != null ? StringList.fromList(expand).toQueryParam() : null;
        return this;
    }

    public Map<String, String> names()
    {
        return names;
    }

    public IssueBean names(@Nullable HashMap<String, String> names)
    {
        this.names = names;
        return this;
    }

    public Map<String, JsonTypeBean> schema()
    {
        return schema;
    }

    public IssueBean schema(@Nullable Map<String, JsonTypeBean> schema)
    {
        this.schema = schema;
        return this;
    }

    public IssueBean editmeta(EditMetaBean editmeta)
    {
        this.editmeta_ = editmeta;
        return this;
    }

    public void setTransitionBeans(List<TransitionBean> transitionBeans)
    {
        this.transitions_.addAll(transitionBeans);
    }

    public void setOperations(OpsbarBean opsbarBean)
    {
        this.operations_ = opsbarBean;
    }

    public IssueBean changelog(ChangelogBean changelog)
    {
        this.changelog_ = changelog;
        return this;
    }
}
