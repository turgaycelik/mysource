notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Job Hung', recipient: 'responsible')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
}

miscellaneousConfig() {
    buildExpiry(artifacts: 'true', buildLogs: 'true', duration: '0', period: 'days', minimumBuildsToKeep: '5', labelsToKeep: 'infrastructure poison retain')
}

qaStandalone(['repoName', 'jdk']) {
    job(name: 'Default Job', key: 'JOB1', description: '') {
        requireLocalLinux()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Linux Installer', location: 'jira-distribution/jira-installer-distribution/target', pattern: 'atlassian-jira-*.bin', shared: 'false')
        artifactDefinition(name: 'Standalone Tar', location: 'jira-distribution/jira-standalone-distribution', pattern: '**/atlassian-jira*.tar.gz', shared: 'false')
        artifactDefinition(name: 'Standalone', location: 'jira-distribution/jira-standalone-distribution', pattern: '**/atlassian-jira*.zi*', shared: 'false')
        artifactDefinition(name: 'Test reports', location: '', pattern: '**/surefire-reports/*', shared: 'false')
        artifactDefinition(name: 'Windows Installer', location: 'jira-distribution/jira-installer-distribution/target', pattern: 'atlassian-jira-*.exe', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runQaStandalone(jdk: '#jdk')
        splitJira()
    }
}

runQaStandalone(['jdk']) {
    task(
            type: 'maven2',
            description: 'Runs QA standalone',
            goal: 'clean install -pl jira-distribution/jira-standalone-distribution,jira-distribution/jira-installer-distribution -am -Pscreenshot-applet -Pdistribution,pseudo-loc -Dinstall4j.home=${bamboo.capability.install4j5} -Djira.installer.signinstaller=true -Djira.screenshotapplet.signscreenshotapplet=true -Dsigncode.keystore.password=${bamboo.atlassian.keystore.password} -Datlassian.keystore.storepass=${bamboo.atlassian.keystore.password}',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true'
    )
}

splitJira() {
    task(
            type: 'script',
            description: 'Split JIRA zip file into several smaller zip files',
            scriptBody: '''
#!/bin/sh

ZIP_FILENAME=`ls **/atlassian-jira*.zip | head -n 1`

echo "Using zip filename: ${ZIP_FILENAME}"

split -b 10485760 -d "${ZIP_FILENAME}" "${ZIP_FILENAME}"

# no zip on agents :(
# zip -s 10m "${ZIP_FILENAME}" --out "${ZIP_FILENAME}.split.zip"''',
            workingSubDirectory: 'jira-distribution/jira-standalone-distribution'
    )
}
