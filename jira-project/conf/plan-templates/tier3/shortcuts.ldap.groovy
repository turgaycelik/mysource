notifications() {
    notification(type: 'Comment Added', recipient: 'group', group: 'jira-developers')
    notification(type: 'Failed Builds and First Successful', recipient: 'watchers')
}

activeDirectory(['repoName', 'jdk']) {
    job(name: 'TPM Active Directory', key: 'JOB1', description: '') {
        requireWindowsSnowflake()
        requireActiveDirectory()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'All logs', location: '.', pattern: '**/*.log*', shared: 'false')
        artifactDefinition(name: 'Jira Version', location: '', pattern: '**/serverInfo.txt', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runActiveDirectoryTests(jdk: '#jdk')
    }
}

runActiveDirectoryTests(['jdk']) {
    task(
            type: 'maven2',
            description: 'Runs TPM tests',
            goal: '-f jira-distribution/tpm-standalone/spm/pom.xml clean install -DJIRA=Trunk -DStandalone -DIntegrationTest=JIRA_ActiveDirectory -Dsnapshot=${bamboo.jira.6.3.branch.version} -B -e',
            environmentVariables: 'MAVEN_OPTS=${bamboo.maven.opts}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}

openLdap(['repoName', 'jdk']) {
    job(name: 'TPM OpenLDAP', key: 'OPENLDAP', description: '') {
        requireLinux()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'All logs', location: '.', pattern: '**/*.log*', shared: 'false')
        artifactDefinition(name: 'Jira Version', location: '', pattern: '**/serverInfo.txt', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runOpenLdapTests(jdk: '#jdk')
    }
}

runOpenLdapTests(['jdk']) {
    task(
            type: 'maven2',
            description: 'Runs TPM tests',
            goal: '-f jira-distribution/tpm-standalone/spm/pom.xml clean install -DJIRA=Trunk -DStandalone -DIntegrationTest=JIRA_OpenLdap -Dsnapshot=${bamboo.jira.6.3.branch.version} -B -e',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}
