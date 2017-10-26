package com.lts.voicedemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lts.voicedemo.R;
import com.lts.voicedemo.base.BaseRecyclerViewHolder;
import com.lts.voicedemo.bean.Message;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created Date:  2017/9/6.
 * author: tsliu
 * email: liutangbei@gmail.com
 */

public class ChatAdapter extends RecyclerView.Adapter {

    private final int FROME = 1;
    private final int TO = 2;
    private final int EMPTY = 3;

    private Context mContext;
    private List<Message> mData;
    private final LayoutInflater mInflater;
    private boolean mShowEmptyView = false;
    private OnlongItemClickListener mOnlongItemClickListener;

    public ChatAdapter(Context context, List<Message> data) {
        this.mContext = context;
        this.mData = data == null ? new ArrayList<Message>() : data;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<Message> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void setOnLongItemClickListener(OnlongItemClickListener longItemClickListener){
        this.mOnlongItemClickListener = longItemClickListener;
    }

    public void addMoreData(List<Message> data) {
        int startPos = mData.size();
        mData.addAll(data);
        notifyItemRangeInserted(startPos, data.size());
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FROME) {
            View view = mInflater.inflate(R.layout.item_chat_message, parent, false);
            return new FromMessageViewHolder(view, mContext);
        } else if (viewType == TO) {
            View inflate = mInflater.inflate(R.layout.item_chat_to_message, parent, false);
            return new MessageViewHolder(inflate,mContext);
        } else if (viewType == EMPTY) {
            View inflate = mInflater.inflate(R.layout.item_empty_view, parent, false);

            return new BaseRecyclerViewHolder(mContext, inflate);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).bindinData(mData.get(position));
        } else if (holder instanceof FromMessageViewHolder) {
            ((FromMessageViewHolder) holder).bindinData(mData.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mShowEmptyView ? 1: mData.size();
    }



    @Override
    public int getItemViewType(int position) {
        if (mShowEmptyView) {
            return EMPTY;
        }

        if (mData.get(position).getType() != 1) {
            return TO;
        } else {
            return FROME;
        }
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Context mContext;

        MessageViewHolder(View itemView, Context context) {
            super(itemView);
            this.mView = itemView;
            this.mContext = context;
        }

        void bindinData(Message message) {

            final TextView content = (TextView) mView.findViewById(R.id.toContent);
            final String text = message.getMessage();
            content.setText(text);
            content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnlongItemClickListener != null) {
                        mOnlongItemClickListener.onLongItemClickListener(text,content);
                    }
                    return true;
                }
            });

        }
    }

    private class FromMessageViewHolder extends RecyclerView.ViewHolder {

        Context mContext;
        View mView;
        public FromMessageViewHolder(View itemView, Context context) {
            super(itemView);
            this.mContext = context;
            this.mView = itemView;
        }

        void bindinData( Message message) {
            CircleImageView fromIcon = (CircleImageView) mView.findViewById(R.id.fromicon);
            final TextView fromContent = (TextView) mView.findViewById(R.id.fromContent);
            final String text = message.getMessage();
            fromContent.setText(text);

            fromContent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnlongItemClickListener != null) {
                        mOnlongItemClickListener.onLongItemClickListener(text,fromContent);
                    }
                    return true;
                }
            });

        }
    }


}
