package ThreadAndRunnable.DeviceScan;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import com.mth.remoteApp.MainActivity2;

import DiskOperation.FolderListR_W;
import Objects.FolderList;

public class ScanDeviceUpdateUi extends MainActivity2 implements Runnable {
    private final Activity activity;
    private FolderList folderList;
    private Context context;
    private LinearLayout linearLayoutSettings;
    private ScrollView scrollViewSettings;

    public ScanDeviceUpdateUi(FolderList folderList, Context context,
                              LinearLayout linearLayoutSettings, ScrollView scrollViewSettings, Activity activity) {
        this.folderList = folderList;
        this.context = context;
        this.linearLayoutSettings = linearLayoutSettings;
        this.scrollViewSettings = scrollViewSettings;
        this.activity = activity;
    }

    @Override
    public void run() {
        FolderScan folderScan = new FolderScan(context);
        FolderListR_W folderListRW = new FolderListR_W(context);

        System.out.println("----Start device scan");
        folderList = folderScan.scanWrapper(folderList);

        System.out.println("----saveFolderListToDisk");
        folderListRW.saveFolderListToDisk(folderList);

        System.out.println("----Update upload switch list");

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayoutSettings.removeAllViews();

                for (int i = 0; i < folderList.getSize(); i++) {
                    if (!folderList.getFolder(i).getFolderName().equals("ThumbnailFolder") && !folderList.getFolder(i).getFolderName().equals("DownloadFolder")) {
                        Switch switchObject = new Switch(context);
                        switchObject.setText(folderList.getFolder(i).getFolderName());
                        switchObject.setChecked(folderList.getFolder(i).isBackup());
                        switchObject.setId(i);

                        switchObject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FolderList folderList = folderListRW.loadFolderList();
                                folderList.setGenerateNewGallery(true);
                                String folderName = switchObject.getText().toString();
                                System.out.println("-----folderList.getSize: " + folderList.getSize());
                                folderList.getFolder(folderName).setBackup(switchObject.isChecked());
                                System.out.println("----- " + folderName + " is now " + switchObject.isChecked());
                                folderListRW.saveFolderListToDisk(folderList);
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
        System.out.println("----ScanDeviceThread all done");
    }
}
