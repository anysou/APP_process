package shellService;

/**
 * 继承线程
 * 开辟一个新线程，执行 shell 指令
 */

public class ServiceThread extends Thread {

    // 定义电脑服务器 的端口
    private int ServicePORT = 0;

    public ServiceThread(int PORT){
        ServicePORT = PORT;
    }


    @Override
    public void run() {
        System.out.println(">>>>>> Shell Service Run <<<<<<");
        //System.out.println(">>>>>> Shell服务端程序被调用 <<<<<<");

        // ServiceGetText 接口
        new Service(new Service.ServiceGetText() {

            @Override  //重定义接口内函数
            public String getText(String text) {
                //text.startsWith 如果开始内容是
                if (text.startsWith("###AreYouOK")){
                    return "###IamOK#";
                }
                try{
                    ServiceShellUtils.ServiceShellCommandResult sr =  ServiceShellUtils.execCommand(text, false);
                    if (sr.result == 0){
                        return "Shell_OK: \n" + sr.successMsg;
                    } else {
                        return "*******ShellError: \n" + sr.errorMsg;
                    }
                }catch (Exception e){
                    return "******CodeError: \n" + e.toString();
                }
            }
        }, ServicePORT);
    }
}
