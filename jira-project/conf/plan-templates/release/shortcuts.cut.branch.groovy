notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Failed Jobs and First Successful', recipient: 'responsible')
}

globalVariables() {
    releaseGlobalVariables()
    variable(key: 'deploySourceJars', value: 'true')
    variable(key: 'isrelease', value: 'false')    
}

createReleaseBranch(['repoName', 'jdk']) {
    job(name: 'Create Git branch', key: 'JIRATAG', description: 'Creates a release branch for the new release in the JIRA Git repository') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        branchJira(jdk: '#jdk')
    }
}

branchJira(['jdk']) {
    task(
            type: 'script',
            description: 'Branch-fu JIRA',
            script: 'bin/jira-release-branch.sh',
            argument: '${bamboo.jira.6.3.release.version} ${bamboo.jira.6.3.release.branch}',
            environmentVariables: 'HOME=${system.bamboo.agent.home} GROOVY_HOME=/opt/java/tools/groovy/groovy-1.8.1 MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

prepareNextDevVersion(['repoName', 'jdk']) {
    job(name: 'Prepare for next development version', key: 'UPDATEJIRA', description: '') {
        requireLocalLinux()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        createNextVersionBranch(jdk: '#jdk')
        deployJiraSnapshot(jdk: '#jdk')
    }
}

createNextVersionBranch(['jdk']) {
    task(
            type: 'script',
            description: 'Prepare branch development on the next version',
            script: 'bin/jira-release-prepare-next-version.sh',
            argument: '${bamboo.jira.6.3.next.version} ${bamboo.jira.6.3.release.version} ${bamboo.jira.version.increment} ${bamboo.jira.6.3.branch.raw}',
            environmentVariables: 'HOME=${system.bamboo.agent.home} MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

deployJiraSnapshot(['jdk']) {
    task(
            type: 'maven2',
            description: 'Deploys a JIRA SNAPSHOT to maven',
            goal: '-Djira.scm=git clean deploy -DskipTests=true',
            environmentVariables: 'MAVEN_OPTS="-Xmx2048m -Xms256m -XX:MaxPermSize=512m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'false'
    )
}
