
pipeline {
    agent any

    environment {
        APP_PORT = '8081'
        DEPLOY_DIR = 'C:\\SpringBootApp'
    }

    stages {

        stage('Stop Application') {
            steps {
                powershell '''
                    $app = Join-Path $env:DEPLOY_DIR "app.jar"

                    $processes = Get-CimInstance Win32_Process |
                        Where-Object {
                            ($_.Name -eq "java.exe" -or $_.Name -eq "javaw.exe") -and
                            $_.CommandLine -and
                            $_.CommandLine.Contains($app)
                        }

                    foreach ($process in $processes) {
                        Write-Host "Stopping Spring Boot PID: $($process.ProcessId)"

                        Stop-Process -Id $process.ProcessId -Force -ErrorAction Stop
                    }

                    Start-Sleep -Seconds 2

                    Write-Host "Stop Application stage completed."
                '''
            }
        }

        stage('Build') {
            steps {
                bat 'mvnw.cmd clean package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                    if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

                    copy /Y "target\\demo-0.0.1-SNAPSHOT.jar" "%DEPLOY_DIR%\\app.jar"

                    if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('Start Application') {
            steps {
                withEnv(['JENKINS_NODE_COOKIE=dontKillMe']) {

                    powershell '''
                        $app = Join-Path $env:DEPLOY_DIR "app.jar"
                        $log = Join-Path $env:DEPLOY_DIR "app.log"
                        $err = Join-Path $env:DEPLOY_DIR "error.log"

                        Write-Host "Starting Spring Boot application..."

                        $process = Start-Process -FilePath "java.exe" `
                            -ArgumentList @("-jar", $app, "--server.port=8081") `
                            -WorkingDirectory $env:DEPLOY_DIR `
                            -RedirectStandardOutput $log `
                            -RedirectStandardError $err `
                            -PassThru

                        Write-Host "Started process PID: $($process.Id)"

                        Start-Sleep -Seconds 8

                        $connection = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue |
                            Where-Object { $_.OwningProcess -eq $process.Id }

                        if (-not $connection) {
                            Write-Host "Application failed to start!"

                            Get-Content $log -Tail 50 -ErrorAction SilentlyContinue
                            Get-Content $err -Tail 50 -ErrorAction SilentlyContinue

                            throw "Spring Boot deployment failed."
                        }

                        Write-Host "Spring Boot application running on port 8081!"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'BUILD AND DEPLOYMENT SUCCESSFUL!'
        }

        failure {
            echo 'PIPELINE FAILED! Check console output.'
        }
    }
}
