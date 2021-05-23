package ThreadAndRunnable.UIRestore;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import com.mth.remoteApp.MainActivity2;

import DiskOperation.FolderListR_W;
import DiskOperation.ServerSettingsR_W;
import Objects.FolderList;
import Objects.ServerSettings;
import Tools.LogTool;

public class RestoreSettingsUI extends MainActivity2 {

    private final Activity activity;
    private Context context;
    private LinearLayout linearLayoutSettings;
    private ScrollView scrollViewSettings;
    //Restore server settings
    private EditText editTextIPAddress, editTextPort, editTextPassword;
    private LogTool logTool;

    public RestoreSettingsUI(Activity activity, Context context, LinearLayout linearLayoutSettings,
                             ScrollView scrollViewSettings, EditText editTextIPAddress, EditText editTextPort,
                             EditText editTextPassword) {
        this.activity = activity;
        this.context = context;
        this.linearLayoutSettings = linearLayoutSettings;
        this.scrollViewSettings = scrollViewSettings;
        this.editTextIPAddress = editTextIPAddress;
        this.editTextPort = editTextPort;
        this.editTextPassword = editTextPassword;
        logTool = new LogTool(this.context);
    }

    public void run() {
        logTool.logToFile("RestoreSettingsUI -runOnUiThread- Starts");

        FolderListR_W folderListRW = new FolderListR_W(context);
        FolderList folderList = folderListRW.loadFolderList();

        //Restore scanned folder and switch state
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < folderList.getSize(); i++) {
                    if (!folderList.getFolder(i).getFolderName().equals("ThumbnailFolder") && !folderList.getFolder(i).getFolderName().equals("DownloadFolder")) {

                        Switch switchObject = new Switch(context);
                        switchObject.setText(folderList.getFolder(i).getFolderName());
                        switchObject.setChecked(folderList.getFolder(i).isBackup());
                        switchObject.setId(i);
                        switchObject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FolderList fl = folderListRW.loadFolderList();
                                fl.setGenerateNewGallery(true);
                                String folderName = switchObject.getText().toString();
                                System.out.println("-----folderList.getSize: " + fl.getSize());
                                fl.getFolder(folderName).setBackup(switchObject.isChecked());
                                System.out.println("----- " + folderName + " is now " + switchObject.isChecked());
                                folderListRW.saveFolderListToDisk(fl);
                                System.out.println("----- folderList saved to disk");
                            }
                        });
                        linearLayoutSettings.addView(switchObject);
                    }
                }
                scrollViewSettings.removeAllViews();
                scrollViewSettings.addView(linearLayoutSettings);
            }
        });
        logTool.logToFile("RestoreSettingsUI -runOnUiThread- Finish");

        //Restore server settings
        ServerSettingsR_W serverSettingsR_w = new ServerSettingsR_W(context);
        ServerSettings serverSettings = serverSettingsR_w.loadServerSettings();
        try {
            String ipAddress = serverSettings.getIp().toString();
            ipAddress = ipAddress.substring(ipAddress.lastIndexOf("/") + 1);
            editTextIPAddress.setText(ipAddress);
            editTextPort.setText(String.valueOf(serverSettings.getPort()));
            editTextPassword.setText(serverSettings.getPassword());
        } catch (Exception e) {
            System.out.println("-----Error restoring ServerSettings");
        }
        logTool.logToFile("RestoreSettingsUI Finish");

    }
}
