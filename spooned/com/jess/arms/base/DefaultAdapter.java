package com.jess.arms.base;


import RecyclerView.ViewHolder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;


public abstract class DefaultAdapter<T> extends RecyclerView.Adapter<BaseHolder<T>> {
    protected List<T> mInfos;

    protected DefaultAdapter.OnRecyclerViewItemClickListener mOnItemClickListener = null;

    private BaseHolder<T> mHolder;

    public DefaultAdapter(List<T> infos) {
        super();
        this.mInfos = infos;
    }

    @Override
    public BaseHolder<T> onCreateViewHolder(ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
        mHolder = getHolder(view, viewType);
        mHolder.setOnItemClickListener(new BaseHolder.OnViewClickListener() {
            @Override
            public void onViewClick(View view, int position) {
                if (((mOnItemClickListener) != null) && ((mInfos.size()) > 0)) {
                    mOnItemClickListener.onItemClick(view, viewType, mInfos.get(position), position);
                }
            }
        });
        return mHolder;
    }

    @Override
    public void onBindViewHolder(BaseHolder<T> holder, int position) {
        holder.setData(mInfos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mInfos.size();
    }

    public List<T> getInfos() {
        return mInfos;
    }

    public T getItem(int position) {
        return (mInfos) == null ? null : mInfos.get(position);
    }

    public abstract BaseHolder<T> getHolder(View v, int viewType);

    public abstract int getLayoutId(int viewType);

    public static void releaseAllHolder(RecyclerView recyclerView) {
        if (recyclerView == null)
            return;

        for (int i = (recyclerView.getChildCount()) - 1; i >= 0; i--) {
            final View view = recyclerView.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if ((viewHolder != null) && (viewHolder instanceof BaseHolder)) {
                ((BaseHolder) (viewHolder)).onRelease();
            }
        }
    }

    public interface OnRecyclerViewItemClickListener<T> {
        void onItemClick(View view, int viewType, T data, int position);
    }

    public void setOnItemClickListener(DefaultAdapter.OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}

