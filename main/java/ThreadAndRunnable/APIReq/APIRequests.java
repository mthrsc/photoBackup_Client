package ThreadAndRunnable.APIReq;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import Builder.PhotoObjectBuilder;
import DiskOperation.ServerSettingsR_W;
import Objects.PhotoObject;
import Objects.ServerSettings;
import ThreadAndRunnable.Exception.WrongPasswordException;
import Tools.JsonConvert;
import Tools.LogTool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class APIRequests {
    private ServerSettings serverSettings;
    private ServerSettingsR_W serverSettingsR_w;
    private Context context;
    private String credentials;
    private String auth;
    private Activity activity;
    private PhotoObjectBuilder pBuilder;
    private JsonConvert jsonConvert;
    private TextView statusTextGallery;
    private LogTool logTool;

    public APIRequests(Context context, Activity activity, TextView statusTextGallery) {
        this.context = context;
        this.activity = activity;
        this.statusTextGallery = statusTextGallery;

        serverSettingsR_w = new ServerSettingsR_W(context);
        serverSettings = serverSettingsR_w.loadServerSettings();
        credentials = "USERNAME" + ":" + serverSettings.getPassword();
        auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        pBuilder = new PhotoObjectBuilder(this.context);
        jsonConvert = new JsonConvert();
        logTool = new LogTool(context);
    }

    public APIRequests(Context context) {
        this.context = context;

        serverSettingsR_w = new ServerSettingsR_W(context);
        serverSettings = serverSettingsR_w.loadServerSettings();
        credentials = "USERNAME" + ":" + serverSettings.getPassword();
        auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        pBuilder = new PhotoObjectBuilder(this.context);
        jsonConvert = new JsonConvert();
        logTool = new LogTool(context);
    }

    private String buildUrl(String extension) {
        //WONT WORK IF ServerSettings not filled
        logTool.logToFile("buildUrl with extension: " + extension);

        StringBuilder url = new StringBuilder();

        url.append("http://");
        url.append(serverSettings.getIp().toString().substring(serverSettings.getIp().toString().lastIndexOf("/") + 1));
        url.append(":");
        url.append(serverSettings.getPort());
        url.append("/rest");
        url.append(extension);

        System.out.println("-----API URI: " + url);
        logTool.logToFile("buildUrl result: " + url.toString());
        return url.toString();
    }

    private OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        return client;
    }

    public boolean isAlive() {
        logTool.logToFile("isAlive starts");

        String res = new String();
        try {
            OkHttpClient client = getClient();

            String url = buildUrl("/hi/alive");

            String credentials = "USERNAME" + ":" + serverSettings.getPassword();
            String auth = "Basic "
                    + Base64.encodeToString(credentials.getBytes(),
                    Base64.NO_WRAP);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", auth)  // add request headers
                    .build();

            Response response = client.newCall(request).execute();


            // Get response body
            res = response.body().string();
            logTool.logToFile("isAlive Success - Answer: " + res);

            System.out.println("-----response.body().string: " + res);

        } catch (Exception e) {
            logTool.logToFile("isAlive Fails: \n" + e.getMessage());
            System.out.println("-----Error from ALIVE?");
            e.printStackTrace();
        }
        if (res.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    public PhotoObject uploadUnit(PhotoObject poToUpload) {
        System.out.println("-----uploadUnit: " + poToUpload.toString());
        logTool.logToFile("uploadUnit: " + poToUpload.toString());

        PhotoObject returnedPo = null;

        String uploadUrl = new String();
        try {
            uploadUrl = buildUrl("/up/file");
        } catch (Exception e) {
            PhotoObject errorPo = new PhotoObject();
            errorPo.setId(-2);
            logTool.logToFile("uploadUnit Error with buildUrl");
            System.out.println("Please verify server details or server status");
            updateTextMessage("Please verify server details or server status");
            return errorPo;
        }

        try {
            if (!probeServerRequest(poToUpload)) {
                updateTextMessage("uploading: " + poToUpload.getFileName());
                logTool.logToFile("uploadUnit uploading : " + poToUpload.toString());
                System.out.println("-----uploadUnit uploading : " + poToUpload.toString());
                String filename = poToUpload.getFileName();
                String mediaType = "image/" + filename.substring(filename.lastIndexOf(".") + 1);
                String sourceFile = poToUpload.getLocationOnDevice();

//                System.out.println("----- fileName: " + filename + " - mediaType: " + mediaType + " - sourceFile: " + sourceFile);

                OkHttpClient client = getClient();


                byte[] getBytes = {};
                File file = new File(sourceFile);
                getBytes = new byte[(int) file.length()];
                InputStream is = new FileInputStream(file);
                is.read(getBytes);
                is.close();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("part", filename, RequestBody.create(getBytes))
                        .build();

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .addHeader("Authorization", auth)  // add request headers
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                // Get response body
                String responseString = response.body().string();
                returnedPo = (PhotoObject) jsonConvert.convertFromJson(responseString, PhotoObject.class);
                System.out.println("-----uploadUnit - response: " + responseString + " for file " + poToUpload.getFileName());
                logTool.logToFile("uploadUnit Success - returnedPo: " + returnedPo.toString());

                if (returnedPo == null) {
                    throw new WrongPasswordException();
                }
                updateTextMessage("");
                return returnedPo;
            } else {
                if (poToUpload.getId() == 0) {
                    returnedPo = requestUpdatedPhotoObject(poToUpload);
                    return returnedPo;
                }
            }
        } catch (IOException | WrongPasswordException | NullPointerException e) {
            logTool.logToFile("uploadUnit Error: \n" + e.getMessage());
            System.out.println("-----Error verifying " + poToUpload + " on server");
            updateTextMessage("Please verify server details or server status");
            PhotoObject errorPo = new PhotoObject();
            errorPo.setId(-2);
            e.printStackTrace();
            return errorPo;
        }
        return returnedPo;
    }

    private PhotoObject requestUpdatedPhotoObject(PhotoObject poToUpload) throws IOException {
        logTool.logToFile("requestUpdatedPhotoObject Starts");

        PhotoObject photoObject = null;

        String probeUrl = new String();
        String responseBody = new String();

        try {
            probeUrl = buildUrl("/down/request");
        } catch (Exception e) {
            PhotoObject errorPo = new PhotoObject();
            errorPo.setId(-2);
            logTool.logToFile("requestUpdatedPhotoObject Error with buildUrl");
            System.out.println("Please verify server details or server status");
            updateTextMessage("Please verify server details or server status");
            return errorPo;
        }

        OkHttpClient client = getClient();
        String json = jsonConvert.convertToJson(poToUpload);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(json, mediaType);

        Request request = new Request.Builder()
                .url(probeUrl)
                .addHeader("Authorization", auth)  // add request headers
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        responseBody = response.body().string();

        photoObject = (PhotoObject) jsonConvert.convertFromJson(responseBody, PhotoObject.class);
        System.out.println("-----responseBody: " + responseBody);
        System.out.println("-----photoObject: " + photoObject.toString());
        logTool.logToFile("requestUpdatedPhotoObject Ends with PO: " + photoObject.toString());
        return photoObject;
    }

    public ArrayList getThirtyFromServer() throws IOException, IllegalStateException {
        logTool.logToFile("getThirtyFromServer Starts");

        ArrayList<PhotoObject> thirtyFromServer = new ArrayList();

        String Url = buildUrl("/down/list");
        String responseBody = new String();

//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = getClient();

        Request request = new Request.Builder()
                .url(Url)
                .addHeader("Authorization", auth)  // add request headers
                .get()
                .build();

        Response response = client.newCall(request).execute();

        // Get response body as JSON
        responseBody = response.body().string();
        System.out.println("-----Success! - response.body().string: " + responseBody);

        Type ListType = TypeToken.getParameterized(ArrayList.class, PhotoObject.class).getType();
        thirtyFromServer = (ArrayList<PhotoObject>) jsonConvert.convertFromJson(responseBody, ListType);

        System.out.println("-----Success! - thirtyFromServer.Size: " + thirtyFromServer.size());
        logTool.logToFile("getThirtyFromServer Ends Successfully");

        return thirtyFromServer;
    }

    public File requestThumbnailFromServer(int id, File thumbnailLocation) throws IOException {
        logTool.logToFile("requestThumbnailFromServer Starts");

        updateTextMessage("Getting thumbnail from server...");
        String thumbUrl = new String();

        thumbUrl = buildUrl("/down/thumb/" + id);

        OkHttpClient client = getClient();

        Request request = new Request.Builder()
                .url(thumbUrl)
                .addHeader("Authorization", auth)  // add request headers
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String h1 = response.header("Content-Disposition");
//        String h2 = response.header("Content-Type");
//        String h3 = response.header("Content-Length");

        String fileName = h1.substring(h1.lastIndexOf("=") + 1);
        InputStream is = response.body().byteStream();

        File targetFile = thumbnailLocation;
        OutputStream os = new FileOutputStream(targetFile);

        int length;
        byte[] bytes = new byte[1024];
        while ((length = is.read(bytes)) != -1) {
            os.write(bytes, 0, length);
        }
        os.close();

        System.out.println("-----requestThumbnailFromServer - fileName " + fileName + " - Destination: " + thumbnailLocation.toString());
        logTool.logToFile("requestThumbnailFromServer Ends Successfully");
        return new File(targetFile.toString());
    }

    public void downloadImage(PhotoObject po, String downloadFolder) throws IOException {
        logTool.logToFile("downloadImage Starts");

        String downloadUrl = new String();

        downloadUrl = buildUrl("/down/file/" + po.getId());

        OkHttpClient client = getClient();

        Request request = new Request.Builder()
                .url(downloadUrl)
                .addHeader("Authorization", auth)  // add request headers
                .get()
                .build();

        Response response = client.newCall(request).execute();

        File targetFile = new File(downloadFolder + po.getFileName());
        InputStream is = response.body().byteStream();
        OutputStream os = new FileOutputStream(targetFile);

        int length;
        byte[] bytes = new byte[1024];
        while ((length = is.read(bytes)) != -1) {
            os.write(bytes, 0, length);
        }
        os.close();
        logTool.logToFile("downloadImage Ends Successfully");
    }

    public boolean probeServerRequest(PhotoObject po) throws IOException {
        logTool.logToFile("probeServerRequest Starts");

        boolean b = false;
        String probeUrl = new String();
        String responseBody = new String();

        probeUrl = buildUrl("/up/probe");

        OkHttpClient client = getClient();

        String json = jsonConvert.convertToJson(po);

//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("part", filename, RequestBody.create(getBytes))
//                .build();
//
//        Request request = new Request.Builder()
//                .url(uploadUrl)
//                .addHeader("Authorization", auth)  // add request headers
//                .post(requestBody)
//                .build();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(probeUrl)
                .addHeader("Authorization", auth)  // add request headers
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        // Get response body
        responseBody = response.body().string();
        System.out.println("-----Success! probeServerRequest - response.body().string: " + responseBody + " for picture: " + po.toString());

        if (responseBody.equals("false")) {
            b = false;
        } else if (responseBody.equals("true")) {
            b = true;
        }
        System.out.println("-----Success! - returning: " + b + " for picture: " + po.toString());

        logTool.logToFile("probeServerRequest Ends Successfully - returns: " + b + " for picture: " + po.toString());
        return b;
    }

    public void updateTextMessage(String message) {
        logTool.logToFile("updateTextMessage: " + message);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextGallery.setText(message);
            }
        });
    }

}
