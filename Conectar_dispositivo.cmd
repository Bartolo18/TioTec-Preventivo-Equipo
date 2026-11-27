@echo off
setlocal
set "SDK=%LOCALAPPDATA%\Android\Sdk"
if not exist "%SDK%\platform-tools\adb.exe" (
  echo No se encontro Android SDK en la ubicacion habitual.
  pause
  exit /b 1
)
"%SDK%\platform-tools\adb.exe" wait-for-device
"%SDK%\platform-tools\adb.exe" reverse tcp:8080 tcp:8080
if errorlevel 1 (
  echo No se pudo conectar el puerto. Compruebe que hay un solo dispositivo conectado.
  pause
  exit /b 1
)
if exist "%~dp0entrega\TioTec-firmado.apk" (
  "%SDK%\platform-tools\adb.exe" install -r "%~dp0entrega\TioTec-firmado.apk"
  if errorlevel 1 (
    echo La instalacion fallo. Revise el mensaje anterior.
    pause
    exit /b 1
  )
  "%SDK%\platform-tools\adb.exe" shell am start -n com.example.tiotec.entrega/com.example.tiotec.MainActivity
)
echo Conexion preparada. Mantenga abierto el servidor.
pause

