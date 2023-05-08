pipeline {
    agent any
    tools {
        maven 'M3'
        jdk 'jdk11'
    }
    parameters {
    choice(
            name:"STAND",
            choices: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11'],
            description: 'Number of the Stand'
    )
    }
    stages {
        stage('Test') {
            steps {
                mvn clean test -Dts.stand=parameters.STAND
            }
            post{
                always{
                allure([
                includeProperties: false,
                jdk: '11.0.18',
                properties: [],
                reportBuildPolicy: 'ALWAYS',
                results: [[path: 'target/allure-results']]
                ])
                }
            }
        }
    }
}
