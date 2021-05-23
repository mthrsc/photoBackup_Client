package ThreadAndRunnable.DeviceScan;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Objects.Folder;
import Objects.FolderList;
import Tools.LogTool;

public class FolderScan {

    private final ArrayList<String> imageFileExtensions = new ArrayList();

    private Context context;
    private LogTool logTool;

    public FolderScan(Context context) {
        this.context = context;
        logTool = new LogTool(this.context);
    }


    public FolderList scanWrapper(FolderList folderList) {
        logTool.logToFile("scanWrapper recursion");
        System.out.println("----------------BeginScan !");

        ArrayList<File> foldersToScan = new ArrayList();

        foldersToScan.add(new File(String.valueOf(Environment.getExternalStorageDirectory())));

        imageFileExtensions.add("jpg");
        imageFileExtensions.add("jpeg");
        imageFileExtensions.add("bmp");
        imageFileExtensions.add("png");
        imageFileExtensions.add("gif");

        try {
            scanRecursion(foldersToScan, folderList);
        } catch (Exception e) {
            logTool.logToFile("scanWrapper recursion error: " + e.getMessage());
            e.printStackTrace();
        }

        logTool.logToFile("scanWrapper folderList size: " + folderList.getSize());

        folderList.setGenerateNewGallery(true);
        logTool.logToFile("scanWrapper recursion done");

        return folderList;
    }

    private void scanRecursion(ArrayList<File> foldersToScan, FolderList folderList) throws Exception {
//        System.out.println("--------Starting scanRecExec with foldersToScan.size: " + foldersToScan.size());
        ArrayList<File> currentLocationFiles = new ArrayList<>();

        File currentLocation = foldersToScan.get(0);

        if (currentLocation.listFiles() != null) {
            //1) separate files from folders
            for (File f : currentLocation.listFiles()) {
                String firstChar = f.getName().substring(0, 1);
                String point = ".";
                if (!f.getName().equals("Android") && !firstChar.equals(point)) {
                    if (f.isDirectory()) {
//                    System.out.println(f.toString() + " is a DIRECTORY");
                        foldersToScan.add(f);
                    } else if (f.isFile()) {
//                    System.out.println(f.toString() + " is a FILE");
                        currentLocationFiles.add(f);
                    }
                }
            }

            //1.1) does current location has image files ?
            int count = 0;
            for (File f2 : currentLocationFiles) {
                String ext = f2.toString().substring(f2.toString().lastIndexOf(".") + 1);
                ext = ext.toLowerCase();

                if (imageFileExtensions.contains(ext)) {
                    // If currentLocation contains image files, is it already in folderList ?
                    count++;
                    if (!folderList.containsPath(currentLocation.toString())) {
                        // Add to folderList
                        folderList.addToList(new Folder(currentLocation.toString(), false));
                    }
                }
                if (count > 0) {
                    break;
                }
            }
        }

        foldersToScan.remove(0);

        if (foldersToScan.size() == 0) {
            return;
        } else {
            scanRecursion(foldersToScan, folderList);
        }
    }

    private void cleanList(FolderList folderList) {
        for (int i = 0; i < folderList.getSize(); i++) {
            File currentLocation = new File(folderList.getFolder(i).getPath());
            if (currentLocation.listFiles().length == 0) {
                folderList.remove(i);
            } else if (currentLocation.listFiles().length > 1) {
                int count = 0;
                for (File f2 : currentLocation.listFiles()) {
                    String ext = f2.toString().substring(f2.toString().lastIndexOf(".") + 1);
                    ext = ext.toLowerCase();

                    if (imageFileExtensions.contains(ext)) {
                        count++;
                    }
                    if (count > 0) {
                        break;
                    }
                }
                if (count == 0) {
                    folderList.remove(i);
                }
            }
        }
    }
}
