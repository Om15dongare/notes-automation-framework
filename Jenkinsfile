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
                bat 'mvn clean compile test-compile'
            }
        }

        // ── 3. Run TestNG Suite (existing 21 tests) ───────────────────────────
        stage('TestNG Tests') {
            steps {
                echo "=== Running all 21 TestNG tests (headless=true via testng-all.xml) ==="
                bat 'mvn test -Dsurefire.suiteXmlFiles=testng-all.xml -Dheadless=true'
            }
            post {
                always {
                    echo "=== Collecting TestNG results ==="
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ── 4. Run Cucumber BDD Suite ──────────────────────────────────────────
        stage('Cucumber BDD Tests') {
            steps {
                echo "=== Running Cucumber BDD scenarios (headless=true via testng-cucumber.xml) ==="
                // catchError keeps the pipeline going so Allure still generates even if BDD fails
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    bat 'mvn test -Dsurefire.suiteXmlFiles=testng-cucumber.xml -Dheadless=true'
                }
            }
            post {
                always {
                    echo "=== Collecting Cucumber results ==="
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    // Archive Cucumber HTML report as a build artifact
                    archiveArtifacts artifacts: 'target/cucumber-reports/**', allowEmptyArchive: true
                }
            }
        }

        // ── 5. Allure Report (covers TestNG + Cucumber results) ───────────────
        stage('Allure Report') {
            steps {
                echo "=== Generating unified Allure Report (TestNG + Cucumber) ==="
                allure([
                    commandline      : 'Allure',
                    includeProperties: false,
                    jdk              : '',
                    properties       : [],
                    reportBuildPolicy: 'ALWAYS',
                    // Both TestNG and Cucumber write to the same allure-results dir
                    results          : [[path: 'target/allure-results']]
                ])
            }
        }
    }

    // ── Post-build actions ─────────────────────────────────────────────────────
    post {
        success {
            echo """
╔══════════════════════════════════════════════════╗
║  BUILD SUCCESS - TestNG + Cucumber BDD Passed!   ║
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
            archiveArtifacts artifacts: 'target/allure-results/**', allowEmptyArchive: true
            cleanWs()
        }
    }
}
