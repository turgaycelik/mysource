package com.atlassian.jira.issue.fields.screen.issuetype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.screen.AbstractGVBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;

import org.ofbiz.core.entity.GenericValue;

public class IssueTypeScreenSchemeImpl extends AbstractGVBean implements IssueTypeScreenScheme
{
    private Long id;
    private String name;
    private String description;

    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private Map<String, IssueTypeScreenSchemeEntity> schemeEntities;

    public IssueTypeScreenSchemeImpl(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, GenericValue genericValue)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        setGenericValue(genericValue);
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            this.id = getGenericValue().getLong("id");
            this.name = getGenericValue().getString("name");
            this.description = getGenericValue().getString("description");
        }

        setModified(false);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        if (getGenericValue() != null)
        {
            throw new IllegalStateException("Cannot change id of an existing entity.");
        }
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        updateGV("name", name);
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
        updateGV("description", description);
    }

    public void store()
    {
        if (isModified())
        {
            if (getGenericValue() == null)
            {
                issueTypeScreenSchemeManager.createIssueTypeScreenScheme(this);
            }
            else
            {
                issueTypeScreenSchemeManager.updateIssueTypeScreenScheme(this);
                setModified(false);
            }
        }

        List<IssueTypeScreenSchemeEntity> schemeEntitiesToStore;
        synchronized (this)
        {
            schemeEntitiesToStore = (schemeEntities == null) ? Collections.EMPTY_LIST : new ArrayList<IssueTypeScreenSchemeEntity>(schemeEntities.values());
        }
        for (IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity : schemeEntitiesToStore)
        {
            issueTypeScreenSchemeEntity.store();
        }
    }

    public void remove()
    {
        if (getProjects().size() > 0)
        {
            throw new IllegalStateException("Cannot delete issue type screen scheme with project attached");
        }
        issueTypeScreenSchemeManager.removeIssueTypeSchemeEntities(this);

        if (getGenericValue() != null)
        {
            issueTypeScreenSchemeManager.removeIssueTypeScreenScheme(this);
        }
    }

    public Collection<IssueTypeScreenSchemeEntity> getEntities()
    {
        List<IssueTypeScreenSchemeEntity> entities = new LinkedList<IssueTypeScreenSchemeEntity>(getInternalEntities().values());
        Collections.sort(entities);
        return Collections.unmodifiableCollection(entities);
    }

    public IssueTypeScreenSchemeEntity getEntity(String issueTypeId)
    {
        return getInternalEntities().get(issueTypeId);
    }

    @Override
    @Nonnull
    public FieldScreenScheme getEffectiveFieldScreenScheme(@Nonnull IssueType type)
    {
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = getEntity(type.getId());
        if (issueTypeScreenSchemeEntity == null)
        {
            issueTypeScreenSchemeEntity = getEntity(null);
        }
        return issueTypeScreenSchemeEntity.getFieldScreenScheme();
    }

    public void addEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        issueTypeScreenSchemeEntity.setIssueTypeScreenScheme(this);
        recordEntity(issueTypeScreenSchemeEntity);
        store();
    }

    public void removeEntity(String issueTypeId)
    {
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity;
        synchronized (this)
        {
            if (!containsEntity(issueTypeId))
            {
                return;
            }
            issueTypeScreenSchemeEntity = getEntity(issueTypeId);

            // containsEntity call above calls getInternalEntities() which inits schemeEntities.
            // Cannot call getInternalEntities() as it returns an unmodifiable view of the schemeEntities map
            schemeEntities.remove(issueTypeId);
        }
        if (issueTypeScreenSchemeEntity != null)
        {
            issueTypeScreenSchemeEntity.remove();
        }
    }

    public boolean containsEntity(String issueTypeId)
    {
        return getInternalEntities().containsKey(issueTypeId);
    }

    public Collection<GenericValue> getProjects()
    {
        return issueTypeScreenSchemeManager.getProjects(this);
    }

    public boolean isDefault()
    {
        return DEFAULT_SCHEME_ID.equals(getId());
    }

    private synchronized Map<String, IssueTypeScreenSchemeEntity> getInternalEntities()
    {
        if (schemeEntities == null)
        {
            schemeEntities = new HashMap<String, IssueTypeScreenSchemeEntity>();
            for (Object o : issueTypeScreenSchemeManager.getIssueTypeScreenSchemeEntities(this))
            {
                IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = (IssueTypeScreenSchemeEntity) o;
                recordEntity(issueTypeScreenSchemeEntity);
            }
        }

        return Collections.unmodifiableMap(schemeEntities);
    }

    private synchronized void recordEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        getInternalEntities();
        if (issueTypeScreenSchemeEntity.getIssueTypeId() != null)
        {
            schemeEntities.put(issueTypeScreenSchemeEntity.getIssueTypeId(), issueTypeScreenSchemeEntity);
        }
        else
        {
            schemeEntities.put(null, issueTypeScreenSchemeEntity);
        }
    }

    @SuppressWarnings ("RedundantIfStatement")
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof IssueTypeScreenSchemeImpl))
        {
            return false;
        }

        final IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) o;

        if (description != null ? !description.equals(issueTypeScreenScheme.getDescription()) : issueTypeScreenScheme.getDescription() != null)
        {
            return false;
        }
        if (id != null ? !id.equals(issueTypeScreenScheme.getId()) : issueTypeScreenScheme.getId() != null)
        {
            return false;
        }
        if (name != null ? !name.equals(issueTypeScreenScheme.getName()) : issueTypeScreenScheme.getName() != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}