package com.atlassian.jira.license;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.extras.common.LicenseException;
import com.atlassian.extras.decoder.api.LicenseDecoder;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.license.DefaultLicenseDetails.ENABLED;

/**
 * Default implementation of {@link LicenseRoleDetails}.
 * <p>
 * This implementation fails fast (in the constructor) on detection of invalid
 * license or license role details.
 * </p>
 *
 * @since v6.3
 */
@ExperimentalApi
public class DefaultLicenseRoleDetails implements LicenseRoleDetails
{
    private static final Logger log = LoggerFactory.getLogger( DefaultLicenseRoleDetails.class );

    /** Pattern that identifies a license role name declaration (group 2)
     * and its corresponding product key (group 1). */
    private static final Pattern LICENSE_ROLE = Pattern.compile( "(.+?)\\.Role\\.(\\w+)\\.NumberOfUsers" );

    /** String suffix for a license property that identifies/declares a "product key". */
    private static final String PRODUCT_SUFFIX = ".active";

    /** An immutable map of license roles contained within this platform license to the
     * granted number of users for each of those roles. */
    private final ImmutableMap<LicenseRoleId,Integer> userLimitByLicenseRole;


    /**
     * @param license An encoded license string.
     * @param licenseDecoder A license decoder capable of decoding the given license.
     * @throws LicenseException on detection of invalid roles or role user counts.
     */
    public DefaultLicenseRoleDetails( @Nonnull String license, @Nonnull LicenseDecoder licenseDecoder )
    throws LicenseException
    {
        Properties licenseProperties = licenseDecoder.decode( license );
        this.userLimitByLicenseRole = extractRolesFrom( licenseProperties );
    }


    /**
     * {@inheritDoc}
     *
     * Note: This implementation returns an unmodifiable {@link Set}.
     */
    @Override
    @Nonnull
    public Set<LicenseRoleId> getLicenseRoles()
    {
        return this.userLimitByLicenseRole.keySet();
    }


    @Override
    public int getUserLimit(@Nonnull LicenseRoleId role)
    {
        Integer numUsers = this.userLimitByLicenseRole.get( role );
        return (numUsers != null) ? numUsers : 0;
    }


    /**
     * Populates {@link #userLimitByLicenseRole} with license roles derived from the
     * given license {@link Properties} as judged by matching the {@link #LICENSE_ROLE}
     * pattern and associated sanity checks.
     *
     * @param licenseProperties A map of String license property names to String values.
     * @return An immutable map of license roles to the number of granted users/seats for those roles.
     * @throws com.atlassian.extras.common.LicenseException on detection of license format errors.
     */
    @Nonnull
    static final ImmutableMap<LicenseRoleId,Integer> extractRolesFrom( @Nonnull Properties licenseProperties )
    throws LicenseException
    {
        Map<LicenseRoleId,Integer> userLimitByLicenseRole = new HashMap<LicenseRoleId, Integer>( 0 );

        for (String propertyName : licenseProperties.stringPropertyNames())
        {
            Matcher m = LICENSE_ROLE.matcher( propertyName );
            if (!m.matches())
            {
                // then propertyName is not a license role declaration
                continue;
            }

            String productKey = m.group( 1 );
            String licenseRoleName = m.group( 2 );

            if (!isProductActivated( productKey, licenseProperties ))
            {
                log.debug(
                    "license role '{}' is declared but product '{}' is not activated",
                    licenseRoleName, productKey
                );
                continue;
            }

            // extract user count
            //
            // note: no check for negative user count -- negative use count is assumed to
            // mean "unlimited user count".
            String licenseRoleValue = licenseProperties.getProperty( propertyName, /*default*/ "0" );
            Integer licenseRoleCount;
            try
            {
                licenseRoleCount = new Integer( licenseRoleValue );
            }
            catch (NumberFormatException ex)
            {
                log.error( "invalid value for license role '{}'", licenseRoleName );
                throw new LicenseException( "Invalid license" );
            }

            log.info( "license provides {} seats for role '{}'", licenseRoleCount, licenseRoleName );

            userLimitByLicenseRole.put( new LicenseRoleId( licenseRoleName ), licenseRoleCount );
        }

        return ImmutableMap.copyOf( userLimitByLicenseRole );
    }


    /**
     * Returns true if the given product would be considered activated by the given
     * license {@link Properties}.
     *
     * @param productKey Canonical String identifying a particular product.
     * @param licenseProperties Map of license properties.
     */
    static final boolean isProductActivated( @Nonnull String productKey, @Nonnull Properties licenseProperties )
    {
        return ENABLED.equals(
            licenseProperties.getProperty( productKey + PRODUCT_SUFFIX ) );
    }
}
