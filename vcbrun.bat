@echo off
REM 
REM vcbrun.bat
REM 
REM 2011, (c) umberto.nicoletti at gmail.com
REM

REM you may use a network share as the target path
REM mount the network share where you want to copy the files to
REM the drive letter and path must match those in the config file
net use q: /delete /y
net use q: \\nas\vcb

set PATH=C:\Program Files\VMware\VMware Consolidated Backup Framework;%PATH%

groovy simplevcb.groovy > vcbrun.txt 2>&1

net use q: /delete /y

