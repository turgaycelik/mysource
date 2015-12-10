/**
 * Manager interface for JIRA.Avatar objects.
 *
 * You should use this for creating, manipulating and deleteing of avatars. Helper methods such as getting avatar
 * urls are also contained within this class.
 *
 * Please use the factory methods for construction
 *
 * @Class {JIRA.AvatarManager}
 */
JIRA.AvatarManager = Class.extend({

    /**
     * @param options
     * ... {JIRA.AvatarStore or something that implements the same interface} store
     * ... {Number, String} defaultAvatarId - This is the avatar that is currently in use if no other have been selected
     * ... {Number, String} anonymousAvatarId - In the case of user avatar, this is the one used for logged out/or annonymous users
     * ... {String} avatarSrcBaseUrl - The base url used to load the avatar image
     */
    init: function (options) {
        this.store = options.store;
        this.ownerId = options.ownerId;
        this.username = options.username;
        this.anonymousAvatarId = options.anonymousAvatarId;
        this.avatarSrcBaseUrl = options.avatarSrcBaseUrl;
    },

    /**
     * Selects avatar, this will become the displayed avatar for the given type (ie project)
     *
     * @param avatar
     * @param options
     */
    selectAvatar: function (avatar, options) {
        return this.store.selectAvatar(avatar, options);
    },

    /**
     * Retrieve the avatar with the given id.
     *
     * @param avatarId must not be null.
     * @param {Object} options
     * ... {Function} success
     * ... {Function} error
     */
    getById: function (id) {
        return this.store.getById(id);
    },

    /**
     * Delete the avatar
     *
     * @param {String} avatar must not be null.
     */
    destroy: function (avatar, options) {
        this.store.destroy(avatar, options);
    },

    /**
     * Saves the avatar as an updated version of the avatar with the same id that is already in the store.
     *
     * @param {JIRA.Avatar} avatar must not be null.
     * @param {Object} options
     * ... {Function} success
     * ... {Function} error
     */
    update: function (avatar, options) {
        this.store.update(avatar, options);
    },

    /**
     * Creates a database record for the given avatar. Use the return value as the persistent avatar, not the one you
     * passed in.
     *
     * @param {JIRA.Avatar} avatar must not be null, must have a null id.
     * @param {Object} options
     * ... {Function} success
     * ... {Function} error
     */
    add: function (avatar, options) {
        this.store._add(avatar, options);
    },

    /**
     * Provides a list of all system avatars.
     *
     * Note: You will need to call refreshStore first
     *
     * @return {Array<JIRA.Avatar>} the system avatars.
     */
    getAllSystemAvatars: function () {
        return this.store.getAllSystemAvatars();
    },

    /**
     * Provides an array of all system avatars.
     *
     * Note: You will need to call refreshStore first
     *
     * @return {Array<JIRA.Avatar>} the custom avatars.
     */
    getAllCustomAvatars: function () {
        return this.store.getAllCustomAvatars();
    },

    /**
     * Gets selected avatar
     *
     * @return JIRA.Avatar
     */
    getSelectedAvatar: function () {
        return this.store.getSelectedAvatar();
    },

    /**
     *
     * Gets all avatars
     *
     * Note: You will need to call refreshStore first
     *
     * @return {Object}
     * ... {Array<JIRA.Avatar>} system
     * ... {Array<JIRA.Avatar>} custom
     */
    getAllAvatars: function () {
        return this.store.getAllAvatars();
    },

    /**
     * Gets a JSON blob, that contains the img src of each avatar based on the supplied size
     *
     * @param {JIRA.Avatar.LARGE, JIRA.Avatar.MEDIUM, JIRA.Avatar.SMALL} size
     * @return {Object}
     * ... {Array[{id, src, isSystemAvatar}]} system
     * ... {Array[{id, src, isSystemAvatar}] custom
     */
    getAllAvatarsRenderData: function (size) {

        var i,
                instance = this,
                avatars = this.getAllAvatars(),
                renderData = {
                    system: [],
                    custom: []
                }

        for (i = 0; i < avatars.system.length; i++) {
            renderData.system.push(instance.getAvatarRenderData(avatars.system[i], size));
        }

        for (i = 0; i < avatars.custom.length; i++) {
            renderData.custom.push(instance.getAvatarRenderData(avatars.custom[i], size));
        }

        return renderData;
    },

    /**
     * Gets json descriptor of given avatar that contains the img src based on the supplied size
     * @param avatar
     * @param size
     */
    getAvatarRenderData: function (avatar, size) {
        var data = avatar.toJSON();

        data.src = this.getAvatarSrc(avatar, size);
        data.width = size.width;
        data.height = size.height;


        return data;
    },

    /**
     * Refreshes avatar store
     *
     * @param options
     * ... {function} success
     * ... {function} error
     */
    refreshStore: function (options) {
        this.store.refresh(options);
    },

    /**
     *
     * @param {JIRA.Avatar} avatar
     * @param {JIRA.Avatar.LARGE, JIRA.Avatar.MEDIUM, JIRA.Avatar.SMALL} size
     * @return String
     */
    getAvatarSrc: function(avatar, size) {

        if (this.store.isTempAvatar(avatar)) {
            // if the user chooses a new temporary avatar we need to keep making this url unique so that the image is kept fresh
            return contextPath + "/secure/temporaryavatar?" + jQuery.param({
                cropped: true,
                magic: new Date().getTime(),
                size: size.param
            });
        }

        return avatar.getUrl(AJS.format('{0}x{1}', size.height, size.width));
    },

    /**
     * Creates temporary avatar from the value in the supplied file input field
     *
     * @param {HTMLElement} field
     * @param {Object} options
     * ... {function} success
     * ... {function} error
     */
    createTemporaryAvatar: function (field, options) {
        this.store.createTemporaryAvatar(field, options);
    },

    /**
     * Creates an avatar with the properties of the given avatar.
     *
     * @param {Object} instructions
     * ... {Number} cropperOffsetX
     * ... {Number} cropperOffsetY
     * ... {Number} cropperWidth
     *
     * @param {Object} options
     * ... {Function(JIRA.Avatar)} success - ajax callback
     * ... {Function(XHR, testStatus, JIRA.SmartAjax.smartAjaxResult)} error - ajax callback
     */
    createAvatarFromTemporary: function (instructions, options) {
        this.store.createAvatarFromTemporary(instructions, options);
    },

    /**
     * Gets the avatar id to use to represent an unknown or anonymous user
     * @return {Number} the avatar id for an anonymous user
     */
    getAnonymousAvatarId: function () {
        return this.anonymousAvatarId;
    }

});


// Factories


/**
 *
 * Creates a project avatar manager
 *
 * @param options
 * ... {String} projectKey
 * ... {String} projectId
 * ... {String} defaultAvatarId
 */
JIRA.AvatarManager.createUniversalAvatarManager = function (options) {

    // Cater for the projectKey being empty
    var restQueryUrl,
            restUpdateUrl = "",
            restCreateTempUrl = "",
            restUpdateTempUrl = "",
            restSingleAvatarUrl = "";

    if (options.projectId) {
        var urlAvatarOwnerPrefix = contextPath + "/rest/api/latest/universal_avatar/type/"+options.avatarType+"/owner/" + options.projectId;

        restQueryUrl = urlAvatarOwnerPrefix;

        var avatarCreateUrl = urlAvatarOwnerPrefix + "/avatar";

        restUpdateUrl = null;
        restCreateTempUrl = urlAvatarOwnerPrefix + "/temp";
        restUpdateTempUrl = avatarCreateUrl;
        restSingleAvatarUrl = avatarCreateUrl;
    } else {
        restQueryUrl = contextPath + "/rest/api/latest/avatar/project/system";
        restCreateTempUrl = contextPath + "/rest/api/latest/avatar/project/temporary";
        restUpdateTempUrl = contextPath + "/rest/api/latest/avatar/project/temporaryCrop";
    }

    var store = new JIRA.AvatarStore({
        restQueryUrl: restQueryUrl,
        restUpdateUrl: restUpdateUrl,
        restCreateTempUrl: restCreateTempUrl,
        restUpdateTempUrl: restUpdateTempUrl,
        restSingleAvatarUrl: restSingleAvatarUrl,
        defaultAvatarId: options.defaultAvatarId
    });

    return new JIRA.AvatarManager({
        store: store,
        ownerId: options.projectId,
        avatarSrcBaseUrl: contextPath + "/secure/projectavatar"
    });
};

/**
 *
 * Creates a project avatar manager
 *
 * @param options
 * ... {String} projectKey
 * ... {String} projectId
 * ... {String} defaultAvatarId
 */
JIRA.AvatarManager.createProjectAvatarManager = function (options) {
    options.avatarType = "project";

    return JIRA.AvatarManager.createUniversalAvatarManager(options)
};

/**
 * Creates a user avatar manager
 *
 * @param options
 * ... {String} username
 * ... {String} defaultAvatarId
 */
JIRA.AvatarManager.createUserAvatarManager = function (options) {

    var userRestUrl = contextPath + "/rest/api/latest/user";
    var store = new JIRA.AvatarStore({
        restQueryUrl: userRestUrl + "/avatars",
        restUpdateUrl: userRestUrl + "/avatar",
        restCreateTempUrl: userRestUrl + "/avatar/temporary",
        restUpdateTempUrl: userRestUrl + "/avatar",
        restSingleAvatarUrl: userRestUrl + "/avatar",
        restParams: { 'username': options.username },
        defaultAvatarId: options.defaultAvatarId
    });

    return new JIRA.AvatarManager({
        store: store,
        username: options.username,
        avatarSrcBaseUrl: contextPath + "/secure/useravatar"
    });
}
