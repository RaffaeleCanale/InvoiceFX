@echo off
echo Preparing...

ping 192.0.2.2 -n 1 -w 1000 > nul

xcopy /s %2\InvoiceFX.jar %1\InvoiceFX.jar