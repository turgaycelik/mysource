// Depends on:
//     tier3/shortcuts.mysql.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'MYSQL50', name: 'TPM MySQL50', description: 'Note: this plan does not support branch builds.') {
    def jdk = 'JDK 1.7'
    def repoName = 'JIRA 6.3.x branch'

    tier3Project()
    tier3Labels()

    repository(name: repoName)
    onceADay(time: '20:00')
    notifications()
    globalVariables()

    stage(name: 'Linux CentOS 5.X, Intel 32bit x86, JDK17, Jira Standalone, MySQL 5.0.67', manual: 'false') {
        mysqlBatches(repoName: repoName, jdk: jdk)
    }
}
