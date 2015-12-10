// --------------------------------------------------------------------------------------------------
// This file contains shortcuts common to all plan templates
// --------------------------------------------------------------------------------------------------
branches(['pattern', 'removeAfterDays']) {
    branchMonitoring(
            enabled: 'true',
            matchingPattern: '#pattern',
            timeOfInactivityInDays: '#removeAfterDays',
            notificationStrategy: 'NOTIFY_COMMITTERS',
            remoteJiraBranchLinkingEnabled: 'true'
    )
}

repositoryPolling(['repoName']) {
    repository(name: '#repoName')
    trigger(description: 'Poll the repository every 3 minutes', type: 'polling', strategy: 'periodically', frequency: '180') {
        repository(name: '#repoName')
    }
}

repositoryPollingWithFrequency(['repoName', 'frequency']) {
    repository(name: '#repoName')
    trigger(description: 'Poll the repository for changes', type: 'polling', strategy: 'periodically', frequency: '#frequency') {
        repository(name: '#repoName')
    }
}

onceADay(['time']) {
    trigger(description:'Single daily build', type:'daily', time:'#time', onlyBranchesWithChanges: 'true')
}

requireLocalLinux() {
    requirement(key: 'elastic', condition: 'equals', value: 'false')
    requireLinux()
}

requireLinux() {
    requirement(key: 'os', condition: 'equals', value: 'Linux')
}

requireElasticWindows() {
    requirement(key: 'elastic', condition: 'equals', value: 'true')
    requireWindows()
}

requireWindows() {
    requirement(key: 'os', condition: 'equals', value: 'Windows')
}

requireWindowsSnowflake() {
    requirement(key: 'os', condition: 'equals', value: 'Windows-snowflake')
}

requireMaven2() {
    requirement(key: 'system.builder.mvn2.Maven 2.1', condition: 'exists')
}

requireMaven30() {
    requirement(key: 'system.builder.mvn3.Maven 3.0', condition: 'exists')
}

requireMaven32() {
    requirement(key: 'system.builder.mvn3.Maven 3.2', condition: 'exists')
}

requireJdk(['jdk']) {
    requirement(key: 'system.jdk.#jdk', condition: 'exists')
}

requireFirefox(['ver']) {
    requirement(key: 'firefox #ver', condition: 'exists')
}

cleanCheckout(['repoName']) {
    task(type: 'checkout', description: 'Clean Source Code Checkout', cleanCheckout: 'true') {
        repository(name: '#repoName')
    }
}

checkoutInDir(['repoName', 'dir']) {
    task(type: 'checkout', description: 'Source Code Checkout'){
        repository(name: '#repoName', checkoutDirectory: '#dir')
    }
}

brokerUrlDiscovery() {
    task(type: 'brokerUrlDiscovery', description: 'Hallelujah Broker Url Discovery')
}

checkout(['repoName', 'forceClean']) {
    task(type: 'checkout', description: 'Source Code Checkout', cleanCheckout: '#forceClean') {
        repository(name: '#repoName')
    }
}

requirePostgres() {
    requirement(key: 'postgres 9.3', condition: 'exists')
}

requireBash() {
    requirement(key: 'system.builder.command.Bash', condition: 'exists')
}

requirePython() {
    requirement(key: 'system.builder.command.Python', condition: 'exists')
}

requireMysql() {
    requirement(key: 'mysql 5.5', condition: 'equals', value: 'true')
}

cleanMavenCache() {
    // this task will only work on Linux, not on Windows
    task(
            type: 'script',
            description: 'Clears JIRA artifacts cached in the local mvn repository',
            scriptBody: '''
echo "=== Cleaning JIRA local .m2 cache ==="
if [ -d ~/.m2/repository/com/atlassian/jira ]; then
   rm -rf ~/.m2/repository/com/atlassian/jira
fi'''
    )
}

cleanMavenCacheAsFinal() {
    // this task will only work on Linux, not on Windows
    task(
            type: 'script',
            description: 'Clears JIRA artifacts cached in the local mvn repository',
            final: 'true',
            scriptBody: '''
echo "=== Cleaning JIRA local .m2 cache ==="
if [ -d ~/.m2/repository/com/atlassian/jira ]; then
   rm -rf ~/.m2/repository/com/atlassian/jira
fi'''
    )
}

purgeMavenSandboxJob() {
    job(name: 'Purge sandbox', key: 'PURGESANDBOX', description: 'Purge the Maven sandbox created by this build.') {
        requireMaven2()
        requireJdk(jdk: 'JDK 1.7')
        requireLinux()
        task(
                type: 'script',
                description: 'Purge the Maven sandbox created by this build',
                environmentVariables: 'PATH=${bamboo.capability.system.builder.mvn2.Maven 2.1}/bin/:$PATH JAVA_HOME=${bamboo.capability.system.jdk.JDK 1.7}',
                scriptBody: '''
#!/bin/bash

# Not using a Maven task so Bamboo won't misconfigure the repository url
# ("unknown protocol: dav" exception)
mvn -B -Dsandbox.key=${bamboo.sandbox.key} com.atlassian.maven.plugins:sandbox-maven-plugin:2.1-beta19:delete'''
        )
    }
}

requireActiveDirectory() {
    requirement(key: 'active_directory', condition: 'equals', value: 'true')
}

requireSqlServer() {
    requirement(key: 'sql-server-2008-r2', condition: 'equals', value: 'true')
}

requireSql2012Server() {
    requirement(key: 'sql-server-2012', condition: 'equals', value: 'true')
}

requireOracle() {
    requirement(key: 'oracle 11g', condition: 'equals', value: 'true')
}

requirePostgres9_0() {
    requirement(key: 'postgres 9.0', condition: 'equals', value: 'true')
}

requireElasticAgents() {
    requirement(key: 'elastic', condition: 'equals', value: 'true')
}
