package com.xiaoqiang.online.commonUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

import com.xiaoqiang.online.util.SPUtil;

import java.util.List;
import java.util.Stack;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * 2015.1.11 程序退出工具类
 *
 * @author xiaoqiang
 *         ActivityUtil (TODO)
 *         主要功能：Activity启动时的栈内存管理，程序退出时清理栈中所有的Activity,并实时管理所有的Activity
 */
public class ActivityUtil {
    private static final String Tag = "ActivityUtil";
    //activity栈管理
    public Stack<Activity> activityStack;
    //实例化的工具类
    private static ActivityUtil activityUtil;

    //单例模式获取类的实例
    public static ActivityUtil getInstance() {
        if (activityUtil == null) {
            synchronized (ActivityUtil.class) {
                if (activityUtil == null) {
                    activityUtil = new ActivityUtil();
                }
            }
        }
        return activityUtil;
    }

    /**
     * 往栈里面添加activity
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        if (activity != null) {
            activityStack.add(activity);
        }
    }

    /**
     * 获得当前栈顶Activity
     */
    public Activity getCurrentActivity() {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * 关闭当前栈顶Activity
     */
    public void finishCurrentActivity() {
        finishThisActivity(getCurrentActivity());
    }

    /**
     * 退出栈顶Activity
     */
    public void finishThisActivity(Activity activity) {
        if (activity != null && activityStack != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 退出栈中其他所有Activity
     *
     * @param cls Class 保留的类名
     */
    @SuppressWarnings("rawtypes")
    public void popOtherActivity(Class cls) {
        if (null == cls) {
            MLog.e(Tag, "cls is null");
            return;
        }
        if (activityStack != null) {
            for (Activity activity : activityStack) {
                if (null == activity || activity.getClass().equals(cls)) {
                    continue;
                }
                activity.finish();
            }
            MLog.d(Tag, "activity num is : " + activityStack.size());
        }
    }

    /**
     * 关闭指定名字activity
     */
    public void finishActivity(Class<?> cls) {
        finishThisActivity(getActivity(cls));
    }

    /**
     * 返回指定名字的activity
     */
    public Activity getActivity(Class<?> cls) {
        Activity finishActivity = null;
        if (activityStack != null) {
            for (Activity activity : activityStack) {
                if (activity.getClass().getName().equals(cls.getName())) {
                    finishActivity = activity;
                }
            }
        }
        return finishActivity;
    }

    /**
     * 关闭所有的activity然后清理栈
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 作为良心开发者，不能因为用户多安装一个app而增加一丝卡顿，app退出时必须调用此方法。
     * 彻底退出应用，清空相关的所有进程和堆栈内存
     * 注意：可用内存查看工具查看app退出后是否在堆栈中彻底清除
     * Ceate author: xiaoqiang
     * Email：sin2t@sina.com
     */
    public void AppExit(Context context) {
        try {
            SPUtil.Init(context).setAutoLogin(false);
            finishAllActivity();//杀死栈中所有Activity
            // Android的真后台特性，app退出后仍运行在后台占用内存。在此退出时清空了所有堆栈
            int pid = android.os.Process.myPid(); //获得自己的pid
            android.os.Process.killProcess(pid);//通过pid自杀
            System.exit(0); //退出JAVA虚拟机，此方法只是辅助，不可单独调用
        } catch (Exception e) {
            popOtherActivity(ActivityUtil.class);
            System.exit(0); //退出JAVA虚拟机
        }
    }

    //判读程序是否在运行
    public static boolean stackResumed(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getApplicationContext().getSystemService(
                        ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        List<RunningTaskInfo> recentTaskInfos = manager.getRunningTasks(1);
        if (recentTaskInfos != null && recentTaskInfos.size() > 0) {
            RunningTaskInfo taskInfo = recentTaskInfos.get(0);
            if (taskInfo.baseActivity.getPackageName().equals(packageName) && taskInfo.numActivities > 1) {
                return true;
            }
        }
        return false;
    }
}
