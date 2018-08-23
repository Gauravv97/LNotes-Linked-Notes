package com.links.gaurav.lnotes;

/**
 * Created by Gaurav on 1/3/2018.
 */
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import data.entry;

class ItemAdapter extends DragItemAdapter<entry, ItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    public boolean mIsMultiSelect=false;
    private Context mContext;
    private entry data[];
    private Dbhandler dbhandler;

    ItemAdapter(ArrayList< entry> list, int layoutId, int grabHandleId, boolean dragOnLongPress, Context context) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        mContext=context;
        dbhandler=new Dbhandler(mContext,null,null,1);
        refreshData();
        setItemList(list);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }
    public void refreshData(){
        data=new entry[dbhandler.rCount()];
        for(int i=0;i<dbhandler.rCount();i++)
            data[i]=dbhandler.getresult(i);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(mContext.getClass()==MainActivity.class){
            holder.mTextView.setText(data[holder.getAdapterPosition()].getName());
            holder.mDateTime.setText(data[holder.getAdapterPosition()].getDateTime());
            holder.mPages.setText("Pages: "+data[holder.getAdapterPosition()].get_sc().length);
            Glide.with(mContext).load(data[holder.getAdapterPosition()].get_sc()[0]).apply(RequestOptions.bitmapTransform(new CenterCrop())).into(holder.mImageView);
        }else {
            holder.mDateTime.setText(mItemList.get(position).getDateTime());
            holder.mPages.setText("Pages: "+mItemList.get(holder.getAdapterPosition()).get_sc().length);
            holder.mTextView.setText(mItemList.get(position).getName());
            Glide.with(mContext).load(mItemList.get(holder.getAdapterPosition()).get_sc()[0]).apply(RequestOptions.bitmapTransform(new CenterCrop())).into(holder.mImageView);
        }holder.itemView.setTag(mItemList.get(position));
        if(mIsMultiSelect)
        {   holder.mGrabHandle.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
            if(((MainActivity) mContext).multiSelectList.contains(holder.getAdapterPosition()))
                holder.checkBox.setChecked(true);
            else holder.checkBox.setChecked(false);
        }else {
            holder.mGrabHandle.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.GONE);
        }
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).get_id();
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        ImageView mImageView,mGrabHandle;
        TextView mTextView,mDateTime,mPages;
        CheckBox checkBox;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                itemView.findViewById(R.id.item_layout).setForeground(mContext.getDrawable(R.drawable.shadow_item));
            else itemView.findViewById(R.id.dragView_listView).setVisibility(View.VISIBLE);*/
            if(mContext.getClass()==ScanForEntries.class)
                itemView.findViewById(mGrabHandleId).setVisibility(View.GONE);

            checkBox=itemView.findViewById(R.id.multiSelectcheckBox);
            mTextView=(TextView)itemView.findViewById(R.id.text_listItem);
            mDateTime=itemView.findViewById(R.id.dateTime_listItem);
            mPages=itemView.findViewById(R.id.pages_listItem);
            mGrabHandle=itemView.findViewById(mGrabHandleId);
            mImageView = (ImageView) itemView.findViewById(R.id.imageView_listItem);

            if(!mIsMultiSelect)
                checkBox.setVisibility(View.GONE);
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            int height= Resources.getSystem().getDisplayMetrics().heightPixels;
            if(width>height)
                width=height;
            mImageView.setLayoutParams(new LinearLayout.LayoutParams(width/3,width/3));
            mImageView.setBackground(ContextCompat.getDrawable(mContext,R.drawable.border));
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);



        }

        @Override
        public void onItemClicked(View view) {
            Intent intent = new Intent(mContext, View_Entry.class);

            if(mContext.getClass()==MainActivity.class){
                if(mIsMultiSelect){
                    view.findViewById(R.id.multiSelectcheckBox).setVisibility(View.VISIBLE);
                    ((MainActivity)mContext).multi_select(getAdapterPosition());
                }else if(!((MainActivity)mContext).getmIsClicked()){
                    ((MainActivity) mContext).onChangeLayout();
                    intent.putExtra("ID", getAdapterPosition());
                    mContext.startActivity(intent);
                }
            }else{
                intent.putExtra("ID", ((entry)view.getTag()).get_id());//As the Matched images are Sorted w.r.t its matched keyp and position not matched..
                mContext.startActivity(intent);
            }
        }

        @Override
        public boolean onItemLongClicked(View view) {
            if(mContext.getClass()==MainActivity.class){
                if(!mIsMultiSelect) {
                    mIsMultiSelect = true;
                    ((MainActivity) mContext).SetMultiToolbar();
                }
                ((MainActivity) mContext).multi_select(getAdapterPosition());

            }
            return true;
        }

    }
}