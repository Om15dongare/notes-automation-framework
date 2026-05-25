pipeline {

    agent any

    tools {
        maven 'Maven_3.9'   // Must match the name configured in Jenkins → Global Tool Configuration
        jdk   'JDK_11'      // Must match the name configured in Jenkins → Global Tool Configuration
    }

    environment {
        // ── Credentials (set as Jenkins Secret Text) ──────────────────────
        USER_EMAIL    = credentials('notes-app-email')      // omgdongare@gmail.com
        USER_PASSWORD = credentials('notes-app-password')   // 123456

        // ── Suite & browser ───────────────────────────────────────────────
        SUITE_XML   = 'testng-all.xml'
        BROWSER     = 'chrome'
        HEADLESS    = 'true'          // Always headless on CI

        // ── Allure ────────────────────────────────────────────────────────
        ALLURE_RESULTS = 'allure-results'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        // ── 1. Checkout ──────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "=== Cloning repository from GitHub ==="
                checkout scm
                echo "Branch: ${env.GIT_BRANCH}  |  Commit: ${env.GIT_COMMIT}"
            }
        }

        // ── 2. Setup config.properties with CI credentials ───────────────
        stage('Configure') {
            steps {
                echo "=== Injecting CI credentials into config.properties ==="
                script {
                    def configFile = 'src/test/resources/config.properties'
                    def content = readFile(configFile)
                    content = content
                        .replaceAll(/user\.email=.*/, "user.email=${USER_EMAIL}")
                        .replaceAll(/user\.password=.*/, "user.password=${USER_PASSWORD}")
                        .replaceAll(/headless=.*/, "headless=${HEADLESS}")
                        .replaceAll(/browser=.*/, "browser=${BROWSER}")
                    writeFile file: configFile, text: content
                    echo "config.properties updated for CI run."
                }
            }
        }

        // ── 3. Build (compile only, no tests) ────────────────────────────
        stage('Build') {
            steps {
                echo "=== Compiling project ==="
                sh 'mvn clean compile test-compile -q'
            }
        }

        // ── 4. Run Full Test Suite ────────────────────────────────────────
        stage('Test') {
            steps {
                echo "=== Running all 21 tests via ${SUITE_XML} ==="
                sh """
                    mvn test \
                        -Dsurefire.suiteXmlFiles=${SUITE_XML} \
                        -Dbrowser=${BROWSER} \
                        -Dheadless=${HEADLESS} \
                        -q
                """
            }
            post {
                always {
                    echo "=== Archiving TestNG results ==="
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ── 5. Allure Report ──────────────────────────────────────────────
        stage('Allure Report') {
            steps {
                echo "=== Generating Allure Report ==="
                allure([
                    includeProperties: false,
                    jdk              : '',
                    properties       : [],
                    reportBuildPolicy: 'ALWAYS',
                    results          : [[path: "${ALLURE_RESULTS}"]]
                ])
            }
        }
    }

    // ── Post-build actions ────────────────────────────────────────────────
    post {
        success {
            echo """
╔══════════════════════════════════════════════════╗
║  ✅  BUILD SUCCESS — All 21 Tests Passed!        ║
╚══════════════════════════════════════════════════╝
            """
        }
        failure {
            echo """
╔══════════════════════════════════════════════════╗
║  ❌  BUILD FAILED — Check Allure report above    ║
╚══════════════════════════════════════════════════╝
            """
        }
        always {
            echo "=== Archiving Allure results folder ==="
            archiveArtifacts artifacts: "${ALLURE_RESULTS}/**", allowEmptyArchive: true
            cleanWs()
        }
    }
}
