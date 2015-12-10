// Depends on:
//     tier3/shortcuts.mssql.2012.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'MSSQL2012', name: 'TPM MSSQL2012', description: 'Note: this plan does not support branch builds.') {
    def jdk = 'JDK 1.7'
    def repoName = 'JIRA 6.3.x branch'

    tier3Project()
    tier3Labels()

    repository(name: repoName)
    onceADay(time: '20:00')
    notifications()
    globalVariables()

    stage(name: 'MSSQL 2012 Batches', manual: 'false') {
        mssqlBatches(repoName: repoName, jdk: jdk)
    }
}
