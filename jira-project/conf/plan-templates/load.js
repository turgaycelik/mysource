(function() {
    "use strict";

    var fs = require('fs'),
        request = require('request'),
        async = require('async'),
        read = require ('read');

    function loadPlan(planPath, shortcutsFilesPaths, username, password) {
        var plan = fs.readFileSync(planPath, {encoding:"utf8"});

        var shortcuts = '';
        for (var index in shortcutsFilesPaths) {
            shortcuts += '\n' + fs.readFileSync(shortcutsFilesPaths[index], {encoding:"utf8"});
        }

        var json = {dsl: plan, shortcuts: shortcuts};

        console.log("Loading plan '"+planPath+"'");
        request({
            url: "https://jira-bamboo.internal.atlassian.com/rest/plantemplate/1.0/json",
            qs: {
                "os_authType": "basic"
            },
            auth: {
                user: username,
                pass: password
            },
            method: "POST",
            json: json
        });
    }

    function printUsage() {
        console.log("Usage: $ node load.js \<path/to/plan.groovy\> \<path/to/shortcuts1.groovy\> \<path/to/shortcuts2.groovy\> ... \<path/to/shortcutsN.groovy\>");
    }

    function argsLengthCorrect() {
        return process.argv.length >= 4;
    }

    if (!argsLengthCorrect()) {
        return printUsage();
    }

    var planPath = process.argv[2];
    var shortcutsFilesPaths = process.argv.slice(3);

    async.series({
        username: async.apply(read, { prompt: 'JBAC username:', silent: false}),
        password: async.apply(read, { prompt: 'JBAC password:', silent: true, replace: "*"})
    }, function(err, results){
        if (err) return console.error(err);

        var username = results.username[0];
        var password = results.password[0];

        async.series([
            async.apply(loadPlan, planPath, shortcutsFilesPaths, username, password)
        ], function(err, results) {
            if (err) return console.error(err);

            results.forEach(function(result) {
                result = result[0];
                if (result.statusCode !== 200) {
                    console.log(result.body);
                }
            });
            console.log("done!");
        });
    })
})();