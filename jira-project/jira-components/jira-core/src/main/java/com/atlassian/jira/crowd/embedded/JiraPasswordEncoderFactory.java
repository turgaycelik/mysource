package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.password.encoder.AtlassianSecurityPasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactoryImpl;

/**
 * Password Encoder that initialises with the correct encoder for JIRA.
 * See JDEV-24089
 * @since v6.1
 */
public class JiraPasswordEncoderFactory extends PasswordEncoderFactoryImpl
{
    public JiraPasswordEncoderFactory()
    {
        super();
        // Refer to JDEV-24089.  There may arrive a new constructor one day to do this.
        super.addEncoder(new AtlassianSecurityPasswordEncoder());
    }
}
