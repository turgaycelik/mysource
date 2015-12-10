package com.atlassian.jira.webtests;

/**
 * This contains all the known license keys for testing.  Add new ones here not in your tests!
 * When you add new licenses remember to use the time bomb feature.
 *
 * @since v4.0
 */
public class LicenseKeys
{
    /**
     * These are the older Licensing Version 1 objects. ***************************************************************
     */
    private static final String V1_ENTERPRISE_KEY = "NQrPoNOBNRHnXHjaFJNPxwrQoropKmMGGTamaPMdptjcGW\n"
            + "mi2KouuJLPf6fVkEq<hggtmF2K6LwzkgD3KEWKGzmc3rLm\n"
            + "QmRSsRRNrponOQoPMNwwqOmmPNQqmRmmQXXsSVUvsVXuos\n"
            + "tUUoomsotmummmmmUUoomsotmummmmmUU1qiXppfXk";

    public static final License V1_ENTERPRISE = new License(V1_ENTERPRISE_KEY, "JIRA Enterprise: Commercial Server", "Atlassian", null, -1);

    /**
     * These are the newer Licensing Version 2 objects. **************************************************************
     */

    public static final License V2_EVAL_EXPIRED = new License("AAABGw0ODAoPeNptUNFqg0AQfM9XHPTZctpKSeAejB6NNDEhmrTkbWu26RU9Ze+U5u9rolADgYPld\n"
            + "nZmdvYha5AFzYl5nLl85k9nvs9CmWZdw32a/CiCx6XKURuUR2VVpYVMMrndbONU9nBICBcgAovC4\n"
            + "5w73O1eD26ArEZKoERxo5ada7x2w/VqJbdhHCwHuUpbyK1cgSoGSvdVLQpLDd7MjFSTpvxEWn/tD\n"
            + "JIRjjtJkVqkOBLz12nmfOz2z87b4bBw5tx97ylrOoFW5rq7CGwBxijQt5F/a0Xn+8Eayr/B4F0wQ\n"
            + "pOTqvtzXTTwyGQLRXM1Y2FVlki5goLtPTZ49cwutbaoQedj882Lt+jxf5XROVKZiAyN7erkDxdym\n"
            + "50wLAIUJjZ/OHlXPtjpnpGC7Wr1/Tt7rw0CFCMJ2F9UWhexdC+7amQDpVhpEKmQX02ea",

            "Expired Evaluation Commercial V2 License", "Atlassian", "TestSEN", -1);

    public static final License V2_COMMERCIAL = new License("AAABIg0ODAoPeNptUMFqg0AQvecrFnq2rGlLMbAHo0tjG42oaUvoZWon6RbdyOwqzd/XqIcGAgvDz\n"
            + "pv35r25KVpkfntgc85cd8G9xYPHApkXfcO9m/0ogtu1KlEblF/KqqMWMilklmZRLkc4IIQzEIJFM\n"
            + "eecO9zt3wimQFYjJVCjuFArTg0O3WATxzILIn89yR21hdLKGFQ1Ufqv6lBYavFi5p9q0tafSJv91\n"
            + "iAZ4bizHKlDikKxfPIK5337eu+87HYrZ8ndt5GyoQNoZQbvwrcVGKNAX0b+bRSdhmDp43w1RWqp/\n"
            + "AaDV/OGaEpSzaD5HGU+k9oiNaQMfixYcKxrpFJBxUZ/I6mP2k9p0OX1jbKDqh197qEy0xFymYgCj\n"
            + "e3r7A+qwpkLMCwCFGQz7py22PaXS0c8o8OEBt72TT3LAhRJAWqqhhADYvl3G2ZbxIELhFLwDQ==X\n"
            + "02em",

            "JIRA Enterprise: Commercial Server", "Atlassian", "TestSEN", -1);

    public static final License V2_PERSONAL = new License("AAABHA0ODAoPeNptj0Frg0AQhe/5FQs9W1ZTkAT2YOLS2CYqatoSepnaSbrFrDK7SvPva9VDA4GBZ\n"
            + "fbNvHnf3a7WLGhPzF0w119yb+n6bC3zgnncnc++FcH9VpWoDcpPZVWthYwLmaVZlMtRXhPCnxCCR\n"
            + "eFxzh3u9jWKKZDVSDGcUVy5FZcGh99UZnkSB9vJrNYWSit3oKppoW9Vh8JSi1cz/zzj9vyBlBz3B\n"
            + "smI+SxH6pCiUKweF4Xztn95cJ4Ph42z4u7ruJHQCbQyQ3AR2AqMUaCveX8aRZeBKvW9zcTTUvkFB\n"
            + "m/ChmhKUs3g+RRlAZPaIjWkDL4vWdpnqzVU42wP2IsadHn7kOygasd4R6jMhJ7LWBRobP/OfgH1W\n"
            + "ZUWMCwCFHJLYmBBiHkB36jU7bVWnjxK3Hx6AhQIm6bnE7iWnf7NQzalVC22Jsvw3A==X02ee",

            "JIRA Enterprise: Personal", "Atlassian", "TestSEN", 3);

    public static final License V2_STARTER = new License("AAABGw0ODAoPeNptkNFLwzAQxt/3VwR8rqR1MlfIQ7cFN3VdaTuV4ctZbzPSpeWSDvffm7V9sDAIh\n"
            + "OT77rv73c260ixqDsyfMn8S8odwHLC5zHIWcP9u9KMIbl9Ugdqg/FJWVVrIOJdpkq4y2clzQrgIC\n"
            + "7AoAs65x313OjEBshophiOKQVp+rrH9zfIodYF9VqUtFFauQZW93z3VCYWlBgeef5Fxc/xE2uy3B\n"
            + "smI+1GGdEJaLcTscZp779vXsfe82y29GfffuooNHUAr084tIluCMQr0EPe3VnRuoZJJsOxxGiq+w\n"
            + "eBV1gWaglTdZj6t0ohJbZFqUgY/QpZZtwqkzur4nKZBF9f7yBOUTTfdHkrTk2cyFjka6+7RH/F2l\n"
            + "H4wLAIUWvw/jnDEE4963OH1O5dNg/gG7SECFEWU+mQNKFb0QN472//3c77+k1EHX02ea",

            "JIRA Enterprise: Starter", "Atlassian", "TestSEN", 5);

    public static final License V2_COMMUNITY = new License("AAABIA0ODAoPeNptj01rwzAMhu/9FYadM+x2UFrwoU3Mmm1JSz72UXbRMrXzSJwgO2X998uSHFYoC\n"
            + "IT0Sq/03ES1Yav2yMSCiflS8OVsznyVZmzKxWzyrQlun3SBxqL61E7XRqo4U8kuCVM1yD4h/AkBO\n"
            + "JRTzrnHRReDuANyBimGCuWFW3ZusO/62yjK4zB7G91q46BwKgJdjhtdqU8oHbV4MfPPNG6rD6TtI\n"
            + "bdIVnpikiKdkMJAru8XmfeaP995j/v9xltz8TKsbOkIRtv+dblyJVirwVwS/zSazj3Xbj7djEQtF\n"
            + "V9g8SpugLYg3fSeD2GyYso4pIa0xfcl8+uqao1252G4Q+xUA6a4fkmdoGyH/w5Q2hE+VbHM0LouT\n"
            + "34BpHKWJDAsAhQMxIl7t2V+71d0r3BlO8JybF6OrQIUEFFFNfevKT1IkGMmCK+bnn/YL2Y=X02ei",

            "JIRA Enterprise: Community", "Atlassian", "TestSEN", -1);

    public static final License V2_OPEN_SOURCE = new License("AAABIA0ODAoPeNptj11LwzAUhu/7KwJeV5ptMFbIRbcFN3Vt6YfKEORYz2akS8tJWty/t7YVHAwCI\n"
            + "XnzPjnPza7SLGiOjC8Yn/t84U9nbCXTjE08PnW+FMHtoypQG5QfyqpKCxlmMomTbSqHeEUIv8EaL\n"
            + "IqJ53mux7s1hDGQ1UghnFBc0LJzjf1tFMvwLY3yZPXHq7SFwsodqHLsdEfVorDU4MWbf9iwOb0jR\n"
            + "YfcIBnhcidFapG2a7G8W2TuS/40cx/2+4279PjzUInoCFqZfngR2BKMUaAvnb9rRefeLJ5PNqNTQ\n"
            + "8UnGLwqvEZTkKp75v02CZjUFqkmZfDVZ1GNmqVVBxhFOsku16CL63/JFspmmPAApRlbqQxFhsZ2u\n"
            + "/MD18uXJTAtAhRi22v5M5p+uKSbtKE2vlWTYuuQKgIVAI17Md7H1G38C47mvOkeyuuUpt0VX02ei",

            "JIRA Enterprise: Open Source", "Atlassian", "TestSEN", -1);

    public static final License V2_DEVELOPER = new License("AAABHg0ODAoPeNptj1Frg0AMx9/7KQ727DjtoGvhHmw9VrfWitpulL1kLu1u2FNyp6zffk59mFAIh\n"
            + "OSf/JPf3bbUzK/PzJ0zd7bw+GL6yFYyzZjH3enkWxHcb1SO2qD8VFaVWsgok0mchKns5RUh/AkBW\n"
            + "BQe59zhbhu9GANZjRTBBcXILbtW2HUDeZCbXSyTwa3UFnIrt6CKYaMtVYPCUo2jmX+mUX35QNqd9\n"
            + "gbJCMedpEgNUhiI5dM8c972hwfn5XhcO0vuvvYrOzqDVqZ7Xfi2AGMU6DHxT6Xo2nHFM289ENWUf\n"
            + "4HBm7gBmpxU1Xk+h4nPpLZIFSmD7wsWYINFWSH1wy1iq2rQ+e1LsoGi7v87QWEG+FRGIkNj2zz5B\n"
            + "XkFlegwLAIUee7TGfMvWh1jHjZiPB3LTc3/ZRQCFFTfBoGP5CKR94SdfvKw423+sZ6nX02ee",

            "JIRA Enterprise: Developer", "Atlassian", "TestSEN", -1);

    public static final License V2_DEMO = new License("AAABIw0ODAoPeNptkNFrgzAQxt/7VwT27DBuUCrkwdawuq1aNN1G2cvNXbsMG+USZf3v59SHFQqBk\n"
            + "Pvu++5+udnUhkXtkfEF4/Mw4GEwZytZKBb4/G72rQlun3WJxqL81E7XRshUyXybJ4Uc5RUh/AkxO\n"
            + "BSB7/uez/szilsgZ5BSOKG4SFPnBodqLDdZWqg8UkmWTom1cVA6uQFdTa7+qTsUjlq86PkXnLanD\n"
            + "6TssLNIVnh8ViB1SEkslg8L5b3tXu69p/1+7S19/jpaMjqC0XZYX0SuAms1mEvqn0bTeWDbzoP1R\n"
            + "NVS+QUWryLHaEvSzZD5mOQRk8YhNaQtvocsxlNtrKNh5mjoMfsOA6a8Pk12ULXjjgeo7PQBhUyFQ\n"
            + "uv6e/YLJGyY6TAsAhQFScW2MOePs00QeULOiohuFQ8b1AIUCfMVl2+3iyV8NU21ENcMMP+MyAs=X\n"
            + "02em",

            "JIRA Enterprise: Demonstration", "Atlassian", "TestSEN", -1);

    public static final License V2_HOSTED = new License("AAABGQ0ODAoPeNptj1FrgzAUhd/9FYE9O9QOSgt5sDVMt9WK2m2UvdzZ2y7DRrmJsv77WfWhQiFwS\n"
            + "c65J+d72FSK+c2JuQvmzpeet3RnbC2ynHmOO7N+JcHjmyxQaRQHaWSluIhzkSZplIlBXhPCVQjAI\n"
            + "Pccx7EdtzuDmAAZhRTDGfkkLb/U2L+G2ywXwRhVKQOFERuQ5WjvrrJFbqjBiecmMW7O30jb404j6\n"
            + "WsDK0NqkaKAr54Xuf25e3+yX/f70F457sews6UTKKn74tw3JWgtQU15/2pJl54qmXvhyNNQ8QMa7\n"
            + "8IGqAuSdZ/5EqU+E8og1SQ1fi1ZWGmDh8HZAXaSAlXc/0a0UDZDuSOUekTPRMxz1Kab1j+MzpP5M\n"
            + "CwCFEf9LaFQ9qFgspGQdniHbiBqTYjnAhQT7TCJ8S+EUYM619f2fy/1fY+8rA==X02ea",

            "JIRA Enterprise: Hosted", "Atlassian", "TestSEN", 200);

    public static final License V2_DEVELOPER_LIMITED = new License("AAABDg0ODAoPeNpdj1FrgzAUhd/zKwJ7dqS6wSrkwc6wurUqartR9pLZW5dhY7iJsv77tZZCu6fL4\n"
            + "XK+c87dstM06hvKAjrxwyAIfZ+KsqI+Y1MSg61RGac6zSuwjraqBm2B7jqkpu0bpekWBmg7A2hJ2\n"
            + "u+/ALPdyh4VfyTPCPLkjaUDfgJ67MljAflRKO8XZ5TYqhEv0koUeZGU4vyWtVMDcIc9kKVU2oGWu\n"
            + "gbxaxQeRmAezEmGjdTKjik8cq20VklNSsABMIn57GVaeR+r9YP3ttnMvRmbvJNSpOOY471pUh0M8\n"
            + "NekiKg4pqFBZeEzpPFlH7k0vq1w5U7lHngs1mKR5aIgeY/1t7Twf/4fRyp8ezAtAhR2RgZb5a98I\n"
            + "4hExn2aanmpAxTLrwIVAIw1YU2Dns+O9mjEsQqh7hCMe67gX02dp",

            "Test license for plugin developers", "Atlassian", "TestSEN", 5);

    public static final License V2_COMMERCIAL_LIMITED = new License("AAABIA0ODAoPeNptj1FrwjAUhd/9FYE9d6SdIgp50BqmzFax1Q3f7urV3dGmcpPK/PerbQcThEDIP\n"
            + "Tnnnu8pKo2YVCfhj4Q/HAf9seyLUCepCKT/0vsmhuclZWgs6gM5Ko3Scao3680i0a0cMsJNmIFDF\n"
            + "UgpPenXpxXXwM4gx1CguktLr2dspuEqivQmXEyWXVxpHGROR0B5Z6mfdEHluMK7P/9S46r4RF4dt\n"
            + "xbZqkEvQb4gL2Zq+jpKvY/tru+97fdzbyr999ax4hMYsk11NXE5WEtg7ol/zsTXhms9DOYdUcXZF\n"
            + "1h8iDtDmzGdm8ywLArkjCAXA3HrJZZUkMOD2AWi29C6alTj0IDJHq/UF8irtugR8j9XomOVonX13\n"
            + "fsFrJ+YsjAsAhQodsJu0ewcdC7NaLjKJpH93o6Y1wIUO1vGIV5B0t4im0yiJB7XxRyFbqk=X02ei",

            "Commercial 5 User Limited V2 License", "Atlassian", "TestSEN", 5);

    /**
     * For testing purpose you can use this MAINTENANCE EXPIRED COMMERCIAL LICENSE
     *
     * However you cant put it in a func test because JIRA will lock up with a Johnson Event!
     *
AAABNA0ODAoPeNpVkE9vgzAMxe/5FJF2pqKM/lmlSCuQA1uhFUWdJu3ipV6bCQJKAlq//SgUdZN9s
9/7PfshqRRdNyf6OKWet5r5K39Jwyinnus+kQiN0LK2slLsJc7WlCuLutbS4MeKhlVZohYSCrpH3
aImoUa4LkdgkV0dnGtPSVgpC8KmUCI7IKqGBmfQXZFvqWGykQKVQX6UPYmnOc92Wbzno5AnIAvWf
g6iZ7AFGCNBTURVEt5C0fRY9gWFwX+e+aXGHhtuk4RnYbzeDPPOVbbIrG6QdO7dXQqUQP5TS335k
3/peC7Z6hMoaQZIjsbSfg+P9IYhe56yqTtfzHyfDM+IIxbk6dw5uO+58xbywAleZztyE3TTTRyNi
vEBd3ijCllKi0eya7Q4g8Ex0+KW6RcTBpI3MCwCFEo+rTahLjs5IQIjIS2OGtr65XncAhRIqzT82
vXSzz/u9sheljEfB4G2JQ==X02ff

     */

    public static class License
    {
        private final String licenseString;
        private final String description;
        private final String organisation;
        private final String sen;
        private final int maxUsers;

        public License(final String licenseString, final String description, final String organisation, final String sen, final int maxUsers)
        {
            this.licenseString = licenseString;
            this.description = description;
            this.organisation = organisation;
            this.sen = sen;
            this.maxUsers = maxUsers;
        }

        public String getLicenseString()
        {
            return nvl(licenseString);
        }

        public String getDescription()
        {
            return nvl(description);
        }

        public String getOrganisation()
        {
            return nvl(organisation);
        }

        public String getSen()
        {
            return nvl(sen);
        }

        public int getMaxUsers()
        {
            return maxUsers;
        }

        private String nvl(String s)
        {
            return s == null ? "" : s;
        }
    }


/*
-----------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------
    To use this code to generate a license, add a dependency on atlassian-extras-encoder (same version as atlassian-extras)
    Update the properties you need and run.

    https://maven.atlassian.com/private/com/atlassian/extras/atlassian-extras-encoder/

    !!!!!!!!!!!!! Do no leave the encoder dependency in, as it MUST NOT ship with products. !!!!!!!!!!!!!
-----------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------
*/

    /*
    public static final int TIME_BOMB_HOURS = 72;

    public static void main(String[] args)
    {
//		generateLicense("Expired Evaluation Commercial V2 License", "2000-01-01", null, LicenseType.COMMERCIAL, LicenseEdition.ENTERPRISE, true, -1);
//      generateLicense("JIRA Enterprise: Commercial Server", null, null, LicenseType.COMMERCIAL, LicenseEdition.ENTERPRISE, false, -1);
//		generateLicense("JIRA Enterprise: Personal", null, null, LicenseType.PERSONAL, LicenseEdition.ENTERPRISE, false, 3);
//		generateLicense("JIRA Enterprise: Starter", null, null, LicenseType.STARTER, LicenseEdition.ENTERPRISE, false, 5);
//		generateLicense("JIRA Enterprise: Community", null, null, LicenseType.COMMUNITY, LicenseEdition.ENTERPRISE, false, -1);
//		generateLicense("JIRA Enterprise: Open Source", null, null, LicenseType.OPEN_SOURCE, LicenseEdition.ENTERPRISE, false, -1);
//		generateLicense("JIRA Enterprise: Developer", null, null, LicenseType.DEVELOPER, LicenseEdition.ENTERPRISE, false, -1);
//		generateLicense("JIRA Enterprise: Demonstration", null, null, LicenseType.DEMONSTRATION, LicenseEdition.ENTERPRISE, false, -1);
//		generateLicense("JIRA Enterprise: Hosted", null, null, LicenseType.HOSTED, LicenseEdition.ENTERPRISE, false, 200);
//		generateLicense("Commercial 5 User Limited V2 License", null, null, LicenseType.COMMERCIAL, LicenseEdition.ENTERPRISE, false, 5);

//		generateExpiredEvalLicenses();
//		generateExpiredLicenses();
//		generateExpiredMainteanceLicenses();
//		generateLicenseOpenEndedLimitedLic();
//		generateLicenseOpenEndedLic();
    }

    private static void generateExpiredEvalLicenses()
    {
        generateLicense("Expired Evaluation Commercial V2 License", "1999-01-01", null, LicenseType.COMMERCIAL, LicenseEdition.ENTERPRISE, true, -1);
    }

    private static void generateExpiredLicenses()
    {
        generateLicense("Expired Commercial V2 License", "1999-01-01", null, LicenseType.COMMERCIAL, LicenseEdition.ENTERPRISE, false, -1);
        generateLicense("Expired Community V2 License", "1999-01-01", null, LicenseType.COMMUNITY, LicenseEdition.ENTERPRISE, false, -1);
        generateLicense("Expired Demo V2 License", "1999-01-01", null, LicenseType.DEMONSTRATION, LicenseEdition.ENTERPRISE, false, -1);
    }

    private static void generateExpiredMainteanceLicenses()
    {
        generateLicense("Maintenance Expired Commercial V2 License", null, "1999-01-01", LicenseType.COMMERCIAL, LicenseEdition.ENTERPRISE, false, -1);
        generateLicense("Maintenance Expired Community V2 License", null, "1999-01-01", LicenseType.COMMUNITY, LicenseEdition.ENTERPRISE, false, -1);
        generateLicense("Maintenance Expired Demo V2 License", null, "1999-01-01", LicenseType.DEMONSTRATION, LicenseEdition.ENTERPRISE, false, -1);
    }

    private static void generateLicenseOpenEndedLimitedLic()
    {
        generateLicense("Commercial 5 User Limited V2 License", null, null, LicenseType.COMMERCIAL, LicenseEdition.ENTERPRISE, false, 5);
    }

    private static void generateLicenseOpenEndedLic()
    {
        generateLicense("Some license description", null, null, LicenseType.COMMERCIAL, LicenseEdition.ENTERPRISE, false, 5);
    }


    private static void generateLicense(final String licenseDesc, final String expiryDate, final String maintenanceDate,
            final LicenseType licenseType, final LicenseEdition licenseEdition, final boolean evaluation, final int maxNumberOfUsers)
    {
        try
        {
            final LicenseCreator licenseCreator = new LicenseCreator();
            licenseCreator.setServerId("BG9T-XUV4-KZZH-B01W");
            licenseCreator.setSupportEntitlementNumber("TestSEN");
            final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final Date date = sdf.parse("2000-01-01");
            licenseCreator.setCreationDate(date);
            licenseCreator.setPurchaseDate(date);
            licenseCreator.setNumberOfUsers(maxNumberOfUsers);
            licenseCreator.setOrganisation("Atlassian");
            licenseCreator.setLicenseType(licenseType);
            licenseCreator.setDescription(licenseDesc);
            licenseCreator.setEvaluation(evaluation);
            licenseCreator.setProducts(ImmutableList.of(Product.JIRA));
            if (expiryDate == null)
            {
                licenseCreator.setTimebombExpiryHours(TIME_BOMB_HOURS);
            }
            else
            {
                licenseCreator.setExpiryDate(sdf.parse(expiryDate));
            }
            if (maintenanceDate == null)
            {
                licenseCreator.setTimebombMaintenanceExpiryHours(TIME_BOMB_HOURS);
            }
            else
            {
                licenseCreator.setMaintenanceExpiry(sdf.parse(maintenanceDate));
            }
            licenseCreator.setPartnerName("");
            licenseCreator.setLicenseEdition(licenseEdition);
            licenseCreator.setProductLicenseBuilderFactory(new Function<Product, LicenseBuilder>()
            {
                public LicenseBuilder apply(com.atlassian.extras.api.Product from)
                {
                    return new DefaultJiraLicenseBuilder();
                }
            });

            final String newLicense = licenseCreator.createLicense();

            printLicense(getJiraLicense(newLicense));

            System.out.println();
            System.out.println(newLicense);
            System.out.println();


            System.out.println("----------------------");
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static JiraLicense getJiraLicense(final String key)
    {
        return (JiraLicense) LicenseManagerFactory.getLicenseManager().getLicense(key).getProductLicense(Product.JIRA);
    }

    private static void printLicense(final JiraLicense license)
    {
        System.out.println("Type: " + license.getLicenseType());
        System.out.println("Desc: " + license.getDescription());
        System.out.println("Creation date: " + license.getCreationDate());
        System.out.println("Purchase date: " + license.getPurchaseDate());
        System.out.println("Maintenance exp: " + license.getMaintenanceExpiryDate());
        System.out.println("Expiry: " + license.getExpiryDate());
        System.out.println("Eval: " + license.isEvaluation());
        System.out.println("Users: " + license.getMaximumNumberOfUsers());
        System.out.println("Org: " + license.getOrganisation().getName());
        System.out.println("Edition: " + license.getLicenseEdition());
        System.out.println("SEN: " + license.getSupportEntitlementNumber());
    }

    private static void printLicenses()
    {
        for (final Field field : LicenseKeys.class.getDeclaredFields())
        {
            try
            {
                field.setAccessible(true);
                final Object val = field.get(null);
                if (val instanceof License)
                {
                    final License lic = (License) val;
                    System.out.println("~~~~ " + field.getName());
                    System.out.println(lic.getLicenseString());
                    System.out.println("");

                    final JiraLicense license = (JiraLicense) LicenseManagerFactory.getLicenseManager()
                            .getLicense(lic.getLicenseString()).getProductLicense(Product.JIRA);

                    printLicense(license);
                    System.out.println("");
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    */
}
