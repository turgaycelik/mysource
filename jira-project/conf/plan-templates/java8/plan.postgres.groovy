plan(key: 'PGJ8', name: 'TPM Postgres 9.3 - Java 8', description: 'Postgres tests running on Java 8') {
    def jdk7 = 'JDK 1.7'
    def jdk8 = 'JDK 1.8'
    def repoName = 'JIRA 6.3.x branch'

    java8Project()
    java8Labels()

    dailyBuild(repoName: repoName, scheduledHour: '20', scheduledMin: '20')
    noBranchMonitoring()
    notifications()
    globalVariables()
    miscellaneousSettings()

    stage(name: 'Build JIRA', manual: 'false') {
        buildJiraFromSources(repoName: repoName, jdk: jdk7)
    }

    stage(name: 'Test JIRA', manual: 'false') {
        postgresTestsJobs(repoName: repoName, jdk: jdk8)
    }

    stage(name: 'Cleanup') {
        purgeMavenSandboxJob()
    }
}
