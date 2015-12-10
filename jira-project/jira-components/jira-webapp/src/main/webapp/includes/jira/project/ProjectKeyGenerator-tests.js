AJS.test.require("jira.webresources:jira-global");

function keygen(len) {
    len = (typeof len == 'number') ? len : 0;
    return new JIRA.ProjectKeyGenerator({desiredKeyLength:len});
}

module("JIRA.ProjectKeyGenerator");

test("always outputs in uppercase", function() {
    _.each(["thekey", "TheKey", "ThEkEy", "tHEKEy"], function(key) {
        equal(keygen(6).generateKey(key), "THEKEY");
    });
});

test("insignificant whitespace is stripped", function() {
    equal(keygen().generateKey("   \r\n    key  "), "KEY", "should ignore spaces and newlines");
});

test("multiple words are converted in to an acronym", function() {
    equal(keygen().generateKey("uno dos tres"), "UDT");
});

test("english grammatical words are not used to generate keys", function() {
    equal(keygen(4).generateKey("the key"), "KEY", "'the' is an ignored grammatical word");
    equal(keygen(4).generateKey("the key of G Major"), "KGM", "'the' and 'of' are ignored grammatical words");
    equal(keygen(8).generateKey("As A User I Would Like To Be Able To"), "UIWLTBAT", "'as' and 'a' are ignored grammatical words");
});

test("ignored words are only ignored when key length must be reduced", function() {
    equal(keygen(0).generateKey("Game of Thrones"), "GOT", "keys of infinite length shouldn't care too much");
    equal(keygen(9).generateKey("Game of Thrones"), "GT", "ignored words are stripped if their original string is longer than the desired length");
});

test("syllables are removed when key longer than desired length", function() {
    equal(keygen(4).generateKey("thekey"), "THEK", "should strip at the second 'e'");
    equal(keygen(6).generateKey("macchiato"), "MAC", "should strip at the second 'c'");
    equal(keygen(7).generateKey("affogato"), "AF", "should strip at the second 'f'");
});

test("punctuation is ignored", function() {
    equal(keygen().generateKey("I'm a little tea-pot, short and stout!"), "IALTSAS");
});

test("numbers are ignored", function() {
    equal(keygen().generateKey("l337sp34k"), "LSPK", "nobody should have to read numbers like they were letters");
});

test("certain diacritic characters are converted to english alphabet equivalents", function() {
    var resume = String.fromCharCode(114, 233, 115, 117, 109, 233);
    var pate = String.fromCharCode(112, 226, 116, 233);
    equal(keygen().generateKey(resume), "RESUME", "accented e should become a regular e");
    equal(keygen().generateKey(pate), "PATE", "accented a should become a regular a");

    var grave = String.fromCharCode(224, 232, 236, 242, 249);
    var acute = String.fromCharCode(225, 233, 237, 243, 250);
    var circumflex = String.fromCharCode(226, 234, 238, 244, 251);
    _.each([grave, acute, circumflex], function(accented) {
        equal(keygen().generateKey(accented), "AEIOU");
    });
});

test("extended characters from utf-8 are ignored", function() {
    var nihonjin = String.fromCharCode(26085, 26412, 20154);
    equal(keygen().generateKey(nihonjin), "", "japanese characters are ignored");

    var anna_karenina = String.fromCharCode(1040, 1085, 1085, 1072, 32, 1050, 1072, 1088, 1077, 1085, 1080, 1085, 1072);
    equal(keygen().generateKey(anna_karenina), "", "cryllic is ignored");
});