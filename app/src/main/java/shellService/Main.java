package shellService;


/**
 *   通过电脑shell启动一个shell的Java程序，就可以长驻手机了，可以做远程录屏、远程控制等特殊功能。
 *  使用 app_process 来调用高权限 API：https://haruue.moe/blog/2017/08/30/call-privileged-api-with-app-process/
 *
 * 1、将整个项目编译后得到APK。
 * 2、将XXX.APK 更名为 xxx.RAR； 再解压缩，得到 classes.dex 文件.
 * 3、手机 和 电脑 通过 USB 或 WIFI 连接成功。  ADB: https://github.com/anysou/awesome-adb
 * 4、adb devices
 *    adb push classes.dex /data/local/tmp  将文件推送到 /data/local/tmp
 *    adb shell
 *    前台运行：（测试用。退出，ctrl+c; usb线拔出就退出了）
 *    cd /data/local/tmp & app_process -Djava.class.path=/data/local/tmp/classes.dex /system/bin shellService.Main
 *    后台运行：（usb线拔出也不会退出，退出，进入shell: ps -le | grep anysou ;找到对应PID ; 执行 kill -9 PID）
 *    cd /data/local/tmp & nohup app_process -Djava.class.path=/data/local/tmp/classes.dex /system/bin --nice-name=shellServer shellService.Main > /dev/null 2>&1 &

 前台运行：（测试用。退出，ctrl+c; usb线拔出就退出了） 将下面代码编辑成bat批出处理文件
 adb push  classes.dex /data/local/tmp  （注意一定要先执行这句）
 @echo off
 del  "temp.txt"
 echo export CLASSPATH=/data/local/tmp/classes.dex >> temp.txt
 echo exec app_process /system/bin --nice-name=shellServer shellService.Main >> temp.txt
 adb shell  <  temp.txt
 del  "temp.txt"
 pause

 后台运行：（usb线拔出也不会退出，退出，进入shell: ps -le | grep shellServer ;找到对应PID ; 执行 kill -9 PID）将下面代码编辑成bat批出处理文件
 adb push  classes.dex /data/local/tmp
 @echo off
 del  "temp.txt"
 echo export CLASSPATH=/data/local/tmp/classes.dex >> temp.txt
 echo nohup app_process /system/bin --nice-name=shellServer shellService.Main ^> /dev/null 2^>^&1 ^& >> temp.txt
 adb shell  <  temp.txt
 del  "temp.txt"
 pause

 注意：先退出DOS,再测试有效，在断USB线。
 问题：做成一个开机系统服务。

 * */

public class Main {

    // 定义要开启的 ServerSocket 端口号
    public static int PORT = 8888;

    public  static void main(String[] args){

        //==== 获取 手机的局域网ip地址
        try{
            // 下面三个Shell 命令都可以获得本IP
            String comd = "ifconfig |grep Bcast |awk -F'[ :]+' '{print $4}'";
            //String comd = "ifconfig |grep \"inet addr:\" |grep -v \"127.0.0.1\" |cut -d: -f2 |awk '{print $1}'";
            //String comd = "ifconfig -a |grep inet |grep -v 127.0.0.1 |grep -v inet6|awk '{print $2}' |tr -d \"addr:\"";

            ServiceShellUtils.ServiceShellCommandResult sr =  ServiceShellUtils.execCommand(comd, false);
            if (sr.result == 0){
                System.out.println("ServerSocket IP at : " + sr.successMsg);
            } else {
                System.out.println("Get ServerSocket IP error: " + sr.errorMsg);
            }
        }catch (Exception e){
            System.out.println("###CodeError#: " + e.toString());
        }

        System.out.println("\nI am run in shell; 我是在电脑端通过shell运行的！！！\n");
        new ServiceThread(PORT).start();
        while (true);
    }
}
