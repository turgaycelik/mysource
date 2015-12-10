package com.atlassian.jira.bc.security.login;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;

/**
 * Denied reason indicating that a CAPTCHA challenge must be passed before authentication can be performed.
 *
 * @since v4.3
 */
@Immutable
public class CaptchaChallengeRequired extends DeniedReason
{
    /**
     * The reason code for this DeniedReason.
     */
    private static final String CAPTCHA_CHALLENGE = "CAPTCHA_CHALLENGE";

    /**
     * The property name for the CAPTCHA URL.
     */
    private static final String LOGIN_URL = "login-url";

    /**
     * Creates a new CaptchaChallengeRequired denied reason, with the given CAPTCHA URL.
     *
     * @param loginURL the URL that the user must visit to pass the captcha challenge
     */
    public CaptchaChallengeRequired(String loginURL)
    {
        super(CAPTCHA_CHALLENGE, Collections.singletonMap(LOGIN_URL, loginURL));
    }
}
