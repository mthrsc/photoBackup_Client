package DiskOperation;

import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import Builder.PhotoObjectBuilder;
import LinkedList.LinkedListPhoto;
import LinkedList.Node;
import Objects.PhotoObject;
import Tools.LogTool;

public class LinkedListPhotoR_W {
    private String savePath;
    private Context context;
    private PhotoObjectBuilder photoObjectBuilder;
    private LogTool logTool;

    public LinkedListPhotoR_W(Context context) {
        this.context = context;
        savePath = context.getFilesDir().toString();
//        savePath = Environment.getExternalStorageDirectory() + "/testphoto/";
        photoObjectBuilder = new PhotoObjectBuilder(this.context);
        logTool = new LogTool(this.context);
    }

    public void saveLinkedList(LinkedListPhoto photoLL) {
        logTool.logToFile("saveLinkedList Starts");

        File output = new File(savePath, "LinkedListPhoto.json");

        FileOutputStream fos;
        BufferedWriter bufferedWriter;

        if (!output.exists()) {
            try {
                output.createNewFile();
            } catch (IOException e) {
                logTool.logToFile("saveLinkedList Error: \n" + e.getMessage());
                e.printStackTrace();
            }
        }
        try {
            fos = new FileOutputStream(output);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
            for (int i = 0; i < photoLL.size(); i++) {
                String object = photoLL.getPhotoObject(i).toString();
                bufferedWriter.write(object);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (Exception e) {
            logTool.logToFile("saveLinkedList Error: \n" + e.getMessage());
        }
        logTool.logToFile("saveLinkedList finished");
    }

    public LinkedListPhoto loadLinkedList() {
        logTool.logToFile("loadLinkedList starts");

        File input = new File(savePath, "LinkedListPhoto.json");
        FileReader fileReader;
        BufferedReader bufferedReader;
        LinkedListPhoto photoLL = new LinkedListPhoto();

        if (!input.exists()) {
            return new LinkedListPhoto();
        }

        try {
            fileReader = new FileReader(input);
            bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while (line != null) {
//                System.out.println("-----Building po from: " + line);
                PhotoObject po = photoObjectBuilder.buildFromString(line);
                photoLL.add(po);
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            logTool.logToFile("loadLinkedList Error: \n" + e.getMessage());
        }
        logTool.logToFile("loadLinkedList finishes Successfully - list size: " + photoLL.size());
        return photoLL;
    }
}
