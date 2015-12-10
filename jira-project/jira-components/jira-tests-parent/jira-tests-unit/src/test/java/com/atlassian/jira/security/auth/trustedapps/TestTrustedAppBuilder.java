package com.atlassian.jira.security.auth.trustedapps;

import org.junit.Test;

public class TestTrustedAppBuilder
{
    @Test
    public void testKeyBuild() throws Exception
    {
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder().setId(1).setApplicationId("confluence:12529470").setName(
            "confluence:12529470");
        builder.setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgortjlt/XSAR3fO7UFIW5dWjj4mzjC0+Av1TSvBdXJMUCirLtwFXiBpFo2Se1YfxfDal2LI5pfxvKJwxuITwYwiIexT+Kl/brkLjheIFU9PmELuCdcB04T5OauS0iS7+1pF2lMxhvADOaC7AivXckv1MXkk+ThxOVxaEd6qSmhg1HNw/XkZFr6//O5F1HkywIbz8EdknXY3agW/ZxJbZQI8Vvb1ZA1uRBE6KrqLB5KIFTsR/otzcohElYPLGEya1W1fvR0+fvBBK/N9S0yCrmcScZAGSzBRLBSwy/fiyCV5jFCUDAbYpev7LqGb8+R9sfl3b300M5v+Bx1qQ3hcuVwIDAQAB");
        System.out.println(builder.toInfo());
    }
}
