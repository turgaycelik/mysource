package com.atlassian.jira.rest.v2.license;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains the results of a call to the license validation REST endpoint.
 *
 * @since v5.2
 */
@XmlRootElement
@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
public class LicenseValidationResults
{
    /**
     * A map of key value pairs indicating any license validation erros.
     */
    private Map<String, String> errors;

    /**
     * The license string.
     */
    private String licenseString;

    /**
     * Default constructor.
     */
    public LicenseValidationResults()
    {
    }

    /**
     * Return any errors associated with the license validation.
     * @return
     */
    public Map<String, String> getErrors()
    {
        return this.errors;
    }

    /**
     * Set the errors associated with this validation result.
     * @param errors
     */
    public void setErrors(Map<String, String> errors)
    {
        this.errors = errors;
    }

    /**
     * Return the license string.
     * @return
     */
    public String getLicenseString()
    {
        return this.licenseString;
    }

    /**
     * Set the license string.
     * @param licenseString
     */
    public void setLicenseString(String licenseString)
    {
        this.licenseString = licenseString;
    }
}
