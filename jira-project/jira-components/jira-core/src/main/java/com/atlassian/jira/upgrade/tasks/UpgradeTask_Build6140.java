
package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.crowd.embedded.ofbiz.EntityAttributeCondition;
import com.atlassian.jira.entity.ApplicationUserEntityFactory;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.ApplicationUserEntity;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import static org.ofbiz.core.entity.EntityOperator.EQUALS;

// JDEV-23962

/**
 * Upgrade task to convert username fields in Scheme Issue Securities.
 * These should have been covered in in the original upgrade but were overlooked, so we have to retry them, here.
 *
 * @since v6.0.8
 */
public class UpgradeTask_Build6140 extends AbstractUpgradeTask
{
    private static final String ENTITY_UPGRADE_HISTORY = "UpgradeHistory";
    private static final String FIELD_UPGRADECLASS = "upgradeclass";
    private static final String UPGRADE_6108 = "com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6108";
    private final Logger log = Logger.getLogger(getClass());
    private final EntityEngine entityEngine;
    private final OfBizDelegator ofBizDelegator;

    private final Cache<String, String> userKeyMapping = CacheBuilder.newBuilder().build(new CacheLoader<String, String>()
    {
        @Override
        public String load(String username)
        {
            String lowerName = IdentifierUtils.toLowerCase(username);
            ApplicationUserEntity appUser = entityEngine.selectFrom(Entity.APPLICATION_USER).whereEqual(ApplicationUserEntityFactory.USER_KEY, lowerName).singleValue();
            return appUser == null ? username : appUser.getKey();
        }
    });

    public UpgradeTask_Build6140(EntityEngine entityEngine, OfBizDelegator ofBizDelegator)
    {
        super(false);
        this.entityEngine = entityEngine;
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "6140";
    }

    @Override
    public String getShortDescription()
    {
        return "Convert Issue Security username references to user keys";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final List<GenericValue> upgradeHistoryList = ofBizDelegator.findByCondition(ENTITY_UPGRADE_HISTORY, new EntityExpr(FIELD_UPGRADECLASS, EQUALS, UPGRADE_6108), null);
        if (upgradeHistoryList.size() > 0)
        {
            log.info("Update username references in scheme issue securities already done in upgrade 6108.");
            return;
        }

        log.info("Updating username references in scheme issue securities ...");

        List<IssueSecurityLevelPermission> userPermissions = entityEngine.selectFrom(Entity.ISSUE_SECURITY_LEVEL_PERMISSION).whereEqual("type", "user").list();
        for (IssueSecurityLevelPermission userPermission : userPermissions)
        {
            String username = userPermission.getParameter();
            String key = userKeyMapping.get(username);
            if (!key.equals(username))
            {
                IssueSecurityLevelPermission newUserPermission =
                        new IssueSecurityLevelPermission(userPermission.getId(), userPermission.getSchemeId(), userPermission.getSecurityLevelId(), userPermission.getType(), key);
                entityEngine.updateValue(Entity.ISSUE_SECURITY_LEVEL_PERMISSION, newUserPermission);
            }
        }
    }
}
