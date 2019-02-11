package com.example.weinner.liron.happygardener;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.media.ExifInterface;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Liron weinner
 */

class ExifData {
    public File imageFile;
    public final int DELETE_IMAGE_FILE = 72;

    public void insertExifData(final View view, Part part){
        final ImageButton part_picture_img_btn = (ImageButton) view;
        PartsHolder.getInstance().bitmap = ((BitmapDrawable) part_picture_img_btn.getDrawable()).getBitmap();

        try {
            final String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            final File myDir = new File(root);
            myDir.mkdir();
            if(PartsHolder.getInstance().imageMetadata.getDate_time() != null){
                imageFile = new File(myDir , PartsHolder.getInstance().imageMetadata.getDate_time().replaceAll("[:]|[ ]","")+part.getName()+".jpg");
            }
            else{
                final Date date = new Date(System.currentTimeMillis());
                final DateFormat dformat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                imageFile = new File(myDir , dformat.format(date).replaceAll("[-]|[:]|[ ]", "")+part.getName()+".jpg");
            }
            if(imageFile.exists()){
                imageFile.delete();
            }
            FileOutputStream out = new FileOutputStream(imageFile);
            PartsHolder.getInstance().bitmap.compress(Bitmap.CompressFormat.JPEG , 100 , out);
            out.flush();
            out.close();
            ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME , PartsHolder.getInstance().imageMetadata.getDate_time());
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE , PartsHolder.getInstance().imageMetadata.getGps_lat());
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF , PartsHolder.getInstance().imageMetadata.getGps_lat_ref());
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE , PartsHolder.getInstance().imageMetadata.getGps_lng());
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF , PartsHolder.getInstance().imageMetadata.getGps_lng_ref());
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final ExifData ourInstance = new ExifData();

    static ExifData getInstance() {
        return ourInstance;
    }

    private ExifData() {
    }
}
