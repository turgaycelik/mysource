package com.atlassian.jira.bc.project.version;

import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.util.Collections;
import java.util.Set;


/**
 * Encapsulates the result of validating a delete version service call
 *
 * @since v3.13
 */
final class ValidationResultImpl extends ServiceResultImpl implements VersionService.ValidationResult
{
    private Version versionToDelete;
    private Version fixSwapVersion;
    private Version affectsSwapVersion;
    private boolean isValid = false;
    private final Set<VersionService.ValidationResult.Reason> reasons;

    ValidationResultImpl(ErrorCollection errorCollection, final Version versionToDelete, final Version affectsSwapVersion, final Version fixSwapVersion, final boolean valid, Set<Reason> reasons)
    {
        super(errorCollection);
        this.versionToDelete = versionToDelete;
        this.fixSwapVersion = fixSwapVersion;
        this.affectsSwapVersion = affectsSwapVersion;
        isValid = valid;
        this.reasons = reasons;
    }

    public boolean isValid()
    {
        return isValid;
    }

    public void setValid(final boolean valid)
    {
        isValid = valid;
    }

    public Version getVersionToDelete()
    {
        return versionToDelete;
    }

    public void setVersionToDelete(final Version versionToDelete)
    {
        this.versionToDelete = versionToDelete;
    }

    public Version getFixSwapVersion()
    {
        return fixSwapVersion;
    }

    public void setFixSwapVersion(final Version fixSwapVersion)
    {
        this.fixSwapVersion = fixSwapVersion;
    }

    public Version getAffectsSwapVersion()
    {
        return affectsSwapVersion;
    }

    public void setAffectsSwapVersion(final Version affectsSwapVersion)
    {
        this.affectsSwapVersion = affectsSwapVersion;
    }

    @Override
    public Set<VersionService.ValidationResult.Reason> getReasons()
    {
        return reasons;
    }
}
