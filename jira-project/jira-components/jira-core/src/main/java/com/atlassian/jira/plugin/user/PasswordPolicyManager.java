package com.atlassian.jira.plugin.user;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.user.ApplicationUser;

/**
 * Checks user password policies.
 *
 * @since v6.1
 */
public interface PasswordPolicyManager
{
    /**
     * Checks the password policies against a proposed password change for an existing user.
     *
     * @param user the user whose password would be changed; must not be {@code null}
     * @param oldPassword the user's current password, if known; may be {@code null}, in which case any checks
     *          that would be made against it will be skipped
     * @param newPassword the proposed new password
     * @return list containing all plugin provided {@link WebErrorMessage}; never {@code null}
     */
    @Nonnull Collection<WebErrorMessage> checkPolicy(@Nonnull ApplicationUser user,
            @Nullable String oldPassword, @Nonnull String newPassword);

    /**
     * Checks the password policies for a proposed new user.
     *
     * @param username the username that the new user would have; must not be {@code null}
     * @param displayName the display name that the new user would have; may be {@code null}
     * @param emailAddress the email address that the new user would have; may be {@code null}
     * @param password the proposed new password; must not be {@code null}
     * @return list containing all plugin provided {@link WebErrorMessage}; never {@code null}
     */
    @Nonnull Collection<WebErrorMessage> checkPolicy(@Nonnull String username, @Nullable String displayName,
            @Nullable String emailAddress, @Nonnull String password);

    /**
     * Returns a list of rules that passwords must follow to satisfy all password policies.
     *
     * @param hasOldPassword whether or not the request concerns the rules when the old password
     *          is provided.  This is {@code true} for the case where an existing user is changing
     *          his/her own password, but not when an administrator is changing another user's
     *          password or a new account is getting created.  The rule list should probably be
     *          different for these cases.  For example, it does not make sense to tell an
     *          administrator that the new password can not be similar to the old password when
     *          the administrator does not even know what the old password was.  Nor does it make
     *          sense to say this to a new user, for whom the whole idea is completely irrelevant.
     * @return a list of rules that passwords must follow to satisfy all policies.
     */
    @Nonnull List<String> getPolicyDescription(boolean hasOldPassword);
}
