package com.lts.voicedemo.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.lts.voicedemo.ActivityOnline;
import com.lts.voicedemo.MessageStatusRecogListener;
import com.lts.voicedemo.R;
import com.lts.voicedemo.StatusRecogListener;
import com.lts.voicedemo.adapter.ChatAdapter;
import com.lts.voicedemo.adapter.OnlongItemClickListener;
import com.lts.voicedemo.constant.IStatus;
import com.lts.voicedemo.control.InitConfig;
import com.lts.voicedemo.control.MyRecognizer;
import com.lts.voicedemo.control.NonBlockSyntherizer;
import com.lts.voicedemo.control.UiMessageListener;
import com.lts.voicedemo.offline.CommonRecogParams;
import com.lts.voicedemo.offline.OfflineRecogParams;
import com.lts.voicedemo.util.OfflineResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 识别的基类Activity。封装了识别的大部分逻辑，包括MyRecognizer的初始化，资源释放、
 * <p>
 * 大致流程为
 * 1. 实例化MyRecognizer ,调用release方法前不可以实例化第二个。参数中需要开发者自行填写语音识别事件的回调类，实现开发者自身的业务逻辑
 * 2. 如果使用离线命令词功能，需要调用loadOfflineEngine。在线功能不需要。
 * 3. 根据识别的参数文档，或者demo中测试出的参数，组成json格式的字符串。调用 start 方法
 * 4. 在合适的时候，调用release释放资源。
 * <p>
 * Created by fujiayi on 2017/6/20.
 */

public abstract class ActivityRecog extends ActivityCommon implements IStatus, OnlongItemClickListener {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;

    private List<com.lts.voicedemo.bean.Message> mDatas = new ArrayList<>();

    /*
     * Api的参数类，仅仅用于生成调用START的json字符串，本身与SDK的调用无关
     */
    protected CommonRecogParams apiParams;

    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    protected boolean enableOffline = false;


    protected String appId = "10280834";

    protected String appKey = "e8AcjmMSCT88pYsIUoreZcfb";

    protected String secretKey = "7398b24c25d3a171fd68d4d03b5ce5d5";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;


    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_speech_female.data为离线男声模型；bd_etts_speech_female.data为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    /**
     * 控制UI按钮的状态
     */
    private int status;

    /**
     * 日志使用
     */
    private static final String TAG = "ActivityRecog";
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private EditText mEditText;

    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    @Override
    protected void initRecog() {
        StatusRecogListener listener = new MessageStatusRecogListener(handler);
        myRecognizer = new MyRecognizer(this, listener);
        apiParams = getApiParams();
        status = STATUS_NONE;
        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams());
        }
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    @Override
    protected void initialTts() {
        SpeechSynthesizerListener listener = new UiMessageListener(handler);
        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, offlineVoice, params, listener);

        synthesizer = new NonBlockSyntherizer(this, initConfig, handler); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0"); // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_VOLUME, "5"); // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");// 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");// 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);         // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        return params;
    }

    /**
     * 销毁时需要释放识别资源。
     */
    @Override
    protected void onDestroy() {
        myRecognizer.release();
        super.onDestroy();
    }

    /**
     * 开始录音，点击“开始”按钮后调用。
     */
    protected void start() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityRecog.this);
        Map<String, Object> params = apiParams.fetch(sp);
        myRecognizer.start(params);
    }


    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    private void stop() {
        myRecognizer.stop();
    }

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     */
    private void cancel() {
        myRecognizer.cancel();
    }


    /**
     * @return
     */
    protected abstract CommonRecogParams getApiParams();

    // 以上为 语音SDK调用，以下为UI部分
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void initView() {
//        super.initView();
        if (this instanceof ActivityOnline) {
            btn = (Button) findViewById(R.id.btn);
            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            mAdapter = new ChatAdapter(this, mDatas);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnLongItemClickListener(this);
            mEditText = (EditText) findViewById(R.id.editText);

        }
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (status) {
                    case STATUS_NONE: // 初始状态
                        start();
                        status = STATUS_WAITING_READY;
                        updateBtnTextByStatus();
//                        txtLog.setText("");
//                        txtResult.setText("");
                        break;
                    case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                    case STATUS_READY: // 引擎准备完毕。
                    case STATUS_SPEAKING:
                    case STATUS_FINISHED:// 长语音情况
                    case STATUS_RECOGNITION:
                        stop();
                        status = STATUS_STOPPED; // 引擎识别中
                        updateBtnTextByStatus();
                        break;
                    case STATUS_STOPPED: // 引擎识别中
                        cancel();
                        status = STATUS_NONE; // 识别结束，回到初始状态
                        updateBtnTextByStatus();
                        break;
                }

            }
        });
    }

    public void send(View view) {
        String trim = mEditText.getText().toString().trim();

        if (TextUtils.isEmpty(trim)) {
            Snackbar.make(mRecyclerView, "发送内容不能为空", Snackbar.LENGTH_LONG).show();
        } else {
            com.lts.voicedemo.bean.Message message = new com.lts.voicedemo.bean.Message(trim, 2);
            mDatas.add(message);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(mDatas.size() -1);
            mEditText.setText("");
        }
    }

    protected void handleMsg(Message msg) {
        super.handleMsg(msg);

        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
//                if (msg.arg2 == 1)
//                    txtResult.setText(msg.obj.toString());

                if (msg.arg2 == 1) {
                    com.lts.voicedemo.bean.Message message = new com.lts.voicedemo.bean.Message(msg.obj.toString(), 1);
                    mDatas.add(message);
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(mDatas.size() -1);
                }

                //故意不写break
            case STATUS_NONE:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                status = msg.what;
                updateBtnTextByStatus();
                break;

        }
    }

    private void updateBtnTextByStatus() {
        switch (status) {
            case STATUS_NONE:
                btn.setText("开始录音");
                btn.setEnabled(true);
//                setting.setEnabled(true);
                break;
            case STATUS_WAITING_READY:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                btn.setText("停止录音");
                btn.setEnabled(true);
//                setting.setEnabled(false);
                break;

            case STATUS_STOPPED:
                btn.setText("取消整个识别过程");
                btn.setEnabled(true);
//                setting.setEnabled(false);
                break;
        }
    }

    @Override
    public void onLongItemClickListener(final String text, TextView textView) {
        View inflate = LayoutInflater.from(this).inflate(R.layout.popupwindow_layout, null, false);
        int[] location = new int[2];
        textView.getLocationOnScreen(location);
        final PopupWindow popupWindow = new PopupWindow(inflate, 300, 100, true);
        popupWindow.showAtLocation(textView, Gravity.NO_GRAVITY,location[0],location[1]-106);
        inflate.findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synthesizer.speak(text);
                popupWindow.dismiss();
            }
        });


    }
}
