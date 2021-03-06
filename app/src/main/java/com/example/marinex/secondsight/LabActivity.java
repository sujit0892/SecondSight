package com.example.marinex.secondsight;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.core.Mat;

public class LabActivity extends AppCompatActivity {

    public static final String PHOTO_FILE_EXTENSION = ".png";
    public static final String PHOTO_MINE_TYPE = "image/png";

    public static final String EXTRA_PHOTO_URI = "com.example.marinex.secondsight.LabActivity.extra.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH = "com.example.marinex.secondsight.LabActivity.extra.PHOTO_DATA_PATH";

    private Uri mUri;
    private String mDataPath;
    private Mat mBgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
       mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
       mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);

        final ImageView imageView = new ImageView(this);

        imageView.setImageURI(mUri);


        setContentView(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.lab_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.delete:
                deletePhoto();
                return true;
            case R.id.edit:
                editPhoto();
                return true;
            case R.id.share:
                sharePhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deletePhoto()
    {
        final AlertDialog.Builder alert = new AlertDialog.Builder(LabActivity.this);
        alert.setTitle(R.string.photo_delete_prompt_title);
        alert.setMessage(R.string.photo_delete_prompt_message);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA
                +"=?",new String[] {mDataPath});
                finish();

            }
        });
        alert.setNegativeButton(android.R.string.cancel,null);
        alert.show();
    }

    private void editPhoto()
    {
       final Intent intent = new Intent(Intent.ACTION_EDIT);
       intent.setDataAndType(mUri,PHOTO_MINE_TYPE);
       startActivity(Intent.createChooser(intent,getString(R.string.photo_edit_chooser_title)));
    }

    private void sharePhoto(){
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(PHOTO_MINE_TYPE);
        intent.putExtra(Intent.EXTRA_STREAM, mUri);
        intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.photo_send_extra_text));
        startActivity(Intent.createChooser(intent, getString(R.string.photo_send_chooser_title)));

    }
}
