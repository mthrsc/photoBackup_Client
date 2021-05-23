package DiskOperation;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import Objects.UploadedList;
import Tools.LogTool;

public class UploadedListR_W {
    private Context context;
    private LogTool logTool;

    public UploadedListR_W(Context context) {
        this.context = context;
        logTool = new LogTool(context);
    }

    public UploadedList loadUploadedList() {
        logTool.logToFile("loadUploadedList starts");

        UploadedList uploadedList = null;

        Gson g = new Gson();
        File jsonFile = new File(context.getFilesDir().toString(), "UploadedList.json");

        String json = new String();
        StringBuilder temp = new StringBuilder();

        if (jsonFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(jsonFile));
                String line;

                while ((line = br.readLine()) != null) {
                    temp.append(line);
                    temp.append('\n');
                }
                br.close();
                json = temp.toString();

            } catch (IOException e) {
                logTool.logToFile("loadUploadedList Error: \n" + e.getMessage());
                System.out.println("---------Error Reading uploadedList file");
                e.printStackTrace();
                uploadedList = new UploadedList();
            }
        } else {
            logTool.logToFile("loadUploadedList - UploadedList file DO NOT exists");
            System.out.println("---------UploadedList file DO NOT exists");
            uploadedList = new UploadedList();
        }

        if (json.equals("") || json.equals(null)) {
            uploadedList = new UploadedList();
        } else {
            uploadedList = g.fromJson(json, UploadedList.class);
        }
        logTool.logToFile("loadUploadedList finishes successfully - uploadedList: " + uploadedList.toString());
        return uploadedList;
    }

    public boolean saveUploadedListToDisk(UploadedList uploadedList) {
        logTool.logToFile("uploadedList starts");
        boolean b = true;
        File jsonFile = new File(context.getFilesDir().toString(), "UploadedList.json");

        try {
            Gson g = new Gson();
            String json = g.toJson(uploadedList);

            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            logTool.logToFile("uploadedList Error: \n" + e.getMessage());
            e.printStackTrace();
            b = false;
        }
        return b;
    }
}