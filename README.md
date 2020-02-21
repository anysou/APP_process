### 通过电脑shell启动一个shell的Java程序，就可以长驻手机了，可以实现：远程录屏、远程控制、等特殊功能。

先来一个效果图，刷文章：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190322142823783.gif#pic_center)

### 实现这自动控制效果：手机非Root、非无障碍实现、非PC连接。

### 原理
Android 安卓是在Linux底层上开发的，各APP的权限及资源共享等都是靠进程来划分的。
app_process 是 Android 上的一个原生程序，是 APP 进程的主入口点，所以通过该运行的纯JAVA MAIN程序拥有比较高的权限。

ADB shell 可以调用 app_process，同时有执行shell很多功能的权限。
所以，我们只要让手机内驻留一个通过app_process启动的程序，该程序能通过Socket方式搭建接收其他APP发来的Shell指令的请求，则可以代替APP完成很多特殊功能。

### 前言
很多刷量、抢单、等需求，通常采用模拟接口访问，经常一番折腾就放弃了，大厂的网络传输加密不是那么好破解的，尝试了几种抓包，拦截，改变参数，再次发送的方式，测试、确往往不能成功。

目前很多采用：按键精灵、网易的AirTest、安卓无障碍服务；但往往会碰到手机功能很有限、或需要连接PC的问题。本方案则是一个很好的补充。

### 实践

大家都知道 [Android 调试桥 (adb)](https://developer.android.com/studio/command-line/adb.html) 是一个通用命令行工具，其允许您与模拟器实例或连接的 Android 设备进行通信。它可为各种设备操作提供便利，如安装和调试应用，并提供对 Unix shell（可用来在模拟器或连接的设备上运行各种命令）的访问。
[adb详解](https://github.com/anysou/awesome-adb)

adb驱动，adb工具包（推荐到adbshell.com下载)

比如模拟按键点击：

```java
	adb shell input tap 460 410
```

点击屏幕 (460 410) 这点，模拟输入文本：

```java
	adb shell input text hello
```

输入文本「hello」，模拟滑动：

```java
	adb shell input swipe 300 1000 300 500
```

参数 300 1000 300 500 分别表示起始点 x 坐标，起始点 y 坐标，结束点 x 坐标，结束点 y 坐标。模拟回车，返回键：

```java
	adb shell input keyevent 66
	adb shell input keyevent 4
```

66 表示回车，4 表示返回键。还有常见的 <font color=#606C8F>adb install</font> ，<font color=#606C8F>adb push</font> 录屏截图等等。想查看更多 adb 命令，请链接 [awesome-adb](https://github.com/mzlogin/awesome-adb)。好了，具体看案例。

#### 「某度」自动浏览文章

「某度」自动浏览文章为了以下几步：

 1. 打开「某度」app
 2. 点击顶部输入框
 3. 输入浏览的文章地址
 4. 回车，搜索文章
 5. 滑动（浏览）
 6. 点击推荐广告
 7. 点击返回键退出

第一步，启动应用 / 调起 Activity 的命令如下：

```java
	adb shell am start [options] <INTENT>
```

例如：

```java
	adb shell am start -n com.tencent.mm/.ui.LauncherUI
```

表示打开「微信」主界面。参数 <font color=#606C8F>com.tencent.mm</font> 表示微信包名 <font color=#606C8F>.ui.LauncherUI</font> 表示打开的 Activity 的名称。查看当前界面 Activity 名称的方式有许多，这里推荐 [android-TopActivity](https://github.com/109021017/android-TopActivity)。

### 查看当前激活的控件：

 1、手机与PC连接。   2、手机打开要获取的APP。   3、执行下面指令

 adb shell dumpsys window windows | findstr mCurrentFocus > APK.TXT

如下图左上角：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190321145829920.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTI1NTEzNTA=,size_16,color_FFFFFF,t_70)

获取到「某度」的包名与主界面的名称为，<font color=#606C8F>com.baidu.searchbox</font> 与 <font color=#606C8F>.MainActivity </font>，那么打开「某度」的 adb 命令如下：

```java
	adb shell am start -n com.baidu.searchbox/com.baidu.searchbox.MainActivity
```

第二步，点击顶部输入框区域，那么需要获取到点击点的坐标位置，可以借助「开发者选择」的「指针位置」来获取：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190321152048984.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTI1NTEzNTA=,size_16,color_FFFFFF,t_70)
那么我们通过模拟点击 (431 380) 来模拟点击输入框：

```java
	adb shell input tap 431 380
```

第三步，输入浏览文章的地址，adb 命令如下：

```java
	adb shell input text 'https://na.mbd.baidu.com/je3rqk2?f=cp'
```

注意：如果你安装了第三方输入法，可能会导致输入错乱，请在「设置」「语言与输入法」「虚拟键盘」下关闭第三方输入法。

第四步，回车搜索，adb 命令如下：

```java
	adb shell input keyevent 66
```

注意：文章搜索是异步，需延迟后续操作，在后文中会讲到。

第五步，模拟滑动，adb 命令如下：

```java
	adb shell input swipe  200 1800  200 0
```

起点 y 坐标 1800 与结束点 y 坐标 0 ，相差越大滑动越大，在每个机型上需要调整，同时滑动到广告出现在屏幕内的次数可能也不一样。具体请在真机上模拟调整。

第六步，点击推荐广告，同上获取到广告区域的坐标点：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190321170751932.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTI1NTEzNTA=,size_16,color_FFFFFF,t_70)

对应的 adb 命令如下：

```java
	adb shell input tap 583 339
```

第七步，点击返回键的 adb 命令：

```java
	adb shell input keyevent 4
```

总共七步就完成了一次自动浏览文章，有小伙伴肯定会有疑问，不会每步都执行 DOS 命令吧，这样比手动点击还慢呢，那有没有脚本可以批处理。

[bat](https://baike.baidu.com/item/bat/365230) （批处理文件类型）就是解决这样的问题。新建 xx.bat 文件，把以下代码拷入：

```java
ping 127.0.0.1 -n 2
adb shell am start -n com.baidu.searchbox/com.baidu.searchbox.MainActivity
ping 127.0.0.1 -n 3
adb shell input tap 431 380
adb shell input text  https://na.mbd.baidu.com/je3rqk2?f=cp
ping 127.0.0.1 -n 1
adb shell input keyevent 66
ping 127.0.0.1 -n 3
adb shell input swipe  200 1800  200 0
ping 127.0.0.1 -n 2
adb shell input swipe  200 1800  200 0
ping 127.0.0.1 -n 2
adb shell input swipe  200 1800  200 0
ping 127.0.0.1 -n 2
adb shell input swipe  200 1200  200 0
ping 127.0.0.1 -n 2
adb shell input tap 583 339
ping 127.0.0.1 -n 5
```

保存，确保手机连上电脑，双击 xx.bat 文件，发现手机自动打开百度，输入地址，浏览文章，哈哈，这样方便多了。但还有一个小小的不足，浏览完一次文章 xx.bat 就结束了，能不能加个循环语句，让文章间断性被浏览。由于并不熟悉 .bat 的写法，研究了一下，功夫不负有心人，我们可以这么做：

```java
:start
ping 127.0.0.1 -n 2
adb shell am start -n com.baidu.searchbox/com.baidu.searchbox.MainActivity
ping 127.0.0.1 -n 3
adb shell input tap 431 380
adb shell input text  https://na.mbd.baidu.com/je3rqk2?f=cp
ping 127.0.0.1 -n 1
adb shell input keyevent 66
ping 127.0.0.1 -n 3
adb shell input swipe  200 1800  200 0
ping 127.0.0.1 -n 2
adb shell input swipe  200 1800  200 0
ping 127.0.0.1 -n 2
adb shell input swipe  200 1800  200 0
ping 127.0.0.1 -n 2
adb shell input swipe  200 1200  200 0
ping 127.0.0.1 -n 2
adb shell input tap 583 339
ping 127.0.0.1 -n 5
adb shell input keyevent 4
adb shell input keyevent 4
adb shell input keyevent 4
adb shell input keyevent 4
adb shell input keyevent 4
goto start
pause
```

`ping 127.0.0.1 -n 1` 用于延迟执行，由于每步操作都是异步，延时时间你可以根据具体情况而定。emmm，大功告成，文章被无限周期性浏览，但还是有两个小小的瑕疵，一是要运行 bat 脚本；二是手机必须连上电脑。针对第一种情况，可不可以在 app 内执行 adb shell 命令，最初尝试提示「permission denied」权限被拒绝，需要 Root 权限，Root 太麻烦，而且用户也不会同意，那么我们可不可以绕过 Root 权限？

这个问题一直困扰着我，在这里非常感谢 gtf 同学的[免Root实现静默安装和点击任意位置（非无障碍）](https://www.jianshu.com/p/86253b2c49f3)文章，这里引用他的一段话：

> 我来问大家个新问题，怎样让 app 获取 root 权限？这个问题答案已经有不少了，网上一查便可知其实是获取「Runtime.getRuntime().exec」的流，在里面用su提权，然后就可以执行需要 root 权限的 shell 命令，比如挂载 system 读写，访问 data 分区，用 shell 命令静默安装，等等。话说回来，是不是和我们今天的主题有点像，如何使 app 获取 shell 权限？嗯，其实差不多，思路也类似，因为本来 root 啦， shell 啦，根本就不是 Android 应用层的名词呀，他们本来就是 Linux 里的名词，只不过是 Android 框架运行于 Linux 层之上， 我们可以调用 shell 命令，也可以在shell 里调用 su 来使shell 获取 root 权限，来绕过 Android 层做一些被限制的事。然而在 app 里调用 shell 命令，其进程还是 app 的，权限还是受限。所以就不能在 app 里运行 shell 命令，那么问题来了，不在 app 里运行在哪运行？答案是在 pc 上运行。当然不可能是 pc 一直连着手机啦，而是 pc 上在 shell 里运行独立的一个 java 程序，这个程序因为是在 shell 里启动的，所以具有 shell 权限。我们想一下，这个 Java 程序在 shell 里运行，建立本地 socket 服务器，和 app 通信，远程执行 app 下发的代码。因为即使拔掉了数据线，这个 Java 程序也不会停止，只要不重启他就一直活着，执行我们的命令，这不就是看起来 app 有了 shell 权限？现在真相大白，飞智和黑域用 usb 调试激活的那一下，其实是启动那个 Java 程序，飞智是执行模拟按键，黑域是监听系统事件，你想干啥就任你开发了。**「注：黑域和飞智由于进程管理的需要，其实是先用 shell 启动一个 so ，然后再用 so 做跳板启动 Java 程序，而且 so 也充当守护进程，当 Java 意外停止可以重新启动，读着有兴趣可以自行研究，在此不多做说明」**

这里有一句话说的非常好，**「你想干啥就任你开发了。」** 如果调整好参数，我可以拿到「某信」的付款二维码，并能截图上传，就问你怕不怕？

`gtf` 同学的思路，让我醍醐灌顶，**并不是绕过 Root 权限，而是在 pc 上在 shell 里运行独立的一个 java 程序，，这个程序因为是在 shell 里启动的，所以具有 shell 权限。这个 Java 程序在 shell 里运行，建立本地 socket 服务器，和 app 通信，远程执行 app 下发的代码。因为即使拔掉了数据线，这个 Java 程序也不会停止，只要不重启他就一直活着，执行我们的命令，这不就是看起来 app 有了 shell 权限？**

大家想一想，如果我把 Java 程序部署到远程服务器上，那么我能随时随地都可以建立 socket ，从而控制手机自动完成想做的事情。

还得再次感谢 `gtf` 同学分享了一个简单的 socket 程序，亲测后的效果图如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190322103313156.gif)

在通过 app_process 在环境下运行 java 程序有以下几个细节：

### 细节一，通过 `javac ` 运行多个 `.java` 文件：

```java
D:\>cd D:\AndroidSpace\app_process-shell-use\app\src\main\java\shellService

D:\AndroidSpace\app_process-shell-use\app\src\main\java\shellService>javac -encoding UTF-8 Main.java Service.java ServiceShellUtils.java ServiceThread.java
```

首先 cd 到 java 目录，然后执行 javac 命令。注意：指定编码格式为 UTF-8 ，不然中文乱码会导致编译不通过。编译过的目录如下图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190322111749455.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTI1NTEzNTA=,size_16,color_FFFFFF,t_70)

### 细节二，多个 `.class` 文件生成 `.dex` 文件

```java
D:\AndroidSpace\app_process-shell-use\app\src\main\java\shellService>cd..

D:\AndroidSpace\app_process-shell-use\app\src\main\java>dx --dex --output=D:\hello.dex shellService/Main.class shellService/Service.class shellService/Service$CreateServerThread.class shellService/Service$ServiceGetText.class shellService/ServiceShellUtils.class shellService/ServiceShellUtils$ServiceShellCommandResult.class shellService/ServiceThread.class shellService/ServiceThread$1.class
```

注意，首先需要 cd 到 java 目录，不然会提示类文件找不到，然后通过 dx --dex 命令生成 .dex 文件，所生成的所有 .class 文件都需要加到命令中。生成的 .dex 文件如下图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190322113404466.jpg)

## 细节一 + 细节二 合并的简单方法

1. 通过 Android Studio 创建一个新项目。
2. 然后在 app/java/下 New -> Package -> 选择 main/java -> 输入 shellService。
3. 将四个 java 文件完成后，编译生成 apk文件。
4. 将XXX.APK 更名为 xxx.RAR； 再解压缩，得到 classes.dex 文件。（该文件就是细节二里的hello.dex）


### 细节三，通过 app_process 运行 java 程序：

1. 先要将手机通过 usb 或 wifi 连接到 PC。  检测是否连接指令： adb devices  （显示 XXXX device 表示连接成功）
2. 将 dex 文件推入手机。 指令：adb push classes.dex /data/local/tmp  （将文件推送到 /data/local/tmp）
3. 进入shell。  指令：adb shell
4. 进入目录并运行：cd /data/local/tmp & app_process -Djava.class.path=/data/local/tmp/classes.dex /system/bin shellService.Main
5. 如果后台运行：cd /data/local/tmp & nohup app_process -Djava.class.path=/data/local/tmp/classes.dex /system/bin --nice-name=shellServer shellService.Main > /dev/null 2>&1 &


通过整理如下：

 前台运行：（测试用。退出，ctrl+c; usb线拔出就退出了） 将下面代码编辑成bat批出处理文件
 ```dos
 adb push  classes.dex /data/local/tmp
 @echo off
 del  "temp.txt"
 echo export CLASSPATH=/data/local/tmp/classes.dex >> temp.txt
 echo exec app_process /system/bin --nice-name=shellServer shellService.Main >> temp.txt
 adb shell  <  temp.txt
 del  "temp.txt"
 pause
 ```

  后台运行：（usb线拔出也不会退出，退出，进入shell: ps -le | grep shellServer ;找到对应PID ; 执行 kill -9 PID）
 ```dos
 adb push  classes.dex /data/local/tmp
 @echo off
 del  "temp.txt"
 echo export CLASSPATH=/data/local/tmp/classes.dex >> temp.txt
 echo nohup app_process /system/bin --nice-name=shellServer shellService.Main ^> /dev/null 2^>^&1 ^& >> temp.txt
 adb shell  <  temp.txt
 del  "temp.txt"
 pause
 ```
 注意：先退出DOS,再测试有效，在断USB线。

### APP作为功能逻辑实现的脚本客户端，查看本开源的 MainActivity.java 、 SocketClient 。

### 总结，思考，参考

问题一： 手机通过连接一次连接PC，完成程序adb推入，并通过 shell 调用 app_process 执行服务程序。如何将该程序变成系统服务，开机自启动、且不死。

问题二：是否有办法不通过连接PC，通过APP启动实现调用 app_process 执行服务程序。（应该需要ROOT权限）

### 新发现

google 开发了 uiautomator2 ，并可兼容其他语言来实现自动化测试的功能。其中涉及用到的ATX APP某种意义上课完全替代本文功能。

uiautomator2 还支持 Python. 手机里使用 Python 有  Qpython 、Pydroid、Termux（是一种Linux系统）

（目前Qpython的 python2 可完全支持 uiautomator2、但 python3 不能支持 uiautomator2 里的 lxml 无法使用xpath ）

（Pydroid 没有安装成功 uiautomator2 ）

（Termux 可行）

查考：

[使用 app_process 来调用高权限 API](https://haruue.moe/blog/2017/08/30/call-privileged-api-with-app-process/)

[app_process两种用法](https://blog.csdn.net/wanchupin/article/details/76223381)

```java
    private int testAppProcess() {
        int status = 0;
        synchronized (HomeActivity.class) {
            Process process = null;
            DataOutputStream os = null;
            MsgInputThread msgInput = null;
            ErrorInputThread errorInput = null;
            try {
                process = Runtime.getRuntime().exec("su");// 切换到root帐号
                os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(" export CLASSPATH=/sdcard/Main.dex   \n");
                os.writeBytes(" exec app_process /sdcard  cn.mimashuo.puppet.Main   \n");
                os.writeBytes(" exit \n");
                os.flush();
                // 启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流=>解决waitFor()阻塞的问题
                msgInput = new MsgInputThread(process.getInputStream(), new MsgInputThread.Listen() {
                    @Override
                    public void test() {

                    }
                });
                msgInput.start();
                errorInput = new ErrorInputThread(process.getErrorStream());
                errorInput.start();
                // waitFor返回的退出值的过程。按照惯例，0表示正常终止。waitFor会一直等待
                status = process.waitFor();// 什么意思呢？具体看http://my.oschina.net/sub/blog/134436
                if (status != 0) {
                    LogUtil.d("root日志：" + msgInput.getMsg());
                }
            } catch (Exception e) {
                LogUtil.e("出错：" + e.getMessage());
                status = -2;
                return status;
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                }
                try {
                    if (process != null) {
                        process.destroy();
                    }
                } catch (Exception e) {
                }
                try {
                    if (msgInput != null) {
                        msgInput.setOver(true);
                    }
                } catch (Exception e) {
                }
                try {
                    if (errorInput != null) {
                        errorInput.setOver(true);
                    }
                } catch (Exception e) {
                }
            }
        } // end synchronized
        return status;
    }

```
