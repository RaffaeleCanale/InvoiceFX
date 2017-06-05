@echo off
echo Preparing...

ping 192.0.2.2 -n 1 -w 3000 > nul
REM cscript MessageBox.vbs "Hello!"



SET actual_jar="InvoiceFX.jar"
SET update_jar="InvoiceFX_update.jar"
SET backup_jar="InvoiceFX_backup.jar"

:: Optional: cd to $1
IF NOT "%~1" == "" cd %~1
IF %ERRORLEVEL% GEQ 1 EXIT /B 2

:: delete tmp download exe
if exist %update_jar% (
	if exist %actual_jar% (
		move %actual_jar% %backup_jar%
	)
	
	move %update_jar% %actual_jar%
)
