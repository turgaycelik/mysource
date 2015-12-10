package com.atlassian.jira.webtest.webdriver.tests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.components.LicenseBanner;
import com.atlassian.jira.pageobjects.config.ResetDataOnce;
import com.atlassian.jira.pageobjects.pages.project.BrowseProjectsPage;
import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.WebResource;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @since v6.3
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION })
@ResetDataOnce
public class TestLicenseBanner extends BaseJiraWebTest
{
    private static final String MAC_TEMPLATE = "utm_source=jira_banner&utm_medium=renewals_reminder&utm_campaign=renewals_%d_reminder";

    /**
     * Evaluation license that will expire in 2 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:259200000, jira.Description=Evaluation to expire in 2 days.,
     * jira.MaintenanceExpiryDate=Duration:259200000, jira.SEN=SEN, jira.Evaluation=true}</code>
     */
    private static final String EVAL_2 = "AAAA5w0ODAoPeNqNT8FqwzAMve8rBDunOGYtLODDSHzIaNKRZL"
            + "dd1FQDj9UJsh2Wv18Ts43cJhBIenpPT/fVYOE5WBASUpHtD1kqQLcdSJE+3H0Yxt3R9GQd"
            + "lYX6rTaAvhhvBqt03enmpSlbHeGcCRegQE9qkUvEIRFyw+3mkWq8kspPVaWbvHw6Rhx7by"
            + "ZSngPFQUs8Ed9M/BRxXIfrmfj0/uqInUrSrbOv0fC83i8Cr2beMrl/lGKJuFqQ69mM8YMJ"
            + "P8O6Bn4AWtgExoKEC85uFwkVGuvJou3/pd/qWt0yNn8H4mffgWCAFzAsAhRm/fM3iLtxTu"
            + "zje1QcDsfkHQXUygIUT+a9Hg3UnVWjjY9tbhqnPvEm8tk=X02c4";

    /**
     * ELA license that will expire in 46 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:4060800000, ELA=true, jira.Description=ELA to expire in 46 days.,
     * jira.MaintenanceExpiryDate=Duration:4060800000, jira.SEN=SEN}</code>
     */
    private static final String ELA_46 = "AAAA5Q0ODAoPeNqFT8FqhEAMvfsVgZ5dorhShDmIzsGiblF762"
            + "XqpjCFHSUzLuvfd92hpZ4aCIT3kvdenprJwMtiAGOIMDumWYQg+wFijJLgS7M61HokY6kq"
            + "xe+0I+RZOz0ZIdtBdq9d1UtPF0xqI0rlSGxyIaYhxrvbYZ2pVRcSxalpZFdUee15NTp9Je"
            + "F4IQ/0xFfie4ifwcPtcvkgPn2+WWIrwmif7DZrXh/+5cKPMO9Zgik+41aBrPM/DiXZkfXs"
            + "f6lzcBPQJkCgDSQpnNVqD361Udo4MsqM/3r48LIV9w6+ASLceE8wLQIUbQ2dxzol5buykG"
            + "OXEVL9N2Z17Y0CFQCEKYk7lXQTDhXAWpuyRvfDIi89fw==X02c4";

    /**
     * ELA license that will expire in 45 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:3974400000, ELA=true, jira.Description=ELA to expire in 45 days.,
     * jira.MaintenanceExpiryDate=Duration:3974400000, jira.SEN=SEN}</code>
     */
    private static final String ELA_45 = "AAAA5g0ODAoPeNqFT8Fqg0AQvfsVAz0bVmtSKuxBdA8WNUXtLZ"
            + "etmcIUssrsGurfN2ZpqacODAzvzbz35qEeDbzMBkQMkUj3hzQSoLoeYhElwSex3lU0oLFY"
            + "FvJ32hDqTI5GI1XTq/a1LTvl6ZxRr0ShHcpVLhSHUMSb236ZsNEXlPmxrlWbl1nleT04uq"
            + "J0PKMHOuQr8i3Ez+DhZr68Ix8/3iyylWG0TfY1ES93/2Lme5hT+vj8lCRirUBV2R+HAu3A"
            + "NPlfqgzcCLgKIJCBZA9nvdidX601GYdGm+FfDx9eNfLWwTcnvXhgMCwCFEXpmX0Iwbtclb"
            + "CrqGMPlcP4eZrbAhQeOZUZLDlSX6q+vsTwoHIBtKfuxA==X02c4";

    /**
     * ELA license that will expire in 31 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:2764800000, ELA=true, jira.Description=ELA to expire in 31 days.,
     * jira.MaintenanceExpiryDate=Duration:2764800000, jira.SEN=SEN}</code>
     */
    private static final String ELA_31 = "AAAA5g0ODAoPeNqFT8Fqg0AQvfsVAz0bdm1iirAH0T0Y1BS1t1"
            + "62ZgobyCqza4h/35glpZ46MDC8N/Pem5dqMHCYDLAIOEt2ccIZyLaDiPFtcNakNqXu0Vgs"
            + "cvE7rQh50k4PRsi6k817U7TS0xmhWohcORSLXMjikEWr224esVYXFNmxqmSTFWnpedU7fU"
            + "XhaEIPtEhXpHuI5+Dherp8IR2/PyySFSFfJ7uNmuaHfz7RI8xnEu3j7RtbKpBl+schR9uT"
            + "Hv0vZQpuAFwEELSBVw4nNduNX62UNg6NMv2/Hj68rMW9gx8mS3hbMCwCFBNIU6PitI8Xnt"
            + "q1jCh1yCjgbm4zAhRibRxDfK4qUIX19iYEdSNBDOSnPQ==X02c4";

    /**
     * ELA license that will expire in 30 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:2678400000, ELA=true, jira.Description=ELA to expire in 30 days.,
     * jira.MaintenanceExpiryDate=Duration:2678400000, jira.SEN=SEN}</code>
     */
    private static final String ELA_30 = "AAAA5g0ODAoPeNqFT8Fqg0AQvfsVAz0bVpvaIuxBdA8GNUXtrZ"
            + "etmcIEssrsGurfN2ZpqacODAzvzbz35qEeDRxmAyKGSKRPSRoJUF0PsYj2wZlY7yoa0Fgs"
            + "C/k7bQh1IkejkarpVfvalp3ydM6oV6LQDuUqF4okFPHmtl8mbPQFZX6sa9XmZVZ5Xg+Ori"
            + "gdz+iBDvmKfAvxM3i4mS8fyMfPN4tsZRhtk31NxMvdv5j5HuY9jZPnl71YK1BV9sehQDsw"
            + "Tf6XKgM3Aq4CCGTgUcBJL3bnV2tNxqHRZvjXw4dXjbx18A0mDHhaMCwCFE0s2lZCYwRjCo"
            + "FEsD0/I+JLDwjvAhRyClJcKl+SIazuiGGUcOhEa8Beog==X02c4";

    /**
     * ELA license that will expire in 25 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:2246400000, ELA=true, jira.Description=ELA to expire in 25 days.,
     * jira.MaintenanceExpiryDate=Duration:2246400000, jira.SEN=SEN}</code>
     */
    private static final String ELA_25 = "AAAA5Q0ODAoPeNqFT8GKgzAQvfsVA3u2xGA9CDmI5mBRW9Te9p"
            + "K1s5CFRpnEUv++tWGXetqBgeG9mffefNSjgcNsgHGIWLpP0oiB7HrgLIqDH01qV+kBjcWy"
            + "EH/ThpAX7fRohGx62Z7aspOezgnVShTKoVjlQpaEjG9u+2XCRl1R5Me6lm1eZpXn1eD0DY"
            + "WjGT3QId2QniF+Bw838/UL6fh9tkhWhNE22X3StLz8i5leYT5TzuMkZmsFssreHAq0A+nJ"
            + "/1Jl4EbAVQBBG+B7uKjF7vxqrbRxaJQZ/vXw4WUjnh08ACILeEwwLQIVAI9i9YtHhIPPEg"
            + "GFasCjkX0fGUVtAhQfphFOX+2rAu2tvwlcSzly0rCGfA==X02c4";

    /**
     * ELA license that will expire in 15 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:1382400000, ELA=true, jira.Description=ELA to expire in 15 days.,
     * jira.MaintenanceExpiryDate=Duration:1382400000, jira.SEN=SEN}</code>
     */
    private static final String ELA_15 = "AAAA5g0ODAoPeNqFT8Fqg0AQvfsVAz0bdm0SirAH0T1Y1BS1t1"
            + "62ZgpTyCqza6h/35ilpZ46MDC8N/Pem4d6tPA8WxAJSJEejqkUoLseEiH30Sex2VU0oHVY"
            + "Fup32hD6TJ5Gq3TT6/alLTsd6JzRrERhPKpVLhbHWCSb236ZsDEXVPmprnWbl1kVeDN4uq"
            + "LyPGMAOuQr8i3EzxDgZr68I58+Xh2yU7HcJvuaiJe7fzHzPcxbKh+fkr1YK9JV9sehQDcw"
            + "TeGXKgM/Aq4CCGRBHuBsFrcLq7Uh69EaO/zrEcLrRt06+gYhy3hLMCwCFHuUQFOxhgMqJr"
            + "00p/gXTV6eE70LAhRA2QIpttujHsg0vpxzo/bpVaF1fA==X02c4";

    /**
     * ELA license that will expire in 8 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:777600000, ELA=true, jira.Description=ELA to expire in 8 days.,
     * jira.MaintenanceExpiryDate=Duration:777600000, jira.SEN=SEN}</code>
     */
    private static final String ELA_8 = "AAAA5A0ODAoPeNqFT8FqhEAMvfsVgZ5dRmm1CHMQnYNF3aL21s"
            + "usm0IKO0pmXOrfd92hpZ4aCDzeS/JeHprJwMtiQMQQiewpySIBqh8gFtFj8EmsDzWNaCxW"
            + "pfxFO0GdydFkpGoH1b12Va+8XDDqTSi1Q7mdC0USini3O6wztvqCsjg2jeqKKq+9rkdHV5"
            + "SOF/REj3xFvoX4AZ5ul8sJ+fjxZpGtDKN9sq+ZeL37lwvfw7xnaZomYqtA1fkfgxLtyDT7"
            + "V+oc3AS47SOQgWc469Ue/GSjyTg02oz/OfjkqpW3Dr4B2ht3zzAsAhRa7DeT6JlNHL8syD"
            + "FVjwBWEEUUEAIUKXSbHLzvTbikZqk3kRlIR6K4JHo=X02c0";

    /**
     * ELA license that will expire in 7 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:691200000, ELA=true, jira.Description=ELA to expire in 7 days.,
     * jira.MaintenanceExpiryDate=Duration:691200000, jira.SEN=SEN}</code>
     */
    private static final String ELA_7 = "AAAA5A0ODAoPeNqFT8FqhEAMvfsVgZ5dZqS1VJiD6Bws6ha1t1"
            + "6mbgop7CiZcal/33WHlnpqIPB4L8l7uWsmC8+LBZGAFNlDmkkBuh8gEfI++iQ2h5pGtA6r"
            + "Uv2inaBP5GmySreD7l66qtdBLhjNJpTGo9rOxSKNRbLbHdYZW3NGVRybRndFlddBN6OnCy"
            + "rPCwaiR74gX0P8gEC3y/kd+fjx6pCdiuU+2ddMvN78y4VvYd6y9EkmYqtI1/kfgxLdyDSH"
            + "V+oc/AS47SOQhUc4mdUdwmRjyHq0xo7/OYTkulXXjr4B1PN3vDAsAhRmBqHfkM/f0zPQ6p"
            + "47y3zOgMgY4gIURfc2thH61V8k4S540qfnalr71Us=X02c0";

    /**
     * ELA license that will expire in 0 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:86400000, ELA=true, jira.Description=ELA to expire in 0 days.,
     * jira.MaintenanceExpiryDate=Duration:86400000, jira.SEN=SEN}</code>
     */
    private static final String ELA_0 = "AAAA4w0ODAoPeNqFUE1rg0AQvfsrBno2rJJIEfYgugeLmqL21s"
            + "vWTGEKWWV2DfHfV7O01FMHBh7vzcebeapHAy+zARFDJNJTkkYCVNdDLKJj8EWsDxUNaCyW"
            + "hfxFO0FdyNFopGp61b62Zae8nDPqTSi0Q7mNC0USinjX2y8TNvqKMj/XtWrzMqu8rgdHN5"
            + "SOZ/REh3xDXk38AE838/UD+fz5ZpGtDKO9s/tEvDz2FzM/zLynz8lRbBGoKvszv0A7ME3+"
            + "kioDNwJu7Qi0PgcuerEHX1lrMg6NNsM/C7xv1cg1g2/1cXdVMCwCFF7N6w+wfi/OBV7mx9"
            + "BWt+RyZcNwAhRvlr7l3Eshd24u4KypV90SVx8SGQ==X02c0";

    /**
     * ELA license that will expire in -1 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=Duration:0, ELA=true, jira.Description=ELA to expire in -1 days.,
     * jira.MaintenanceExpiryDate=Duration:0, jira.SEN=SEN}</code>
     */
    private static final String ELA_EXPIRED = "AAAA3w0ODAoPeNp1T8Fqg0AQvfsVAz0bdqXNQdiD6B4saoKaWy"
            + "9bM4UpZJXZNdS/T8zSEg89DDzem/fmzUs9WnifLYgEpEjf9qkUoLseEiFfo29is6toQOuw"
            + "LNQf2gj6TJ5Gq3TT6/bYlp0Ocs5oVqEwHtUaF4t9LJKNt18mbMwFVX6oa93mZVYF3Qyerq"
            + "g8zxiIDvmKfC/xCwLdzJdP5MPXySE7Fctts5+JeHncL2Z+lPlIRaSr7Cm4QDcwTeGFKgM/"
            + "Aq4+BLIQSzibxe3Cam3IerTGDv9Fh6q6UfeJbl1kdL8wLAIUfvHhlrsXyIA71JJQhuMw75"
            + "YJxwICFFQJQJsCpLQ8+YI7yuSoDWjaO7d8X02bn";

    /**
     * Perpetual license with maintenance expiry in 46 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 46 days.,
     * jira.MaintenanceExpiryDate=Duration:4060800000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_46 = "AAAA5w0ODAoPeNpVT8FqhEAMve9XBHp2GcVKEeZQdA6W1S1qb7"
            + "3MagopdZTMuNS/7+q0ZQ0EHnnvJS8P5WjgZTYgIghF+pikoQDVtBCJMD58EuvjiTo0Fotc"
            + "/qMdoXpyNBqpqlbVr3XRKE9njHolcu1QrusCkQQi2nnbZcJKDyizc1mqOiueT57XnaMrSs"
            + "cz+kGDfEW+hfgDflzNwwX5/PFmka0Mwn2y74l42e7P5osGcth7QY62Y5q23KUm49Bo0yG4"
            + "EXA1IZCBOIFeL/boLXeyu735zNuT72ksEvEk1voNrCp568MPrg13BDAsAhRsEIuaS5tCbm"
            + "bkoYQ91sZwsaMQJAIUO7U6eXTK56YQk4wxXeUlpf34pi8=X02c4";

    /**
     * Perpetual license with maintenance expiry in 45 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 45 days.,
     * jira.MaintenanceExpiryDate=Duration:3974400000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_45 = "AAAA6Q0ODAoPeNpVT8FqhEAMve9XBHp2Ga27pcIcis7BsrpF7a"
            + "2XqaaQUkfJjEv9+65OW9ZA4JH3XvJyVwwGnicDIoJQJIdjEgpQdQORCOPdJ7Hen6hFYzHP"
            + "5D/aEKojR4ORqmxU9VLltfJ0yqgXItMO5bIuEMdARBtvM49Y6h5lei4KVaX508nzunV0Qe"
            + "l4Qj+okS/I1xB/wI/LqX9HPn+8WmQrg3Cb7Hskntf7k/minhx2XpChbZnGNXehyTg02rQI"
            + "bgBcTAhkID5Ap2e795Yb2c3ebOL1ybfk/vEhjsVSv4FVKa+9+wGumXcMMC0CFD2+RpOYw+"
            + "S9LlN4P7HLIaT/OW9KAhUAg7hvzY1avYQZpu/ghLsmhKYfi0w=X02c8";

    /**
     * Perpetual license with maintenance expiry in 31 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 31 days.,
     * jira.MaintenanceExpiryDate=Duration:2764800000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_31 = "AAAA6Q0ODAoPeNpVT8FqhDAQve9XBHp2SezWFiGHojlYVreove"
            + "0l1SlMqVEmyVL/vqtpyzow8Jj33sybu3I07MUbxmMmePqQpIIz1bQs5uKw+0TS+yN2YCwU"
            + "ufxHG0L16HA0UlWtql/rolGBzgj0QuTagVzWRTyJeLzxtvMElR5AZqeyVHVWPB8DrzuHF5"
            + "COPIRBA3QBuob4A2Fc+eEd6PTxZoGsjMQ22feENK/3vfnCAR30QZCD7QinNXep0Tgw2nTA"
            + "3MhgMQFDw+4F6/Vs98FyI7vZm3tanzyn8WNyeOJL/QZWlbz27getM3cHMCwCFD9Y8dcDSO"
            + "jbXmiJqFNRiYZB854nAhQ0J2bk8kQLaKhSAAAOncJ/aa8RUQ==X02c8";

    /**
     * Perpetual license with maintenance expiry in 30 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 30 days.,
     * jira.MaintenanceExpiryDate=Duration:2678400000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_30 = "AAAA6Q0ODAoPeNpVT8FqhEAMve9XBHp2Ge3WFmEORedgWd2i9r"
            + "aXqaaQUkfJjEv9+65OW9ZA4JH3XvJyVwwGXiYDIoJQJA9xEgpQdQORCA+7T2K9P1KLxmKe"
            + "yX+0IVRHjgYjVdmo6rXKa+XplFEvRKYdymVdIOJARBtvM49Y6h5leioKVaX589HzunV0Qe"
            + "l4Qj+okS/I1xB/wI/LqX9HPn28WWQrg3Cb7Hskntf7k/minhx2XpChbZnGNXehyTg02rQI"
            + "bgBcTAhk4F5Ap2e795Yb2c3ebOL1yXMSxY9PB7HUb2BVymvvfgCs8XcGMCwCFFL0QS3gvT"
            + "atWgnlCIXVMXQK6An9AhRqfzYSnMXjLcvhSi5fIwQNkkMHuA==X02c8";

    /**
     * Perpetual license with maintenance expiry in 25 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 25 days.,
     * jira.MaintenanceExpiryDate=Duration:2246400000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_25 = "AAAA6A0ODAoPeNpVT01rg0AQvedXDPRsWBfjQdhD0T1Yoilqb7"
            + "1sdQJT6ir7Eeq/b3TbEgcGHvPem3nzVE0aXrwGxiFm2SnNYgay7YCzODl8klHHM/WoLZaF"
            + "+Ec7Qg7kaNJC1p1sXpuylYHODaqVKJRDsa6LWBoxvvN2y4y1GlHkl6qSTV4+nwOvekc3FM"
            + "54DIMWzQ3NPcQfCOPajx9oLtc3i8aKKN4n+57JLNt9r79oJIdDEBRoe0PzlrtSpB1qpXsE"
            + "NwGuJgTSwE8wqMUeg+VB9rC38GZ78j3jPEkTttZvYFmLex9+AK08dwEwLAIUF46w2/D/cN"
            + "3skOX9vFL1kq8Nr7gCFAG7KCW6KU3Jb4cDSPPyb9PLtloGX02c4";

    /**
     * Perpetual license with maintenance expiry in 15 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 15 days.,
     * jira.MaintenanceExpiryDate=Duration:1382400000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_15 = "AAAA6Q0ODAoPeNpVT01rg0AQvedXDPRs2DUfFGEPRfdgiaaovf"
            + "Wy0SlMqKvsR6j/vtFtSxwYeMx7b+bNUzFoePUaWAycJYdjwhnIuoGY8f3mSkZtT9Sitphn"
            + "4h+tCNmRo0ELWTayeqvyWgY6NahmIlMOxbwuYseIxStvM41Yqh5Fei4KWaX5yynwqnV0Q+"
            + "GMxzCo0dzQ3EP8gTAufX9Bc/58t2isiPg62fdIZlrue/1FPTnsgiBD2xoal9yFIu1QK90i"
            + "uAFwNiGQBn6ATk12GywPsoe9mTfLkx8J3z3HezbXb2BZintvfgCs+XcAMCwCFA80bfjkqp"
            + "sMOn+3EHIy5QMXCIKnAhQWakOgQyeveyzNUGSZ0wkw/QI5yg==X02c8";

    /**
     * Perpetual license with maintenance expiry in 8 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 8 days.,
     * jira.MaintenanceExpiryDate=Duration:777600000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_8 = "AAAA5g0ODAoPeNpVT8FqhEAMve9XBHp2GaXVIsyh6Bwsq1vU3n"
            + "qZagopdZTMuNS/7+q0ZQ0EHnnvJS935WjgeTYgIghF+hCnoQDVtBCJ8P7wSayPJ+rQWCxy"
            + "+Y92hOrJ0WikqlpVv9RFozydMeqVyLVDua4LRByIaOdtlwkrPaDMzmWp6qx4Onled44uKB"
            + "3P6AcN8gX5GuIP+HE1D+/I549Xi2xlEO6TfU/Ey3Z/Nl80kMPeC3K0HdO05S41GYdGmw7B"
            + "jYCrCYEMPEKvF3v0jhvVzdp85u3HtzRJklis9RtXVfLahx/LRHarMCwCFBp9sqXmlVptVt"
            + "lwZg9tBW3Lda1/AhRJXcXoX9FwQ/qU9JxctdAwsGYFnA==X02c4";

    /**
     * Perpetual license with maintenance expiry in 7 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 7 days.,
     * jira.MaintenanceExpiryDate=Duration:691200000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_7 = "AAAA5w0ODAoPeNpVT8FqhDAQve9XBHp2SWRrqZBD0Rwsq1vU3n"
            + "rJ6hRmqVEmyVL/vqtpyzow8Jj33sybh3I07NUbxmMmePqYpIIz1bQs5uKwuyDp/RE7MBaK"
            + "XP6jDaF6dDgaqapW1W910ahAZwR6IXLtQC7rIp5EPN5423mCSg8gs1NZqjorXo6B153DK0"
            + "hHHsKgAboC3UL8gTCu/HAGOn2+WyArI7FN9j0hzet9b75wQAd9EORgO8JpzV1qNA6MNh0w"
            + "NzJYTMDQsCfW69nug+NOdbc297T++JEmzyLmS/3GVZW89e4Hykd2oTAtAhUAiPblc2xaRb"
            + "d0qk/0JuXBnroePfgCFHcj440gHtmd1XkmCH8lqJUGrEmYX02c4";

    /**
     * Perpetual license with maintenance expiry in 1 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 1 days.,
     * jira.MaintenanceExpiryDate=Duration:172800000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_1 = "AAAA5w0ODAoPeNpVT8FqhEAMve9XBHp2mZF2W4Q5FJ2DZXWL2l"
            + "svs5pCSh0lMy7177s6bVkDgUfee8nLXTFYeJksiBikSB4OiRSg6wZiIe93n8Rmf6QWrcM8"
            + "U/9oQ+iOPA1W6bLR1WuV1zrQKaNZiMx4VMu6SBwiEW+8zTxiaXpU6akodJXmz8fAm9bTBZ"
            + "XnCcOgRr4gX0P8gTAup/6MfPp4c8hORXKb7Hskntf7k/2injx2QZCha5nGNXdhyHq0xrYI"
            + "fgBcTAhkQUJnZrcPjhvVzdps4vXH90Q+xk9iqd+4ulTX3v0AyJ12mzAtAhQDKdYrGrXuTD"
            + "64PqIy341Qn3L4/wIVAIkC/7VlBDWqZPoTQSwfugZ/9O8VX02c4";

    /**
     * Perpetual license with maintenance expiry in 0 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in 0 days.,
     * jira.MaintenanceExpiryDate=Duration:86400000, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_0 = "AAAA5w0ODAoPeNpVUMFqg0AQvecrFno2rJJKEPZQdA+WaIraWy"
            + "8bncCEusrsbqh/X3XTEgcGhnnz3ryZl2LQ7N1pxiMW8uQ1TkLOZN2wiIeH3Q1J7U/YgjaQ"
            + "Z+K/2gCyQ4uDFrJsZPVR5bX0cEqgFiBTFsQiF/A44NGG20wjlKoHkZ6LQlZp/nbyuGot3k"
            + "FYcuAbNdAdaDbxV/h26foL0Pn6aYCMCMKts58RaVr3O/2NPVro/EAGpiUcV9+FQm1BK90C"
            + "swODhQQM55ewTk1m7xlPU0+ymaP1xq/kGB/4Eg+3shRz7n4BUyV2ajAsAhRACkAvSbqozi"
            + "ni66PITJXptE5b2wIUTHujzGjrH+FS6fP774xFQ5zxrU8=X02c4";

    /**
     * Perpetual license with maintenance expiry in -1 days.
     * <p/>
     * <code>{jira.LicenseID=LicenseID, jira.LicenseEdition=ENTERPRISE, jira.CreationDate=2014-06-02,
     * jira.LicenseTypeName=COMMERCIAL, jira.active=true, jira.ServerID=ServerID, jira.NumberOfUsers=-1,
     * jira.LicenseExpiryDate=unlimited, jira.Description=Maintenance to expire in -1 days.,
     * jira.MaintenanceExpiryDate=Duration:0, jira.SEN=SEN}</code>
     */
    private static final String PERPETUAL_EXPIRED = "AAAA4g0ODAoPeNpVT01rg0AQvedXDPRsWCXNQdhD0T0Yoilqb7"
            + "1sdQJT6iqzu6H++0a3LfEw8Jj3MW+eytHAyRsQCcQifT6msQDVtJCI+LD7JNb7M3VoLBa5"
            + "/EcbQvXkaDRSVa2qX+uiUYHOGPVC5NqhXOIicYxEsvG284SVHlBml7JUdVa8nAOvO0c3lI"
            + "49hkWDfEO+l/gDYV354QP5cn2zyFZG8bbZ90Q8r/e9+aKBHPZBkKPtmKa1d6nJODTadAhu"
            + "BFxMCGQgiqHXs90Hy4PsITf3vD75norfnqqS99n9AI2NdTYwLgIVAJbM1xUiOLsjALb8E2"
            + "VtbpwTWSHeAhUAh40yjvaSdImN/IMHYqn9IIeO4AU=X02c0";


    private static final String LICENSE_SERVICE_URL = "www.atlassian.com";
    private static final String SALES = "sales@atlassian.com";

    private static final String OTHER_ADMIN = "admin2";
    private static final String NON_ADMIN = "fred";

    private LicenseBannerRest bannerRestClient;

    @Before
    public void init()
    {
        bannerRestClient = new LicenseBannerRest(jira.environmentData());
        bannerRestClient.clearState();
    }

    @BeforeClass
    public static void addAdmin2()
    {
        backdoor.usersAndGroups().addUser(OTHER_ADMIN).addUserToGroup(OTHER_ADMIN, "jira-administrators");
    }

    @Test
    public void evaluationLicenseDoesNotShowBanner()
    {
        backdoor.license().set(EVAL_2);

        final LicenseBanner licenseBannerRest = getLicenseBanner();
        assertThat(licenseBannerRest.isPresent(), Matchers.equalTo(false));
    }

    @Test
    public void subscriptionLicense()
    {
        //This license should not display because it is outside of 45 days.
        backdoor.license().set(ELA_46);
        LicenseBanner banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //ELA in the range of [45, 30) grouped and dismissed together.
        assertElaRemindMe(ELA_45, 45, ELA_31);

        //ELA in the range of [30, 15) grouped and dismissed together.
        assertElaRemindMe(ELA_30, 30, ELA_25);

        //ELA in the range of [15, 8) grouped and dismissed together.
        assertElaRemindMe(ELA_15, 15, ELA_8);

        //ELA in the range of [7, -inf) grouped together and cannot be dismissed.
        assertElaCannotRemindMe(ELA_7, 7);
        assertElaCannotRemindMe(ELA_0, 0);
        assertElaCannotRemindMe(ELA_EXPIRED, -1);

        //This should reset the remind-me later.
        backdoor.license().set(ELA_46);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        assertElaRemindMe(ELA_45, 45, ELA_31);

        //Banner is dismissed for admin but not for admin2.
        jira.quickLogin(OTHER_ADMIN, OTHER_ADMIN);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(true));

        //Banner should not be visible for non-admin.
        jira.quickLogin(NON_ADMIN, NON_ADMIN);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));
    }

    @Test
    public void perpetualLicenseRemindLater()
    {
        //This license should not display because it is outside of 45 days.
        backdoor.license().set(PERPETUAL_46);
        LicenseBanner banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        assertPerpetualRemindMe(PERPETUAL_45, 45, PERPETUAL_31);
        assertPerpetualRemindMe(PERPETUAL_30, 30, PERPETUAL_25);
        assertPerpetualRemindMe(PERPETUAL_15, 15, PERPETUAL_8);
        assertPerpetualRemindMe(PERPETUAL_7, 7, PERPETUAL_1);

        backdoor.license().set(PERPETUAL_EXPIRED);
        assertPerpetualBanner(getLicenseBanner(), -1);

        //This should reset the remind-me later.
        backdoor.license().set(PERPETUAL_46);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        backdoor.license().set(PERPETUAL_31);
        banner = getLicenseBanner();
        assertPerpetualBanner(banner, 31);
        banner.remindLater();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //Banner is dismissed for admin but not for admin2.
        jira.quickLogin(OTHER_ADMIN, OTHER_ADMIN);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(true));

        //Banner should not be visible for non-admin.
        jira.quickLogin(NON_ADMIN, NON_ADMIN);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));
    }

    @Test
    public void perpetualLicenseRemindNever()
    {
        //This license should not display because it is outside of 45 days.
        backdoor.license().set(PERPETUAL_46);
        LicenseBanner banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        backdoor.license().set(PERPETUAL_31);
        banner = getLicenseBanner();
        assertPerpetualBanner(banner, 31);

        //Remind-me never.
        banner.remindNever();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //Should still be hidden on reload.
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        for (String license : ImmutableList.of(PERPETUAL_25, PERPETUAL_15, PERPETUAL_1, PERPETUAL_0, PERPETUAL_EXPIRED))
        {
            backdoor.license().set(license);
            banner = getLicenseBanner();
            assertThat(banner.isPresent(), Matchers.equalTo(false));
        }

        //Banner is dismissed for admin but not for admin2.
        jira.quickLogin(OTHER_ADMIN, OTHER_ADMIN);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(true));

        //Banner should not be visible for non-admin.
        jira.quickLogin(NON_ADMIN, NON_ADMIN);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //This should reset the banner.
        jira.quickLoginAsAdmin();
        backdoor.license().set(PERPETUAL_46);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //This banner should display after the reset to a good licnese.
        backdoor.license().set(PERPETUAL_25);
        banner = getLicenseBanner();
        assertPerpetualBanner(banner, 25);
        banner.canRemindNever();

        //Ela license should still be displayed after remind-me never.
        jira.quickLoginAsAdmin();
        backdoor.license().set(ELA_25);
        banner = getLicenseBanner();
        assertSubscriptionBanner(banner, 25, true);
    }

    /**
     * Make sure the user can close the license banner for the passed perpetual license.
     *
     * @param triggerLicense the license that will trigger the banner.
     * @param triggerDays the days until the license is to expire.
     * @param noopLicense another license that is close enough to {@code triggerLicense} such that it wont trigger a
     * banner.
     */
    private void assertPerpetualRemindMe(final String triggerLicense, final int triggerDays, final String noopLicense)
    {
        backdoor.license().set(triggerLicense);

        //The trigger license should show the banner again.
        LicenseBanner banner = getLicenseBanner();
        assertPerpetualBanner(banner, triggerDays);

        //Remind later hides the banner.
        banner.remindLater();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //Check the banner not visible on page pop.
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //This license should not show the banner again.
        backdoor.license().set(noopLicense);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));
    }

    private void assertPerpetualBanner(final LicenseBanner banner, final int days)
    {
        assertThat(banner.isPresent(), Matchers.equalTo(true));
        assertThat(banner.canRemindLater(), Matchers.equalTo(true));
        assertThat(banner.canRemindNever(), Matchers.equalTo(true));
        assertThat(banner.days(), Matchers.equalTo(days));
        assertThat(banner.isSubscription(), Matchers.equalTo(false));
        assertMacUrl(banner, days);
    }

    /**
     * Make sure the user cannot close the license banner for the passed ELA license.
     *
     * @param triggerLicense the license to test.
     * @param triggerDays the days until the license is to expire.
     */
    private void assertElaCannotRemindMe(final String triggerLicense, final int triggerDays)
    {
        backdoor.license().set(triggerLicense);

        assertSubscriptionBanner(getLicenseBanner(), triggerDays, false);

        //The user has hit the REST resource to remind later directly.
        bannerRestClient.remindLater();

        assertSubscriptionBanner(getLicenseBanner(), triggerDays, false);

        //The user has hit the REST resource to remind never directly.
        bannerRestClient.remindNever();

        assertSubscriptionBanner(getLicenseBanner(), triggerDays, false);
    }

    /**
     * Make sure the user can close the license banner for the passed license.
     *
     * @param triggerLicense the license that will trigger the banner.
     * @param triggerDays the days until the license is to expire.
     * @param noopLicense another license that is close enough to {@code triggerLicense} such that it wont trigger a
     * banner.
     */
    private void assertElaRemindMe(final String triggerLicense, final int triggerDays, final String noopLicense)
    {
        //This license should trigger the banner.
        backdoor.license().set(triggerLicense);
        LicenseBanner banner = getLicenseBanner();

        assertSubscriptionBanner(banner, triggerDays, true);

        //The user has hit the REST resource to remind never directly. It shouldn't work.
        bannerRestClient.remindNever();
        banner = getLicenseBanner();
        assertSubscriptionBanner(banner, triggerDays, true);

        //Remind later hides the banner.
        banner.remindLater();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //Check the banner not visible on page pop.
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));

        //This license should not trigger a new banner.
        backdoor.license().set(noopLicense);
        banner = getLicenseBanner();
        assertThat(banner.isPresent(), Matchers.equalTo(false));
    }

    private void assertSubscriptionBanner(final LicenseBanner banner, final int days, final boolean remind)
    {
        assertThat(banner.isPresent(), Matchers.equalTo(true));
        assertThat(banner.isSubscription(), Matchers.equalTo(true));
        assertThat(banner.days(), Matchers.equalTo(days));
        assertThat(banner.canRemindLater(), Matchers.equalTo(remind));

        assertMacUrl(banner, days);

        final URI saleUri = URI.create(banner.getSalesUrl());
        assertThat(saleUri.getScheme(), Matchers.equalTo("mailto"));
        assertThat(saleUri.getSchemeSpecificPart(), Matchers.equalTo(SALES));
    }

    private void assertMacUrl(final LicenseBanner banner, final int days)
    {
        final URI macUri = URI.create(banner.getMacUrl());
        assertThat(macUri.getHost(), Matchers.equalTo(LICENSE_SERVICE_URL));
        assertThat(macUri.getQuery(), Matchers.equalTo(generateQueryString(days)));
    }

    private String generateQueryString(int days)
    {
        int dayCategory;
        if (days <= 7)
        {
            dayCategory = 7;
        }
        else if (days <= 15)
        {
            dayCategory = 15;
        }
        else if (days <= 30)
        {
            dayCategory = 30;
        }
        else if (days <= 45)
        {
            dayCategory = 45;
        }
        else
        {
            throw new IllegalArgumentException();
        }
        return String.format(MAC_TEMPLATE, dayCategory);
    }

    private LicenseBanner getLicenseBanner()
    {
        jira.goTo(BrowseProjectsPage.class);
        return pageBinder.bind(LicenseBanner.class);
    }

    private static class LicenseBannerRest extends RestApiClient<LicenseBannerRest>
    {
        protected LicenseBannerRest(final JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        public LicenseBannerRest clearState()
        {
            laterResource()
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .delete();

            return this;
        }

        public LicenseBannerRest remindLater()
        {
            laterResource()
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post();

            return this;
        }

        public LicenseBannerRest remindNever()
        {
            bannerResource().path("remindnever")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post();

            return this;
        }

        private WebResource laterResource()
        {
            return bannerResource().path("remindlater");
        }

        private WebResource bannerResource() {return createResourceInternal().path("licensebanner");}
    }
}
