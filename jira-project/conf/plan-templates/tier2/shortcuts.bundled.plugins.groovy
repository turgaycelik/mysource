noTrigger(['jiraRepoName', 'accTestRunnerRepoName']) {
    repository(name: '#jiraRepoName')
    repository(name: '#accTestRunnerRepoName')
}

notifications() {
    notification(type: 'All Builds Completed', recipient: 'stash')
}

miscellaneousSettings() {
    planMiscellaneous() {
        sandbox(enabled:"true", automaticPromotion:"false")
    }
}

prepareBundledPlugins(['jiraRepoName', 'accTestRunnerRepoName', 'jdk']) {
    job(name: 'Prepare', key: 'PBP', description: '') {
        requireBash()
        requireMaven2()
        requireJdk(jdk: '#jdk')
        requireLocalLinux()

        artifactDefinition(name: 'Bundled plugins versions for onDemand', location: 'runner/', pattern: 'ondemand_versions.txt', shared: 'true')
        artifactDefinition(name: 'Bundled Plugins Versions Info', location: 'runner/', pattern: 'versions.txt', shared: 'true')

        checkoutInDir(repoName: '#jiraRepoName', dir: 'jira-master')
        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        buildAllJira(jdk: '#jdk')
        getBundledPluginsForOnDemand(jdk: '#jdk')
        getBundledPluginsVersion(jdk: '#jdk')
    }
}

buildAllJira(['jdk']) {
    task(
            type: 'maven2',
            description: 'Build All JIRA (must be sandboxed!)',
            goal: '-B clean deploy -Pondemand -P-dependency-tracking -Djira.screenshotapplet.signscreenshotapplet=true -Datlassian.keystore.storepass=${bamboo.sign.password} -DskipTests',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}" MAVEN_OPTS="-XX:MaxPermSize=256m -Xms128m -Xmx1024m"',
            workingSubDirectory: 'jira-master',
            hasTests: 'false'
    )
}

getBundledPluginsForOnDemand(['jdk']) {
    task(
            type: 'script',
            description: 'Get bundled plugins versions for OnDemand',
            script: 'get-bundled-plugins-versions.sh',
            argument: '-b ../jira-master/jira-ondemand-project/jira-ondemand-webapp/ -o ondemand_versions.txt -- -Dmaven.repo.local=/tmp/${bamboo.buildResultKey}',
            environmentVariables: 'M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            workingSubDirectory: 'runner'
    )
}

getBundledPluginsVersion(['jdk']) {
    task(
            type: 'script',
            description: 'Get bundled plugins version',
            script: 'get-bundled-plugins-versions.sh',
            argument: '-b ../jira-master/jira-components/jira-plugins/jira-bundled-plugins/ -o versions.txt -- -Dmaven.repo.local=/tmp/${bamboo.buildResultKey}',
            environmentVariables: 'M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            workingSubDirectory: 'runner'
    )
}

activeObjsPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'ActiveObjects Plugin', key: 'AOP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.activeobjects -a activeobjects-jira-master-tests -j ${bamboo.plugin.builds.jira.stable.version} -v 0.21.2 -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

adminQuickSearchPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'Admin Quicksearch Plugin', key: 'AQSP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-r "^com.atlassian.administration:atlassian-admin-quicksearch-jira:[^:]+:([^:]+):.*$" -g com.atlassian.administration -a atlassian-admin-quicksearch-jira-smoke-tests -j ${bamboo.plugin.builds.jira.stable.version} -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

atlassianBotKiller(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'Atlassian Bot Killer', key: 'ABK', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.labs -a atlassian-bot-killer -j ${bamboo.plugin.builds.jira.stable.version} -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

atlassianGadgets(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'Atlassian Gadgets', key: 'AG', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.gadgets -a atlassian-gadgets-jira-master-tests -r "^com.atlassian.gadgets:atlassian-gadgets-api:[^:]+:([^:]+):.*$" -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3 -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraAuditingPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Auditing Plugin', key: 'JAP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.jira.plugins -a jira-auditing-plugin -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3 -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraBambooPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Bamboo Plugin', key: 'JBP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')
        artifactDefinition(name: 'More logs', location: 'runner/target/artifact/target/container/tomcat6x', pattern: '**/*.log', shared: 'false')
        artifactDefinition(name: 'More logs 1', location: 'runner/target/artifact/target/container', pattern: '**/*.log', shared: 'false')
        artifactDefinition(name: 'More logs 2', location: 'runner/target/artifact/target/bamboo', pattern: '**/*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.jira.plugins -a jira-bamboo-plugin -j ${bamboo.plugin.builds.jira.stable.version} -X -i --force-mvn-version=3 -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.2}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraChartingPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Charting Plugin', key: 'JCP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')
        artifactSubscription(name: 'Bundled plugins versions for onDemand', destination: 'runner')


        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.jira.ext.charting -a jira-charting-plugin -j ${bamboo.plugin.builds.jira.stable.version} -f ondemand_versions.txt -i  -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraDvcsPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA DVCS plugin', key: 'JDVCSP', description: 'Bundled plugin tests for DVCS plugin') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven32()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/artifact', pattern: '**/*.log', shared: 'false')
        artifactDefinition(name: 'Screenshots', location: 'runner/target/artifact/target/', pattern: '**/webdriverTests/**', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/artifact/target', pattern: '**/surefire-reports/**', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsWithErrorCheckScript(
                argument: '-g com.atlassian.jira.plugins -a jira-dvcs-connector-plugin -j ${bamboo.plugin.builds.jira.stable.version} -X --force-mvn-version=3 -- ${bamboo.sandbox.cli.options} -Djirabitbucketconnector.password=dvcsconnector23 -Ddvcsconnectortest.password=dvcsconnector23',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.2}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraForSoftwareTeamsPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA for Software Teams Plugin', key: 'JFSTP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsWithErrorCheckScript(
                argument: '-g com.atlassian.jira.plugins -a jira-software-plugin -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3 -X -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraHealthCheckPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA HealthCheck Plugin', key: 'JHCP', description: 'https://jdog.atlassian.net/browse/JDEV-23665') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Integration Tests', location: 'runner/target/artifact/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.jira.plugins -a jira-healthcheck-plugin -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3 -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraHtml5AttachImagesPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA HTML5 Attach Images', key: 'JHAIP', description: '', enabled: 'false') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-X -g com.atlassian.plugins -a jira-html5-attach-images -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3 -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraICalendarPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA iCalendar Plugin', key: 'JICP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '--force-mvn-version 3 -g com.atlassian.jira.extra -a jira-ical-feed -j ${bamboo.plugin.builds.jira.stable.version}  -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.JDK 1.6}" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraImportersPluginJobs(['accTestRunnerRepoName', 'jdk']) {
    jiraImportersPluginJob(
            name: 'csv1',
            runTestsCommand: '-X -s ignore-script -g com.atlassian.jira.plugins -a jira-importers-plugin -j ${bamboo.plugin.builds.jira.stable.version} -- -PcsvTestSubgroups -DtestGroups=csv-1 ${bamboo.sandbox.cli.options} com.atlassian.maven.plugins:maven-amps-plugin:4.2.10:validate-test-manifest',
            accTestRunnerRepoName: '#accTestRunnerRepoName',
            jdk: '#jdk'
    )
    jiraImportersPluginJob(
            name: 'csv2',
            runTestsCommand: '-X -s ignore-script -g com.atlassian.jira.plugins -a jira-importers-plugin -j ${bamboo.plugin.builds.jira.stable.version} -- -PcsvTestSubgroups -DtestGroups=csv-2 ${bamboo.sandbox.cli.options} com.atlassian.maven.plugins:maven-amps-plugin:4.2.10:validate-test-manifest',
            accTestRunnerRepoName: '#accTestRunnerRepoName',
            jdk: '#jdk'
    )
    jiraImportersPluginJob(
            name: 'csv3',
            runTestsCommand: '-i -X -s ignore-script -g com.atlassian.jira.plugins -a jira-importers-plugin -j ${bamboo.plugin.builds.jira.stable.version} -- -PcsvTestSubgroups -DtestGroups=csv-3 ${bamboo.sandbox.cli.options} com.atlassian.maven.plugins:maven-amps-plugin:4.2.10:validate-test-manifest',
            accTestRunnerRepoName: '#accTestRunnerRepoName',
            jdk: '#jdk'
    )
    jiraImportersPluginJob(
            name: 'csvO',
            runTestsCommand: '-X -s ignore-script -g com.atlassian.jira.plugins -a jira-importers-plugin -j ${bamboo.plugin.builds.jira.stable.version} -i -- -PcsvTestSubgroups -DtestGroups=csv-other ${bamboo.sandbox.cli.options} com.atlassian.maven.plugins:maven-amps-plugin:4.2.10:validate-test-manifest',
            accTestRunnerRepoName: '#accTestRunnerRepoName',
            jdk: '#jdk'
    )
}


jiraImportersPluginJob(['name', 'runTestsCommand', 'accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Importers Plugin-#name', key: 'JIMP#name', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '**', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        setUpImportersPluginCredentials()
        runBundledPluginTestsScript(
                argument: '#runTestsCommand',
                environmentVariables: 'MAVEN_OPTS="-Xmx712m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseImportersPluginPluginJUnitResults()
    }
}

setUpImportersPluginCredentials() {
    task(
            type: 'script',
            description: 'Set up credentials',
            scriptBody: 'echo -e "pivotal.username = elmadir+pivotal@gmail.com\\\\npivotal.password = ABCTestingPivotal\\\\n" > it.properties'
    )
}

parseImportersPluginPluginJUnitResults() {
    task(type: 'jUnitParser', description: 'Parse test results', final: 'true', resultsDirectory: 'runner/target/artifact/target/**/tomcat6x/surefire-reports/*.xml, runner/target/artifact/**/target/group-__no_test_group__/tomcat6x/surefire-reports/*.xml, runner/target/artifact/target/surefire-reports/*.xml')
}

jiraInviteUserPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Invite User Plugin', key: 'JIUP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.jira -a jira-invite-user-plugin -j ${bamboo.plugin.builds.jira.stable.version} -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraIssueCollectorPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Issue Collector Plugin', key: 'JICOLP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '--force-mvn-version 3 --start-x-display --x-display-geometry 1600x1200 -g com.atlassian.jira.collector.plugin -a jira-issue-collector-plugin -j ${bamboo.plugin.builds.jira.stable.version} -i -ta jira-issue-collector-plugin-it  -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraMailPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Mail Plugin', key: 'JMP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '**/*.*', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        prepareItProperties()
        runBundledPluginTestsScript(
                argument: '--force-mvn-version=3 -g com.atlassian.jira -a jira-mail-plugin -j ${bamboo.plugin.builds.jira.stable.version} -X -pa -i -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'IT_GMAIL_FROM="inbox-test@spartez.com" IT_GMAIL_PASSWORD="m9QD02dlNemT0O" IT_GMAIL_USERNAME="inbox-test@spartez.com"  MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"  M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}"'
        )
        parseBundledPluginJUnitResults()
    }
}

prepareItProperties() {
    task(
            type: 'script',
            description: 'Prepare it.properties',
            scriptBody: 'echo -e "gmail.from=inbox-test@spartez.com\\\\ngmail.password=m9QD02dlNemT0O\\\\ngmail.username=inbox-test@spartez.com\\\\n" > it.properties',
            workingSubDirectory: 'runner'
    )
}

jiraProjectConfigPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Project Config Plugin', key: 'JPCP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-X -g com.atlassian.jira -a jira-project-config-plugin -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3 -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraProjectCreationPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Project Creation Plugin', key: 'JPCRP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-jira', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.plugins -a jira-project-creation -j ${bamboo.plugin.builds.jira.stable.version}  -- ${bamboo.sandbox.cli.options} -DtestGroups=jira',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraProjectCreationWithCTKPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Project Creation Plugin with CTK', key: 'JPCRPCTK', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'CTK Logs', location: 'runner/target/plugin/target', pattern: 'ctk-server.log', shared: 'false')
        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-jira-and-ctk', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsWithErrorCheckScript(
                argument: '-g com.atlassian.plugins -a jira-project-creation -j ${bamboo.plugin.builds.jira.stable.version}  -- ${bamboo.sandbox.cli.options} -DtestGroups=jira-and-ctk',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraWelcomePlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Welcome Plugin', key: 'JWP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.jira.welcome -a jira-welcome-plugin -j ${bamboo.plugin.builds.jira.stable.version}  -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

jiraWorkflowSharingPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'JIRA Workflow Sharing Plugin', key: 'JWSP', description: '') {

        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireMaven30()
        requirePython()
        requireLocalLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'All Logs', location: '.', pattern: '**/*.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '--start-x-display --x-display-geometry 1600x1200 -g com.atlassian.jira.plugins.workflow.sharing -a jira-workflow-sharing-plugin -j ${bamboo.plugin.builds.jira.stable.version} -- "-- -Djvmargs=-Xmx2048m  ${bamboo.sandbox.cli.options}"',
                environmentVariables: 'MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

myWorkPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'MyWork Plugins', key: 'MWP', description: 'Tests for MyWork JIRA integration - NOT JDK8 compliant and https://jdog.jira-dev.com/browse/JDEV-29025') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireMaven30()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/test-reports', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')
        artifactDefinition(name: 'WebDriver screenshots', location: 'mywork-acceptance-test-runner/target/webdriverTests', pattern: '**/*.png', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.mywork -a mywork-acceptance-test-runner -r "^com.atlassian.mywork:mywork-api:[^:]+:([^:]+):.*$" -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3 -- -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.0}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()

    }
}

rpcPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'RPC Plugin', key: 'RPCP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-r "^com.atlassian.jira.plugins:atlassian-jira-rpc-plugin:[^:]+:([^:]+):.*$" -g com.atlassian.jira.plugins -a atlassian-jira-rpc-plugin-func-tests -j ${bamboo.plugin.builds.jira.stable.version}  -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

devStatusPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'DevStatus Plugin', key: 'JDSP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven32()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')
        artifactDefinition(name: 'Acceptance test runner logs', location: 'runner/logs', pattern: '**/*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.jira.plugins -a jira-development-integration-plugin -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3  -- -Djira.xml.data.location=target/test-classes ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m -DtestGroups=FuncTest" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.2}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

triggersPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'Triggers Plugin', key: 'AITP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven32()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')
        artifactDefinition(name: 'Acceptance test runner logs', location: 'runner/logs', pattern: '**/*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.jira.plugins -a jira-transition-triggers-plugin -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3  -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m -DtestGroups=FuncTest" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.2}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

remoteEventsPlugin(['accTestRunnerRepoName', 'jdk']) {
    job(name: 'Remote Events Plugin', key: 'AREP', description: '') {
        requireBash()
        requireJdk(jdk: '#jdk')
        requireMaven32()
        requirePython()
        requireLinux()

        artifactDefinition(name: 'Integration Tests', location: 'runner/target/plugin/target/group-__no_test_group__', pattern: '**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'runner/target/plugin/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports', location: 'runner/target/plugin/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: 'runner/target/plugin/target/test-reports', pattern: '*.log', shared: 'false')

        artifactDefinition(name: 'Integration Tests 2', location: 'runner/target/artifact/target', pattern: 'group-*/**/surefire-reports/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs 2', location: 'runner/target/artifact/target/jira/home/log', pattern: 'atlassian-jira.log', shared: 'false')
        artifactDefinition(name: 'Surefire Reports 2', location: 'runner/target/artifact/target/surefire-reports', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Test Reports 2', location: 'runner/target/artifact/target/test-reports', pattern: '*.log', shared: 'false')
        artifactDefinition(name: 'Acceptance test runner logs', location: 'runner/logs', pattern: '**/*.log', shared: 'false')

        artifactSubscription(name: 'Bundled Plugins Versions Info', destination: 'runner')

        checkoutInDir(repoName: '#accTestRunnerRepoName', dir: 'runner')
        runBundledPluginTestsScript(
                argument: '-g com.atlassian.plugins -a atlassian-remote-event-consumer-plugin -j ${bamboo.plugin.builds.jira.stable.version} --force-mvn-version=3  -- ${bamboo.sandbox.cli.options}',
                environmentVariables: 'MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m -DtestGroups=jira" M3_HOME="${bamboo.capability.system.builder.mvn3.Maven 3.2}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
        )
        parseBundledPluginJUnitResults()
    }
}

runBundledPluginTestsScript(['argument', 'environmentVariables']) {
    task(
            type: 'script',
            description: 'Run tests',
            scriptBody: '''
python prepare-and-run-jira-plugin-tests.py "$@"

# **Do not edit this script, use Argument field instead**
# to configure arguments
#
# Arguments (basic configuration):
# -g gruop-id
# -a artifact-id
# -j jira-version
## ${bamboo.plugin.builds.jira.stable.version} will
## refer to actual stable version
#
# For full list of arguments please see help output for
# the prepare-and-run-tests.py script.''',
            argument: '#argument',
            environmentVariables: '#environmentVariables',
            workingSubDirectory: 'runner'
    )
}

runBundledPluginTestsWithErrorCheckScript(['argument', 'environmentVariables']) {
    task(
            type: 'script',
            description: 'Run tests',
            scriptBody: '''
python prepare-and-run-jira-plugin-tests.py "$@"
if [ $? != 0 ];then
    echo "Running tests failed."
    exit 0
fi

# **Do not edit this script, use Argument field instead**
# to configure arguments
#
# Arguments (basic configuration):
# -g gruop-id
# -a artifact-id
# -j jira-version
## ${bamboo.plugin.builds.jira.stable.version} will
## refer to actual stable version
#
# For full list of arguments please see help output for
# the prepare-and-run-tests.py script.''',
            argument: '#argument',
            environmentVariables: '#environmentVariables',
            workingSubDirectory: 'runner'
    )
}

parseBundledPluginJUnitResults() {
    task(type: 'jUnitParser', description: 'Parse test results', final: 'true', resultsDirectory: 'runner/target/artifact/**/target/surefire-reports/*.xml, runner/target/artifact/**/target/*/tomcat6x/surefire-reports/*.xml')
}

checkoutInDir(['repoName', 'dir']) {
    task(type: 'checkout', description: 'Source Code Checkout'){
        repository(name: '#repoName', checkoutDirectory: '#dir')
    }
}
