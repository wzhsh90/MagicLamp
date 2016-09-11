package com.rebo.bulb.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.rebo.bulb.R;
import com.rebo.bulb.model.MusicModel;

import java.util.List;

/**
 * Created by guodunsong on 16/7/9.
 */
public class MusicListAdapter extends CommonAdapter<MusicModel>{
    private Context context;
    private TextView mTitleTextView;
    List<MusicModel> mDatas;

    public MusicListAdapter(Context context, List<MusicModel> mDatas, int resource){
        super(context, mDatas, resource);
        this.context = context;
        this.mDatas = mDatas;
    }

    @Override
    public void convert(ViewHolder helper, MusicModel item) {
        View convertView = helper.getConvertView();
        mTitleTextView = (TextView)convertView.findViewById(R.id.tv_title);
        mTitleTextView.setText(item.getName());
    }

    public MusicModel getMusicModel(int position){
        return this.mDatas.get(position);
    }
}
