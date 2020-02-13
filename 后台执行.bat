 adb push  classes.dex /data/local/tmp
 @echo off
 del  "temp.txt"
 echo export CLASSPATH=/data/local/tmp/classes.dex >> temp.txt
 echo nohup app_process /system/bin --nice-name=shellServer shellService.Main ^> /dev/null 2^>^&1 ^& >> temp.txt
 adb shell  <  temp.txt
 del  "temp.txt"
 pause