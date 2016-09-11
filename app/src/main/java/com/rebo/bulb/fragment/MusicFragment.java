package com.rebo.bulb.fragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.exception.BleException;
import com.rebo.bulb.AppConst;
import com.rebo.bulb.BaseApplication;
import com.rebo.bulb.R;
import com.rebo.bulb.activity.DeviceDetailActivity;
import com.rebo.bulb.ble.BleConst;
import com.rebo.bulb.model.MusicModel;
import com.rebo.bulb.utils.EventBusUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by guodunsong on 16/7/10.
 */
@SuppressLint("NewApi")
public class MusicFragment extends BaseFragment {

    //    private ImageView musicImageView;
    private static final String TAG = "MusicFragment";

    private static final float VISUALIZER_HEIGHT_DIP = 360f;
    private static final int SPLIT_CNT = 20;
    private SeekBar seekBar;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;
    private Equalizer mEqualizer;
    private MusicModel curMusicModel;
    private TextView mMusicTitleTextView;
    private Animation operatingAnim;
    private final ConcurrentLinkedQueue<byte[]> commandQueue = new ConcurrentLinkedQueue<byte[]>();
    private boolean bleProcessing;

    @Bind(R.id.iv_dvd)
    ImageView dvdImageView;


    ImageView mPlayButton;

    Handler handler = new Handler();
    Runnable updateThread = new Runnable() {
        public void run() {
            // 获得歌曲现在播放位置并设置成播放进度条的值
            seekBar.setProgress(mMediaPlayer.getCurrentPosition());
            // 每次延迟100毫秒再启动线程
            handler.postDelayed(updateThread, 100);
        }
    };
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerEventBus(JSONObject jsonObject) {
        if (null != jsonObject) {
            String code;
            try {
                code = jsonObject.getString("code");
                switch (code) {
                    case AppConst.BLUE_MUSIC_WRITE_SUC:
                        commandCompleted();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, null);
        ButterKnife.bind(this, view);
        EventBusUtil.registerEvent(this);
        mPlayButton = (ImageView) view.findViewById(R.id.iv_play);
        mMusicTitleTextView = (TextView) view.findViewById(R.id.tv_music_title);


        mLinearLayout = (LinearLayout) view.findViewById(R.id.ll);
        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        // Create the MediaPlayer

        mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.a);
//        mMediaPlayer = new MediaPlayer();
        Log.d(TAG,
                "MediaPlayer audio session ID: "
                        + mMediaPlayer.getAudioSessionId());

        seekBar.setMax(mMediaPlayer.getDuration());
        setupVisualizerFxAndUI();
        mVisualizer.setEnabled(true);

        // 设置了均衡器就与音量大小无关拉
        mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
        mEqualizer.setEnabled(true);

        mMediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mVisualizer.setEnabled(false);
                        getActivity().getWindow().clearFlags(
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        getActivity().setVolumeControlStream(AudioManager.STREAM_SYSTEM);

                    }
                });

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer.start();

        handler.post(updateThread);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // fromUser判断是用户改变的滑块的值
                if (fromUser == true) {
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        //初始化歌曲
        DeviceDetailActivity parentActivity = (DeviceDetailActivity) getActivity();
        List<MusicModel> list = parentActivity.getData();
        playMusic(list.get(0));
        pause();

        //专辑旋转动画
        operatingAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_rotate);
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        operatingAnim.setInterpolator(linearInterpolator);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        EventBusUtil.unRegisterEvent(this);
        super.onDestroy();
    }

    @Override
    protected void lazyLoad() {
    }


    private void setupVisualizerFxAndUI() {
        mVisualizerView = new VisualizerView(getContext());
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources()
                        .getDisplayMetrics().density)));

        mVisualizerView.setProgress(13);// 设置波形的高度
        mVisualizerView.setmHeight(8);// 让水位处于最高振幅
        mLinearLayout.addView(mVisualizerView);

        final int maxCR = Visualizer.getMaxCaptureRate();
        // 实例化Visualizer，参数SessionId可以通过MediaPlayer的对象获得
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        // 设置需要转换的音乐内容长度，专业的说这就是采样,该采样值一般为2的指数倍
        mVisualizer.setCaptureSize(128);
        // 接下来就好理解了设置一个监听器来监听不断而来的所采集的数据。一共有4个参数，第一个是监听者，第二个单位是毫赫兹，表示的是采集的频率，第三个是是否采集波形，第四个是是否采集频率
        mVisualizer.setDataCaptureListener(
                // 这个回调应该采集的是波形数据
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes); // 按照波形来画图
                        if(musicPlaying()){
                            writeWaveData(bytes);
                        }
                    }
                    // 这个回调应该采集的是快速傅里叶变换有关的数据
                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] fft, int samplingRate) {
                        mVisualizerView.updateVisualizer(fft);
                    }
                }, maxCR / 2, true, true);
    }

    private void writeWaveData(byte[] data) {
        int len = data.length;
        for (int i = 0; i < len; i += SPLIT_CNT) {
            byte[] splitData = Arrays.copyOfRange(data, i, Math.min(i + SPLIT_CNT, len));
            commandQueue.add(splitData);
        }
        if (!bleProcessing) {
            processCommands();
        }
    }

    // command finished, queue the next command
    private void commandCompleted() {
        bleProcessing = false;
        processCommands();
    }

    private void processCommands() {
        if (bleProcessing) {
            return;
        }
        if (commandQueue.isEmpty()) {
            return;
        }
        if(!musicPlaying()){
            commandQueue.clear();
            return;
        }
        byte[] command = commandQueue.poll();
        if (command != null) {
            bleProcessing = true;
//            try {
//                TimeUnit.MILLISECONDS.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            write(command);
        }
    }

    private void write(byte[] data) {
        if (!BaseApplication.getBleManager().isConnected()) {
            return;
        }
        BaseApplication.getBleManager().writeDevice(
                BleConst.RX_SERVICE_UUID,
                BleConst.RX_WRITE_UUID,
                BleConst.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR,
                data,
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        EventBusUtil.postEvent(AppConst.BLUE_MUSIC_WRITE_SUC,"");
//                        Log.d(TAG, "写特征值成功: " + '\n' + Arrays.toString(characteristic.getValue()));
                    }

                    @Override
                    public void onFailure(BleException exception) {
//                        Log.e(TAG, "写读特征值失败: " + '\n' + exception.toString());
//                        bleManager.handleException(exception);
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity().isFinishing() && mMediaPlayer != null) {
            handler.removeCallbacks(updateThread);
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void playMusic(MusicModel musicModel) {
        if (mMediaPlayer == null) {
            return;
        }
        try {
            curMusicModel = musicModel;
            mMusicTitleTextView.setText(curMusicModel.getName());
            mMediaPlayer.reset();// 重置
            mMediaPlayer.setDataSource(musicModel.getPath());
            mMediaPlayer.prepare();
            play();
            seekBar.setMax(mMediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean musicPlaying(){
        boolean flag=false;
        flag=null!=mMediaPlayer&&mMediaPlayer.isPlaying();
        return  flag;
    }

    /**
     * 播放暂停
     */
    @OnClick({R.id.iv_play})
    public void onPlay() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }


    /**
     * 上一曲
     */
    @OnClick({R.id.iv_play_prev})
    public void onPlayPrevious() {
        DeviceDetailActivity parentActivity = (DeviceDetailActivity) getActivity();
        List<MusicModel> list = parentActivity.getData();
        for (int i = 0; i < list.size(); i++) {
            MusicModel musicModel = list.get(i);
            if (musicModel.getMusicId() == curMusicModel.getMusicId()) {
                if ((i - 1) < 0) {
                    curMusicModel = list.get(list.size() - 1);
                } else {
                    curMusicModel = list.get(i - 1);
                }
                playMusic(curMusicModel);
                break;
            }
        }
    }

    /**
     * 下一曲
     */
    @OnClick({R.id.iv_play_next})
    public void onPlayNext() {
        DeviceDetailActivity parentActivity = (DeviceDetailActivity) getActivity();
        List<MusicModel> list = parentActivity.getData();
        for (int i = 0; i < list.size(); i++) {
            MusicModel musicModel = list.get(i);
            if (musicModel.getMusicId() == curMusicModel.getMusicId()) {
                if (i + 1 == list.size()) {
                    curMusicModel = list.get(0);
                } else {
                    curMusicModel = list.get(i + 1);
                }
                playMusic(curMusicModel);
                break;
            }
        }
    }

    /**
     * 播放
     */
    private void play() {
        mMediaPlayer.start();
        mPlayButton.setImageResource(R.mipmap.ic_play_pressed);
        if (operatingAnim != null) {
            dvdImageView.startAnimation(operatingAnim);
        }

    }

    /**
     * 暂停
     */
    private void pause() {
        mMediaPlayer.pause();
        mPlayButton.setImageResource(R.mipmap.ic_play_normal);
        if (operatingAnim != null) {
            dvdImageView.clearAnimation();
        }
    }

}
