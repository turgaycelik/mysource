define('jira/data/session-storage', function() {
    /**
     * so we can know its out window.name if need be
     */
    var MAGIC_MARK = "jsessionstorage:";

    /**
     * Non native support via window.name
     */
    var nonNativeSessionStorageObjInitialised = false;
    var nonNativeSessionStorageObj = {};
    var nonNativeSessionStorageImpl = {

        nonnativeimplementation : true,

        _storage : function()
        {
            if (nonNativeSessionStorageObjInitialised)
            {
                return nonNativeSessionStorageObj;
            }
            if (typeof window.name != 'string')
            {
                window.name = MAGIC_MARK + '{}';
            }
            if (window.name.indexOf(MAGIC_MARK) != 0)
            {
                window.name = MAGIC_MARK + '{}';
            }
            var jsonData = window.name.substr(MAGIC_MARK.length);
            nonNativeSessionStorageObj = JSON.parse(jsonData);
            if (! nonNativeSessionStorageObj)
            {
                nonNativeSessionStorageObj = {};
            }
            nonNativeSessionStorageObjInitialised = true;

            return nonNativeSessionStorageObj;
        },

        _persistStorage : function()
        {
            var storeObj = this._storage();
            var jsonData = JSON.stringify(storeObj);

            window.name = MAGIC_MARK + jsonData;
        },
        
        length : function()
        {
            var i = 0;
            var store = this._storage();
            for (var x in store)
            {
                i++;
            }
            return i;
        },
        
        key : function(index)
        {
            var i = 0;
            var store = this._storage();
            for (var x in store)
            {
                if (i == index)
                {
                    return x;
                }
                i++;
            }
            return null;
        },

        getItem : function(key)
        {
            return this._storage()[key];
        },

        setItem : function(key, value)
        {
            this._storage()[key] = value;
            this._persistStorage();
        },

        removeItem : function(key)
        {
            delete this._storage()[key];
            this._persistStorage();
        },

        clear : function()
        {
            var store = this._storage();
            for (var x in store)
            {
                delete x;
            }
            this._persistStorage();
        }
    };

    /*
     * This is our implementation of web session storage, native if we have it otherwise synthesized
     * via window name as above
     */
    var _sessionStorageImpl = window.sessionStorage != null ? window.sessionStorage : nonNativeSessionStorageImpl;

    return {
        /**
         * This boolean is set to true if the browser supports session storage natively
         */
        nativesupport: (window.sessionStorage != null),

        /**
         * This is our one departure from the proper Web Storage method shape.  Length is hard to represent as a
         * direct property since JS arrays arent truly associative like JS objects and JS objects dont have length
         * properties.
         */
        length: function() {
            if (typeof _sessionStorageImpl.length === 'function') {
                return _sessionStorageImpl.length();
            }
            return _sessionStorageImpl.length;
        },

        key: function(index) { return _sessionStorageImpl.key(index); },
        getItem: function(key) { return _sessionStorageImpl.getItem(key); },
        setItem: function(key, value) { return _sessionStorageImpl.setItem(key, value); },
        removeItem: function(key) { return _sessionStorageImpl.removeItem(key); },
        clear: function() { return _sessionStorageImpl.clear(); },

        /**
         * Returns a JSON string representation of the web storage in play
         */
        asJSON: function() {
            var len = this.length();
            var jsData = '{\n';
            for (var i = 0; i < len; i++)
            {
                var key = this.key(i);
                var value = this.getItem(key);
                jsData += key + ':' + value;
                if (i < len-1)
                {
                    jsData += ',';
                }
                jsData += '\n';
            }
            jsData += '}\n';
            return jsData;
        }
    };
});

/** Preserve legacy namespace
    @deprecated jira.app.session.storage */
AJS.namespace("jira.app.session.storage", null, require('jira/data/session-storage'));
AJS.namespace('JIRA.SessionStorage', null, require('jira/data/session-storage'));
