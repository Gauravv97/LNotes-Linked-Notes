package com.links.gaurav.lnotes;

        import android.content.Context;
        import android.content.Intent;
        import android.content.res.Resources;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.design.widget.FloatingActionButton;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.Toolbar;
        import android.support.v4.util.Pair;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.bumptech.glide.Glide;
        import com.bumptech.glide.load.resource.bitmap.CenterCrop;
        import com.bumptech.glide.request.RequestOptions;
        import com.woxthebox.draglistview.DragItemAdapter;
        import com.woxthebox.draglistview.DragListView;

        import java.io.File;
        import java.util.ArrayList;

        import data.entry;

/**
 * Created by Gaurav on 2/3/2018.
 */

public class ScanForEntries extends AppCompatActivity {
    Dbhandler dbhandler;
    String CurrentTempPath;
    private DragListView mDragListView;

    entry AllEntries[];
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_for_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        dbhandler=new Dbhandler(this,null,null,1);
        AllEntries=new entry[dbhandler.rCount()];
        for (int i=0;i<dbhandler.rCount();i++){
            AllEntries[i]=dbhandler.getresult(i);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        FloatingActionButton floatingActionButton=(FloatingActionButton)findViewById(R.id.fabScan);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraForMarker();
            }
        });
        mDragListView=(DragListView)findViewById(R.id.drag_list_view);
        mDragListView.setDragEnabled(false);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        openCameraForMarker();


    }
    private void openCameraForMarker(){
        Intent intent=new Intent(ScanForEntries.this,OpenCamera.class);
        intent.putExtra("option",OpenCamera.Req_for_Marker);
        startActivityForResult(intent,OpenCamera.Req_for_Marker);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==OpenCamera.Req_for_Marker&&resultCode==RESULT_OK) {
            CurrentTempPath = data.getStringExtra("path");
            (new MatchingTask()).execute();
            //new Matchr().compare(AllEntries, dbhandler.rCount(), CurrentTempPath, ScanForEntries.this);
        }
    }
    private class MatchingTask extends AsyncTask<Void,Void,Void>
    {
        Pair<Pair<Integer, Integer>,Integer> Matches[];
        Matchr matcher=new Matchr();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Matches = matcher.compare(AllEntries, dbhandler.rCount(), CurrentTempPath, ScanForEntries.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            File file=new File(CurrentTempPath);
            file.delete();
            if(Matches!=null&&Matches.length>0){
                mDragListView.setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.empty_view)).setVisibility(View.GONE);
                mDragListView.setLayoutManager(new LinearLayoutManager(ScanForEntries.this));
                ArrayList<Pair<Pair<Integer, Integer>,Integer>> list =new ArrayList<>(5);
                for (Pair<Pair<Integer, Integer>,Integer> pair:Matches)
                    list.add(pair);

                MatchItemAdapter listAdapter =
                        new MatchItemAdapter(list, R.layout.list_item_, R.id.dragHandle_listView, false,ScanForEntries.this);
                mDragListView.setAdapter(listAdapter, true);
            }
            else {
                mDragListView.setVisibility(View.GONE);
                ((TextView)findViewById(R.id.empty_view)).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    class MatchItemAdapter extends DragItemAdapter<Pair<Pair<Integer, Integer>,Integer>, MatchItemAdapter.MatchViewHolder> {

        private int mLayoutId;
        private int mGrabHandleId;
        private boolean mDragOnLongPress;
        public boolean mIsMultiSelect=false;
        private Context mContext;
        private entry data[];

        MatchItemAdapter(ArrayList< Pair<Pair<Integer, Integer>,Integer>> list, int layoutId, int grabHandleId, boolean dragOnLongPress, Context context) {
            mLayoutId = layoutId;
            mGrabHandleId = grabHandleId;
            mDragOnLongPress = dragOnLongPress;
            mContext=ScanForEntries.this;
            refreshData();
            setItemList(list);
        }
        @Override
        public MatchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
            return new MatchViewHolder(view);
        }
        public void refreshData(){
            Dbhandler dbhandler=new Dbhandler(ScanForEntries.this,null,null,1);
            data=new entry[dbhandler.rCount()];
            for(int i=0;i<dbhandler.rCount();i++)
                data[i]=dbhandler.getresult(i);
        }
        @Override
        public void onBindViewHolder(MatchViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
                holder.mDateTime.setText(data[mItemList.get(position).first.first].getDateTime());
                holder.mPages.setText("Pages: "+data[mItemList.get(position).first.first].get_sc().length);
                holder.mTextView.setText(data[mItemList.get(position).first.first].getName());
                Glide.with(ScanForEntries.this).load(data[mItemList.get(position).first.first].get_sc()[mItemList.get(position).first.second]).apply(RequestOptions.bitmapTransform(new CenterCrop())).into(holder.mImageView);
            holder.itemView.setTag(mItemList.get(position));

        }

        @Override
        public long getUniqueItemId(int position) {
            return position;
        }

        class MatchViewHolder extends DragItemAdapter.ViewHolder {
            ImageView mImageView,mGrabHandle;
            TextView mTextView,mDateTime,mPages;
            MatchViewHolder(final View itemView) {
                super(itemView, mGrabHandleId, mDragOnLongPress);
                    itemView.findViewById(mGrabHandleId).setVisibility(View.GONE);

               itemView.findViewById(R.id.multiSelectcheckBox).setVisibility(View.GONE);
                mTextView=(TextView)itemView.findViewById(R.id.text_listItem);
                mDateTime=itemView.findViewById(R.id.dateTime_listItem);
                mPages=itemView.findViewById(R.id.pages_listItem);
                mGrabHandle=itemView.findViewById(mGrabHandleId);
                mImageView = (ImageView) itemView.findViewById(R.id.imageView_listItem);


                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                int height= Resources.getSystem().getDisplayMetrics().heightPixels;
                if(width>height)
                    width=height;
                mImageView.setLayoutParams(new LinearLayout.LayoutParams(width/3,width/3));
                mImageView.setBackground(ContextCompat.getDrawable(ScanForEntries.this,R.drawable.border));
                mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            @Override
            public void onItemClicked(View view) {
                Intent intent = new Intent(ScanForEntries.this, View_Entry.class);
                intent.putExtra("ID", ((Pair<Pair<Integer, Integer>,Integer>)view.getTag()).first.first);//As the Matched images are Sorted w.r.t its matched keyp and position not matched..
                intent.putExtra("Serial",((Pair<Pair<Integer, Integer>,Integer>)view.getTag()).first.second);
                ScanForEntries.this.startActivity(intent);
            }

        }
    }
}
