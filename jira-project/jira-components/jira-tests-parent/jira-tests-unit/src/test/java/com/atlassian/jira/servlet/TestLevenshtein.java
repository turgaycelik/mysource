package com.atlassian.jira.servlet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLevenshtein
{

    /**
     * Tets that the Levenshtein will produce all of the words that are
     * nearby 'dick'.
     */
    @Test
    public void nearbyDick()
    {
        Set<String> expected = new HashSet<String>();

        // changes to the first character
        expected.addAll(Arrays.asList(
            "aick", "bick", "cick", "dick", "eick", "fick", "gick", "hick",
            "iick", "jick", "kick", "lick", "mick", "nick", "oick", "pick",
            "qick", "rick", "sick", "tick", "uick", "vick", "wick", "xick",
            "yick", "zick"
        ));
        // changes to the second character
        expected.addAll(Arrays.asList(
            "dack", "dbck", "dcck", "ddck", "deck", "dfck", "dgck", "dhck",
            "dick", "djck", "dkck", "dlck", "dmck", "dnck", "dock", "dpck",
            "dqck", "drck", "dsck", "dtck", "duck", "dvck", "dwck", "dxck",
            "dyck", "dzck"
        ));
        // changes to the third character
        expected.addAll(Arrays.asList(
            "diak", "dibk", "dick", "didk", "diek", "difk", "digk", "dihk",
            "diik", "dijk", "dikk", "dilk", "dimk", "dink", "diok", "dipk",
            "diqk", "dirk", "disk", "ditk", "diuk", "divk", "diwk", "dixk",
            "diyk", "dizk"
        ));
        // changes to the fourth character
        expected.addAll(Arrays.asList(
            "dica", "dicb", "dicc", "dicd", "dice", "dicf", "dicg", "dich",
            "dici", "dicj", "dick", "dicl", "dicm", "dicn", "dico", "dicp",
            "dicq", "dicr", "dics", "dict", "dicu", "dicv", "dicw", "dicx",
            "dicy", "dicz"
        ));
        // additions at each place within the string
        expected.addAll(Arrays.asList(
            "ick", "dck", "dik", "dic"
        ));
        // deletions from the start of the string
        expected.addAll(Arrays.asList(
            "adick", "bdick", "cdick", "ddick", "edick", "fdick", "gdick", "hdick",
            "idick", "jdick", "kdick", "ldick", "mdick", "ndick", "odick", "pdick",
            "qdick", "rdick", "sdick", "tdick", "udick", "vdick", "wdick", "xdick",
            "ydick", "zdick"
        ));
        // deletions from between the first two characters
        expected.addAll(Arrays.asList(
            "daick", "dbick", "dcick", "ddick", "deick", "dfick", "dgick", "dhick",
            "diick", "djick", "dkick", "dlick", "dmick", "dnick", "doick", "dpick",
            "dqick", "drick", "dsick", "dtick", "duick", "dvick", "dwick", "dxick",
            "dyick", "dzick"
        ));
        // deletions from between the second and third characters
        expected.addAll(Arrays.asList(
            "diack", "dibck", "dicck", "didck", "dieck", "difck", "digck", "dihck",
            "diick", "dijck", "dikck", "dilck", "dimck", "dinck", "diock", "dipck",
            "diqck", "dirck", "disck", "ditck", "diuck", "divck", "diwck", "dixck",
            "diyck", "dizck"
        ));
        // deletions from between the third and fourth characters
        expected.addAll(Arrays.asList(
            "dicak", "dicbk", "dicck", "dicdk", "dicek", "dicfk", "dicgk", "dichk",
            "dicik", "dicjk", "dickk", "diclk", "dicmk", "dicnk", "dicok", "dicpk",
            "dicqk", "dicrk", "dicsk", "dictk", "dicuk", "dicvk", "dicwk", "dicxk",
            "dicyk", "diczk"
        ));
        // deletions from the end of the string
        expected.addAll(Arrays.asList(
            "dicka", "dickb", "dickc", "dickd", "dicke", "dickf", "dickg", "dickh",
            "dicki", "dickj", "dickk", "dickl", "dickm", "dickn", "dicko", "dickp",
            "dickq", "dickr", "dicks", "dickt", "dicku", "dickv", "dickw", "dickx",
            "dicky", "dickz"
        ));

        assertEquals(expected, Levenshtein.nearbyWords("dick", "abcdefghijklmnopqrstuvwxyz"));
    }

}
