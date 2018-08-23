package com.links.gaurav.lnotes;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import data.entry;
import utilities.ShareFiles;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    SharedPreferences mSharedPref;
    private DrawerLayout drawer;
    private ArrayList<entry> mItemArray;
    private DragListView mDragListView;
    private Dbhandler dbhandler;
    private Boolean mIsClicked=false,mIsSwiped=false,mIsChangeRequired=false;
    private   static final int PERMISSIONS_MULTIPLE_REQUEST = 123;
    private static int Inserting_entry=1001;
    private boolean search=false,insert=false;
    private ItemAdapter listAdapter;
    public ArrayList<Integer> multiSelectList;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    IntentFilter statusIntentFilter = new IntentFilter(
            "ImageProcessingCompleteBrodcast");
    MainActivity.MyResponseReceiver responseReceiver =
            new MainActivity.MyResponseReceiver();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPref.getBoolean("isFirstRun",true)) {
            AgreeTerms();
        }
        dbhandler=new Dbhandler(this,null,null,1);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search=false;
                insert=true;
               check_permissions();
            }
        });

        drawer= (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle= new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        FloatingActionButton scanButton = (FloatingActionButton) findViewById(R.id.fabScan);
        scanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Intent code for open new activity through intent.
                search=true;
                insert=false;
                check_permissions();

            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, statusIntentFilter );
        init_List();
    }

    void init_List(){
        mDragListView = (DragListView) findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                mIsSwiped=true;
            }
            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    dbhandler.move_entry(fromPosition,toPosition);

                }
                    mIsSwiped=false;
                    mIsChangeRequired=false;
                    listAdapter.refreshData();
                    listAdapter.notifyDataSetChanged();
            }
        });

        setupListRecyclerView();
    }
    private static class MyDragItem extends DragItem {

        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            Bitmap bitmap=getBitmapFromView(clickedView);
            ((ImageView)dragView.findViewById(R.id.dragView_listView)).setVisibility(View.VISIBLE);
            ((ImageView)dragView.findViewById(R.id.dragView_listView)).setImageBitmap(bitmap);
            dragView.findViewById(R.id.item_layout).setVisibility(View.GONE);
            ((ImageView)dragView.findViewById(R.id.dragView_listView)).setColorFilter(dragView.getResources().getColor(R.color.list_item_background), android.graphics.PorterDuff.Mode.MULTIPLY);
            dragView.findViewById(R.id.dragView_listView).setBackgroundColor(dragView.getResources().getColor(R.color.list_item_background));
        }
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(this));
        mItemArray = new ArrayList<>();
        for (int i = 0; i < dbhandler.rCount(); i++) {
            mItemArray.add(dbhandler.getresult(i));
        }
        listAdapter= new ItemAdapter(mItemArray, R.layout.list_item_, R.id.dragHandle_listView, false,this);
        mDragListView.setAdapter(listAdapter, false);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(new MainActivity.MyDragItem(this, R.layout.list_item_));
        CheckIfListisEmpty();
    }
    private void CheckIfListisEmpty(){
        if(mDragListView.getAdapter().getItemCount()>0){
            mDragListView.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.empty_view)).setVisibility(View.GONE);
        }else {
            mDragListView.setVisibility(View.GONE);
            ((TextView)findViewById(R.id.empty_view)).setVisibility(View.VISIBLE);
        }
    }
    public void multi_select(int position) {
            if(multiSelectList.contains(position)){
                multiSelectList.remove(Integer.valueOf(position));
                getSupportActionBar().setTitle(multiSelectList.size()+"/"+dbhandler.rCount());
            }else {
                multiSelectList.add(position);
                getSupportActionBar().setTitle(multiSelectList.size() + "/" + dbhandler.rCount());
            }
            listAdapter.notifyDataSetChanged();
    }
    public void SetMultiToolbar(){
        multiSelectList=new ArrayList<>();
        mDragListView.setDragEnabled(false);
        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetToolbar();
            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(0+"/"+dbhandler.rCount());
        invalidateOptionsMenu();
        findViewById(R.id.fab).setVisibility(View.GONE);
        findViewById(R.id.fabScan).setVisibility(View.GONE);
    }
    public void resetToolbar(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDragListView.setDragEnabled(true);
        multiSelectList.clear();
        listAdapter.mIsMultiSelect=false;
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(null);
        toggle.setDrawerIndicatorEnabled(true);
        drawer= (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle= new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        invalidateOptionsMenu();
        findViewById(R.id.fab).setVisibility(View.VISIBLE);
        findViewById(R.id.fabScan).setVisibility(View.VISIBLE);
        listAdapter.notifyDataSetChanged();
    }



    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, statusIntentFilter );
        mIsClicked=false;
        int tot=dbhandler.rCount();
        if(!(listAdapter.getItemCount()==tot)) {
            setupListRecyclerView();
        }else {
            listAdapter.refreshData();
            listAdapter.notifyDataSetChanged();
        }
       CheckIfListisEmpty();
    }
    public Boolean getmIsClicked(){
        return mIsClicked;
    }
    public void onChangeLayout(){
        mIsClicked=true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(listAdapter.mIsMultiSelect) {
            resetToolbar();
        } else
            {
                super.onBackPressed();
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if(listAdapter.mIsMultiSelect){
            getMenuInflater().inflate(R.menu.multi_select,menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            if(multiSelectList.toArray().length>0){
                AlertDialog.Builder statsOptInDialog = new AlertDialog.Builder(this);
                statsOptInDialog.setCancelable(false);
                statsOptInDialog.setTitle("Confirm");
                if(multiSelectList.toArray().length==1)
                    statsOptInDialog.setMessage("Are you sure you want to delete "+multiSelectList.toArray().length+" item?");
                else statsOptInDialog.setMessage("Are you sure you want to delete "+multiSelectList.toArray().length+" items?");
                statsOptInDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer pos[]=new Integer[multiSelectList.toArray().length];
                        multiSelectList.toArray(pos);
                        Arrays.sort(pos, new Comparator<Integer>() {
                            @Override
                            public int compare(Integer t1, Integer t2) {
                                return t2.compareTo(t1);
                            }
                        });
                        for (int i=0;i<pos.length;i++){
                            dbhandler.DeleteEntry(pos[i]);
                        }
                        setupListRecyclerView();
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
                statsOptInDialog.create().show();
            }

            return true;
        }
        if(id==R.id.action_share){
            {

                CharSequence Choice[] = new CharSequence[] {"As Images", "As PDF"};

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Share as");
                builder.setItems(Choice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer pos[]=new Integer[multiSelectList.toArray().length];
                        multiSelectList.toArray(pos);
                        // the user clicked on colors[which]
                        if(which==0){
                            ShareFiles.share(MainActivity.this,pos, ShareFiles.ShareMode.images);
                        }else if(which==1){
                            ShareFiles.share(MainActivity.this,pos, ShareFiles.ShareMode.pdf);
                        }
                    }
                });
                builder.show();


                //test Feature pdf creator...
                /*String ppp=null;
                try {File image = File.createTempFile(
                        "Orig",  *//* prefix *//*
                        ".pdf",         *//* suffix *//*
                        getExternalFilesDir("Pictures")      *//* directory *//*
                );
                    ppp=image.getAbsolutePath();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }

                PrintAttributes printAttributes=new PrintAttributes.Builder().
                        setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                        setMediaSize(PrintAttributes.MediaSize.ISO_A4).
                        setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                        build();

                PrintedPdfDocument document=new PrintedPdfDocument(MainActivity.this,printAttributes);
                PdfDocument.Page page=document.startPage(0);
                Bitmap bitmap= BitmapFactory.decodeFile(e[0].get_sc()[0]);
                *//*ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 20, out);
                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                float originalWidth = bitmap.getWidth();
                float originalHeight = bitmap.getHeight();
                float scale = page.getCanvas().getWidth()  / originalWidth;
                float xTranslation = 0.0f;
                float yTranslation = (page.getCanvas().getHeight()  - originalHeight * scale) / 2.0f;
                Matrix transformation = new Matrix();
                transformation.postTranslate(xTranslation, yTranslation);
                transformation.preScale(scale, scale);

                Paint paint = new Paint();
                paint.setFilterBitmap(true);
                page.getCanvas().drawBitmap(bitmap, transformation, paint);*//*
                bitmap=Bitmap.createScaledBitmap(bitmap,page.getCanvas().getWidth(),page.getCanvas().getHeight(),false);
                page.getCanvas().drawBitmap(bitmap,0,0,null);
                document.finishPage(page);
                try {
                    document.writeTo(new FileOutputStream(new File(ppp)));
                }catch (Exception ex){

                }
*/



            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Settings) {
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void AgreeTerms() {
        AlertDialog.Builder statsOptInDialog = new AlertDialog.Builder(this);
        statsOptInDialog.setCancelable(false);
        statsOptInDialog.setTitle("Welcome");
        statsOptInDialog.setMessage("This thing right here will Be re-Written with all license and T&C .\n if U press agree u can use this app.\n ");
        statsOptInDialog.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSharedPref.edit().putBoolean("isFirstRun",false).commit();
                dialog.dismiss();
            }
        });
        statsOptInDialog.setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MainActivity.this.finish();
            }
        });
        /*statsOptInDialog.setNeutralButton("Maybe,Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });*/
        statsOptInDialog.create().show();
    }
    public Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }


        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Inserting_entry&&resultCode==RESULT_OK){

        }
    }
    private class MyResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(((int)intent.getExtras().get("ID"))>=0){
                if(mIsSwiped==true){
                    mIsChangeRequired=true;
                }else {
                    listAdapter.refreshData();
                    listAdapter.notifyDataSetChanged();
                }

            }
        }
    }

    private static Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private void check_permissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askPermission();

        } else {
            if(search&&!mIsClicked){
                mIsClicked=true;
                Intent intent = new Intent(MainActivity.this, ScanForEntries.class);
                startActivity(intent);
            }else if(insert&&!mIsClicked){
                mIsClicked=true;
                Intent intent = new Intent(MainActivity.this, InsertNewEntry.class);
                startActivity(intent);
            }

            // write your logic here
        }
    }
    private void askPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED|| ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.CAMERA)) {

                Snackbar.make(this.findViewById(android.R.id.content),
                        "Please Grant Permissions to store images",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    requestPermissions(
                                            new String[]{Manifest.permission
                                                    .READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                            PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        }).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requestPermissions(
                            new String[]{Manifest.permission
                                    .READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            PERMISSIONS_MULTIPLE_REQUEST);
            }
        } else {
            if(search&&!mIsClicked){
                mIsClicked=true;
                Intent intent = new Intent(MainActivity.this, ScanForEntries.class);
                startActivity(intent);
            }else if(insert&&!mIsClicked){
                mIsClicked=true;
                Intent intent = new Intent(MainActivity.this, InsertNewEntry.class);
                startActivityForResult(intent,Inserting_entry);
            }
            // write your logic code if permission already granted
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(cameraPermission && readExternalFile)
                    {
                        if(search&&!mIsClicked){
                            mIsClicked=true;
                            Intent intent = new Intent(MainActivity.this, ScanForEntries.class);
                            startActivity(intent);
                        }else if(insert&&!mIsClicked){
                            mIsClicked=true;
                            Intent intent = new Intent(MainActivity.this, InsertNewEntry.class);
                            startActivityForResult(intent,Inserting_entry);
                        }
                        // write your logic here
                    } else {
                        Snackbar.make(this.findViewById(android.R.id.content),
                                "Please Grant Permissions to enable Camera",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                            requestPermissions(
                                                    new String[]{Manifest.permission
                                                            .READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                                    PERMISSIONS_MULTIPLE_REQUEST);
                                    }
                                }).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.responseReceiver);
    }
}
