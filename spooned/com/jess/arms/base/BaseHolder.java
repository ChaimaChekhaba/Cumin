package com.jess.arms.base;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.jess.arms.utils.ThirdViewUtil;


public abstract class BaseHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected BaseHolder.OnViewClickListener mOnViewClickListener = null;

    protected final String TAG = this.getClass().getSimpleName();

    public BaseHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        if ((ThirdViewUtil.USE_AUTOLAYOUT) == 1)
            com.zhy.autolayout.utils.AutoUtils.autoSize(itemView);

        ThirdViewUtil.bindTarget(this, itemView);
    }

    public abstract void setData(T data, int position);

    protected void onRelease() {
    }

    @Override
    public void onClick(View view) {
        if ((mOnViewClickListener) != null) {
            mOnViewClickListener.onViewClick(view, this.getPosition());
        }
    }

    public interface OnViewClickListener {
        void onViewClick(View view, int position);
    }

    public void setOnItemClickListener(BaseHolder.OnViewClickListener listener) {
        this.mOnViewClickListener = listener;
    }
}

