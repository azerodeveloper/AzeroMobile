package com.soundai.azero.lib_todaystepcounter;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import org.json.JSONArray;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TodayStepService extends Service implements Handler.Callback {

    private static final String TAG = "TodayStepService";

    private static final String STEP_CHANNEL_ID = "stepChannelId";

    /**
     * 步数通知ID
     */
    private static final int NOTIFY_ID = 1000;

    /**
     * 保存数据库频率
     */
    private static final int DB_SAVE_COUNTER = 300;

    /**
     * 传感器刷新频率
     */
    private static final int SAMPLING_PERIOD_US = SensorManager.SENSOR_DELAY_FASTEST;

    /**
     * 运动停止保存步数
     */
    private static final int HANDLER_WHAT_SAVE_STEP = 0;

    /**
     * 刷新通知栏步数
     */
    private static final int HANDLER_WHAT_REFRESH_NOTIFY_STEP = 2;


    /**
     * 如果走路如果停止，10秒钟后保存数据库
     */
    private static final int LAST_SAVE_STEP_DURATION = 10 * 1000;

    /**
     * 刷新通知栏步数，3s一次
     */
    private static final int REFRESH_NOTIFY_STEP_DURATION = 3 * 1000;

    /**
     * 点击通知栏广播requestCode
     */
    private static final int BROADCAST_REQUEST_CODE = 100;

    public static final String INTENT_NAME_0_SEPARATE = "intent_name_0_separate";
    public static final String INTENT_NAME_BOOT = "intent_name_boot";
    public static final String INTENT_STEP_INIT = "intent_step_init";

    /**
     * 当前步数
     */
    private static int CURRENT_STEP = 0;

    private SensorManager mSensorManager;
    /**
     * Sensor.TYPE_ACCELEROMETER
     * 加速度传感器计算当天步数，需要保持后台Service
     */
    private TodayStepDetector mStepDetector;
    /**
     * Sensor.TYPE_STEP_COUNTER
     * 计步传感器计算当天步数，不需要后台Service
     */
    private TodayStepCounter mStepCounter;

    private boolean mSeparate = false;
    private boolean mBoot = false;

    /**
     * 保存数据库计数器
     */
    private int mDbSaveCount = 0;

    /**
     * 数据库
     */
    private ITodayStepDBHelper mTodayStepDBHelper;

    private final Handler sHandler = new Handler(this);

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLER_WHAT_SAVE_STEP: {
                //走路停止保存数据库
                mDbSaveCount = 0;
                saveDb(true, CURRENT_STEP);
                break;
            }
            case HANDLER_WHAT_REFRESH_NOTIFY_STEP: {
                //刷新通知栏
                updateTodayStep(CURRENT_STEP);
                sHandler.removeMessages(HANDLER_WHAT_REFRESH_NOTIFY_STEP);
                sHandler.sendEmptyMessageDelayed(HANDLER_WHAT_REFRESH_NOTIFY_STEP, REFRESH_NOTIFY_STEP_DURATION);
                break;
            }
            default:
                break;
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTodayStepDBHelper = TodayStepDBHelper.factory(getApplicationContext());
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        getSensorRate();
        Map<String, String> map = getLogMap();
        map.put("current_step", String.valueOf(CURRENT_STEP));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            mSeparate = intent.getBooleanExtra(INTENT_NAME_0_SEPARATE, false);
            mBoot = intent.getBooleanExtra(INTENT_NAME_BOOT, false);
            String setStep = intent.getStringExtra(INTENT_STEP_INIT);
            if (!TextUtils.isEmpty(setStep)) {
                try {
                    setSteps(Integer.parseInt(setStep));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        mDbSaveCount = 0;

        Map<String, String> map = getLogMap();
        map.put("current_step", String.valueOf(CURRENT_STEP));
        map.put("mSeparate", String.valueOf(mSeparate));
        map.put("mBoot", String.valueOf(mBoot));
        map.put("mDbSaveCount", String.valueOf(mDbSaveCount));
        //注册传感器
        startStepDetector();

        sHandler.removeMessages(HANDLER_WHAT_REFRESH_NOTIFY_STEP);
        sHandler.sendEmptyMessageDelayed(HANDLER_WHAT_REFRESH_NOTIFY_STEP, REFRESH_NOTIFY_STEP_DURATION);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Map<String, String> map = getLogMap();
        map.put("current_step", String.valueOf(CURRENT_STEP));


        sHandler.removeMessages(HANDLER_WHAT_REFRESH_NOTIFY_STEP);
        sHandler.sendEmptyMessageDelayed(HANDLER_WHAT_REFRESH_NOTIFY_STEP, REFRESH_NOTIFY_STEP_DURATION);

        return mIBinder.asBinder();
    }

    private void startStepDetector() {
        if (getStepCounter()) {
            addStepCounterListener();
        } else {
            addBasePedoListener();
        }
    }

    private void addStepCounterListener() {
        if (null != mStepCounter) {
            WakeLockUtils.getLock(this);
            CURRENT_STEP = mStepCounter.getCurrentStep();
            Map<String, String> map = getLogMap();
            map.put("current_step", String.valueOf(CURRENT_STEP));
            return;
        }
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (null == countSensor) {
            return;
        }
        mStepCounter = new TodayStepCounter(getApplicationContext(), mOnStepCounterListener, mSeparate, mBoot);
        CURRENT_STEP = mStepCounter.getCurrentStep();
        boolean registerSuccess = mSensorManager.registerListener(mStepCounter, countSensor, SAMPLING_PERIOD_US);
        Map<String, String> map = getLogMap();
        map.put("current_step", String.valueOf(CURRENT_STEP));
        map.put("current_step_registerSuccess", String.valueOf(registerSuccess));
    }

    private void addBasePedoListener() {
        if (null != mStepDetector) {
            WakeLockUtils.getLock(this);
            CURRENT_STEP = mStepDetector.getCurrentStep();
            Map<String, String> map = getLogMap();
            map.put("current_step", String.valueOf(CURRENT_STEP));
            return;
        }
        //没有计步器的时候开启定时器保存数据
        Sensor sensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (null == sensor) {
            return;
        }
        mStepDetector = new TodayStepDetector(this, mOnStepCounterListener);
        CURRENT_STEP = mStepDetector.getCurrentStep();
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        boolean registerSuccess = mSensorManager.registerListener(mStepDetector, sensor, SAMPLING_PERIOD_US);
        Map<String, String> map = getLogMap();
        map.put("current_step", String.valueOf(CURRENT_STEP));
        map.put("current_step_registerSuccess", String.valueOf(registerSuccess));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * 步数每次回调的方法
     *
     * @param currentStep
     */
    private void updateTodayStep(int currentStep) {
        CURRENT_STEP = currentStep;
        saveStep(currentStep);
    }

    private void saveStep(int currentStep) {
        sHandler.removeMessages(HANDLER_WHAT_SAVE_STEP);
        sHandler.sendEmptyMessageDelayed(HANDLER_WHAT_SAVE_STEP, LAST_SAVE_STEP_DURATION);

        if (DB_SAVE_COUNTER > mDbSaveCount) {
            mDbSaveCount++;

            return;
        }
        mDbSaveCount = 0;

        saveDb(false, currentStep);
    }

    /**
     * @param handler     true handler回调保存步数，否false
     * @param currentStep
     */
    private void saveDb(boolean handler, int currentStep) {

        TodayStepData todayStepData = new TodayStepData();
        todayStepData.setToday(getTodayDate());
        todayStepData.setDate(System.currentTimeMillis());
        todayStepData.setStep(currentStep);
        if (null != mTodayStepDBHelper) {
            if (!handler || !mTodayStepDBHelper.isExist(todayStepData)) {
                mTodayStepDBHelper.insert(todayStepData);
                Map<String, String> map = getLogMap();
                map.put("saveDb_currentStep", String.valueOf(currentStep));

            }
        }
    }

    private void cleanDb() {
        Map<String, String> map = getLogMap();
        map.put("cleanDB_current_step", String.valueOf(CURRENT_STEP));

        mDbSaveCount = 0;

        if (null != mTodayStepDBHelper) {
            //保存多天的步数
            mTodayStepDBHelper.deleteTable();
            mTodayStepDBHelper.createTable();
        }
    }

    private String getTodayDate() {
        return DateUtils.getCurrentDate("yyyy-MM-dd");
    }

    private boolean getStepCounter() {
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        return null != countSensor;
    }

    private OnStepCounterListener mOnStepCounterListener = new OnStepCounterListener() {
        @Override
        public void onChangeStepCounter(int step) {

            if (StepUtil.isUploadStep()) {
                CURRENT_STEP = step;
            }
        }

        @Override
        public void onStepCounterClean() {
            CURRENT_STEP = 0;
            cleanDb();
        }

    };
    private final ISportStepInterface.Stub mIBinder = new ISportStepInterface.Stub() {
        private JSONArray getSportStepJsonArray(List<TodayStepData> todayStepDataArrayList) {
            return SportStepJsonUtils.getSportStepJsonArray(todayStepDataArrayList);
        }

        @Override
        public int getCurrentTimeSportStep() throws RemoteException {
            return CURRENT_STEP;
        }

        @Override
        public String getTodaySportStepArray() throws RemoteException {
            if (null != mTodayStepDBHelper) {
                List<TodayStepData> todayStepDataArrayList = mTodayStepDBHelper.getQueryAll();
                JSONArray jsonArray = getSportStepJsonArray(todayStepDataArrayList);
                return jsonArray.toString();
            }
            return null;
        }
    };

    public static String getReceiver(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_RECEIVERS);
            ActivityInfo[] activityInfos = packageInfo.receivers;
            if (null != activityInfos && activityInfos.length > 0) {
                for (ActivityInfo activityInfo : activityInfos) {
                    String receiverName = activityInfo.name;
                    Class superClazz = Class.forName(receiverName).getSuperclass();
                    int count = 1;
                    while (null != superClazz) {
                        if (superClazz.getName().equals("java.lang.Object")) {
                            break;
                        }
                        if (superClazz.getName().equals(BaseClickBroadcast.class.getName())) {
                            return receiverName;
                        }
                        if (count > 20) {
                            //用来做容错，如果20个基类还不到Object直接跳出防止while死循环
                            break;
                        }
                        count++;
                        superClazz = superClazz.getSuperclass();

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取传感器速率
     */
    @SuppressLint("SoonBlockedPrivateApi")
    private void getSensorRate() {

        Class<?> personType = SensorManager.class;

        //访问私有方法
        //getDeclaredMethod可以获取到所有方法，而getMethod只能获取public
        Method method = null;
        try {
            method = personType.getDeclaredMethod("getDelay", int.class);
            //压制Java对访问修饰符的检查
            method.setAccessible(true);
            //调用方法;person为所在对象
            int rate = (int) method.invoke(null, SAMPLING_PERIOD_US);
            Map<String, String> map = getLogMap();
            map.put("getSensorRate", String.valueOf(rate));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置步数初始值，目前只支持设置用加速度传感器进行计步
     *
     * @param steps
     */
    private void setSteps(int steps) {
        if (null != mStepDetector) {
            mStepDetector.setCurrentStep(steps);
        }
    }

    private Map<String, String> map;

    private Map<String, String> getLogMap() {
        if (map == null) {
            map = new HashMap<>();
        } else {
            map.clear();
        }
        return map;
    }


}
