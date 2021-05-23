package DiskOperation;

import android.content.Context;

import Objects.FolderList;
import Tools.LogTool;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FolderListR_W {
    private Context context;
    private LogTool logTool;

    public FolderListR_W(Context context) {
        this.context = context;
        logTool = new LogTool(context);
    }

    public FolderList loadFolderList() {
        logTool.logToFile("loadFolderList starts");
        FolderList folderList = null;

        Gson g = new Gson();
        File jsonFile = new File(context.getFilesDir().toString(), "FolderList.json");

        String json = new String();
        StringBuilder temp = new StringBuilder();

        if (jsonFile.exists()) {
            System.out.println("---------FolderList file exists");
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
                logTool.logToFile("loadFolderList Error: \n" + e.getMessage());
                System.out.println("---------Error Reading folderList file");
                folderList = new FolderList();
            }
        } else if (!jsonFile.exists()) {
            System.out.println("---------FolderList file DO NOT exists");
            folderList = new FolderList();
        }

        if (json.equals("") || json.equals(null)) {
            folderList = new FolderList();
        } else {
            folderList = g.fromJson(json, FolderList.class);
        }
        logTool.logToFile("loadFolderList Finished - folderList size: " + folderList.getSize());
        return folderList;
    }

    public boolean saveFolderListToDisk(FolderList folderList) {
        logTool.logToFile("saveFolderListToDisk starts");

        boolean b = true;
        File jsonFile = new File(context.getFilesDir().toString(), "FolderList.json");

        try {
            Gson g = new Gson();
            String json = g.toJson(folderList);
            if (!jsonFile.exists()) {
                boolean bb = jsonFile.createNewFile();
                logTool.logToFile("saveFolderListToDisk creating new jsonFile: " + bb);
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            logTool.logToFile("saveFolderListToDisk Error: \n" + e.getMessage());
            e.printStackTrace();
        }
        return b;
    }
}
