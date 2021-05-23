package com.mth.remoteApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.multidex.MultiDex;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import DiskOperation.FolderListR_W;
import DiskOperation.LinkedListPhotoR_W;
import DiskOperation.ServerSettingsR_W;
import LinkedList.LinkedListPhoto;
import Objects.FolderList;
import Objects.ServerSettings;
import ThreadAndRunnable.ThreadManager;
import Tools.ImageFullScreen;
import Tools.LogTool;


public class MainActivity extends AppCompatActivity {

    private NestedScrollView scrollViewGallery;
    private LinearLayout linearLayoutGallery;
    private TextView statusTextGallery;
    private Button settingsBtn, uploadBtn, returnBtn;
    private ConstraintLayout constraintLayout;

    private Context context;

    private ThreadManager threadManager;
    private LinkedListPhotoR_W linkedListPhotoR_w;
    private LogTool logTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        MultiDex.install(this);
        context = getApplicationContext();
        logTool = new LogTool(this);
        logTool.logToFile("App starting");
        requestWRITE_EXTERNAL_STORAGE();

        scrollViewGallery = findViewById(R.id.scrollViewGallery);
        linearLayoutGallery = new LinearLayout(this);
        linearLayoutGallery.setOrientation(LinearLayout.VERTICAL);
        statusTextGallery = findViewById(R.id.statusTextGallery);
        constraintLayout = findViewById(R.id.constraintLayout);
        settingsBtn = findViewById(R.id.settingsBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        returnBtn = findViewById(R.id.returnBtn);


        createFolders();

        linkedListPhotoR_w = new LinkedListPhotoR_W(context);

        ImageFullScreen imageFullScreen = new ImageFullScreen(scrollViewGallery, statusTextGallery,
                settingsBtn, uploadBtn, returnBtn, constraintLayout, context, this);

        threadManager = new ThreadManager(context, this, linearLayoutGallery,
                scrollViewGallery, statusTextGallery, imageFullScreen);

        threadManager.createNewGallery();
        logTool.logToFile("App started");
    }

    public void switchToSettings(android.view.View View) {
        logTool.logToFile("Switch to settings");

        Intent intent = new Intent(context, MainActivity2.class);
        startActivity(intent);
    }

    public void requestWRITE_EXTERNAL_STORAGE() {
        logTool.logToFile("requestWRITE_EXTERNAL_STORAGE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                logTool.logToFile("requestWRITE_EXTERNAL_STORAGE Granted");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                logTool.logToFile("requestWRITE_EXTERNAL_STORAGE Request");
            }
        }
    }

    public void uploadBtnOnClick(android.view.View View) {
        logTool.logToFile("uploadBtnOnClick starts");

        LinkedListPhoto photoLL = linkedListPhotoR_w.loadLinkedList();
        threadManager.uploadUnits(photoLL);

        logTool.logToFile("uploadBtnOnClick finished");
    }

    private void createFolders() {
        String thumbnailOutputPath = context.getExternalFilesDir(null) + "/ThumbnailFolder/";
        File thumbnailDir = new File(thumbnailOutputPath);
        System.out.println("----- thumbnailDir: " + thumbnailDir.toString());
        if (!thumbnailDir.exists()) {
            if (thumbnailDir.mkdirs()) {
                logTool.logToFile("thumbnail Directory is created");
                System.out.println("----- thumbnail Directory is created!");
            } else {
                logTool.logToFile("Failed to create thumbnail directory");
                System.out.println("----- Failed to create thumbnail directory!");
            }
        } else {
            logTool.logToFile("thumbnail directory already exists");
            System.out.println("----- thumbnail directory already exists!");
        }

        String downloadPath = context.getExternalFilesDir(null) + "/DownloadFolder/";
        File downloadDir = new File(downloadPath);
        System.out.println("----- downloadDir: " + downloadDir.toString());
        if (!downloadDir.exists()) {
            if (downloadDir.mkdirs()) {
                logTool.logToFile("downloadDir is created");
                System.out.println("----- downloadDir is created!");
            } else {
                logTool.logToFile("Failed to create downloadDir");
                System.out.println("----- Failed to create downloadDir!");
            }
        } else {
            logTool.logToFile("downloadDir already exists");
            System.out.println("----- downloadDir already exists!");
        }
    }
}
