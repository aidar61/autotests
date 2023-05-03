pipeline {
    agent any
    tools {
        maven 'M3'
        jdk 'jdk11'
    }
    parameters {
    choice(
            name:"Stand",
            choices: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11'],
            description: 'Number of the stend to use',
            defaultValue: '4'
    )
    }
    stages {
        stage('Build') {
            steps {
                sh "mvn properties:set -Dproperty=ts.stand -Dnewvalue=${params.Stand} -Dfile=app.properties"
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Publish Results') {
          steps {
            sh 'mvn allure:report'
            allure([
                includeProperties: true,
                jdk: '11',
                properties: [[key: 'environment', value: 'dev']],
                results: [[path: 'target/allure-results']]
            ])
          }
        }
    }
}
