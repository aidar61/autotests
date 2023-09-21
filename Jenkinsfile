pipeline {
    agent any
    tools {
        maven 'M3'
        jdk 'jdk11'
    }
    parameters {
        choice(
            name: "STAND",
            choices: ['http://tsdev1.dev.colvir.ru/TrackStudio'
            , 'http://tsdev2.dev.colvir.ru/TrackStudio'
            , 'http://tsdev4.dev.colvir.ru/TrackStudio'
            ,  'https://cdp.colvir.ru/TrackStudio/'],
            description: 'Number of the Stand'
        )
        choice(
            name:"TEST_TYPE",
            choices:["TaskGenerator"],
            description: 'Choose type test'
        )
    }
    stages {
        stage('Test') {
            steps {
                script {
                    def stand = params.STAND ?: '4'
                    def testType = params.TEST_TYPE ?: 'TaskGenerator'
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
