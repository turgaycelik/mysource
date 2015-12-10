package com.atlassian.jira.bc.project.version;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.PagerFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;

import static com.atlassian.fugue.Option.option;

/**
 * @since v3.13
 */
public class DefaultVersionService implements VersionService
{
    private static final Logger log = Logger.getLogger(DefaultVersionService.class);

    private final VersionManager versionManager;
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final SearchProvider searchProvider;
    private final DateFieldFormat dateFieldFormat;
    private final ProjectManager projectManager;

    /**
     * The I18nBean.
     */
    private final I18nBean.BeanFactory i18n;

    public DefaultVersionService(
            final VersionManager versionManager,
            final PermissionManager permissionManager,
            final IssueManager issueManager,
            final SearchProvider searchProvider,
            final I18nHelper.BeanFactory i18n,
            final DateFieldFormat dateFieldFormat,
            final ProjectManager projectManager)
    {
        this.versionManager = versionManager;
        this.permissionManager = permissionManager;
        this.issueManager = issueManager;
        this.i18n = i18n;
        this.searchProvider = searchProvider;
        this.dateFieldFormat = dateFieldFormat;
        this.projectManager = projectManager;
    }

    @Override
    public VersionResult getVersionById(final ApplicationUser user, final Project project, final Long versionId)
    {
        return getVersionById(ApplicationUsers.toDirectoryUser(user), project, versionId);
    }

    @Override
    public VersionResult getVersionById(final User user, final Project project, final Long versionId)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        Assertions.notNull("versionId", versionId);

        if (!hasReadPermission(user, project))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.read.permission"));
            return new VersionResult(errors);
        }

        Version version = versionManager.getVersion(versionId);

        if (version == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist.with.id", String.valueOf(versionId)));
            return new VersionResult(errors);
        }

        return new VersionResult(errors, version);
    }

    @Override
    public VersionResult getVersionById(final ApplicationUser user, final Long versionId)
    {
        return getVersionById(ApplicationUsers.toDirectoryUser(user), versionId);
    }

    @Override
    public VersionResult getVersionById(final User user, final Long versionId)
    {
        Version version = versionManager.getVersion(versionId);
        if (version == null)
        {
            I18nHelper i18nBean = getI18nBean(user);
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist.with.id", String.valueOf(versionId)));
            return new VersionResult(errors);
        }

        return getVersionById(user, version.getProjectObject(), versionId);
    }

    @Override
    public VersionResult getVersionByProjectAndName(final User user, final Project project, final String versionName)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        Assertions.notNull("project", project);
        Assertions.notBlank("versionName", versionName);

        if (!hasReadPermission(user, project))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.read.permission"));
            return new VersionResult(errors);
        }

        Version version = versionManager.getVersion(project.getId(), versionName);

        if (version == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist", versionName, project.getName()));
            return new VersionResult(errors);
        }

        return new VersionResult(errors, version);
    }

    @Override
    public VersionsResult getVersionsByProject(final User user, final Project project)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        Assertions.notNull("project", project);

        if (!hasReadPermission(user, project))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.read.permission"));
            return new VersionsResult(errors, Collections.<Version>emptyList());
        }

        return new VersionsResult(errors, versionManager.getVersions(project.getId()));
    }

    private boolean hasReadPermission(final User user, final Project project)
    {
        return hasEditPermission(user, project) || permissionManager.hasPermission(Permissions.BROWSE, project, user);
    }

    private boolean hasCreatePermission(final User user, final Project project)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user) || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user);
    }

    private boolean hasEditPermission(final User user, final Project project)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user) || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user);
    }

    @Override
    public ServiceOutcome<Version> setVersionDetails(final User user, final Version version, final String name, final String description)
    {
        final ErrorCollection errors = new SimpleErrorCollection();

        if (hasEditPermission(user, version.getProjectObject()))
        {
            // this method is deprecated. even though clients of this method may not be expecting validation to occur,
            // it's the right thing to do.
            VersionBuilder versionBuilder = newBuilder(version)
                    .name(name)
                    .description(description);
            VersionBuilderValidationResult versionBuilderValidationResult = validateUpdate(user, versionBuilder);
            if (!versionBuilderValidationResult.isValid())
            {
                return ServiceOutcomeImpl.from(versionBuilderValidationResult.getErrorCollection(), null);
            }

            return update(user, versionBuilderValidationResult);
        }
        else
        {
            final String message = i18n.getInstance(user).getText("admin.errors.projectversions.could.not.edit", version.getName());
            errors.addErrorMessage(message, ErrorCollection.Reason.FORBIDDEN);
            return new ServiceOutcomeImpl<Version>(errors);
        }
    }


    @Override
    public ErrorCollection validateVersionDetails(final User user, final Version version, final String name, final String description)
    {
        VersionBuilder versionBuilder = newBuilder(version)
                .name(name)
                .description(description);

        VersionBuilderValidationResult versionBuilderValidationResult = validateUpdate(user, versionBuilder);
        return versionBuilderValidationResult.getErrorCollection();
    }

    private ServiceOutcome<Version> setReleaseDate(User user, Version version, Either<String, Date> releaseDate)
    {
        ServiceOutcome<Date> releaseDateOutcome = parseDateFromEither(user, DateField.RELEASE_DATE, releaseDate);
        if(! releaseDateOutcome.isValid())
        {
            return ServiceOutcomeImpl.error(releaseDateOutcome);
        }

        return setReleaseDate(user, version, releaseDateOutcome.getReturnedValue());
    }

    @Override
    public ServiceOutcome<Version> setReleaseDate(User user, Version version, Date releaseDate)
    {
        VersionBuilder versionBuilder = newBuilder(version)
                .releaseDate(releaseDate);
        VersionBuilderValidationResult versionBuilderValidationResult = validateUpdate(user, versionBuilder);
        if (!versionBuilderValidationResult.isValid())
        {
            return ServiceOutcomeImpl.from(versionBuilderValidationResult.getErrorCollection(), null);
        }
        
        return update(user, versionBuilderValidationResult);
    }

    @Override
    public ServiceOutcome<Version> validateReleaseDate(User user, Version version, String releaseDate)
    {
        return setReleaseDate(user, version, Either.<String, Date>left(releaseDate));
    }

    @Override
    public ServiceOutcome<Version> setReleaseDate(User user, Version version, String releaseDate)
    {
        return setReleaseDate(user, version, Either.<String, Date>left(releaseDate));
    }

    @Override
    public ValidationResult validateDelete(final JiraServiceContext context, final Long versionId, final VersionAction affectsAction, final VersionAction fixAction)
    {
        log.debug("Validating delete of version with id " + versionId);

        // Validate that we can find the version we are deleting
        final DeleteVersionValidator validator = new DeleteVersionValidator(context, versionManager, permissionManager);
        return validator.validate(versionId, affectsAction, fixAction);
    }

    @Override
    public void delete(final JiraServiceContext context, final ValidationResult result)
    {
        if (!result.isValid())
        {
            throw new IllegalArgumentException("Result from validation is invalid");
        }

        final Version version = result.getVersionToDelete();
        log.debug("Deleting version with id " + version.getId());
        final Option<Version> affectsSwapVersion = option(result.getAffectsSwapVersion());
        final Option<Version> fixSwapVersion = option(result.getFixSwapVersion());
        versionManager.swapVersionForRelatedIssues(context.getLoggedInApplicationUser(), version, affectsSwapVersion, fixSwapVersion);
        versionManager.deleteVersion(version);
    }

    /**
     * Implementation is the same as deleting, with the actions set to SWAP and the swapVersionId being passed as both
     * Affects Version swap and Fix Version swap
     */
    @Override
    public ValidationResult validateMerge(final JiraServiceContext context, final Long versionId, final Long swapVersionId)
    {
        final SwapVersionAction swapVersionAction = new SwapVersionAction(swapVersionId);
        return validateDelete(context, versionId, swapVersionAction, swapVersionAction);
    }

    @Override
    public void merge(final JiraServiceContext context, final ValidationResult result)
    {
        delete(context, result);
    }

    @Override
    public CreateVersionValidationResult validateCreateVersion(final User user, final Project project, final String versionName,
            final String releaseDateStr, final String description, final Long scheduleAfterVersion)
    {
        // do the old validation first, disregarding release date (that comes later)
        ValidateResult result = validateCreateParameters(user, project, versionName, null, releaseDateStr);

        // if the old validation failed, return now
        if (!result.isValid())
        {
            return new CreateVersionValidationResult(result.errors, result.reasons);
        }

        // go the new path of validation, for measure
        VersionBuilder versionBuilder = newBuilder()
                .projectId(project.getId())
                .name(versionName)
                .description(description)
                .releaseDate(result.getParsedReleaseDate())
                .scheduleAfterVersion(scheduleAfterVersion);

        return newCreateResult(validateCreate(user, versionBuilder));
    }

    @Override
    public CreateVersionValidationResult validateCreateVersion(final User user, final Project project, final String versionName,
            final Date releaseDate, final String description, final Long scheduleAfterVersion)
    {
        // go the new path of validation, for measure
        VersionBuilder versionBuilder = newBuilder()
                .projectId(project == null ? null : project.getId())
                .name(versionName)
                .description(description)
                .releaseDate(releaseDate)
                .scheduleAfterVersion(scheduleAfterVersion);

        return newCreateResult(validateCreate(user, versionBuilder));
    }

    @Override
    public Version createVersion(User user, CreateVersionValidationResult request)
    {
        VersionBuilder versionBuilder = newBuilder()
                .projectId(request.getProject().getId())
                .name(request.getVersionName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .releaseDate(request.getReleaseDate())
                .scheduleAfterVersion(request.getScheduleAfterVersion());

        VersionBuilderValidationResult versionBuilderValidationResult = validateCreate(user, versionBuilder);
        if (!versionBuilderValidationResult.isValid())
        {
            throw new IllegalArgumentException("Should not have received a create version request which was not valid");
        }

        ServiceOutcome<Version> outcome = create(user, versionBuilderValidationResult);
        if (!outcome.isValid())
        {
            throw new IllegalArgumentException("Attempted to create a version and failed");
        }

        return outcome.getReturnedValue();
    }

    @Override
    public ReleaseVersionValidationResult validateReleaseVersion(final User user, final Version version, final Date releaseDate)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        checkVersionValid(errors, i18nBean, user, version);
        if (errors.hasAnyErrors())
        {
            return new ReleaseVersionValidationResult(errors);
        }

        if (version.isReleased())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.release.already.released"));
        }

        if (errors.hasAnyErrors())
        {
            return new ReleaseVersionValidationResult(errors);
        }
        return new ReleaseVersionValidationResult(errors, version, releaseDate);
    }

    @Override
    public ReleaseVersionValidationResult validateReleaseVersion(final User user, final Version version, final String releaseDate)
    {
        try
        {
            return validateReleaseVersion(user, version, parseDate(user, DateField.RELEASE_DATE, releaseDate));
        }
        catch (DateParseException e)
        {
            return new ReleaseVersionValidationResult(e.parseErrors);
        }
    }

    @Override
    public ReleaseVersionValidationResult validateUnreleaseVersion(final User user, final Version version, final Date releaseDate)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        checkVersionValid(errors, i18nBean, user, version);
        if (errors.hasAnyErrors())
        {
            return new ReleaseVersionValidationResult(errors);
        }

        if (!version.isReleased())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.release.not.released"));
        }

        if (errors.hasAnyErrors())
        {
            return new ReleaseVersionValidationResult(errors);
        }
        return new ReleaseVersionValidationResult(errors, version, releaseDate);
    }

    @Override
    public ReleaseVersionValidationResult validateUnreleaseVersion(User user, Version version, String releaseDate)
    {
        try
        {
            return validateUnreleaseVersion(user, version, parseDate(user, DateField.RELEASE_DATE, releaseDate));
        }
        catch (DateParseException e)
        {
            return new ReleaseVersionValidationResult(e.parseErrors);
        }
    }

    @Override
    public ArchiveVersionValidationResult validateArchiveVersion(final User user, final Version version)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);
        checkVersionValid(errors, i18nBean, user, version);

        if (version.isArchived())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.archive.already.archived"));
        }

        if (errors.hasAnyErrors())
        {
            return new ArchiveVersionValidationResult(errors);
        }
        return new ArchiveVersionValidationResult(errors, version);
    }

    @Override
    public ArchiveVersionValidationResult validateUnarchiveVersion(final User user, final Version version)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);
        checkVersionValid(errors, i18nBean, user, version);
        if (errors.hasAnyErrors())
        {
            return new ArchiveVersionValidationResult(errors);
        }

        if (!version.isArchived())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.archive.not.archived"));
        }

        if (errors.hasAnyErrors())
        {
            return new ArchiveVersionValidationResult(errors);
        }
        return new ArchiveVersionValidationResult(errors, version);
    }

    private void checkVersionValid(ErrorCollection errors, I18nHelper i18nHelper, User user, Version version)
    {
        Assertions.notNull("version", version);

        final Project project = version.getProjectObject();
        if (project == null)
        {
            errors.addErrorMessage(i18nHelper.getText("admin.errors.must.specify.valid.project"));
            return;
        }

        //check if the user is either a global admin or project admin for the selected project.
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)
                && !permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user))
        {
            errors.addErrorMessage(i18nHelper.getText("admin.errors.version.no.permission"));
            return;
        }

        if (StringUtils.isEmpty((version.getName())))
        {
            errors.addError("name", i18nHelper.getText("admin.errors.enter.valid.version.name"));
        }
    }

    @Override
    public Version releaseVersion(final ReleaseVersionValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not release a version with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not release a version with an invalid validation result.");
        }

        Version version = result.getVersion();
        version.setReleaseDate(result.getReleaseDate());
        version.setReleased(true);

        versionManager.releaseVersion(version, true);
        return versionManager.getVersion(version.getId());
    }

    @Override
    public void moveUnreleasedToNewVersion(User user, Version currentVersion, Version newVersion)
    {
        final List<Issue> issues = getUnresolvedIssues(user, currentVersion);
        if (!issues.isEmpty())
        {
            for (final Issue issue : issues)
            {
                // Need to look this up from the DB since we have DocumentIssues from the search.
                final MutableIssue mutableIssue = issueManager.getIssueObject(issue.getId());
                final Collection<Version> versions = mutableIssue.getFixVersions();
                versions.remove(currentVersion);
                versions.add(newVersion);
                mutableIssue.setFixVersions(versions);
                issueManager.updateIssue(user, mutableIssue, EventDispatchOption.ISSUE_UPDATED, true);
            }
        }

    }

    private List<Issue> getUnresolvedIssues(User user, Version toRelease)
    {
        try
        {
            Long pid = toRelease.getProjectObject().getId();
            final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().project(pid).and().unresolved();
            builder.and().fixVersion(toRelease.getId());

            final SearchResults results = searchProvider.search(builder.buildQuery(), user, PagerFilter.getUnlimitedFilter());
            final List<Issue> issues = results.getIssues();
            return (issues == null) ? Collections.<Issue> emptyList() : issues;
        }
        catch (final Exception e)
        {
            log.error("Exception whilst getting unresolved issues " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }



    @Override
    public Version unreleaseVersion(final ReleaseVersionValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not unrelease a version with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not unrelease a version with an invalid validation result.");
        }

        Version version = result.getVersion();
        version.setReleaseDate(result.getReleaseDate());
        version.setReleased(false);

        versionManager.releaseVersion(version, false);
        return versionManager.getVersion(version.getId());
    }

    @Override
    public Version archiveVersion(ArchiveVersionValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not archive a version with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not archive a version with an invalid validation result.");
        }

        Version version = result.getVersion();
        version.setArchived(true);
        versionManager.archiveVersion(version, true);
        return versionManager.getVersion(version.getId());
    }

    @Override
    public Version unarchiveVersion(ArchiveVersionValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not unarchive a version with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not unarchive a version with an invalid validation result.");
        }

        Version version = result.getVersion();
        version.setArchived(false);
        versionManager.archiveVersion(version, false);
        return versionManager.getVersion(version.getId());
    }

    @Override
    public MoveVersionValidationResult validateMoveToStartVersionSequence(final User user, long versionId)
    {
        return validateMove(user, versionId);
    }

    @Override
    public MoveVersionValidationResult validateIncreaseVersionSequence(final User user, long versionId)
    {
        return validateMove(user, versionId);
    }

    @Override
    public MoveVersionValidationResult validateDecreaseVersionSequence(final User user, long versionId)
    {
        return validateMove(user, versionId);
    }

    @Override
    public MoveVersionValidationResult validateMoveToEndVersionSequence(final User user, long versionId)
    {
        return validateMove(user, versionId);
    }

    @Override
    public MoveVersionValidationResult validateMoveVersionAfter(final User user, long versionId, Long scheduleAfterVersionId)
    {
        MoveVersionValidationResult moveVersionValidationResult =  validateMove(user, versionId);

        if (!moveVersionValidationResult.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = new SimpleErrorCollection();
                    final I18nHelper i18nBean = getI18nBean(user);

            Version version = moveVersionValidationResult.getVersion();
            Version scheduleAfterVersion = versionManager.getVersion(scheduleAfterVersionId);
            if (scheduleAfterVersion == null || !scheduleAfterVersion.getProjectObject().equals(version.getProjectObject()))
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist.with.id.for.project", scheduleAfterVersionId.toString(), version.getProjectObject().getKey()));
                moveVersionValidationResult = new MoveVersionValidationResult(errors, EnumSet.of(MoveVersionValidationResult.Reason.SCHEDULE_AFTER_VERSION_NOT_FOUND));
            }
            else
            {
                moveVersionValidationResult = new MoveVersionValidationResult(new SimpleErrorCollection(), version, scheduleAfterVersionId);
            }
        }
        return moveVersionValidationResult;
    }

    private MoveVersionValidationResult validateMove(User user, long versionId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        Version version = versionManager.getVersion(versionId);
        if (version == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist.with.id", String.valueOf(versionId)));
            return new MoveVersionValidationResult(errors, EnumSet.of(MoveVersionValidationResult.Reason.NOT_FOUND));
        }

        Project project = version.getProjectObject();
        //check if the user is either a global admin or project admin for the selected project.
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)
                && !permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.permission"));
            return new MoveVersionValidationResult(errors, EnumSet.of(MoveVersionValidationResult.Reason.FORBIDDEN));
        }

        return new MoveVersionValidationResult(errors, version);
    }

    @Override
    public void moveToStartVersionSequence(MoveVersionValidationResult moveVersionValidationResult)
    {
        versionManager.moveToStartVersionSequence(moveVersionValidationResult.getVersion());
    }

    @Override
    public void increaseVersionSequence(MoveVersionValidationResult moveVersionValidationResult)
    {
        versionManager.increaseVersionSequence(moveVersionValidationResult.getVersion());
    }

    @Override
    public void decreaseVersionSequence(MoveVersionValidationResult moveVersionValidationResult)
    {
        versionManager.decreaseVersionSequence(moveVersionValidationResult.getVersion());
    }

    @Override
    public void moveToEndVersionSequence(MoveVersionValidationResult moveVersionValidationResult)
    {
        versionManager.moveToEndVersionSequence(moveVersionValidationResult.getVersion());
    }

    @Override
    public void moveVersionAfter(MoveVersionValidationResult moveVersionValidationResult)
    {
        versionManager.moveVersionAfter(moveVersionValidationResult.getVersion(), moveVersionValidationResult.getScheduleAfterVersion());
    }

    @Override
    public boolean isOverdue(Version version)
    {
        return versionManager.isVersionOverDue(Assertions.notNull("version", version));
    }

    @Override
    public long getFixIssuesCount(Version version)
    {
        return versionManager.getIssueIdsWithFixVersion(version).size();
    }

    @Override
    public long getAffectsIssuesCount(Version version)
    {
        return versionManager.getIssueIdsWithAffectsVersion(version).size();
    }

    @Override
    public long getUnresolvedIssuesCount(final User user, final Version version)
    {
        return getUnresolvedIssues(user, version).size();
    }

    I18nHelper getI18nBean(User user)
    {
        return i18n.getInstance(user);
    }

    /**
     * @deprecated since v6.0. This is only around so that refactoring of Unit Tests is not necessary.
     */
    ValidateResult validateCreateParameters(User user, Project project, String versionName, String startDateStr, String releaseDateStr)
    {
        I18nHelper i18nBean = getI18nBean(user);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        if (project == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.must.specify.valid.project"));
            return new ValidateResult(errors, EnumSet.of(CreateVersionValidationResult.Reason.BAD_PROJECT));
        }

        //check if the user is either a global admin or project admin for the selected project.
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)
                && !permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.permission"));
            return new ValidateResult(errors, EnumSet.of(CreateVersionValidationResult.Reason.FORBIDDEN));
        }

        Set<CreateVersionValidationResult.Reason> reasons = EnumSet.noneOf(CreateVersionValidationResult.Reason.class);
        if (StringUtils.isEmpty((versionName)))
        {
            errors.addError("name", i18nBean.getText("admin.errors.enter.valid.version.name"));
            reasons.add(CreateVersionValidationResult.Reason.BAD_NAME);
        }
        else
        {
            Collection<Version> versions = versionManager.getVersions(project.getId());
            for (final Version version : versions)
            {
                if (versionName.equalsIgnoreCase(version.getName()))
                {
                    errors.addError("name", i18nBean.getText("admin.errors.version.already.exists"));
                    reasons.add(CreateVersionValidationResult.Reason.DUPLICATE_NAME);
                }
            }

            if (versionName.length() > 255)
            {
                errors.addError("name", i18nBean.getText("admin.errors.version.name.toolong"));
                reasons.add(CreateVersionValidationResult.Reason.VERSION_NAME_TOO_LONG);
            }
        }

        Date startDate = null;
        try
        {
            startDate = parseDate(user, DateField.START_DATE, startDateStr);
        }
        catch (DateParseException e)
        {
            errors.addErrorCollection(e.parseErrors);
            reasons.add(CreateVersionValidationResult.Reason.BAD_START_DATE);
        }

        Date releaseDate = null;
        try
        {
            releaseDate = parseDate(user, DateField.RELEASE_DATE, releaseDateStr);
        }
        catch (DateParseException e)
        {
            errors.addErrorCollection(e.parseErrors);
            reasons.add(CreateVersionValidationResult.Reason.BAD_RELEASE_DATE);
        }

        ServiceOutcome<Void> validateDatesOutcome = validateStartReleaseDates(user, DateField.START_DATE, startDate, releaseDate);
        if(! validateDatesOutcome.isValid())
        {
            errors.addErrorCollection(validateDatesOutcome.getErrorCollection());
            reasons.add(CreateVersionValidationResult.Reason.BAD_START_RELEASE_DATE_ORDER);
        }

        return new ValidateResult(errors, reasons, startDate, releaseDate);
    }

    private static Date makeMidnight(Date date, Locale locale)
    {
        if (date == null)
        {
            return date;
        }
        Calendar calendar = Calendar.getInstance(locale);
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private ServiceOutcome<Date> parseDateFromEither(User user, DateField field, Either<String, Date> date)
    {
        if (date.isLeft())
        {
            try
            {
                return ServiceOutcomeImpl.ok(parseDate(user, field, date.left().get()));
            }
            catch (DateParseException e)
            {
                return new ServiceOutcomeImpl<Date>(e.parseErrors);
            }
        }
        else
        {
            return ServiceOutcomeImpl.ok(date.right().get());
        }
    }

    /**
     * Parses a string release date into a Date object, throwing an exception if there is a problem parsing the string.
     * If the release date is an empty string, this method returns null.
     *
     * @param user the user who has provided the date
     * @param field a DateField instance containing information what kind of date we are parsing
     * @param date a string containing a date
     * @return a Date object, or null
     * @throws com.atlassian.jira.bc.project.version.DefaultVersionService.DateParseException if there is a
     * problem parsing the date string
     */
    @Nullable
    protected Date parseDate(User user, DateField field, String date) throws DateParseException
    {
        if (StringUtils.isEmpty(date))
        {
            return null;
        }

        try
        {
            return dateFieldFormat.parseDatePicker(date);
        }
        catch (IllegalArgumentException e)
        {
            I18nHelper i18n = getI18nBean(user);
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addError(field.name, i18n.getText("admin.errors.incorrect.date.format", dateFieldFormat.getFormatHint()));

            throw new DateParseException(errors);
        }
    }

    protected ServiceOutcome<Void> validateStartReleaseDates(User user, DateField field, Date startDate, Date releaseDate)
    {
        if(startDate != null && releaseDate != null && startDate.after(releaseDate))
        {
            I18nHelper i18n = getI18nBean(user);
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addError(field.name, i18n.getText("admin.errors.version.start.release.date.order"));

            return ServiceOutcomeImpl.from(errors, null);
        }

        return ServiceOutcomeImpl.ok(null);
    }

    static class ValidateResult
    {
        private final ErrorCollection errors;
        private final Set<CreateVersionValidationResult.Reason> reasons;

        private final Date parsedStartDate;
        private final Date parsedReleaseDate;

        ValidateResult(ErrorCollection errors, Set<CreateVersionValidationResult.Reason> reasons)
        {
            this(errors, reasons, null, null);
        }

        ValidateResult(ErrorCollection errors, Set<CreateVersionValidationResult.Reason> reasons, Date parsedStartDate, Date parsedReleaseDate)
        {
            if (!reasons.isEmpty() && !errors.hasAnyErrors())
            {
                throw new IllegalArgumentException("Cannot have reasons without error messages.");
            }

            this.errors = errors;
            this.reasons = reasons;

            this.parsedStartDate = parsedStartDate;
            this.parsedReleaseDate = parsedReleaseDate;
        }

        boolean isValid()
        {
            return !errors.hasAnyErrors();
        }

        ErrorCollection getErrors()
        {
            return errors;
        }

        Set<CreateVersionValidationResult.Reason> getReasons()
        {
            return reasons;
        }

        Date getParsedStartDate()
        {
            return parsedStartDate;
        }

        Date getParsedReleaseDate()
        {
            return parsedReleaseDate;
        }
    }

    /**
     * Thrown when a string cannot be parsed as a date.
     */
    static class DateParseException extends Exception
    {
        /**
         * An internationalised error collection.
         */
        final ErrorCollection parseErrors;

        DateParseException(ErrorCollection parseErrors)
        {
            this.parseErrors = parseErrors;
        }
    }

    static enum DateField
    {
        START_DATE("startDate"),
        RELEASE_DATE("releaseDate");

        public final String name;

        private DateField(String name)
        {
            this.name = name;
        }
    }

    @Override
    public VersionBuilder newBuilder()
    {
        return new VersionBuilder();
    }

    @Override
    public VersionBuilderValidationResult validateCreate(User user, VersionBuilder versionBuilder)
    {
        I18nHelper i18nBean = getI18nBean(user);
        SimpleErrorCollection errors = new SimpleErrorCollection();

        Long projectId = versionBuilder.projectId;
        if (projectId == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.must.specify.valid.project"));
            return new VersionBuilderValidationResult(errors, EnumSet.of(VersionService.CreateVersionValidationResult.Reason.BAD_PROJECT));
        }

        Project project = projectManager.getProjectObj(projectId);
        if (project == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.must.specify.valid.project"));
            return new VersionBuilderValidationResult(errors, EnumSet.of(VersionService.CreateVersionValidationResult.Reason.BAD_PROJECT));
        }

        //check if the user is either a global admin or project admin for the selected project.
        if (! hasCreatePermission(user, project))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.permission"));
            return new VersionBuilderValidationResult(errors, EnumSet.of(VersionService.CreateVersionValidationResult.Reason.FORBIDDEN));
        }

        return validateUpdate(i18nBean, errors, versionBuilder);
    }

    @Override
    public ServiceOutcome<Version> create(User user, VersionBuilderValidationResult validationResult)
    {
        check(validationResult);

        try
        {
            VersionBuilder builder = validationResult.getResult();
            Version version = versionManager.createVersion(builder.name, builder.startDate, builder.releaseDate, builder.description, builder.projectId, builder.scheduleAfterVersion);
            return ServiceOutcomeImpl.ok(version);
        }
        catch (CreateException ex)
        {
            log.error("Unable to create version", ex);
            return ServiceOutcomeImpl.error("Unable to create version: " + ex.getMessage(), ErrorCollection.Reason.SERVER_ERROR);
        }
    }

    @Override
    public VersionBuilder newBuilder(Version version)
    {
        if(version == null)
        {
            throw new IllegalArgumentException("Version object is required when updating version");
        }

        return new VersionBuilder(version);
    }

    @Override
    public VersionBuilderValidationResult validateUpdate(User user, VersionBuilder versionBuilder)
    {
        I18nHelper i18nBean = getI18nBean(user);
        ErrorCollection errors = new SimpleErrorCollection();

        if(versionBuilder.version == null)
        {
            throw new IllegalArgumentException("Version object is required when updating version");
        }

        Project project = projectManager.getProjectObj(versionBuilder.projectId);
        if (! hasCreatePermission(user, project))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.permission"));
            return new VersionBuilderValidationResult(errors, EnumSet.of(VersionService.CreateVersionValidationResult.Reason.FORBIDDEN));
        }

        return validateUpdate(i18nBean, errors, versionBuilder);
    }

    @Override
    public ServiceOutcome<Version> update(User user, VersionBuilderValidationResult validationResult)
    {
        check(validationResult);

        final VersionBuilder versionBuilder = validationResult.getResult();
        final Version version = versionBuilder.build();

        return ServiceOutcomeImpl.ok(versionManager.update(version));
    }

    private void check(VersionBuilderValidationResult validatedData)
    {
        if (validatedData == null)
        {
            throw new IllegalArgumentException("You can not create a version with a null validation result.");
        }

        if (! validatedData.isValid())
        {
            throw new IllegalStateException("You can not create a version with an invalid validation result.");
        }

        if (validatedData.getResult() == null)
        {
            throw new IllegalArgumentException("You can not create a null version.");
        }
    }

    private VersionBuilderValidationResult validateUpdate(I18nHelper i18nBean, ErrorCollection errors, VersionBuilder versionBuilder)
    {
        Set<VersionService.CreateVersionValidationResult.Reason> reasons = EnumSet.noneOf(VersionService.CreateVersionValidationResult.Reason.class);

        String versionName = versionBuilder.name;
        if (StringUtils.isEmpty((versionName)))
        {
            errors.addError("name", i18nBean.getText("admin.errors.enter.valid.version.name"));
            reasons.add(VersionService.CreateVersionValidationResult.Reason.BAD_NAME);
        }
        else
        {
            if (versionName.length() > 255)
            {
                errors.addError("name", i18nBean.getText("admin.errors.version.name.toolong"));
                reasons.add(VersionService.CreateVersionValidationResult.Reason.VERSION_NAME_TOO_LONG);
            }

            if(versionBuilder.version != null)
            {
                if (versionManager.isDuplicateName(versionBuilder.version, versionName))
                {
                    final String message = i18nBean.getText("admin.errors.projectversions.version.exists");
                    errors.addError("name", message, ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
            else
            {
                Collection<Version> versions = versionManager.getVersions(versionBuilder.projectId);
                for (final Version version : versions)
                {
                    if (versionName.equalsIgnoreCase(version.getName()))
                    {
                        errors.addError("name", i18nBean.getText("admin.errors.version.already.exists"));
                        reasons.add(VersionService.CreateVersionValidationResult.Reason.DUPLICATE_NAME);
                    }
                }
            }
        }

        Locale locale = i18nBean.getLocale();
        if (versionBuilder.startDate != null)
        {
            versionBuilder.startDate(makeMidnight(versionBuilder.startDate, locale));
        }
        if (versionBuilder.releaseDate != null)
        {
            versionBuilder.releaseDate(makeMidnight(versionBuilder.releaseDate, locale));
        }

        if(versionBuilder.startDate != null && versionBuilder.releaseDate != null && versionBuilder.startDate.after(versionBuilder.releaseDate))
        {
            String sourceField = "startDate";
            if(versionBuilder.version != null && versionBuilder.startDate.equals(versionBuilder.version.getStartDate()))
            {
                sourceField = "releaseDate";
            }

            errors.addError(sourceField, i18nBean.getText("admin.errors.version.start.release.date.order"), ErrorCollection.Reason.VALIDATION_FAILED);
            reasons.add(VersionService.CreateVersionValidationResult.Reason.BAD_START_RELEASE_DATE_ORDER);
        }

        return new VersionBuilderValidationResult(errors, reasons, versionBuilder);
    }

    private CreateVersionValidationResult newCreateResult(VersionBuilderValidationResult versionBuilderValidationResult)
    {
        if (!versionBuilderValidationResult.isValid())
        {
            return new CreateVersionValidationResult(versionBuilderValidationResult.getErrorCollection(), versionBuilderValidationResult.getSpecificReasons());
        }

        VersionBuilder builder = versionBuilderValidationResult.getResult();
        Project project = projectManager.getProjectObj(builder.projectId);

        return new CreateVersionValidationResult(versionBuilderValidationResult.getErrorCollection(), project, builder.name, builder.startDate, builder.releaseDate, builder.description, builder.scheduleAfterVersion);
    }

}
