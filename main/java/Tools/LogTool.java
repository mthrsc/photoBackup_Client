package Tools;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogTool {
    String filePath;
    File logFile;
    Context context;
    Activity activity;

    public LogTool(Context context) {
        this.context = context;
        filePath = this.context.getExternalFilesDir(null) + "/" + "photoBackupLog_Client.txt";
        logFile = new File(filePath);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                System.out.println("-----Could not create log file");
                e.printStackTrace();
            }
        }
    }

    public void logToFile(String message) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String date = formatter.format(new Date());
        try {
            FileWriter fr = new FileWriter(logFile, true);
            fr.write(date + " - " + message + "\n");
            fr.close();
        } catch (IOException e) {
            System.out.println("-----Could not write log file");
            e.printStackTrace();
        }
    }
}
