package com.links.gaurav.lnotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.draglistview.DragListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import data.ImageProperties;
import data.entry;
import utilities.ShareFiles;

/**
 * Created by Gaurav on 2/10/2018.
 */

public class View_Entry extends AppCompatActivity {
    Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    private DragListView mDragListView;
    private GridItemAdapter mDragAdapter;
    private ArrayList<Pair<Integer,String>> mItemList;

    private ArrayList<Integer> multiSelectList;
    private int CurrentMarkerAddingPosition;
    private Dbhandler dbhandler;
    private entry CurrentEntry;
    private int ID;
    FragmentManager fragmentManager;
    SlideShowDialogFragment newFragment;
    private int Request_for_Points=111,Request_for_images=12321;
    private int spanCount=2;

    IntentFilter statusIntentFilter = new IntentFilter(
            "ImageProcessingCompleteBrodcast");
    View_Entry.MyResponseReceiver responseReceiver =
            new View_Entry.MyResponseReceiver();

    private boolean mIsRearrangeOn=false,mIsDragging=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_entry);
        mDragListView=findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setPadding(0,12,0,0);
        ID=getIntent().getExtras().getInt("ID");
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                mIsDragging=true;
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    dbhandler.move_image(ID,fromPosition,toPosition);
                }
                CurrentEntry=dbhandler.getresult(ID);
                mIsDragging=false;
                mDragAdapter.notifyDataSetChanged();
            }
        });
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dbhandler=new Dbhandler(this,null,null,1);

        fragmentManager=getSupportFragmentManager();
        newFragment = SlideShowDialogFragment.newInstance();

        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, statusIntentFilter );
        SetUpGridView();
        if(getIntent().getIntExtra("Serial",-1)!=-1){
            StartFragment(getIntent().getIntExtra("Serial",-1));
        }
        FloatingActionButton fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(View_Entry.this,InsertNewEntry.class);
                intent.putExtra("ID",ID);
                startActivityForResult(intent,Request_for_images);
            }
        });

    }
    private void SetUpGridView(){

        spanCount=2;
        if(Resources.getSystem().getDisplayMetrics().widthPixels>Resources.getSystem().getDisplayMetrics().heightPixels)
        {
            spanCount=3;
        }


        mDragListView.getRecyclerView().setClipToPadding(false);
        mDragListView.getRecyclerView().setPadding(8,0,8,8);
        mDragListView.setLayoutManager(new GridLayoutManager(this,spanCount));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(View_Entry.this, R.dimen.item_offset);
        mDragListView.getRecyclerView().addItemDecoration(itemDecoration);
        mDragListView.setCanDragHorizontally(true);
        mDragListView.setDragEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        CurrentEntry=dbhandler.getresult(getIntent().getExtras().getInt("ID"));
        getSupportActionBar().setTitle(CurrentEntry.getName());
        mItemList=new ArrayList<>(CurrentEntry.get_sc().length);
        for (int i=0;i<CurrentEntry.get_sc().length;i++){
            mItemList.add(new Pair<Integer, String>(i,CurrentEntry.get_sc()[i]));
        }
        mDragAdapter=new GridItemAdapter(mItemList,R.layout.grid_item,R.id.grid_item_layout,true,View_Entry.this);
        mDragListView.setAdapter(mDragAdapter,false);
    }
    private void CheckIfListisEmpty(){
        if(mDragListView.getAdapter().getItemCount()>0){
            mDragListView.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.empty_view)).setVisibility(View.GONE);
        }else {
            dbhandler.DeleteEntry(ID);
            View_Entry.this.finish();
        }
    }
    public void multi_select(int position) {
        if(multiSelectList.contains(position)){
            multiSelectList.remove(Integer.valueOf(position));
        }else {
            multiSelectList.add(position);
        }
        invalidateOptionsMenu();
        getSupportActionBar().setTitle(multiSelectList.size() + "/" + CurrentEntry.get_sc().length);
        mDragAdapter.notifyDataSetChanged();
    }
    public void SetMultiToolbar(){
        multiSelectList=new ArrayList<>();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(0+"/"+CurrentEntry.get_sc().length);
        invalidateOptionsMenu();
    }
    public void resetToolbar(){
        mDragAdapter.mIsMultiSelect=false;
        mDragListView.setDragEnabled(false);
        mIsRearrangeOn=false;
        getSupportActionBar().setTitle(CurrentEntry.getName());
        invalidateOptionsMenu();
        mIsRearrangeOn=false;
        mDragAdapter.notifyDataSetChanged();
    }
    private void UpdateList(){
        CurrentEntry=dbhandler.getresult(getIntent().getExtras().getInt("ID"));
        mItemList=new ArrayList<>(CurrentEntry.get_sc().length);
        for (int i=0;i<CurrentEntry.get_sc().length;i++){
            mItemList.add(new Pair<Integer, String>(i,CurrentEntry.get_sc()[i]));
        }
        mDragAdapter=new GridItemAdapter(mItemList,R.layout.grid_item,R.id.grid_item_layout,true,View_Entry.this);
        mDragListView.setAdapter(mDragAdapter,false);
        getSupportActionBar().setTitle(CurrentEntry.getName());
        CheckIfListisEmpty();
    }
    void AddMarker(int x){
        CurrentMarkerAddingPosition=x;
        Intent intent=new Intent(View_Entry.this,OpenCamera.class);
        intent.putExtra("option",OpenCamera.Req_for_Marker);
        startActivityForResult(intent,OpenCamera.Req_for_Marker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!(newFragment!=null &&  newFragment.getDialog()!=null
                && newFragment.getDialog().isShowing())) {
            if (mIsRearrangeOn) {
                menu.clear();

            } else {
                getMenuInflater().inflate(R.menu.multi_grid, menu);
                if (mDragAdapter.mIsMultiSelect) {
                    menu.findItem(R.id.action_rearrange).setVisible(false);
                    menu.findItem(R.id.action_rename).setVisible(false);
                    if(multiSelectList.size()==1)
                        menu.findItem(R.id.action_addMarker).setVisible(true);
                    else menu.findItem(R.id.action_addMarker).setVisible(false);
                } else {
                    menu.findItem(R.id.action_addMarker).setVisible(false);
                    menu.findItem(R.id.action_rearrange).setVisible(true);
                    menu.findItem(R.id.action_rename).setVisible(true);

                }
            }
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_rename){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter new Name");

            final EditText input = new EditText(View_Entry.this);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!input.getText().toString().equals("")) {
                        dbhandler.renameEntry(ID, input.getText().toString());
                        CurrentEntry = dbhandler.getresult(ID);
                        resetToolbar();
                    }else Toast.makeText(View_Entry.this,"Enter A Valid Name",Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            return true;

        }
        if(id==R.id.action_rearrange){
            getSupportActionBar().setTitle("Re-Arrange by long pressing item");
            mIsRearrangeOn=true;
            mDragListView.setDragEnabled(true);
            invalidateOptionsMenu();
            return true;
        }
        if(id==R.id.action_share){
            CharSequence Choice[] = new CharSequence[] {"As Images", "As PDF"};

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(View_Entry.this);
            builder.setTitle("Share as");
            builder.setItems(Choice, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // the user clicked on colors[which]
                    if(which==0){
                        if(mDragAdapter.mIsMultiSelect){
                            Integer pos[]=new Integer[multiSelectList.toArray().length];
                            multiSelectList.toArray(pos);
                            ShareFiles.share(View_Entry.this,ID,pos, ShareFiles.ShareMode.images);
                        }else {Integer posi[]={ID};
                            ShareFiles.share(View_Entry.this,posi, ShareFiles.ShareMode.images);
                        }
                    }else if(which==1){
                        if(mDragAdapter.mIsMultiSelect){
                            Integer pos[]=new Integer[multiSelectList.toArray().length];
                            multiSelectList.toArray(pos);
                            ShareFiles.share(View_Entry.this,ID,pos, ShareFiles.ShareMode.pdf);
                        }else {Integer posi[]={ID};
                            ShareFiles.share(View_Entry.this,posi, ShareFiles.ShareMode.pdf);
                        }

                    }
                }
            });
            builder.show();


            return true;
        }
        if(id==R.id.action_delete){

            AlertDialog.Builder statsOptInDialog = new AlertDialog.Builder(this);
            statsOptInDialog.setCancelable(false);
            statsOptInDialog.setTitle("Confirm");
            statsOptInDialog.setMessage("Are you sure you want to delete ?");
            statsOptInDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(mDragAdapter.mIsMultiSelect){
                        Integer pos[];
                        pos=new Integer[multiSelectList.toArray().length];
                        multiSelectList.toArray(pos);
                        Arrays.sort(pos, new Comparator<Integer>() {
                            @Override
                            public int compare(Integer t1, Integer t2) {
                                return t2.compareTo(t1);
                            }
                        });
                        for (int i:pos){
                            dbhandler.DeleteImage(ID,i);
                            CurrentEntry=dbhandler.getresult(ID);
                            mDragAdapter.removeItem(i);
                            mDragAdapter.notifyDataSetChanged();
                        }
                        if(mDragAdapter.getItemCount()==0)
                        {
                            dbhandler.DeleteEntry(ID);
                            View_Entry.this.finish();
                        }
                    }else {
                        dbhandler.DeleteEntry(ID);
                        View_Entry.this.finish();
                    }

                    resetToolbar();
                    dialog.dismiss();
                }
            });
            statsOptInDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            if(!mDragAdapter.mIsMultiSelect||!(multiSelectList!=null&&multiSelectList.size()==0))
            { statsOptInDialog.create().show();
            }
            return true;
        }
        if(id==R.id.action_addMarker){
            AddMarker(multiSelectList.get(0));
            return true;
        }
        if(id==R.id.action_openPdf){
            if(mDragAdapter.mIsMultiSelect){
                if(multiSelectList.size()>0) {
                    Integer pos[];
                    pos = new Integer[multiSelectList.toArray().length];
                    multiSelectList.toArray(pos);
                    CurrentEntry = dbhandler.getresult(ID);
                    String[] path = new String[pos.length];
                    for (int i = 0; i < pos.length; i++) {
                        path[i] = CurrentEntry.get_sc()[pos[i]];
                    }
                    ShareFiles.openPDF(View_Entry.this, path);
                }
            }else ShareFiles.openPDF(View_Entry.this,dbhandler.getresult(ID).get_sc());

        }

        return super.onOptionsItemSelected(item);
    }

    private class GridItemAdapter extends DragItemAdapter<Pair<Integer,String>,GridItemAdapter.ViewHolder> {
        private int mLayoutId;
        private int mGrabHandleId;
        private boolean mDragOnLongPress;
        private int viewHeight;
        public boolean mIsMultiSelect=false;
        private Context mContext;
        GridItemAdapter(ArrayList<Pair<Integer,String>> list, int layoutId, int grabHandleId, boolean dragOnLongPress, Context context){
            mContext=context;
            mLayoutId = layoutId;
            mGrabHandleId = grabHandleId;
            mDragOnLongPress = dragOnLongPress;
            mContext=context;
            setItemList(list);
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
            if(parent.getMeasuredHeight()>parent.getMeasuredWidth()) {
                viewHeight =(int)((parent.getMeasuredWidth()/spanCount)*4*1.0/3);
            }
            else  viewHeight =(int)((parent.getMeasuredWidth()/spanCount)*4*1.0/3);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);

            holder.mFrameLayout.getLayoutParams().height=viewHeight;
            holder.mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if(mIsMultiSelect)
            {
                holder.mCheckBox.setVisibility(View.VISIBLE);
                if(multiSelectList.contains(position))
                    holder.mCheckBox.setChecked(true);
                else holder.mCheckBox.setChecked(false);
            }else {
                holder.mCheckBox.setVisibility(View.GONE);
            }
            holder.mTextView.setText(""+(position+1));
            holder.mTextView.setWidth(holder.mFrameLayout.getWidth());
            Glide.with(mContext).load(CurrentEntry.get_sc()[position]).apply(RequestOptions.bitmapTransform(new com.bumptech.glide.load.resource.bitmap.CenterCrop() )).into(holder.mImageView);

        }

        @Override
        public long getUniqueItemId(int position) {
            return getItemList().get(position).first;
        }

        class ViewHolder extends DragItemAdapter.ViewHolder {
            ImageView mImageView;
            TextView mTextView;
            FrameLayout mFrameLayout;
            CheckBox mCheckBox;
            ViewHolder(final View itemView) {
                super(itemView,  mGrabHandleId, mDragOnLongPress);
                mFrameLayout=(FrameLayout)itemView.findViewById(R.id.grid_item_layout);
                mImageView=(ImageView)itemView.findViewById(R.id.grid_item_imageView);
                mTextView=(TextView)itemView.findViewById(R.id.grid_item_textView);
                mCheckBox=(CheckBox)itemView.findViewById(R.id.grid_item_checkbox);
                mCheckBox.setVisibility(View.GONE);
            }

            @Override
            public void onItemClicked(View view) {

                //start fragment from its position
                if(mIsMultiSelect){
                    mCheckBox.setVisibility(View.VISIBLE);
                    multi_select(getAdapterPosition());
                }else{
                    StartFragment(getAdapterPosition());
                    //call fragment
                }

            }

            @Override
            public boolean onItemLongClicked(View view) {
                //start multiselect toolbar function
                super.onItemLongClicked(view);
                if(!mIsMultiSelect) {
                    mIsMultiSelect = true;
                    SetMultiToolbar();
                }
                multi_select(getAdapterPosition());
                return true;
            }
        }
    }
    private class MyResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!mIsDragging) {
                CurrentEntry=dbhandler.getresult(ID);
                mDragAdapter.notifyDataSetChanged();
                if(newFragment!=null &&  newFragment.getDialog()!=null
                        && newFragment.getDialog().isShowing())
                    View_Entry.this.newFragment.Update(dbhandler.getresult(ID).get_sc());
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(mIsRearrangeOn||mDragAdapter.mIsMultiSelect)
            resetToolbar();
        else onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mIsRearrangeOn||mDragAdapter.mIsMultiSelect)
            resetToolbar();
        else super.onBackPressed();
    }



    public void StartFragment(int position){
        try {
            newFragment.onDestroy();
        }catch (Exception e){
        }
        //getSupportActionBar().hide();
        newFragment=SlideShowDialogFragment.newInstance();
        FragmentTransaction ft=fragmentManager.beginTransaction();
        Bundle bundle=new Bundle();
        entry e= dbhandler.getresult(ID);
        bundle.putSerializable("path",e.get_sc());
        bundle.putInt("position",position);
        bundle.putInt("total",e.get_sc().length);
        newFragment.setArguments(bundle);
        newFragment.onCreate(bundle);
        newFragment.show(ft, "slideshow");
    }
    public void Modify_Image(int position){
        Intent intent=new Intent(this,Modify.class);
        entry e= dbhandler.getresult(ID);
        if(e.get_im()[position]!=null)
            intent.putExtra("path",e.get_im()[position]);
        startActivityForResult(intent,Request_for_Points);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(RESULT_OK==resultCode&&requestCode==Request_for_Points){
            ImageProperties imageProperties[]=new ImageProperties[]{(ImageProperties)data.getExtras().get("imageProperty")};
            //Start your processing
            Intent intent=new Intent(this,ImageProcessingService.class);
            intent.putExtra("ImageProperties",imageProperties);
            startService(intent);
            return;
        }
        if(RESULT_OK==resultCode&&requestCode==OpenCamera.Req_for_Marker) {
            final Context context=getBaseContext();
            Intent intent=new Intent(context,MarkerProcessingService.class);
            intent.putExtra("Marker",data.getStringExtra("path"));
            intent.putExtra("Image",dbhandler.getresult(ID).get_im()[CurrentMarkerAddingPosition]);
            startService(intent);
            resetToolbar();
            return;
        }
        if(RESULT_OK==resultCode&&requestCode==Request_for_images){
            CurrentEntry=dbhandler.getresult(ID);
            mItemList=new ArrayList<>(CurrentEntry.get_sc().length);
            for (int i=0;i<CurrentEntry.get_sc().length;i++){
                mItemList.add(new Pair<Integer, String>(i,CurrentEntry.get_sc()[i]));
            }
            mDragAdapter.setItemList(mItemList);
            mDragAdapter.notifyDataSetChanged();
        }


    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.responseReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, statusIntentFilter );
        CurrentEntry=dbhandler.getresult(ID);
        mDragAdapter.notifyDataSetChanged();
        if(newFragment!=null &&  newFragment.getDialog()!=null
                && newFragment.getDialog().isShowing())
            View_Entry.this.newFragment.Update(dbhandler.getresult(ID).get_sc());
    }
    void startOCR(int position){
        Intent intent=new Intent(View_Entry.this,OCRReader.class);
        intent.putExtra("path",dbhandler.getresult(ID).get_im()[position]);
        startActivity(intent);
    }
    class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(Context context,int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

}
