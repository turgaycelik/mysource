AJS.test.require("jira.webresources:avatar-picker");

function createAvatarManager()
{

    var store = new JIRA.AvatarStore({
        restQueryUrl: "blah",
        restCreateTempUrl: "blah",
        restUpdateTempUrl: "blah",
        defaultAvatarId: 1000
    });

    return new JIRA.AvatarManager({
        store: store,
        defaultAvatarId: 1000,
        avatarSrcBaseUrl: "/secure/projectavatar"
    });
}


function expectSuccessfulAjax(withData)
{

    var oldAjax = jQuery.ajax;

    jQuery.ajax = function (options)
    {
        if (options.success)
        {
            options.success(withData, "success", {
                status: 200
            });
        }

        if (options.complete)
        {
            options.complete("success", {
                status: 200
            });
        }

        jQuery.ajax = oldAjax;
    }
}

function expectErrorAjax(type)
{

    var oldAjax = jQuery.ajax;

    jQuery.ajax = function (options)
    {

        if (options.error)
        {
            options.error({
                status: type || 500
            }, "error");
        }

        if (options.complete)
        {
            options.complete("error", {
                status: type || 500
            });
        }

        jQuery.ajax = oldAjax;
    }
}

test("getById", function ()
{

    var avatarManager = createAvatarManager();

    equal(avatarManager.getById(1000), undefined, "Expected undefined if no match");

    expectSuccessfulAjax({
        id: 1000
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1000
    }));

    var avatar = avatarManager.getById(1000);

    ok(avatar instanceof JIRA.Avatar, "Expected instance of JIRA.Avatar");
    equal(avatar.getId(), 1000, "Expected avatar to have id [1000]");

});

test("destroy", function ()
{

    var avatarManager = createAvatarManager();

    expectSuccessfulAjax({
        id: 1000
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1000
    }));

    var avatar = avatarManager.getById(1000);

    expectSuccessfulAjax({
        id: 1000
    });

    avatarManager.destroy(avatar);

    equal(avatarManager.getById(1000), undefined, 'Expected avatar to be destroyed');

    expectSuccessfulAjax({
        id: 1000
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1000
    }));

    avatar = avatarManager.getById(1000);


    expectErrorAjax();

    avatarManager.destroy(avatar);

    ok(avatarManager.getById(1000) instanceof JIRA.Avatar,
            'Expected avatar to not be detroyed in case of server error');

});


test("getAllSystemAvatars", function ()
{

    var avatarManager = createAvatarManager();

    expectSuccessfulAjax({
        id: 1001
    });

    avatarManager.add(JIRA.Avatar.createSystemAvatar({
        id: 1001
    }));

    expectSuccessfulAjax({
        id: 1002
    });

    avatarManager.add(JIRA.Avatar.createSystemAvatar({
        id: 1002
    }));

    expectSuccessfulAjax({
        id: 1003
    });

    avatarManager.add(JIRA.Avatar.createSystemAvatar({
        id: 1003
    }));

    expectSuccessfulAjax({
        id: 1004
    });

    avatarManager.add(JIRA.Avatar.createSystemAvatar({
        id: 1004
    }));

    expectSuccessfulAjax({
        id: 1005
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1005
    }));


    var systemAvatars = avatarManager.getAllSystemAvatars();

    equal(systemAvatars[0].getId(), 1001);
    equal(systemAvatars[1].getId(), 1002);
    equal(systemAvatars[2].getId(), 1003);
    equal(systemAvatars[3].getId(), 1004);
    equal(systemAvatars[4], undefined);

});

test("getAllCustomAvatars", function ()
{

    var avatarManager = createAvatarManager();

    expectSuccessfulAjax({
        id: 1001
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1001
    }));

    expectSuccessfulAjax({
        id: 1002
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1002
    }));

    expectSuccessfulAjax({
        id: 1003
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1003
    }));

    expectSuccessfulAjax({
        id: 1004
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1004
    }));

    expectSuccessfulAjax({
        id: 1005
    });

    avatarManager.add(JIRA.Avatar.createSystemAvatar({
        id: 1005
    }));


    var systemAvatars = avatarManager.getAllCustomAvatars();

    equal(systemAvatars[0].getId(), 1001);
    equal(systemAvatars[1].getId(), 1002);
    equal(systemAvatars[2].getId(), 1003);
    equal(systemAvatars[3].getId(), 1004);
    equal(systemAvatars[4], undefined);

});

test("getAllAvatars", function ()
{

    var avatarManager = createAvatarManager();

    expectSuccessfulAjax({
        id: 1001
    });

    avatarManager.add(JIRA.Avatar.createCustomAvatar({
        id: 1001
    }));

    expectSuccessfulAjax({
        id: 1002
    });

    avatarManager.add(JIRA.Avatar.createSystemAvatar({
        id: 1002
    }));

    var avatars = avatarManager.getAllAvatars()

    equal(avatars.custom.length, 1);
    equal(avatars.system.length, 1);
    equal(avatars.custom[0].getId(), 1001);
    equal(avatars.system[0].getId(), 1002);

});

test("refreshStore", function ()
{

    var avatarManager = createAvatarManager();

    function refreshAjaxRespone()
    {
        expectSuccessfulAjax({
            system: [
                {id: 1000, isSystemAvatar: true,},
                {id: 1001, isSystemAvatar: true},
                {id: 1002, isSystemAvatar: true}
            ],
            custom: [
                {id: 1003},
                {id: 1004}
            ]
        });

        avatarManager.refreshStore();
    }

    refreshAjaxRespone();

    var systemAvatars = avatarManager.getAllSystemAvatars(),
            customAvatars = avatarManager.getAllCustomAvatars();


    equal(systemAvatars.length, 3);
    equal(systemAvatars[0].getId(), 1000);
    equal(systemAvatars[1].getId(), 1001);
    equal(systemAvatars[2].getId(), 1002);

    equal(customAvatars.length, 2);
    equal(customAvatars[0].getId(), 1003);
    equal(customAvatars[1].getId(), 1004);

    refreshAjaxRespone();

    systemAvatars = avatarManager.getAllSystemAvatars(),
            customAvatars = avatarManager.getAllCustomAvatars();

    equal(systemAvatars.length, 3);
    equal(customAvatars.length, 2);
});

test("getAvatarSrc", function ()
{
    var urls = {
        "16x16": "http://localhost:8090/jira/secure/useravatar?size=xsmall&avatarId=10041",
        "24x24": "http://localhost:8090/jira/secure/useravatar?size=small&avatarId=10041",
        "32x32": "http://localhost:8090/jira/secure/useravatar?size=medium&avatarId=10041",
        "48x48": "http://localhost:8090/jira/secure/useravatar?avatarId=10041"
    };

    var avatarManager = createAvatarManager(),
            avatar = JIRA.Avatar.createSystemAvatar({
                id: 1002,
                urls: urls
            }),
            anotherAvatar = JIRA.Avatar.createSystemAvatar({
                id: 999,
                urls: urls
            });

    equal(avatarManager.getAvatarSrc(avatar, JIRA.Avatar.SMALL), urls["16x16"]);
    equal(avatarManager.getAvatarSrc(anotherAvatar, JIRA.Avatar.MEDIUM), urls["32x32"]);
    equal(avatarManager.getAvatarSrc(anotherAvatar, JIRA.Avatar.LARGE), urls["48x48"]);
});
