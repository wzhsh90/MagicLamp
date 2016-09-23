package com.rebo.bulb.fragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
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
import com.rebo.bulb.ble.BleCommand;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by guodunsong on 16/7/10.
 */
@SuppressLint("NewApi")
public class MusicFragment extends BaseFragment {

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

    @BindView(R.id.iv_dvd)
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
                    case AppConst.BLUE_CONN133:
                        bleProcessing = false;
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
        releaseMedia();
        bleProcessing = false;
        ButterKnife.bind(this, view);
        EventBusUtil.registerEvent(this);
        mPlayButton = ButterKnife.findById(view, R.id.iv_play);
        mMusicTitleTextView = ButterKnife.findById(view, R.id.tv_music_title);
        mLinearLayout = ButterKnife.findById(view, R.id.ll);
        seekBar = ButterKnife.findById(view, R.id.seekbar);
        setWindow();
        initMediaPlayer();
        setWaveUI();
        // 设置了均衡器就与音量大小无关拉
        setEqualizer();
        handler.post(updateThread);
        setSeekBarListener();
        spinAnimation();
        return view;
    }

    private void setWindow() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private void setEqualizer() {
        mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
        mEqualizer.setEnabled(true);
    }

    private void setVisualizerEnable(boolean flag) {
        if (null != mVisualizer) {
            mVisualizer.setEnabled(flag);
        }
    }

    private void releaseMedia() {
        if (null != mMediaPlayer) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
    }

    private void initMediaPlayer() {
        try{
            DeviceDetailActivity parentActivity = (DeviceDetailActivity) getActivity();
            List<MusicModel> list = parentActivity.getData();
            if (!list.isEmpty()) {
                curMusicModel=list.get(0);
                mMusicTitleTextView.setText(curMusicModel.getName());
                mMediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(curMusicModel.getPath()));
            } else {
                mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.a);
            }
            mMediaPlayer
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            setVisualizerEnable(false);
                            getActivity().getWindow().clearFlags(
                                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            getActivity().setVolumeControlStream(AudioManager.STREAM_SYSTEM);

                        }
                    });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d(TAG, "OnError - Error code: " + what + " Extra code: " + extra);
                    switch (what) {
                        case -1004:
                            Log.d(TAG, "MEDIA_ERROR_IO");
                            break;
                        case -1007:
                            Log.d(TAG, "MEDIA_ERROR_MALFORMED");
                            break;
                        case 200:
                            Log.d(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                            break;
                        case 100:
                            Log.d(TAG, "MEDIA_ERROR_SERVER_DIED");
                            break;
                        case -110:
                            Log.d(TAG, "MEDIA_ERROR_TIMED_OUT");
                            break;
                        case 1:
                            Log.d(TAG, "MEDIA_ERROR_UNKNOWN");
                            break;
                        case -1010:
                            Log.d(TAG, "MEDIA_ERROR_UNSUPPORTED");
                            break;
                    }
                    switch (extra) {
                        case 800:
                            Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING");
                            break;
                        case 702:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_END");
                            break;
                        case 701:
                            Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE");
                            break;
                        case 802:
                            Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE");
                            break;
                        case 801:
                            Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE");
                            break;
                        case 1:
                            Log.d(TAG, "MEDIA_INFO_UNKNOWN");
                            break;
                        case 3:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START");
                            break;
                        case 700:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING");
                            break;
                    }
                    return false;
                }
            });
            seekBar.setMax(mMediaPlayer.getDuration());
            Log.d(TAG, "MediaPlayer audio session ID: " + mMediaPlayer.getAudioSessionId());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setSeekBarListener() {
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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    private void spinAnimation() {
        //专辑旋转动画
        operatingAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_rotate);
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        operatingAnim.setInterpolator(linearInterpolator);
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
        mVisualizerView.stop();
        handler.removeCallbacks(updateThread);
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
            mMediaPlayer.stop();
        }

        super.onDestroy();
    }

    @Override
    protected void lazyLoad() {
    }

    public void setWaveUI() {
        setupVisualizerFxAndUI();
        setVisualizerEnable(true);
    }

    private void setupVisualizerFxAndUI() {
        if (null != mVisualizerView) {
            return;
        }
        mVisualizerView = new VisualizerView(getContext());
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources()
                        .getDisplayMetrics().density)));

        mVisualizerView.setProgress(13);// 设置波形的高度
        mVisualizerView.setmHeight(8);// 让水位处于最高振幅
        mLinearLayout.addView(mVisualizerView);

        int maxCR = Visualizer.getMaxCaptureRate();
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
                        if (musicPlaying()) {
//                            System.out.println(Arrays.toString(bytes));
                            writeWaveData(bytes);
                        }
                    }

                    // 这个回调应该采集的是快速傅里叶变换有关的数据
                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] fft, int samplingRate) {
                        mVisualizerView.updateVisualizer(fft);
                        if (musicPlaying()) {

                        }
                    }
                }, maxCR, true, true);
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
        if (!musicPlaying()) {
            commandQueue.clear();
            return;
        }
        byte[] command = commandQueue.poll();
        if (command != null) {
            bleProcessing = true;
//            System.out.println(Arrays.toString(command));
            byte[] waveHighLow=getWaveHighLow(command);
            write(waveHighLow);
        }
    }

    private void write(byte[] data) {
        if (!BaseApplication.getBleManager().isConnected()) {
            return;
        }
        if(data[0]==0){
            EventBusUtil.postEvent(AppConst.BLUE_MUSIC_WRITE_SUC, "");
            return;
        }
        byte[] allData=BleCommand.getAllData(BleCommand.getHead(0, 0), BleCommand.musicStartBody(data[0]));
        BaseApplication.getBleManager().writeDevice(
                BleConst.RX_SERVICE_UUID,
                BleConst.RX_WRITE_UUID,
                BleConst.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR,
                allData,
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        EventBusUtil.postEvent(AppConst.BLUE_MUSIC_WRITE_SUC, "");
//                        Log.d(TAG, "写特征值成功: " + '\n' + Arrays.toString(characteristic.getValue()));
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        EventBusUtil.postEvent(AppConst.BLUE_MUSIC_WRITE_SUC, "");
//                        Log.e(TAG, "写读特征值失败: " + '\n' + exception.toString());
//                        bleManager.handleException(exception);
                    }
                });
    }

    private byte[] getWaveHighLow(byte[] wave) {

        byte[] highLow = new byte[2];
        highLow[0] = 0;
        highLow[1] = 0;
        int highCnt = 0;
        int lowCnt = 0;
        int direction = wave[0] > 0 ? -1 : 1;
        int wavelen = wave.length - 1;
        for (int i = 0; i < wavelen; i++) {
            if ((wave[i + 1] - wave[i]) * direction > 0) {
                direction *= -1;
                if (direction == 1) {
                    highCnt++;
                    System.out.println("(" + i + "," + wave[i] + ")" + "波峰");
                } else {
                    lowCnt++;
                    System.out.println("(" + i + "," + wave[i] + ")" + "波谷");
                }
            }
        }
        highLow[0] = Integer.valueOf(highCnt).byteValue();
        highLow[1] = Integer.valueOf(lowCnt).byteValue();

        return highLow;
    }

    @Override
    public void onPause() {
        super.onPause();
//        pause();
        if (getActivity().isFinishing() && mMediaPlayer != null) {
            handler.removeCallbacks(updateThread);
            mMediaPlayer.reset();// 重置
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void playMusic(MusicModel musicModel) {
        if (mMediaPlayer == null) {
            return;
        }
        bleProcessing = false;
        try {
            curMusicModel = musicModel;
            mMusicTitleTextView.setText(curMusicModel.getName());
            mMediaPlayer.reset();// 重置
            mMediaPlayer.setDataSource(musicModel.getPath());
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    play();
                    seekBar.setMax(mMediaPlayer.getDuration());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean musicPlaying() {
        boolean flag;
        flag = null != mMediaPlayer && mMediaPlayer.isPlaying();
        return flag;
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
    private List<MusicModel> getParentListData(){
        DeviceDetailActivity parentActivity = (DeviceDetailActivity) getActivity();
        List<MusicModel> list = parentActivity.getData();
        return list;
    }

    /**
     * 上一曲
     */
    @OnClick({R.id.iv_play_prev})
    public void onPlayPrevious() {
        List<MusicModel> list = getParentListData();
        int len = list.size();
        for (int i = 0; i < len; i++) {
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
        List<MusicModel> list = getParentListData();
        int len = list.size();
        for (int i = 0; i < len; i++) {
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
        setVisualizerEnable(true);
        mPlayButton.setImageResource(R.mipmap.ic_play_pressed);
        if (operatingAnim != null) {
            dvdImageView.startAnimation(operatingAnim);
        }

    }

    /**
     * 暂停
     */
    private void pause() {
        if (null == mMediaPlayer) {
            return;
        }
        mMediaPlayer.pause();
        mPlayButton.setImageResource(R.mipmap.ic_play_normal);
        if (operatingAnim != null) {
            dvdImageView.clearAnimation();
        }
    }

}
