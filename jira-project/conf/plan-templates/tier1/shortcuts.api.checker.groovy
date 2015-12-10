notifications() {
    notification(type: 'Failed Builds and First Successful', recipient: 'committers')
}

apiCheck(['repoName', 'jdk']) {
    job(name: 'API Check', key: 'UNIT', description: '') {
        requireLocalLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'dependencies report', location: 'jira-components/jira-core', pattern: 'dependencies.txt*', shared: 'false')
        artifactDefinition(name: 'maven stats', location: '', pattern: 'maven-stats.log', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: '', pattern: '**/TEST-*.xml', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        performApiCheck(jdk: '#jdk')
    }
}

performApiCheck(['jdk']) {
    task(
            type: 'maven3',
            description: 'Performs the Api check',
            goal: '-B -am clean install -DperformApiCheck -DskipTests -pl jira-components/jira-api',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'false'
    )
}
