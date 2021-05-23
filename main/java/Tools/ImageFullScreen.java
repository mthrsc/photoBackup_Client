package Tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import java.io.File;
import java.io.IOException;

import Objects.PhotoObject;
import ThreadAndRunnable.APIReq.APIRequests;
import ThreadAndRunnable.ThreadManager;

public class ImageFullScreen {
    private final String downloadPath;
    private NestedScrollView scrollViewGallery;
    private TextView statusTextGallery;
    private Button settingsBtn, uploadBtn, returnBtn;
    private ConstraintLayout constraintLayout;
    private Context context;
    private ImageView fullScreenImageView;
    private ThreadManager threadManager;
    private Activity activity;

    public ImageFullScreen(NestedScrollView scrollViewGallery, TextView statusTextGallery,
                           Button settingsBtn, Button uploadBtn, Button returnBtn, ConstraintLayout constraintLayout, Context context,
                           Activity activity) {
        this.scrollViewGallery = scrollViewGallery;
        this.statusTextGallery = statusTextGallery;
        this.settingsBtn = settingsBtn;
        this.uploadBtn = uploadBtn;
        this.returnBtn = returnBtn;
        this.constraintLayout = constraintLayout;
        this.context = context;
        this.activity = activity;
        downloadPath = this.context.getExternalFilesDir(null) + "/DownloadFolder/";
        threadManager = new ThreadManager(context, activity);
        this.returnBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        returnNormal();
                    }
                }
        );
    }

    public void setImageFullScreen(PhotoObject photoObject) {
        fullScreenImageView = new ImageView(context);

        constraintLayout.setBackgroundColor(Color.BLACK);
        scrollViewGallery.setVisibility(View.INVISIBLE);
        uploadBtn.setVisibility(View.INVISIBLE);
        settingsBtn.setVisibility(View.INVISIBLE);
        returnBtn.setVisibility(View.VISIBLE);

        String s = isFileOnDevice(photoObject);

        if (s.equals("public")) {
            Bitmap imageBMP = BitmapFactory.decodeFile(photoObject.getLocationOnDevice());
            fullScreenImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            fullScreenImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            fullScreenImageView.setImageBitmap(imageBMP);
            constraintLayout.addView(fullScreenImageView);
        } else if (s.equals("private")) {
            Bitmap imageBMP = BitmapFactory.decodeFile(downloadPath + photoObject.getFileName());
            fullScreenImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            fullScreenImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            fullScreenImageView.setImageBitmap(imageBMP);
            constraintLayout.addView(fullScreenImageView);
        } else {
            threadManager.downloadAndDisplay(constraintLayout, photoObject, downloadPath, fullScreenImageView);
        }
        returnBtn.bringToFront();
    }

    public String isFileOnDevice(PhotoObject photoObject) {
        File publicStorage = null;
        if (photoObject.getLocationOnDevice() != null) {
            publicStorage = new File(photoObject.getLocationOnDevice());
        }

        File privateStorage = new File(downloadPath + photoObject.getFileName());
        if (publicStorage != null) {
            if (publicStorage.exists()) {
                return "public";
            }
        }
        if (privateStorage.exists()) {
            return "private";
        }
        return "none";
    }

    public void returnNormal() {
        constraintLayout.setBackgroundColor(Color.WHITE);
        constraintLayout.removeView(fullScreenImageView);

        scrollViewGallery.setVisibility(View.VISIBLE);
        uploadBtn.setVisibility(View.VISIBLE);
        settingsBtn.setVisibility(View.VISIBLE);

        returnBtn.setVisibility(View.INVISIBLE);
    }
}
