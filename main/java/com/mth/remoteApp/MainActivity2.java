package com.mth.remoteApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.InetAddress;

import DiskOperation.FolderListR_W;
import DiskOperation.ServerSettingsR_W;
import Objects.FolderList;
import Objects.ServerSettings;
import ThreadAndRunnable.ThreadManager;
import Tools.Crypto;
import Tools.LogTool;

public class MainActivity2 extends AppCompatActivity {

    private FolderListR_W folderListR_w;
    private FolderList folderList;

    private Context context;
    private Button scanBtnObject;
    private Button galleryBtn;
    private LinearLayout linearLayoutSettings;
    private ScrollView scrollViewSettings;
    private TextView statusTextGallery;
    private EditText editTextIPAddress, editTextPort, editTextPassword;
    private ServerSettings serverSettings;
    private ThreadManager threadManager;
    private LogTool logTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        context = getApplicationContext();

        logTool = new LogTool(context);
        logTool.logToFile("Settings activity Starting");

        scanBtnObject = findViewById(R.id.scanBtn);
        galleryBtn = findViewById(R.id.galleryBtn1);
        scrollViewSettings = findViewById(R.id.scrollView);
        linearLayoutSettings = new LinearLayout(this);
        linearLayoutSettings.setOrientation(LinearLayout.VERTICAL);
        linearLayoutSettings.setTranslationY(50.0f);

        editTextIPAddress = findViewById(R.id.editTextIPAddress);
        editTextPort = findViewById(R.id.editTextPort);
        editTextPassword = findViewById(R.id.editTextPassword);
        statusTextGallery = findViewById(R.id.statusTextGallery);


        folderListR_w = new FolderListR_W(context);
        folderList = folderListR_w.loadFolderList();
        serverSettings = new ServerSettings();

        threadManager = new ThreadManager(context, this, linearLayoutSettings,
                scrollViewSettings, folderList, editTextIPAddress, editTextPort, editTextPassword);

        threadManager.restoreSettingsUI();
        logTool.logToFile("Settings activity Started");

    }

    public void scanDeviceBtnOnClick(android.view.View View) {
        logTool.logToFile("scanDeviceBtnOnClick");

        System.out.println("--------Scan Device !");
        folderList.setGenerateNewGallery(true);
        folderListR_w.saveFolderListToDisk(folderList);
        threadManager.scanDeviceForFolders();
    }


    public void switchToGallery(android.view.View View) {
        logTool.logToFile("switchToGallery");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void saveServerSettings(android.view.View View) {
        logTool.logToFile("saveServerSettings starts");

        editTextIPAddress.setTextColor(Color.BLACK);
        editTextPort.setTextColor(Color.BLACK);
        editTextPassword.setTextColor(Color.BLACK);

        ServerSettingsR_W serverSettingsR_w = new ServerSettingsR_W(context);
        serverSettings = serverSettingsR_w.loadServerSettings();

        boolean allGood = true;

        String IPAddress = new String();
        InetAddress ip;
        int port = 0;
        String password = new String();
        Crypto crypto = new Crypto();

        try {
            IPAddress = editTextIPAddress.getText().toString();
            ip = InetAddress.getByName(IPAddress);
            serverSettings.setIp(ip);
        } catch (Exception e) {
            logTool.logToFile("saveServerSettings Error getting IPAddress: \n" + e.getMessage());
//          e.printStackTrace();
            editTextIPAddress.setTextColor(Color.RED);
            allGood = false;
        }

        try {
            System.out.println("----editTextPort.getText().toString: " + editTextPort.getText().toString());
            if (editTextPort.getText().toString().length() > 4) {
                throw new Exception();
            }
            port = Integer.parseInt(editTextPort.getText().toString());
            serverSettings.setPort(port);
        } catch (Exception e) {
            logTool.logToFile("saveServerSettings Error getting port: \n" + e.getMessage());
            e.printStackTrace();
            editTextPort.setTextColor(Color.RED);
            allGood = false;
        }

        password = editTextPassword.getText().toString();
        System.out.println("----- Input password: " + password);

        // Is the password on the UI the same than the stored password
        //It makes sure we do not encounter the problem of hashing an already hashed password
        //resulting in a authentication failure
        try {
            String oldPassword;
            if ((oldPassword = serverSettings.getPassword()) != null) {
                if (!password.equals(oldPassword)) {
                    password = crypto.encryptPassword(password);
                    System.out.println("----- Encrypted password: " + password);
                    serverSettings.setPassword(password);
                }
            } else {
                password = crypto.encryptPassword(password);
                System.out.println("----- Encrypted password: " + password);
                serverSettings.setPassword(password);
            }
        } catch (NullPointerException e) {
            logTool.logToFile("saveServerSettings Error getting password: \n" + e.getMessage());
            editTextPassword.setTextColor(Color.RED);
            allGood = false;
            e.printStackTrace();
        }

        if (allGood) {
            logTool.logToFile("saveServerSettings inputs are all valid - sending to serverSettingsR_w");
            serverSettingsR_w.saveServerSettings(serverSettings);
        }
    }
}