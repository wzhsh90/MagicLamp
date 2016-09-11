package com.rebo.bulb.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.rebo.bulb.R;
import com.rebo.bulb.model.GroupModel;

import java.util.List;

/**
 * Created by guodunsong on 16/7/9.
 */
public class GroupListAdapter extends CommonAdapter<GroupModel>{

    private Context context;
    private TextView mTitleTextView;

    public GroupListAdapter(Context context, List<GroupModel> mDatas, int resource){
        super(context, mDatas, resource);
        this.context = context;
    }

    @Override
    public void convert(ViewHolder helper, GroupModel item) {
        View convertView = helper.getConvertView();
        mTitleTextView = (TextView)convertView.findViewById(R.id.tv_title);
        mTitleTextView.setText(item.getTitle());
    }
}
