pipeline {
    agent any
    tools {
        maven 'M3'
        jdk 'jdk11'
    }
    parameters {
        choice(
            name: "STAND",
            choices: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', 'ts-integration'],
            description: 'Number of the Stand'
        )
        choice(
            name:"TEST_TYPE",
            choices:['Regression','SlaFeature', 'SlaBug', 'SlaHelp', 'PotentialGap', 'GapSolution', 'Advice', 'DevTask', 'WorkTask', 'BugTask',"SdQuestion"],
            description: 'Choose type test'
        )
    }
    stages {
        stage('Test') {
            steps {
                script {
                    def stand = params.STAND ?: '4'
                    def testType = params.TEST_TYPE ?: 'Regression'
                    sh "mvn clean test -Dts.stand=$stand -Dgroups=$testType -DfailIfNoTests=false"
                }
            }
            post {
                always {
                    allure([
                        includeProperties: false,
                        jdk: '11.0.18',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'target/allure-results']]
                    ])
                }
//                 unstable {
//                         env.BUILD_STATUS = 'SUCCESS'
//                 }
            }
        }
    }
}
