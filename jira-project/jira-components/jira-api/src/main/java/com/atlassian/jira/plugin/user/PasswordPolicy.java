package com.atlassian.jira.plugin.user;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;

/**
 * Provides a mechanism for rejecting proposed passwords.  Some example reasons might include:
 * <p/>
 * <ul>
 * <li>The password must be at least 8 characters long.</li>
 * <li>The password must not contain your username.</li>
 * <li>The password must not contain a year within the past century.</li>
 * <li>The password must not be similar to your previous password.</li>
 * <li>The password can not repeat any of the previous 4 passwords that you have used.</li>
 * <li>The password can not be based on a dictionary word.</li>
 * </ul>
 * <p/>
 * ... and so on.
 *
 * @since v6.1
 */
@PublicSpi
public interface PasswordPolicy
{
    /**
     * This will be called when a user attempts to change a password.  Returning a non-empty list of
     * {@link WebErrorMessage} will prevent the new password from being accepted.
     *
     * @param user the user whose password would be changed.  This will never be {@code null}, but if the
     *          intent of the request is to create a new user, then the user will not yet exist and
     *          services like the {@code UserManager} and {@code ApplicationUsers.from(User)} will not
     *          be able to resolve it.  The user's
     *          {@link com.atlassian.crowd.embedded.api.User#getDirectoryId() directory ID} will be
     *          {@code -1L} for this case.
     * @param oldPassword the user's existing password, or {@code null} if that information is not
     *          available, either because this is a new user or because an administrator is changing
     *          the password
     * @param newPassword the user's proposed new password
     * @return a collection of {@link WebErrorMessage}s explaining why the password cannot be accepted
     */
    Collection<WebErrorMessage> validatePolicy(@Nonnull final User user,
            @Nullable final String oldPassword, @Nonnull final String newPassword);

    /**
     * Returns a list of rules that passwords must follow to satisfy the policy.
     *
     * @param hasOldPassword whether or not the request concerns the rules when the old password
     *          is provided.  This is {@code true} for the case where an existing user is changing
     *          his/her own password, but not when an administrator is changing another user's
     *          password or a new account is getting created.  The rule list should probably be
     *          different for these cases.  For example, it does not make sense to tell an
     *          administrator that the new password can not be similar to the old password when
     *          the administrator does not even know what the old password was.  Nor does it make
     *          sense to say this to a new user, for whom the whole idea is completely irrelevant.
     * @return a list of rules that passwords must follow to satisfy the policy.
     */
    List<String> getPolicyDescription(boolean hasOldPassword);
}
