package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.bind.DateAdapter;
import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.atlassian.jira.rest.v2.entity.RemoteEntityLinkJsonBean;
import com.atlassian.jira.rest.v2.issue.Examples;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
* @since v4.2
*/
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="version")
public class VersionBean
{
    public static final String EXPAND_OPERATIONS = "operations";
    public static final String EXPAND_REMOTE_LINKS = "remotelinks";

    /**
     * A version bean instance used for auto-generated documentation.
     */
    static final VersionBean DOC_EXAMPLE;
    static final VersionBean DOC_EXAMPLE_2;
    static final List<VersionBean> DOC_EXAMPLE_LIST = new ArrayList<VersionBean>();
    static final VersionBean DOC_CREATE_EXAMPLE;
    static
    {
        final DateFormat exampleDateFormat = new SimpleDateFormat("d/MMM/yyyy", Locale.ENGLISH);

        VersionBean version = new VersionBean.Builder().build();
        version.self = Examples.restURI("version/10000");
        version.id = "10000";
        version.name = "New Version 1";
        version.description = "An excellent version";
        version.archived = false;
        version.released = true;
        version.overdue = true;
        version.releaseDate = new Date(1278385482288L);
        version.userReleaseDate = exampleDateFormat.format(version.releaseDate);
        version.projectId = 10000l;

        DOC_EXAMPLE = version;

        version = new VersionBean.Builder().build();
        version.self = Examples.restURI("version/10010");
        version.id = "10010";
        version.name = "Next Version";
        version.description = "Minor Bugfix version";
        version.archived = false;
        version.released = false;
        version.overdue = false;
        version.projectId = 10000l;

        DOC_EXAMPLE_2 = version;
        DOC_EXAMPLE_LIST.add(DOC_EXAMPLE);
        DOC_EXAMPLE_LIST.add(DOC_EXAMPLE_2);

        version = new VersionBean.Builder().build();
        version.project = "PXA";
        version.projectId = 10000l;
        version.name = "New Version 1";
        version.description = "An excellent version";
        version.archived = false;
        version.released = true;
        version.releaseDate = new Date(1278385482288L);
        version.userReleaseDate = exampleDateFormat.format(version.releaseDate);

        DOC_CREATE_EXAMPLE = version;

    }

    @XmlAttribute
    private String expand;

    @XmlElement
    private URI self;

    @XmlElement
    private String id;

    @XmlElement
    private String description;

    @XmlElement
    private String name;

    @XmlElement
    private Boolean archived;

    @XmlElement
    private Boolean released;

    private Date startDate;

    @XmlTransient
    private boolean startDateSet = false;

    private Date releaseDate;

    /** This field is used to trap the fact the Release Date has been set, even though it may have been set to null. */
    @XmlTransient
    private boolean releaseDateSet = false;

    @XmlElement
    private Boolean overdue;

    @XmlElement
    private String userStartDate;

    @XmlElement
    private String userReleaseDate;

    /**
     * @deprecated use projectId instead. Remove this with 7.0.
     */
    @XmlElement
    @Deprecated
    private String project;

    @XmlElement
    private Long projectId;

    @XmlElement
    private URI moveUnfixedIssuesTo;

    @XmlElement
    private ArrayList<SimpleLinkBean> operations;

    @XmlElement
    private List<RemoteEntityLinkJsonBean> remotelinks;

    public String getProject()
    {
        return project;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public String getUserStartDate()
    {
        return userStartDate;
    }

    public String getUserReleaseDate()
    {
        return userReleaseDate;
    }

    public Boolean getOverdue()
    {
        return overdue;
    }

    @XmlElement
    @XmlJavaTypeAdapter (DateAdapter.class)
    public Date getStartDate()
    {
        return startDate;
    }

    @XmlTransient
    public boolean isStartDateSet()
    {
        return startDateSet;
    }

    @XmlElement
    @XmlJavaTypeAdapter (DateAdapter.class)
    public Date getReleaseDate()
    {
        return releaseDate;
    }

    @XmlTransient
    public boolean isReleaseDateSet()
    {
        return releaseDateSet;
    }

    public Boolean isReleased()
    {
        return released;
    }

    public Boolean isArchived()
    {
        return archived;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getExpand()
    {
        return expand;
    }

    public URI getSelf()
    {
        return self;
    }

    public URI getMoveUnfixedIssuesTo()
    {
        return moveUnfixedIssuesTo;
    }

    public void setSelf(URI self)
    {
        this.self = self;
    }

    public void setId(Long id)
    {
        this.id = id == null ? null : id.toString();
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setArchived(Boolean archived)
    {
        this.archived = archived;
    }

    public void setReleased(Boolean released)
    {
        this.released = released;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
        this.startDateSet = true;
    }

    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
        this.releaseDateSet = true;
    }

    public void setOverdue(Boolean overdue)
    {
        this.overdue = overdue;
    }

    public void setUserStartDate(String userStartDate)
    {
        this.userStartDate = userStartDate;
    }

    public void setUserReleaseDate(String userReleaseDate)
    {
        this.userReleaseDate = userReleaseDate;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public void setMoveUnfixedIssuesTo(URI moveUnfixedIssuesTo)
    {
        this.moveUnfixedIssuesTo = moveUnfixedIssuesTo;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    //Needed so that JAXB works.
    public VersionBean() {}

    private VersionBean(Long id, String project, Long projectId, URI self, String name, String description, boolean archived, boolean released,
            Date startDate, boolean startDateSet, String userStartDate,
            Date releaseDate, boolean releaseDateSet, String userReleaseDate,
            Boolean overdue, ArrayList<SimpleLinkBean> operations, List<RemoteEntityLinkJsonBean> remotelinks,
            String expand)
    {
        this.id = id == null ? null : id.toString();
        this.self = self;
        this.description = description;
        this.name = name;
        this.archived = archived;
        this.released = released;

        this.startDate = startDate;
        this.startDateSet = startDateSet;
        this.userStartDate = userStartDate;

        this.releaseDate = releaseDate;
        this.releaseDateSet = releaseDateSet;
        this.userReleaseDate = userReleaseDate;

        this.overdue = overdue;
        this.project = project;
        this.projectId = projectId;
        this.operations = operations;
        this.remotelinks = remotelinks;
        this.expand = expand;
    }

    public static class Builder
    {
        private URI self;
        private Long id;
        private String description;
        private String name;
        private boolean archived;
        private boolean released;
        private Date startDate;
        private boolean startDateSet;
        private Date releaseDate;
        private boolean releaseDateSet;
        private Boolean overdue;
        private String userStartDate;
        private String userReleaseDate;
        /**
         * @deprecated use projectId instead. Remove with 7.0.
         */
        @Deprecated
        private String project;
        private Long projectId;
        private ArrayList<SimpleLinkBean> operations;
        private List<RemoteEntityLinkJsonBean> remotelinks;
        private String expand;

        public URI getSelf()
        {
            return self;
        }

        public Builder setSelf(URI self)
        {
            this.self = self;
            return this;
        }

        public Builder setVersion(Version version)
        {
            this.id = version.getId();
            this.name = version.getName();
            this.description = StringUtils.stripToNull(version.getDescription());
            this.startDate = version.getStartDate();
            this.releaseDate = version.getReleaseDate();
            this.archived = version.isArchived();
            this.released = version.isReleased();
            return this;
        }

        public Long getId()
        {
            return id;
        }

        public Builder setId(Long id)
        {
            this.id = id;
            return this;
        }

        public String getDescription()
        {
            return description;
        }

        public Builder setDescription(String description)
        {
            this.description = description;
            return this;
        }

        public String getName()
        {
            return name;
        }

        public Builder setName(String name)
        {
            this.name = name;
            return this;
        }

        public boolean isArchived()
        {
            return archived;
        }

        public Builder setArchived(boolean archived)
        {
            this.archived = archived;
            return this;
        }

        public boolean isReleased()
        {
            return released;
        }

        public Builder setReleased(boolean released)
        {
            this.released = released;
            return this;
        }

        public Date getStartDate()
        {
            return startDate;
        }

        public Builder setStartDate(Date startDate)
        {
            this.startDate = startDate;
            this.startDateSet = true;
            return this;
        }

        public Date getReleaseDate()
        {
            return releaseDate;
        }

        public Builder setReleaseDate(Date releaseDate)
        {
            this.releaseDate = releaseDate;
            this.releaseDateSet = true;
            return this;
        }

        public Boolean getOverdue()
        {
            return overdue;
        }

        public Builder setOverdue(Boolean overdue)
        {
            this.overdue = overdue;
            return this;
        }

        public String getUserStartDate()
        {
            return userStartDate;
        }

        public Builder setUserStartDate(String userStartDate)
        {
            this.userStartDate = userStartDate;
            return this;
        }

        public String getUserReleaseDate()
        {
            return userReleaseDate;
        }

        public Builder setUserReleaseDate(String userReleaseDate)
        {
            this.userReleaseDate = userReleaseDate;
            return this;
        }

        public String getProject()
        {
            return project;
        }

        public Builder setProject(String project)
        {
            this.project = project;
            return this;
        }

        public ArrayList<SimpleLinkBean> getOperations()
        {
            return operations;
        }

        public Builder setOperations(ArrayList<SimpleLinkBean> operations)
        {
            this.operations = operations;
            return this;
        }

        public List<RemoteEntityLinkJsonBean> getRemoteLinks()
        {
            return remotelinks;
        }

        public Builder setRemoteLinks(List<RemoteEntityLinkJsonBean> remotelinks)
        {
            this.remotelinks = remotelinks;
            return this;
        }

        public Builder setProjectId(final Long projectId)
        {
            this.projectId = projectId;
            return this;
        }

        public Builder setExpand(final String expand)
        {
            this.expand = expand;
            return this;
        }

        public VersionBean build()
        {
            return new VersionBean(id, project, projectId, self, name, description, archived, released,
                    startDate, startDateSet, userStartDate, releaseDate, releaseDateSet,
                    userReleaseDate, overdue, operations, remotelinks, expand);
        }
    }

}
