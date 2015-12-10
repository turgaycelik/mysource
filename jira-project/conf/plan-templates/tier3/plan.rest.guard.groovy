// Depends on:
//     tier3/shortcuts.rest.guard.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'RESTAPIGUARD', name: 'CI REST API Guard', description: '') {
    def jdk = 'JDK'
    def jiraRepo = 'JIRA 6.3.x branch'
    def restClientRepo = 'JIRA REST Java Client - REST API GUARD'

    tier3Project()
    tier3Labels()

    repository(name: jiraRepo)
    repository(name: restClientRepo)
    trigger(description: 'Polling repo every 3 minutes', type: 'polling', strategy: 'periodically', frequency: '180') {
        repository(name: jiraRepo)
        repository(name: restClientRepo)
    }

    notifications()

    stage(name: 'Default Stage', manual: 'false') {
        restApiGuard(jiraRepo: jiraRepo, restClientRepo: restClientRepo, jdk: jdk)
    }
}
