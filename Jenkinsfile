pipeline {
  agent any
  environment {
    ALLURE_HOME = '/opt/allure'
  }
  stages {
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [[$class: 'CleanCheckout']], userRemoteConfigs: [[url: 'https://gitlab.colvir.ru/trackstudio/autotests/-/tree/master/']]])
      }
    }
    stage('Build') {
      steps {
        sh 'mvn clean install -Dmaven.test.failure.ignore=true'
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
