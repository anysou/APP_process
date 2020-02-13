 adb push  classes.dex /data/local/tmp
 @echo off
 del  "temp.txt"
 echo export CLASSPATH=/data/local/tmp/classes.dex >> temp.txt
 echo exec app_process /system/bin --nice-name=anysou shellService.Main >> temp.txt
 adb shell  <  temp.txt
 del  "temp.txt"
 pause