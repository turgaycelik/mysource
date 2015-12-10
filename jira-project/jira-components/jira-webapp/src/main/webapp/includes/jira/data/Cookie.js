define('jira/data/cookie', function() {
    var Cookie = {};

    Cookie.saveToConglomerate = function saveToConglomerateCookie(cookieName, name, value) {
        var cookieValue = Cookie.getValue(cookieName);
        cookieValue = Cookie.addOrAppendToValue(name, value, cookieValue);

        Cookie.save(cookieName, cookieValue, 365);
    };

    Cookie.readFromConglomerate = function readFromConglomerateCookie(cookieName, name, defaultValue) {
        var cookieValue = Cookie.getValue(cookieName);
        var value = Cookie.getValueFromCongolmerate(name, cookieValue);
        if (value != null) {
            return value;
        }

        return defaultValue;
    };

    Cookie.eraseFromConglomerate = function eraseFromConglomerateCookie(cookieName, name) {
        Cookie.saveToConglomerate(cookieName, name, "");
    };

    Cookie.getValueFromCongolmerate = function getValueFromCongolmerate(name, cookieValue) {
        // a null cookieValue is just the first time through so create it
        if (cookieValue == null) {
            cookieValue = "";
        }
        var eq = name + "=";
        var cookieParts = cookieValue.split('|');
        for (var i = 0; i < cookieParts.length; i++) {
            var cp = cookieParts[i];
            while (cp.charAt(0) == ' ') {
                cp = cp.substring(1, cp.length);
            }
            // rebuild the value string excluding the named portion passed in
            if (cp.indexOf(name) == 0) {
                return cp.substring(eq.length, cp.length);
            }
        }
        return null;
    };

    /**
     * Either append or replace the value in the cookie string
     */
    Cookie.addOrAppendToValue = function addOrAppendToValue(name, value, cookieValue) {
        var newCookieValue = "";
        // a null cookieValue is just the first time through so create it
        if (cookieValue == null) {
            cookieValue = "";
        }

        var cookieParts = cookieValue.split('|');
        for (var i = 0; i < cookieParts.length; i++) {
            var cp = cookieParts[i];

            // ignore any empty tokens
            if (cp != "") {
                while (cp.charAt(0) == ' ') {
                    cp = cp.substring(1, cp.length);
                }
                // rebuild the value string excluding the named portion passed in
                if (cp.indexOf(name) != 0) {
                    newCookieValue += cp + "|";
                }
            }
        }

        // always append the value passed in if it is not null or empty
        if (value != null && value != '') {
            var pair = name + "=" + value;
            if ((newCookieValue.length + pair.length) < 4020) {
                newCookieValue += pair;
            }
        }
        return newCookieValue;
    };

    Cookie.getValue = function getCookieValue(name) {
        var eq = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') {
                c = c.substring(1, c.length);
            }
            if (c.indexOf(eq) == 0) {
                return unescape(c.substring(eq.length, c.length));
            }
        }

        return null;
    };

    Cookie.save = function saveCookie(name, value, days) {

        if (typeof contextPath === "undefined") {
            return;
        }
        var path = contextPath;
        if (!path) {
            path = "/";
        }

        var ex;
        if (days) {
            var d = new Date();
            d.setTime(d.getTime() + (days * 24 * 60 * 60 * 1000));
            ex = "; expires=" + d.toGMTString();
        }
        else {
            ex = "";
        }
        document.cookie = name + "=" + escape(value) + ex + ";path=" + path;
    };

    /**
     * Reads a cookie. If none exists, then it returns and
     */
    Cookie.read = function readCookie(name, defaultValue) {
        var cookieVal = Cookie.getValue(name);
        if (cookieVal != null) {
            return cookieVal;
        }

        // No cookie found, then save a new one as on!
        if (defaultValue) {
            Cookie.save(name, defaultValue, 365);
            return defaultValue;
        }
        else {
            return null;
        }
    };

    Cookie.erase = function eraseCookie(name) {
        Cookie.save(name, "", -1);
    };

    return Cookie;
});
