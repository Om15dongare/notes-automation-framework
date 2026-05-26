pipeline {

    agent any

    tools {
        maven 'Maven_3.9'
        jdk 'JDK_25'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        HEADLESS = 'false'
    }

    stages {

        // ─────────────────────────────────────────────
        // 1. Checkout Source Code
        // ─────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "=== Cloning repository from GitHub ==="

                checkout scm

                echo "Branch: ${env.GIT_BRANCH}"
                echo "Commit: ${env.GIT_COMMIT}"
            }
        }

        // ─────────────────────────────────────────────
        // 2. Build Project
        // ─────────────────────────────────────────────
        stage('Build') {
            steps {

                echo "=== Compiling project ==="

                bat '''
                    mvn clean compile test-compile
                '''
            }
        }

        // ─────────────────────────────────────────────
        // 3. Execute Full Test Suite
        // ─────────────────────────────────────────────
        stage('Test') {
            steps {

                echo "=== Running automation suite ==="

                bat """
                    mvn test ^
                    -Dsurefire.suiteXmlFiles=testng-all.xml ^
                    -Dheadless=${HEADLESS}
                """
            }

            post {

                always {

                    echo "=== Collecting TestNG results ==="

                    junit allowEmptyResults: true,
                           testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ─────────────────────────────────────────────
        // 4. Generate Allure Report
        // ─────────────────────────────────────────────
        stage('Allure Report') {
            steps {

                echo "=== Generating Allure Report ==="

                allure([
                    commandline: 'Allure',
                    includeProperties: false,
                    jdk: '',
                    properties: [],
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: 'allure-results']]
                ])
            }
        }
    }

    // ─────────────────────────────────────────────
    // Post Build Actions
    // ─────────────────────────────────────────────
    post {

        success {

            echo '''
╔══════════════════════════════════════════════════╗
║  BUILD SUCCESS - All Tests Passed Successfully  ║
╚══════════════════════════════════════════════════╝
'''
        }

        failure {

            echo '''
╔══════════════════════════════════════════════════╗
║  BUILD FAILED - Check Console Output            ║
╚══════════════════════════════════════════════════╝
'''
        }

        always {

            echo "=== Archiving Allure Results ==="

            archiveArtifacts artifacts: 'allure-results/**',
                              allowEmptyArchive: true

            cleanWs()
        }
    }
}