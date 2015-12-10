// --------------------------------------------------------------------------------------------------
// This file contains shortcuts common to all plans on Tier 3
// --------------------------------------------------------------------------------------------------
tier3Project() {
    project(key: 'J63STABLEPT', name: 'JIRA 6.3 Stable - Plan templates', description: '')
}

tier3Labels() {
    label(name: 'plan-templates')
    label(name: 'tier_3')
}
