package com.atlassian.jira.functest.config.sharing;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;

/**
 * Class that cleans up share permissions associated with a {@link ConfigSharedEntity}.
 *
 * @since v4.2
 */
public class SharePermissionsCleaner implements ConfigSharedEntityCleaner
{
    private final Document document;

    public SharePermissionsCleaner(final Document document)
    {
        this.document = document;
    }

    public boolean clean(final ConfigSharedEntity entity)
    {
        final List<Element> elementList = getElementsForEntity(entity);
        if (elementList.isEmpty())
        {
            return false;
        }

        for (Element element : elementList)
        {
            ConfigXmlUtils.removeElement(element);
        }
        return true;
    }

    private List<Element> getElementsForEntity(final ConfigSharedEntity entity)
    {
        if (entity.getId() == null || StringUtils.isBlank(entity.getEntityType()))
        {
            return Collections.emptyList();
        }

        return ConfigXmlUtils.getElementsByXpath(document,
                String.format("/entity-engine-xml/SharePermissions[@entityId='%d' and @entityType='%s']", entity.getId(), entity.getEntityType()));
    }
}
