package ThreadAndRunnable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import com.mth.remoteApp.MainActivity;
import com.mth.remoteApp.MainActivity2;

import java.io.IOException;

import DiskOperation.FolderListR_W;
import DiskOperation.LinkedListPhotoR_W;
import DiskOperation.UploadedListR_W;
import LinkedList.LinkedListPhoto;
import Objects.FolderList;
import Objects.PhotoObject;
import Objects.UploadedList;
import ThreadAndRunnable.APIReq.APIRequests;
import ThreadAndRunnable.DeviceScan.ScanDeviceUpdateUi;
import ThreadAndRunnable.Gallery.GenerateGalleryThread;
import ThreadAndRunnable.UIRestore.RestoreSettingsUI;
import Tools.ImageFullScreen;

public class ThreadManager extends MainActivity {

    private Context context;
    private LinkedListPhoto photoLL;
    private Activity activity;

    private LinearLayout linearLayoutSettings;
    private ScrollView scrollViewSettings;
    private FolderList folderList;

    private LinearLayout linearLayoutGallery;
    private NestedScrollView scrollViewGallery;
    private TextView statusTextGallery;

    private MainActivity2 main2;
    private LinkedListPhotoR_W linkedListPhotoR_w;
    private FolderListR_W folderListR_w;
    private ImageFullScreen imageFullScreen;

    private EditText editTextIPAddress, editTextPort, editTextPassword;

    private APIRequests apiRequests;


    //Constructor for Gallery activity
    public ThreadManager(Context context, Activity activity, LinearLayout linearLayoutGallery,
                         NestedScrollView scrollViewGallery, TextView statusTextGallery,
                         ImageFullScreen imageFullScreen) {
        this.context = context;
        this.activity = activity;
        this.linearLayoutGallery = linearLayoutGallery;
        this.scrollViewGallery = scrollViewGallery;
        this.statusTextGallery = statusTextGallery;
        this.linkedListPhotoR_w = new LinkedListPhotoR_W(context);
        this.folderListR_w = new FolderListR_W(context);
        apiRequests = new APIRequests(context, activity, statusTextGallery);
        this.imageFullScreen = imageFullScreen;
    }

    //Constructor for Settings activity
    public ThreadManager(Context context, Activity activity, LinearLayout linearLayoutSettings,
                         ScrollView scrollViewSettings, FolderList folderList, EditText editTextIPAddress, EditText editTextPort, EditText editTextPassword) {
        this.context = context;
        this.activity = activity;
        this.linearLayoutSettings = linearLayoutSettings;
        this.scrollViewSettings = scrollViewSettings;
        this.folderList = folderList;
        this.editTextIPAddress = editTextIPAddress;
        this.editTextPort = editTextPort;
        this.editTextPassword = editTextPassword;
        main2 = new MainActivity2();
    }

    //Constructor for full screen ImageView and download original
    public ThreadManager(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void createNewGallery() {
        photoLL = linkedListPhotoR_w.loadLinkedList();
        folderList = folderListR_w.loadFolderList();
        if (folderList.isGenerateNewGallery()) {
            System.out.println("-----THREAD MANAGER createNewGallery");
            GenerateGalleryThread generateGalleryThread = new GenerateGalleryThread(linearLayoutGallery, scrollViewGallery,
                    context, this, statusTextGallery, photoLL, folderList, imageFullScreen);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    generateGalleryThread.run();
                    linkedListPhotoR_w.saveLinkedList(photoLL);
                }
            }).start();
        } else {
            updateCurrentGallery();
        }
    }

    private void updateCurrentGallery() {
        System.out.println("-----THREAD MANAGER updateCurrentGallery - photoLL.size: " + photoLL.size());
        GenerateGalleryThread generateGalleryThread = new GenerateGalleryThread(linearLayoutGallery, scrollViewGallery,
                context, this, statusTextGallery, photoLL, folderList, imageFullScreen);
        new Thread(new Runnable() {
            @Override
            public void run() {
                generateGalleryThread.run();
            }
        }).start();
    }

    public void restoreSettingsUI() {
        RestoreSettingsUI restoreSettingsUI = new RestoreSettingsUI(this, context, linearLayoutSettings,
                scrollViewSettings, editTextIPAddress, editTextPort, editTextPassword);
        new Thread(new Runnable() {
            @Override
            public void run() {
                restoreSettingsUI.run();
            }
        }).start();
    }

    public void scanDeviceForFolders() {
        ScanDeviceUpdateUi scanDeviceUpdateUi = new ScanDeviceUpdateUi(folderList, context, linearLayoutSettings, scrollViewSettings, this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanDeviceUpdateUi.run();
            }
        }).start();
    }

    public void uploadUnits(LinkedListPhoto photoLL) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadedListR_W uploadedListR_w = new UploadedListR_W(context);
                UploadedList uploadedList = uploadedListR_w.loadUploadedList();
                System.out.println("-----uploadUnits size: " + uploadedList.getSize());

                for (int i = 0; i < photoLL.size(); i++) {
                    if (photoLL.getPhotoObject(i).getLocationOnDevice() != null) {
                        PhotoObject returnedPo = null;
                        boolean b = uploadedList.contains(photoLL.getPhotoObject(i));
                        System.out.println("-----uploadUnits b: " + b);

                        if (!b) {
                            System.out.println("-----uploadUnits photoLL.getPhotoObject: " + photoLL.getPhotoObject(i));
                            returnedPo = apiRequests.uploadUnit(photoLL.getPhotoObject(i));
                            System.out.println("-----uploadUnits returnedPo: " + returnedPo);
                        } else {
                            System.out.println("-----Tagged as already uploaded: " + photoLL.getPhotoObject(i));
                        }
                        if (returnedPo != null) {
                            if (returnedPo.getId() > -1) {
                                photoLL.getPhotoObject(i).setId(returnedPo.getId());
                                photoLL.getPhotoObject(i).setDateUploaded(returnedPo.getDateUploaded());
                                if (!returnedPo.getFileName().equals(photoLL.getPhotoObject(i).getFileName())) {
                                    photoLL.getPhotoObject(i).setFileNameAndRenameFile(returnedPo.getFileName());
                                    System.out.println("-----New Object: " + photoLL.getPhotoObject(i).toString());
                                }
                                if (!uploadedList.contains(photoLL.getPhotoObject(i))) {
                                    System.out.println("-----Adding Object: " + photoLL.getPhotoObject(i).toString() + " to uploadedList");
                                    uploadedList.addToList(photoLL.getPhotoObject(i));
                                }
                            } else if (returnedPo.getId() < 0) {
                                System.out.println("-----Error uploadUnits for " + photoLL.getPhotoObject(i).toString());
                                updateTextMessage("Fatal error: Upload cancelled");
                                Thread.currentThread().interrupt();
                                return;
                            }
                        } else {
                            if (!uploadedList.contains(photoLL.getPhotoObject(i))) {
                                System.out.println("-----Adding Object: " + photoLL.getPhotoObject(i).toString() + " to uploadedList");
                                uploadedList.addToList(photoLL.getPhotoObject(i));
                            }
                        }
                        uploadedListR_w.saveUploadedListToDisk(uploadedList);
                        linkedListPhotoR_w.saveLinkedList(photoLL);
                    }
                }
                updateTextMessage("Upload finished");
            }
        }).start();
    }

    public void isServerAlive() {
        new Thread(() -> apiRequests.isAlive()).start();
    }

    public void downloadAndDisplay(ConstraintLayout constraintLayout, PhotoObject photoObject,
                                   String downloadPath, ImageView fullScreenImageView) {
        APIRequests apiRequests = new APIRequests(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    apiRequests.downloadImage(photoObject, downloadPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap imageBMP = BitmapFactory.decodeFile(downloadPath + photoObject.getFileName());
                        fullScreenImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        fullScreenImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        fullScreenImageView.setImageBitmap(imageBMP);
                        constraintLayout.addView(fullScreenImageView);
                    }
                });
            }
        }).start();
    }

    public void updateTextMessage(String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextGallery.setText(message);
            }
        });
    }
}
