package com.atlassian.jira.action.admin;

import java.util.List;

import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableMap;

import org.ofbiz.core.entity.GenericValue;

/**
 * Serves one purpose in life: to downgrade upgrade tasks that are destructive.
 * <p>
 * The general rule is that {@code downgrade()} should always be a no-op on {@code master}.  If an upgrade task
 * is introduced on {@code master} that requires special treatment in a downgrade, put that logic in this class
 * on the branch, and delete it as part of the merge to master.  You will also need to change the minimum downgrade
 * version that master has in {@code pom.xml} and {@code jira-distribution/pom.xml} to specify the stable release
 * that the downgrade step is first going into.  Please also give the versions for which the downgrade matters,
 * what is getting downgraded, and why.
 * </p>
 *
 * @since v5.2.8
 */
public class OfBizDowngradeHandler
{
    private final OfBizDelegator ofBizDelegator;

    public OfBizDowngradeHandler(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    /**
     * Called to perform special downgrade tasks.
     * <p>
     * This method is only invoked when we have identified that you are importing data from a newer version of
     * JIRA, presumably because an OnDemand customer has exported JIRA data to import into their on premise
     * instance.
     * </p>
     */
    public void downgrade()
    {
        disableProjectCentricNav();
    }

    /**
     * When a customer downgrades from JIRA 6.4 OD to 6.3.x BTF, the values for the Dark Features related to
     * the jira-projects plugin will be applied to their BTF instance.
     *
     * If those Dark Features were enabled on their OD instance, then they will also be enabled on their BTF
     * instance after the downgrade and JAG will try to render the sidebar.
     *
     * While the latest version of JAG is using the latest jira-projects' API, the version of jira-projects bundled
     * on 6.3.x is quite old. When JAG tries to render the sidebar, it will call some methods on jira-projects that
     * are not defined at runtime on the BTF instance.
     *
     * To avoid this we are removing the Dark Feature values related with jira-projects, so JAG won't render the sidebar.
     */
    private void disableProjectCentricNav()
    {
        List<GenericValue> featureEntriesToRemove = ofBizDelegator.findByLike("Feature", ImmutableMap.of("featureName", "com.atlassian.jira.projects.ProjectCentricNavigation%"));
        ofBizDelegator.removeAll(featureEntriesToRemove);
    }
}
