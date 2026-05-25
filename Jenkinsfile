pipeline {

    agent any

    tools {
        maven 'Maven_3.9'   // Must match name in Jenkins → Global Tool Configuration
        jdk   'JDK_25'      // Must match name in Jenkins → Global Tool Configuration
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        // ── 1. Checkout ────────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "=== Cloning repository from GitHub ==="
                checkout scm
                echo "Branch: ${env.GIT_BRANCH}  |  Commit: ${env.GIT_COMMIT}"
            }
        }


        stage('Build') {
            steps {
                echo "=== Compiling project ==="
                bat 'mvn clean compile test-compile -q'
            }
        }

        // ── 3. Run Full Test Suite ─────────────────────────────────────────────
        stage('Test') {
            steps {
                echo "=== Running all 21 tests (headless=true via testng-all.xml) ==="
                bat 'mvn test -Dsurefire.suiteXmlFiles=testng-all.xml -Dheadless=true -q'
            }
            post {
                always {
                    echo "=== Collecting TestNG results ==="
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ── 5. Allure Report ───────────────────────────────────────────────────
        stage('Allure Report') {
            steps {
                echo "=== Generating Allure Report ==="
                allure([
                    includeProperties: false,
                    jdk              : '',
                    properties       : [],
                    reportBuildPolicy: 'ALWAYS',
                    results          : [[path: 'allure-results']]
                ])
            }
        }
    }

    // ── Post-build actions ─────────────────────────────────────────────────────
    post {
        success {
            echo """
╔══════════════════════════════════════════════════╗
║  BUILD SUCCESS - All 21 Tests Passed!            ║
╚══════════════════════════════════════════════════╝
            """
        }
        failure {
            echo """
╔══════════════════════════════════════════════════╗
║  BUILD FAILED - Check console output above       ║
╚══════════════════════════════════════════════════╝
            """
        }
        always {
            echo "=== Archiving Allure results ==="
            archiveArtifacts artifacts: 'allure-results/**', allowEmptyArchive: true
            cleanWs()
        }
    }
}
