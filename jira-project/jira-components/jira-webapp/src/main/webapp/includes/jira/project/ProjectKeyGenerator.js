define('jira/util/strings/character-map', function() {
    // The (non-ascii) characters used as keys will be replaced with their (ascii) value.
    var CHARACTER_MAP = {};
    CHARACTER_MAP[199] = "C"; // Ç
    CHARACTER_MAP[231] = "c"; // ç
    CHARACTER_MAP[252] = "u"; // ü
    CHARACTER_MAP[251] = "u"; // û
    CHARACTER_MAP[250] = "u"; // ú
    CHARACTER_MAP[249] = "u"; // ù
    CHARACTER_MAP[233] = "e"; // é
    CHARACTER_MAP[234] = "e"; // ê
    CHARACTER_MAP[235] = "e"; // ë
    CHARACTER_MAP[232] = "e"; // è
    CHARACTER_MAP[226] = "a"; // â
    CHARACTER_MAP[228] = "a"; // ä
    CHARACTER_MAP[224] = "a"; // à
    CHARACTER_MAP[229] = "a"; // å
    CHARACTER_MAP[225] = "a"; // á
    CHARACTER_MAP[239] = "i"; // ï
    CHARACTER_MAP[238] = "i"; // î
    CHARACTER_MAP[236] = "i"; // ì
    CHARACTER_MAP[237] = "i"; // í
    CHARACTER_MAP[196] = "A"; // Ä
    CHARACTER_MAP[197] = "A"; // Å
    CHARACTER_MAP[201] = "E"; // É
    CHARACTER_MAP[230] = "ae"; // æ
    CHARACTER_MAP[198] = "Ae"; // Æ
    CHARACTER_MAP[244] = "o"; // ô
    CHARACTER_MAP[246] = "o"; // ö
    CHARACTER_MAP[242] = "o"; // ò
    CHARACTER_MAP[243] = "o"; // ó
    CHARACTER_MAP[220] = "U"; // Ü
    CHARACTER_MAP[255] = "Y"; // ÿ
    CHARACTER_MAP[214] = "O"; // Ö
    CHARACTER_MAP[241] = "n"; // ñ
    CHARACTER_MAP[209] = "N"; // Ñ

    return CHARACTER_MAP;
});

define('jira/project/project-key-generator', [
    'jira/util/strings/character-map',
    'jira/lib/class',
    'jquery',
    'underscore'
], function(
    CHARACTER_MAP,
    Class,
    jQuery,
    _
) {
    // These words will not be used in key generation for acronyms.
    var IGNORED_WORDS = ["THE", "A", "AN", "AS", "AND", "OF", "OR"];

    function getTotalLength(words) {
        return words.join("").length;
    }

    function removeIgnoredWords(words) {
        return _.reject(words, function(word) {
            return jQuery.inArray(word, IGNORED_WORDS) !== -1;
        });
    }

    function createAcronym(words) {
        var result = "";
        jQuery.each(words, function(i, word) {
            result += word.charAt(0);
        });
        return result;
    }

    function getFirstSyllable(word) {
        // Best guess at getting the first syllable
        // Returns the substring up to and including the first consonant to appear after a vowel
        var pastVowel = false;
        var i;
        for (i = 0; i < word.length; i++) {
            if (isVowelOrY(word[i])) {
                pastVowel = true;
            } else {
                if (pastVowel) {
                    return word.substring(0, i + 1);
                }
            }
        }
        return word;
    }

    function isVowelOrY(c) {
        return c && c.length === 1 && c.search("[AEIOUY]") !== -1;
    }

    /**
     * @class ProjectKeyGenerator
     * @extends Class
     */
    return Class.extend({

        init: function (options) {
            options = jQuery.extend({}, options);
            this.desiredKeyLength = (typeof options.desiredKeyLength == 'number') ? options.desiredKeyLength : 4;
            this.maxKeyLength = (typeof options.maxKeyLength == 'number') ? options.maxKeyLength : 0;
        },

        generateKey: function(name) {
            name = jQuery.trim(name);
            if (!name) {
                return "";
            }

            // Brute-force chunk-by-chunk substitution and filtering.
            var filtered = [];
            for(var i=0, ii=name.length; i<ii; i++) {
                var sub = CHARACTER_MAP[name.charCodeAt(i)];
                filtered.push(sub ? sub : name[i]);
            }
            name = filtered.join('');

            // Split into words
            var words = [];
            jQuery.each(name.split(/\s+/), function(i, word) {
                if (word) {
                    // Remove whitespace and punctuation characters (i.e. anything not A-Z)
                    word = word.replace(/[^a-zA-Z]/g, "");
                    // uppercase the word (NOTE: JavaScript attempts to convert characters like ß in to SS)
                    word = word.toUpperCase();
                    // add the word, should it be worthy.
                    word.length && words.push(word);
                }
            });

            // Remove ignored words
            if (this.desiredKeyLength && getTotalLength(words) > this.desiredKeyLength) {
                words = removeIgnoredWords(words);
            }

            var key;

            if (words.length == 0) {
                // No words were worthy!
                key = "";
            } else if (words.length == 1) {
                // If we have one word, and it is longer than a desired key, get the first syllable
                var word = words[0];
                if (this.desiredKeyLength && word.length > this.desiredKeyLength) {
                    key = getFirstSyllable(word);
                } else {
                    // The word is short enough to use as a key
                    key = word;
                }
            } else {
                // If we have more than one word, just take the first letter from each
                key = createAcronym(words);
            }

            // Limit the length of the key
            if (this.maxKeyLength && key.length > this.maxKeyLength) {
                key = key.substr(0, this.maxKeyLength);
            }

            return key;
        }
    });
});

AJS.namespace('JIRA.ProjectKeyGenerator', null, require('jira/project/project-key-generator'));
