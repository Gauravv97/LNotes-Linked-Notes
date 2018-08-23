package com.links.gaurav.lnotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.ImageProperties;
import data.entry;
import utilities.BitmapFunctions;
import utilities.ImageProcessor;
import utilities.Shredder;

import static utilities.PointMapFunctions.MapToPoint;

/**
 * Created by Gaurav on 1/22/2018.
 */

public class InsertNewEntry extends AppCompatActivity {
    private ArrayList<ImageProperties> imageProperties;
    private EditText Name;
    Toolbar toolbar;
    ImageButton SaveButton;
    private Dbhandler dbhandler;
    private ArrayList<String> previewImages,ImagePaths;
    private FloatingActionButton fabAdd;
    Boolean IsSavingSafe=true;
    private static int Request_Modify_Image=1212;
    private int currentModifyPosition;
    RecyclerView mRecyclerView;
    private int ID;
    private SharedPreferences mSharedPref;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insert_new_entry);
        mRecyclerView = (RecyclerView) findViewById(R.id.previewImageRecyclerView);
        Name=(EditText)findViewById(R.id.EditText_NewEntry);
        SaveButton=findViewById(R.id.Save_NewEntry);
        fabAdd=findViewById(R.id.fabAdd);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveNewImages();
            }
        });
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowPopUp();
            }
        });
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(2));
        previewImages=new ArrayList<>(4);
        ImagePaths=new ArrayList<>(4);
        imageProperties=new ArrayList<>(4);
        mRecyclerView.setAdapter(new previewAdapter(previewImages, R.layout.gallery_item, InsertNewEntry.this));
        SnapHelper snapHelper=new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        mSharedPref= PreferenceManager.getDefaultSharedPreferences(this);
        dbhandler=new Dbhandler(this,null,null,1);
        try {

            ID = getIntent().getExtras().getInt("ID", -1);
        }catch (NullPointerException e){
            ID=-1;
        }

        if(ID!=-1){
            Name.setText(dbhandler.getresult(ID).getName());
            Name.setEnabled(false);
        }
        ShowPopUp();

    }

    public void deleteImage(int pos){
        Parcelable state=mRecyclerView.getLayoutManager().onSaveInstanceState();
        Shredder.Shred(previewImages.get(pos));
        previewImages.remove(pos);
        imageProperties.remove(pos);
        Shredder.Shred(ImagePaths.get(pos));
        ImagePaths.remove(pos);
        mRecyclerView.setAdapter(new previewAdapter(previewImages, R.layout.gallery_item, InsertNewEntry.this));
        if(pos>0&&pos==mRecyclerView.getAdapter().getItemCount())
            mRecyclerView.scrollToPosition(pos-1);
        else mRecyclerView.getLayoutManager().onRestoreInstanceState(state);
    }
    public void ModifyImage(int pos){
        currentModifyPosition=pos;
        Intent intent=new Intent(InsertNewEntry.this,Modify.class);
        intent.putExtra("path",imageProperties.get(pos).getOriginal_Image());
        startActivityForResult(intent,Request_Modify_Image);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==OpenCamera.Req_for_Image&&resultCode == RESULT_OK) {
            int total_images = data.getIntExtra("total", 0);
            ImageProperties ip[]= (ImageProperties[]) data.getExtras().get("imageProperties");
            String []pI=(String[]) data.getExtras().get("PreviewImages");
            for (int i=0;i<ip.length;i++){
                imageProperties.add(ip[i]);
                previewImages.add(pI[i]);
                ImagePaths.add(ip[i].getOriginal_Image());
            }
            //set preview images to horizontal imageview and then ask if want to add new Images,Marker ask for name..etc
            mRecyclerView.setAdapter(new previewAdapter(previewImages, R.layout.gallery_item, InsertNewEntry.this));
            return;
        }
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            List<Image> images = ImagePicker.getImages(data);
            String []newPath=new String[images.size()];
            for (int i=0;i<images.size();i++){
                newPath[i]=images.get(i).getPath();
            }
            if(newPath.length>0) {
                LoadImagesFromStorage newTask = new LoadImagesFromStorage(newPath);
                newTask.execute();
            }
            return;

        }
        if(requestCode==Request_Modify_Image&&resultCode==RESULT_OK){
            previewImages.set(currentModifyPosition,data.getExtras().getString("preview"));
            imageProperties.set(currentModifyPosition,(ImageProperties)data.getExtras().get("imageProperty"));
            Parcelable state=mRecyclerView.getLayoutManager().onSaveInstanceState();
            mRecyclerView.setAdapter(new previewAdapter(previewImages, R.layout.gallery_item, InsertNewEntry.this));
            mRecyclerView.getLayoutManager().onRestoreInstanceState(state);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void ShowPopUp(){
        CharSequence Choice[] = new CharSequence[] {"From storage", "From Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("From");
        builder.setItems(Choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                if(which==0){
                    ImagePicker.create(InsertNewEntry.this)
                            .returnMode(ReturnMode.NONE)
                            .folderMode(true)
                            .toolbarFolderTitle("Folder")
                            .toolbarImageTitle("Tap to select")
                            .showCamera(false)
                            .theme(R.style.ImagePickerTheme)
                            .enableLog(false)
                            .start();
                }else if(which==1){
                    Intent intent=new Intent(InsertNewEntry.this,OpenCamera.class);
                    intent.putExtra("option",OpenCamera.Req_for_Image);
                    startActivityForResult(intent,OpenCamera.Req_for_Image);
                }
            }
        });
        builder.show();
    }
    private class previewAdapter extends RecyclerView.Adapter<previewAdapter.ViewHolder>{
        ArrayList<String> mPaths;
        int mTargetLayout;
        Context mContext;
        previewAdapter(ArrayList<String> paths, int targetLayout, Context context){
            mContext=context;
            mPaths=paths;
            mTargetLayout=targetLayout;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(mTargetLayout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

                Glide.with(mContext).load(mPaths.get(position)).apply(RequestOptions.bitmapTransform(new CenterInside())).into(holder.mImageView);
                holder.mImageView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border));
                holder.mTextView.setText(""+(position+1));
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteImage(holder.getAdapterPosition());
                    }
                });
                holder.modify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ModifyImage(holder.getAdapterPosition());
                    }
                });

        }

        @Override
        public int getItemCount() {
            return mPaths.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView mImageView;
            LinearLayout opLayout;
            TextView mTextView;
            ImageButton modify,delete;
            ViewHolder(final View itemView){
                super(itemView);
                //itemView.getLayoutParams().width=Resources.getSystem().getDisplayMetrics().widthPixels/2;
                //itemView.getLayoutParams().height=Resources.getSystem().getDisplayMetrics().heightPixels/2;
                mImageView=(ImageView)itemView.findViewById(R.id.gallery_image);
                opLayout=itemView.findViewById(R.id.gallery_item_options);
                modify=itemView.findViewById(R.id.gallery_item_edit);
                delete=itemView.findViewById(R.id.gallery_item_delete);
                mTextView=itemView.findViewById(R.id.itemNo);
            }
        }


    }
    private void SaveNewImages(){
        if(IsSavingSafe) {
            if (mRecyclerView.getAdapter().getItemCount() == 0) {
                Toast.makeText(InsertNewEntry.this, "No Images Found", Toast.LENGTH_SHORT).show();
            }else if (!Name.getText().toString().equals("")) {
                if(ID!=-1){
                    String[] temp = new String[ImagePaths.size()];
                    ImagePaths.toArray(temp);
                    dbhandler.add_Images(ID,temp);
                    setResult(RESULT_OK);
                }else {
                    entry e = new entry();
                    String[] temp = new String[ImagePaths.size()];
                    ImagePaths.toArray(temp);
                    e.set_im(temp);
                    e.setName(Name.getText().toString());
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    e.setDateTime(date);
                    dbhandler.addRow(e);
                }
                final Context context = getBaseContext();
                Intent intent = new Intent(context, ImageProcessingService.class);
                ImageProperties[] tempIP = new ImageProperties[imageProperties.size()];
                imageProperties.toArray(tempIP);
                intent.putExtra("ImageProperties", tempIP);
                startService(intent);
                setResult(RESULT_OK);
                InsertNewEntry.this.finish();
            } else {
                Toast.makeText(InsertNewEntry.this, "Name can't be null", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;
        public SpacesItemDecoration(int space) {
            this.space = space;
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;            // Add top margin only for the first item to avoid double space between items
        }
    }
    public String CreatePreviewImages(String s){
        try{
            File file=File.createTempFile("tmp",".jpg",getExternalFilesDir("TEMP"));
            int Screenwidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            Bitmap bmp= BitmapFunctions.decodeSampledBitmapFromResource(s,Screenwidth/2,Screenwidth/2);
            Mat tmat=new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bmp,tmat);
            bmp.recycle();
            Imgproc.cvtColor(tmat,tmat,Imgproc.COLOR_RGBA2BGRA);
            ImageProcessor.ProcessImage(tmat, Modify.ScanMode.values()[Integer.parseInt(mSharedPref.getString(SettingsFragment.KEY_PREF_SCANNINGMODE,"3"))],2,9,1);
            Imgcodecs.imwrite(file.getAbsolutePath(),tmat);
            return  file.getAbsolutePath();
        }catch (Exception e){
            Log.e("mapp","m",e);
        }
        return  null;
    }
    class LoadImagesFromStorage extends AsyncTask<Void,Void,Void>{
        String []path;
        public LoadImagesFromStorage(String []path) {
            super();
            this.path=path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(null);
            IsSavingSafe=false;
            ((TextView)findViewById(R.id.loadingText)).setText("Loading and creating preview images");
        }

        @Override
        protected Void doInBackground(Void... voids) {
        for(String srcPath:path) {
            try {
                OpenCVLoader.initDebug();
                File dst = File.createTempFile(
                        "Orig",  /* prefix */
                        ".jpg",         /* suffix */
                        getExternalFilesDir("Pictures")      /* directory */
                );
                File src=new File(srcPath);
                copyFile(src,dst);
                String tmpPrevImage=CreatePreviewImages(dst.getAbsolutePath());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getAbsolutePath(), options);
                int height = options.outHeight;
                int width = options.outWidth;
                Map<Integer, PointF> outlinePoints = new HashMap<>();
                outlinePoints.put(0, new PointF(0, 0));
                outlinePoints.put(1, new PointF(width, 0));
                outlinePoints.put(2, new PointF(0,height));
                outlinePoints.put(3, new PointF(width, height));
                ImageProperties tempIP=new ImageProperties(dst.getAbsolutePath(),MapToPoint(outlinePoints), Modify.ScanMode.values()[Integer.parseInt(mSharedPref.getString(SettingsFragment.KEY_PREF_SCANNINGMODE,"3"))]);
                imageProperties.add(tempIP);
                previewImages.add(tmpPrevImage);
                ImagePaths.add(dst.getAbsolutePath());
            } catch (IOException ex) {
            }
        }
            //copy Images to data folder and create preview images and image properties
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            IsSavingSafe=true;
            Parcelable state=mRecyclerView.getLayoutManager().onSaveInstanceState();
            mRecyclerView.setAdapter(new previewAdapter(previewImages, R.layout.gallery_item, InsertNewEntry.this));
            mRecyclerView.getLayoutManager().onRestoreInstanceState(state);
            fabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowPopUp();
                }
            });
            // set View to normal and add images to lists
        }
        private void copyFile(File sourceFile, File destFile) throws IOException {
            if (!sourceFile.exists()) {
                return;
            }

            FileChannel source = null;
            FileChannel destination = null;
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            if (destination != null && source != null) {
                destination.transferFrom(source, 0, source.size());
            }
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }


        }
    }

}
