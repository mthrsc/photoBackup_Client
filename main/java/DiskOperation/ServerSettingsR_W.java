package DiskOperation;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import Objects.ServerSettings;
import Tools.LogTool;

public class ServerSettingsR_W {
    private Context context;
    private LogTool logTool;

    public ServerSettingsR_W(Context context) {
        this.context = context;
        logTool = new LogTool(this.context);
    }

    public boolean saveServerSettings(ServerSettings serverSettings) {
        logTool.logToFile("saveServerSettings starts");

        boolean b = true;

        File jsonFile = new File(context.getFilesDir().toString(), "ServerSettings.json");

        try {
            Gson g = new Gson();
            String json = g.toJson(serverSettings);

            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            logTool.logToFile("saveServerSettings Error: \n" + e.getMessage());
            e.printStackTrace();
            b = false;
        }
        logTool.logToFile("saveServerSettings finishes Successfully - return : " + b);
        return b;
    }

    public ServerSettings loadServerSettings() {
        logTool.logToFile("loadServerSettings Starts");

        ServerSettings serverSettings = null;

        Gson g = new Gson();
        File jsonFile = new File(context.getFilesDir().toString(), "ServerSettings.json");

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
                logTool.logToFile("loadServerSettings Error: \n" + e.getMessage());
                System.out.println("---------Error Reading ServerSettings file");
                e.printStackTrace();
                serverSettings = new ServerSettings();
            }
        } else {
            logTool.logToFile("loadServerSettings - ServerSettings file DO NOT exists");
            System.out.println("---------ServerSettings file DO NOT exists");
            serverSettings = new ServerSettings();
        }

        if (json.equals("") || json.equals(null)) {
            serverSettings = new ServerSettings();
        } else {
            serverSettings = g.fromJson(json, ServerSettings.class);
        }

        logTool.logToFile("loadServerSettings finishes successfully - serverSettings: " + serverSettings.toString());

        return serverSettings;
    }
}
