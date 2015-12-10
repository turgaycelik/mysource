// Depends on:
//     tier3/shortcuts.postgres.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'POSTGRES9', name: 'TPM Postgres 9', description: '') {
    def jdk = 'JDK 1.7'
    def repoName = 'JIRA 6.3.x branch'

    tier3Project()
    tier3Labels()

    repository(name: repoName)
    onceADay(time: '19:00')
    notifications()
    globalVariables()
    miscellaneousConfig()

    stage(name: 'Linux CentOS 5.X, Intel 32bit x86, Jira Standalone, PostgreSQL', manual: 'false') {
        postgresBatches(repoName: repoName, jdk: jdk)
    }
}
