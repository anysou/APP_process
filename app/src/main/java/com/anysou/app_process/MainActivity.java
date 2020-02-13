package com.anysou.app_process;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;

import java.util.Random;


/** app_process 是 Android 上的一个原生程序，是 APP 进程的主入口点。总之就是个可以让虚拟机从 main() 方法开始执行一个 Java 程序的东西啦。
 *
 * 一、注意添加网络访问权限。
 * 二、新建Socket服务器包：app/java/ New -> Package -> 选择 main/java -> 输入 shellService。（里面对应四个文件）
 * 三、服务器端创建，请看服务器包文件中 Main.java 的说明。
 * */

/**
 查看当前激活的控件：
 1、手机与PC连接。 2、手机打开要获取的APP。 3、执行下面指令
 adb shell dumpsys window windows | findstr mCurrentFocus > APK.TXT

 启动百度极速版的Activity：adb shell am start -n com.baidu.searchbox.lite/com.baidu.searchbox.MainActivity
 点击输入框位置坐标:adb shell input tap 460 410
 (注意输入法要切换成 Sigma键盘)
 输入文字： adb shell input text https://na.mbd.baidu.com/je3rqk2?f=cp
 回车： adb shell input keyevent 66
 从下到上滑动： adb shell input swipe  200 1800  200 0
 从左到右滑动： adb shell input swipe  100 1000  900 1000
 返回键： adb shell input keyevent 4
 */

public class MainActivity extends AppCompatActivity {

    private EditText editTextHostname;
    private EditText editTextPort;
    private EditText mCmdInputEt;
    private TextView mOutputTv;

    private String[] mWebAddress = {
            "https://na.mbd.baidu.com/je3rqk2?f=cp",
            "https://mo.mbd.baidu.com/3z7ipq3?f=cp",
            "https://mt.mbd.baidu.com/pck0n7u?f=cp",
            "https://mi.mbd.baidu.com/wxk4dl0?f=cp",
            "https://mx.mbd.baidu.com/olc3ija?f=cp"};

    private int i = 0;
    //adb shell dumpsys window windows | findstr mCurrentFocus > APK.TXT
    //adb shell am start -n
    //adb shell monkey -p com.milecn.milevideo -c android.intent.category.LAUNCHER 1
    //adb shell am force-stop com.milecn.milevideo
    private String[] video_name = {"快手","刷宝","抖音","火山","快看点","秘乐"};
    private String[] video_down = {"https://b1.sur5mz8y634c.com/nebula/task/invitation-by-area?code=2vptrjh&platform=copylink&fid=1720370650",
            "刷宝","抖音","火山","快看点","秘乐"};
    private String[] video_start = {"adb shell am start -n com.kuaishou.nebula/com.yxcorp.gifshow.HomeActivity",
            "adb shell am start -n com.jm.video/com.jm.video.ui.main.MainActivity",
            "adb shell am start -n com.ss.android.ugc.aweme.lite/com.ss.android.ugc.aweme.main.MainActivity",
            "adb shell am start -n com.ss.android.ugc.livelite/com.ss.android.ugc.live.main.MainActivity",
            "adb shell am start -n com.yuncheapp.android.pearl/com.kuaishou.athena.MainActivity",
            "adb shell monkey -p com.milecn.milevideo -c android.intent.category.LAUNCHER 1"};
    private String[] video_stop = {"adb shell am force-stop com.kuaishou.nebula",
            "adb shell am force-stop com.jm.video",
            "adb shell am force-stop com.ss.android.ugc.aweme.lite",
            "adb shell am force-stop com.ss.android.ugc.livelite",
            "adb shell am force-stop com.yuncheapp.android.pearl",
            "adb shell am force-stop com.milecn.milevideo"};
    private int id = 0;

    private void initView() {
        editTextHostname = (EditText) findViewById(R.id.editTextIP);
        editTextPort = (EditText) findViewById(R.id.editTextPORT);
        mCmdInputEt = (EditText) findViewById(R.id.et_cmd);
        mOutputTv = (TextView) findViewById(R.id.tv_output);
        mOutputTv.setMovementMethod(new ScrollingMovementMethod()); //textview要能滚动 android:scrollbars="vertical"
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        // 获取屏幕的size
        GetSize();
    }

    // 获取屏幕的size
    public void GetSize(){
        String msg = "";
        //此时获取到的是该Activity的实际占屏尺
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        msg = "本Activity的实际占屏尺：X="+metrics.widthPixels+"; Y="+metrics.heightPixels;

        // 此时获取到的是系统的显示尺寸
        metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;int height = metrics.heightPixels;
        msg += "\n此时获取到的是系统的显示尺寸：X="+metrics.widthPixels+"; Y="+metrics.heightPixels;

        mOutputTv.setText(msg);
    }


    // 运行输入的指令
    public void RunShell(View view) {
        mOutputTv.setText("");
        String cmd = mCmdInputEt.getText().toString();
        if (TextUtils.isEmpty(cmd)) {
            Toast.makeText(MainActivity.this, "输入内容为空，请输入Shell指令", Toast.LENGTH_SHORT).show();
            return;
        }
        runShell(cmd);
    }

    // 批处理运行命令，刷百度极速版
    public void RunBaidu(View view) {
        try {
            //adb shell am start -n com.baidu.searchbox/com.baidu.searchbox.MainActivity
            //adb shell am start -n com.baidu.searchbox.lite/com.baidu.searchbox.MainActivity
            runShell("adb shell am start -n com.baidu.searchbox.lite/com.baidu.searchbox.MainActivity");

            Thread.sleep(1000);

            // adb shell input tap 500 380

            runShell("adb shell input tap 460 410");

            Thread.sleep(1000);

            // adb shell input text  https://baijiahao.baidu.com/u?app_id=1604509451686382
            runShell("adb shell input text  " + mWebAddress[i % mWebAddress.length]);

            Thread.sleep(3000);

            // adb shell input keyevent 66 回车
            runShell("adb shell input keyevent 66");

            Thread.sleep(3000);

            //滑动
            runShell("adb shell input swipe  200 1800  200 0");

            Thread.sleep(1500);

            runShell("adb shell input swipe  200 1800  200 0");

            Thread.sleep(1500);

            runShell("adb shell input swipe  200 1800  200 0");

            Thread.sleep(1500);

            // 545 1330
            runShell("adb shell input tap 545 980");

            Thread.sleep(4000);

            //返回键
            runShell("adb shell input keyevent 4");

            Thread.sleep(200);

            runShell("adb shell input keyevent 4");

            Thread.sleep(200);

            runShell("adb shell input keyevent 4");

            Thread.sleep(200);

            runShell("adb shell input keyevent 4");

            Thread.sleep(200);

            runShell("adb shell input keyevent 4");

            Thread.sleep(1000);

            i++;

            if (i >= 10000000) {
                i = 0;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RunBaidu(null); //循环执行自己
    }

    // 刷视频
    public void video(View view) {
        try {
            int count = 0;
            mOutputTv.setText(video_start[id]);
            runShell(video_start[id]);
            Toast.makeText(MainActivity.this, "打开视频"+video_name[id], Toast.LENGTH_SHORT).show();
            Thread.sleep(getNum(10000,20000));
            while (count<getNum(200,300)){
                count ++;
                Thread.sleep(getNum(10000,20000));
                runShell("adb shell input swipe  200 1800  200 0");
                //Toast.makeText(MainActivity.this, ""+count, Toast.LENGTH_SHORT).show();
                mOutputTv.setText(""+count);
            }
            runShell(video_stop[id]);
            id ++;
            if(id>=video_start.length)
                id = 0;
        } catch ( Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        video(null); //循环执行自己
    }


    /**
     * 生成一个startNum 到 endNum之间的随机数(不包含endNum的随机数)
     * @param startNum
     * @param endNum
     * @return
     */
    public static int getNum(int startNum,int endNum){
        if(endNum > startNum){
            Random random = new Random();
            return random.nextInt(endNum - startNum) + startNum;
        }
        return 0;
    }


    // 通过多线程，Socket连接服务器，发送Shell执行指令 给服务器端的 app_process 权限的代为执行
    private void runShell(final String cmd) {
        final String hostname = editTextHostname.getText().toString();
        if (TextUtils.isEmpty(hostname)) {
            Toast.makeText(MainActivity.this, "输入Socket服务器IP不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        final String portstr = editTextPort.getText().toString();
        if (TextUtils.isEmpty(portstr)) {
            Toast.makeText(MainActivity.this, "输入Socket服务器PORT不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(cmd)) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SocketClient(hostname,portstr,cmd, new SocketClient.onServiceSend() {
                    @Override
                    public void getSend(String result) {
                        showTextOnTextView(result);
                    }
                });
            }
        }).start();
    }

    // 显示返回结果
    private void showTextOnTextView(final String text) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(mOutputTv.getText())) {
                    mOutputTv.setText(text);
                } else {
                    mOutputTv.setText(mOutputTv.getText() + "\n" + text);
                }
            }
        });
    }


    // 打开LoaCAT
    private void openLynxActivity() {
        LynxConfig lynxConfig = new LynxConfig();
        lynxConfig.setMaxNumberOfTracesToShow(4000)
                .setFilter("SocketClient");

        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        startActivity(lynxActivityIntent);
    }

    public void LogCat(View view) {
        openLynxActivity();
    }


}
