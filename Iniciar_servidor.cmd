@echo off
setlocal
cd /d "%~dp0"
if exist "%USERPROFILE%\.jdks\jbr-21.0.11\bin\java.exe" set "JAVA_HOME=%USERPROFILE%\.jdks\jbr-21.0.11"
if not defined JAVA_HOME if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo No se encontro Java. Abra este proyecto en Android Studio y configure Java 21.
  pause
  exit /b 1
)
if not exist "backend\target\revisiones-1.1.0.jar" (
  set "MAVEN_CMD=mvn.cmd"
  for /d %%I in ("%ProgramFiles%\JetBrains\IntelliJ IDEA*") do if exist "%%I\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" set "MAVEN_CMD=%%I\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
  call :compilar
  if errorlevel 1 exit /b 1
)
if not exist "logs" mkdir logs
powershell -NoProfile -Command "try { $r=Invoke-RestMethod 'http://127.0.0.1:8080/api/salud'; if ($r.servicio -eq 'tiotec-revisiones') { exit 0 }; exit 1 } catch { exit 1 }"
if not errorlevel 1 (
  echo El servidor TioTec ya esta iniciado.
  exit /b 0
)
cd backend
echo Iniciando el servidor en http://127.0.0.1:8080
echo Mantenga esta ventana abierta. Para detenerlo, presione Ctrl+C.
"%JAVA_HOME%\bin\java.exe" -jar target\revisiones-1.1.0.jar
exit /b %ERRORLEVEL%
:compilar
pushd backend
call "%MAVEN_CMD%" -B -DskipTests package
set "RESULTADO=%ERRORLEVEL%"
popd
exit /b %RESULTADO%

