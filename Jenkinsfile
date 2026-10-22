
pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    environment {
        DEPLOY_DIR = 'C:\\SpringBootApp'
        TASK_NAME = 'SpringBootApp'
        APP_PORT = '8081'
    }

    stages {

        stage('Build') {
            steps {
                bat 'mvnw.cmd clean package -DskipTests'
            }
        }

        stage('Stop Application') {
            steps {
                powershell '''
                    Write-Host "Stopping previous Spring Boot application..."

                    $task = Get-ScheduledTask -TaskName $env:TASK_NAME -ErrorAction Stop

                    if ($task.State -eq 'Running') {
                        Stop-ScheduledTask -TaskName $env:TASK_NAME -ErrorAction Stop
                    }

                    $stopped = $false

                    for ($i = 0; $i -lt 30; $i++) {
                        $task = Get-ScheduledTask -TaskName $env:TASK_NAME

                        if ($task.State -ne 'Running') {
                            $stopped = $true
                            break
                        }

                        Start-Sleep -Seconds 1
                    }

                    if (-not $stopped) {
                        throw "Previous scheduled task did not stop."
                    }

                    $processes = Get-CimInstance Win32_Process |
                        Where-Object {
                            $_.Name -in @('java.exe', 'javaw.exe') -and
                            $_.CommandLine -and
                            $_.CommandLine.Contains('C:\\SpringBootApp\\app.jar')
                        }

                    foreach ($process in $processes) {
                        Write-Host "Stopping remaining Java PID $($process.ProcessId)"
                        Stop-Process -Id $process.ProcessId -Force -ErrorAction Stop
                    }

                    Start-Sleep -Seconds 2

                    $remaining = Get-CimInstance Win32_Process |
                        Where-Object {
                            $_.Name -in @('java.exe', 'javaw.exe') -and
                            $_.CommandLine -and
                            $_.CommandLine.Contains('C:\\SpringBootApp\\app.jar')
                        }

                    if ($remaining) {
                        throw "Previous application process is still running."
                    }

                    Write-Host "Previous application stopped."
                '''
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                    if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

                    copy /Y "target\\demo-0.0.1-SNAPSHOT.jar" "%DEPLOY_DIR%\\app.jar"

                    if errorlevel 1 exit /b 1

                    echo New JAR deployed successfully.
                '''
            }
        }

        stage('Start Application') {
            steps {
                powershell '''
                    Write-Host "Starting Spring Boot scheduled task..."

                    $startedAt = Get-Date

                    Start-ScheduledTask -TaskName $env:TASK_NAME -ErrorAction Stop

                    $ready = $false

                    for ($i = 1; $i -le 60; $i++) {
                        Start-Sleep -Seconds 1

                        $process = Get-CimInstance Win32_Process |
                            Where-Object {
                                $_.Name -in @('java.exe', 'javaw.exe') -and
                                $_.CommandLine -and
                                $_.CommandLine.Contains('C:\\SpringBootApp\\app.jar')
                            }

                        foreach ($p in $process) {
                            $listener = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue |
                                Where-Object { $_.OwningProcess -eq $p.ProcessId }

                            if ($listener) {
                                $ready = $true
                                Write-Host "Spring Boot started successfully. PID: $($p.ProcessId)"
                                break
                            }
                        }

                        if ($ready) {
                            break
                        }

                        Write-Host "Waiting for Spring Boot... $i/60"
                    }

                    if (-not $ready) {
                        Write-Host "Scheduled Task Status:"
                        Get-ScheduledTask -TaskName $env:TASK_NAME |
                            Get-ScheduledTaskInfo |
                            Format-List *

                        Write-Host "Startup Log:"
                        Get-Content "$env:DEPLOY_DIR\\startup.log" -Tail 20 -ErrorAction SilentlyContinue

                        Write-Host "Application Log:"
                        Get-Content "$env:DEPLOY_DIR\\app.log" -Tail 50 -ErrorAction SilentlyContinue

                        throw "Spring Boot application failed to start."
                    }
                '''
            }
        }
    }

    post {
        success {
            echo 'BUILD AND DEPLOYMENT SUCCESSFUL!'
        }

        failure {
            echo 'PIPELINE FAILED! Check Jenkins console output.'
        }
    }
}
