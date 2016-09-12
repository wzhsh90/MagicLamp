package com.rebo.bulb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 通用 Adapter
 * @author gao_chun
 *
 * @param <T>
 */
public abstract class CommonAdapter<T> extends BaseAdapter{

    protected LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas= Lists.newArrayList();
    protected final int mItemLayoutId;

    /**
     * 初始化通用Adapter
     * @param context   上下文
     * @param itemLayoutId  子布局文件
     */
    public CommonAdapter(Context context, int itemLayoutId)
    {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mItemLayoutId = itemLayoutId;
    }

    public CommonAdapter(Context context, List<T> mDatas, int itemLayoutId)
    {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        if(null!=mDatas){
            this.mDatas = mDatas;
        }
        this.mItemLayoutId = itemLayoutId;
    }

    @Override
    public int getCount()
    {
        return mDatas.size();
    }

    @Override
    public T getItem(int position)
    {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //从ViewHolder中获取控件view，若为空则创建
        final ViewHolder viewHolder = getViewHolder(position, convertView,parent);
        convert(viewHolder, getItem(position));
        return viewHolder.getConvertView();


    }

    /**
     * 抽取出getView中间改变的部分
     * @param helper    holder缓存对象
     * @param item      Bean对象
     */
    public abstract void convert(ViewHolder helper, T item);


    /**
     * 获得ViewHolder中的view
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    private ViewHolder getViewHolder(int position, View convertView,ViewGroup parent)
    {
        return ViewHolder.get(mContext, convertView, parent, mItemLayoutId,position);
    }

}
