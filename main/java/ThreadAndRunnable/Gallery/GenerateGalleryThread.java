package ThreadAndRunnable.Gallery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import Builder.PhotoObjectBuilder;
import DiskOperation.FolderListR_W;
import DiskOperation.LinkedListPhotoR_W;
import LinkedList.LinkedListPhoto;
import Objects.FolderList;
import Objects.PhotoObject;
import ThreadAndRunnable.APIReq.APIRequests;
import Tools.ImageFullScreen;
import Tools.LogTool;

public class GenerateGalleryThread {

    private final ArrayList<String> imageFileExtensions = new ArrayList();
    private final int thumbnailSize = 256;
    private final String thumbnailOutputPath, downloadPath;
    private LinearLayout linearLayoutGallery;
    private NestedScrollView scrollViewGallery;
    private String threadName;
    private Context context;
    private LinkedListPhoto photoLL;
    private FolderList folderList;
    private Activity activity;
    private TextView statusTextGallery;
    private LinkedListPhotoR_W linkedListPhotoR_w;
    private FolderListR_W folderListR_w;
    private PhotoObjectBuilder pBuilder;
    private APIRequests apiRequests;
    private ImageFullScreen imageFullScreen;
    private LogTool logTool;
    private boolean serverAlive;

    public GenerateGalleryThread(LinearLayout linearLayoutGallery, NestedScrollView scrollViewGallery, Context context, Activity activity, TextView statusTextGallery,
                                 LinkedListPhoto photoLL, FolderList folderList, ImageFullScreen imageFullScreen) {
        this.linearLayoutGallery = linearLayoutGallery;
        this.scrollViewGallery = scrollViewGallery;
        this.threadName = "GenerateGalleryThread";
        this.context = context;
        this.activity = activity;
        this.statusTextGallery = statusTextGallery;
        thumbnailOutputPath = this.context.getExternalFilesDir(null) + "/ThumbnailFolder/";
        downloadPath = this.context.getExternalFilesDir(null) + "/DownloadFolder/";
        linkedListPhotoR_w = new LinkedListPhotoR_W(context);
        folderListR_w = new FolderListR_W(context);
        pBuilder = new PhotoObjectBuilder(context);
        apiRequests = new APIRequests(context, activity, statusTextGallery);
        this.photoLL = photoLL;
        this.folderList = folderList;
        this.imageFullScreen = imageFullScreen;
        logTool = new LogTool(this.context);
    }

    public void run() {
        logTool.logToFile("GenerateGalleryThread > run");

        updateUiMessage("Generating gallery > Adding pictures from folders");

        imageFileExtensions.add("jpg");
        imageFileExtensions.add("jpeg");
        imageFileExtensions.add("bmp");
        imageFileExtensions.add("png");
        imageFileExtensions.add("gif");

        System.out.println("----folderList to string:");
        System.out.println(folderList.toString());

        addFromLocalSource(photoLL, folderList);

        updateUiMessage("Generating gallery > Trying to reach server...");
        serverAlive = apiRequests.isAlive();

        if (serverAlive) {
            logTool.logToFile("Server isAlive");
            addFromServer(photoLL);
        } else {
            logTool.logToFile("Could not reach server");
        }

        photoLL.sortChronologically();

        updateUiMessage("Generating gallery > Generating thumbnails");
        if (!photoLL.isEmpty()) {
            generateThumbnails(photoLL);
        }
        System.out.println("-----photoLL.size: " + photoLL.size());
        System.out.println("-----thumbnailOutputPath.size: " + new File(thumbnailOutputPath).listFiles().length);

        folderList.setGenerateNewGallery(false);

        folderListR_w.saveFolderListToDisk(folderList);
        linkedListPhotoR_w.saveLinkedList(photoLL);

        System.out.println("-----Finished creating new photoLL");
        updateGalleryView();
    }

    public void updateGalleryView() {
        updateUiMessage("Generating gallery > updateGalleryView");

        //        Create gallery view from thumbnails
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    linearLayoutGallery.removeAllViews();
                    for (int i = 0; i < photoLL.size(); i++) {
                        ImageView image = new ImageView(context);
                        image.setTranslationY(5.0f);
                        System.out.println("----Adding to gallery image: " + photoLL.getPhotoObject(i).getFileName() + " DATE: " + photoLL.getPhotoObject(i).getDateUploaded());
                        Bitmap imageBMP = BitmapFactory.decodeFile(thumbnailOutputPath + photoLL.getPhotoObject(i).getFileName());
                        image.setImageBitmap(imageBMP);
                        image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        final int index = i;
                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageFullScreen.setImageFullScreen(photoLL.getPhotoObject(index));
                            }
                        });
                        linearLayoutGallery.addView(image);
                    }

                    linearLayoutGallery.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)); // -1 is equal to match_parent
                    linearLayoutGallery.setOrientation(LinearLayout.VERTICAL);

                    scrollViewGallery.removeAllViews();
                    scrollViewGallery.addView(linearLayoutGallery);
                } catch (Exception e) {
                    System.out.println("-----Error updateGalleryView");
                    e.printStackTrace();
                }
            }
        });

        updateUiMessage("");
    }

    private void addFromLocalSource(LinkedListPhoto photoLL, FolderList folderList) {
        logTool.logToFile("addFromLocalSource");

        //Scan folder list for pictures that should be added to gallery
        for (int i = 0; i < folderList.getSize(); i++) {
            if (folderList.getFolder(i).isBackup()) {   //If folder i is selected for backup then...
                File folder = new File(folderList.getPath(i));
                for (File f : folder.listFiles()) {
                    String ext = f.toString().substring(f.toString().lastIndexOf(".") + 1);
                    ext = ext.toLowerCase();
                    if (imageFileExtensions.contains(ext)) {
                        if (!photoLL.containsFileLocation(f.getAbsolutePath())) {
                            PhotoObject photoObject = pBuilder.buildPhotoObject(f);
                            System.out.println("-----Adding to photoLL " + photoObject.getFileName());
                            photoLL.add(photoObject);
                            System.out.println("-----Adding photoLL.size() " + photoLL.size());
//                            logTool.logToFile("Adding to photoLL " + photoObject.toString());
                        } else {
                            System.out.println("-----" + f.getAbsolutePath() + " is already in photoLL");
                        }
                    }
                }
            } else if (!folderList.getFolder(i).isBackup()) {
                File folder = new File(folderList.getPath(i));
                for (File f : folder.listFiles()) {
                    String path = f.getAbsolutePath();
                    if (photoLL.containsFileLocation(path)) {
                        photoLL.removeByPath(path);
                    }
                }
            }
        }
    }

    private void addFromServer(LinkedListPhoto photoLL) {
        logTool.logToFile("addFromServer");

        System.out.println("-----Start addFromServer photoLL size before: " + photoLL.size());
        ArrayList<PhotoObject> thirtyFromServer = new ArrayList();

        try {
            thirtyFromServer = apiRequests.getThirtyFromServer();
            if (thirtyFromServer != null) {
                for (int i = 0; i < thirtyFromServer.size(); i++) {
                    PhotoObject po1 = thirtyFromServer.get(i);
//                    logTool.logToFile("addFromServer Trying to add: " + po1.toString());
                    System.out.println("-----addFromServer Trying to add " + po1.getFileName() + " - " + po1.getId() + " to photoLL");
                    if (!photoLL.containsId(po1.getId())) {
                        photoLL.add(thirtyFromServer.get(i));
                    } else {
//                        logTool.logToFile("addFromServer Already in photoLL: " + po1.toString());
                        System.out.println("-----" + po1.getFileName() + " is already in photoLL");
                    }
                }
            }
            cleanDownloadFolder(thirtyFromServer);
        } catch (Exception e) {
            logTool.logToFile("addFromServer Error: \n" + e.getMessage());
            System.out.println("-----Error getting thirtyFromServer");
            e.printStackTrace();
        }
    }

    private void generateThumbnails(LinkedListPhoto photoLL) {
        logTool.logToFile("generateThumbnails");

        //Loop through photoLL and generate thumbnails
        for (int i = 0; i < photoLL.size(); i++) {
//            logTool.logToFile("generateThumbnails for photo: " + photoLL.getPhotoObject(i).toString());

            Bitmap thumbnailBmp = null;
            File thumbnailLocation = null;
            File originalPictureLocation = null;

            //1 If file exists on the android device, we generate thumbnail locally
            //If getLocationOnDevice is not null, then original picture in on device
            //We can generate thumbnail from it
            if (photoLL.getPhotoObject(i).getLocationOnDevice() != null) {
                logTool.logToFile("generateThumbnails Original photo on device");

                thumbnailLocation = new File(thumbnailOutputPath + photoLL.getPhotoObject(i).getFileName());
                originalPictureLocation = new File(photoLL.getPhotoObject(i).getLocationOnDevice());

                if (!thumbnailLocation.exists()) {
                    logTool.logToFile("generateThumbnails Original photo on device - creating thumbnail");
                    originalPictureLocation = new File(photoLL.getPhotoObject(i).getLocationOnDevice());
                    thumbnailBmp = ThumbnailUtils.extractThumbnail(
                            BitmapFactory.decodeFile(originalPictureLocation.toString()),
                            thumbnailSize,
                            thumbnailSize);
                    try {
                        FileOutputStream out = new FileOutputStream(thumbnailOutputPath + photoLL.getPhotoObject(i).getFileName());
                        thumbnailBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                    } catch (IOException e) {
                        logTool.logToFile("generateThumbnails Original photo on device - Error creating thumbnails: \n" + e.getMessage());
                        System.out.println("-----Error creating thumbnails");
                        e.printStackTrace();
                    }
                }
            } else if (photoLL.getPhotoObject(i).getLocationOnDevice() == null && serverAlive) {
                logTool.logToFile("generateThumbnails Original photo NOT on device");
                thumbnailLocation = new File(thumbnailOutputPath + photoLL.getPhotoObject(i).getFileName());
                if (!thumbnailLocation.exists()) {
                    try {
//                        logTool.logToFile("generateThumbnails Original photo on device - downloading thumbnail");
                        thumbnailLocation = apiRequests.requestThumbnailFromServer(photoLL.getPhotoObject(i).getId(), thumbnailLocation);
                    } catch (IOException e) {
                        logTool.logToFile("generateThumbnails Original photo on device - downloading thumbnail - Error: \n" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void cleanDownloadFolder(ArrayList<PhotoObject> thirtyFromServer) {
        logTool.logToFile("cleanDownloadFolder starts");

        File downloadFolder = new File(downloadPath);
        ArrayList<File> fileList = new ArrayList<>();

        if (downloadFolder.listFiles().length > 0) {
            for (File f : downloadFolder.listFiles()) {
                fileList.add(f);
            }
        }
        ArrayList<Integer> indexToRemove = new ArrayList<>();

        logTool.logToFile("cleanDownloadFolder Scanning loop");
        for (int i = 0; i < fileList.size(); i++) {
            String fileName = fileList.get(i).getName();
            int count = 0;
            for (int ii = 0; ii < thirtyFromServer.size(); ii++) {
                if (fileName.equals(thirtyFromServer.get(ii).getFileName())) {
                    count++;
                    break;
                }
            }
            if (count == 0) {
                fileList.get(i).delete();
            }
        }
    }

    private void updateUiMessage(String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextGallery.setText(message);
            }
        });
    }
}
