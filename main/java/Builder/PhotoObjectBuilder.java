package Builder;

import android.content.Context;

import java.io.File;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;

import static com.drew.metadata.exif.ExifDirectoryBase.TAG_DATETIME_ORIGINAL;

import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import static com.drew.metadata.exif.GpsDirectory.TAG_ALTITUDE;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import Objects.PhotoObject;
import Tools.LogTool;

public class PhotoObjectBuilder {
    private Context context;
    private LogTool logTool;

    public PhotoObjectBuilder(Context context) {
        this.context = context;
        logTool = new LogTool(this.context);
    }

    public PhotoObject buildPhotoObject(File file) {
        logTool.logToFile("buildPhotoObject starts: " + file.toString());

        PhotoObject po = new PhotoObject(file.getName(), file.getAbsolutePath());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = formatter.format(file.lastModified());
        po.setDateUploaded(date);

        try {
            Double lat = null;
            Double longi = null;
            Double alt = null;
            String dateTaken = new String();
            String altString = new String();

            GpsDirectory gps;
            GeoLocation geo;

            Metadata metadata = ImageMetadataReader.readMetadata(file);

//            System.out.println("\n\n--------Metadata Extraction-------------");
            gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);

            try {
                geo = gps.getGeoLocation();
                lat = geo.getLatitude();
                longi = geo.getLongitude();
            } catch (NullPointerException e) {
//                System.err.println("-------No GPS Data");
            }

            try {
                altString = gps.getDescription(TAG_ALTITUDE);
                altString = altString.substring(0, altString.indexOf(" "));
                alt = Double.parseDouble(altString);
            } catch (NullPointerException e) {
//                System.err.println("-------No Altitude Data");
            }

            ExifSubIFDDirectory exifSub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            try {
                Date d = exifSub.getDate(TAG_DATETIME_ORIGINAL);
                if (d != null) {
                    dateTaken = formatter.format(d);
                } else {
                    dateTaken = null;
                }
            } catch (NullPointerException e) {
                dateTaken = null;
//                System.out.println("-------No date metadata");
            }

            if (dateTaken != null) {
                po.setDateTaken(dateTaken);
            }
            if (lat != null && longi != null) {
                po.setLatitude(lat);
                po.setLongitude(longi);
            }
            if (alt != null) {
                po.setAltitude(alt);
            }
        } catch (ImageProcessingException | IOException | NumberFormatException e) {
            System.out.println("-----No metadata to extract");
//            e.printStackTrace();
        }
        logTool.logToFile("buildPhotoObject from: " + file.toString() + " to: " + po.toString());
        return po;
    }

    //Used when loading object from text file
    public PhotoObject buildFromString(String input) {
        logTool.logToFile("buildFromString from: " + input);
        //    PhotoObject{id=0, fileName='IMG_20210331_202248.jpg', dateTaken='2021-03-31 21:22:48', longitude=null, latitude=null, altitude=null, locationOnDisk='null', dateUploaded='2021-03-31 20:22:48', locationOnDevice='/storage/emulated/0/DCIM/Camera/IMG_20210331_202248.jpg'}
        PhotoObject photoObject = new PhotoObject();
        StringTokenizer st = new StringTokenizer(input, "=");
        String temp = new String();
        int i = -1;

        while (st.hasMoreTokens()) {
            temp = st.nextElement().toString();
//            System.out.println("----- st:" + temp + " -i: " + i);
            switch (i) {
                case 0:
                    //id
                    temp = temp.substring(0, temp.indexOf(","));
//                    System.out.println("-----ID detected: " + temp);
                    photoObject.setId(Integer.parseInt(temp));
                    break;
                case 1:
                    //fileName
                    temp = temp.substring(1, temp.lastIndexOf("'"));
//                    System.out.println("-----fileName detected: " + temp);
                    photoObject.setFileName(temp);
                    break;
                case 2:
                    //dateTaken
                    temp = temp.substring(1, temp.lastIndexOf("'"));
                    if (!temp.equals("null")) {
                        photoObject.setDateTaken(temp);
//                        System.out.println("-----dateTaken detected: " + temp);
                    }
                    break;
                case 3:
                    //longitude
                    temp = temp.substring(0, temp.lastIndexOf(","));
                    try {
                        Double d = Double.parseDouble(temp);
                        photoObject.setLongitude(d);
                    } catch (Exception e) {
//                        System.out.println("-----Could not parse longitude");
                    }
                    break;
                case 4:
                    //latitude
                    temp = temp.substring(0, temp.lastIndexOf(","));
                    try {
                        Double d = Double.parseDouble(temp);
                        photoObject.setLatitude(d);
                    } catch (Exception e) {
//                        System.out.println("-----Could not parse latitude");
                    }
                    break;
                case 5:
                    //altitude
                    temp = temp.substring(0, temp.lastIndexOf(","));
                    try {
                        Double d = Double.parseDouble(temp);
                        photoObject.setAltitude(d);
                    } catch (Exception e) {
//                        System.out.println("-----Could not parse altitude");
                    }
                    break;
                case 6:
                    //locationOnDisk
                    temp = temp.substring(1, temp.lastIndexOf("'"));
                    if (!temp.equals("null")) {
                        photoObject.setLocationOnDisk(temp);
//                        System.out.println("-----locationOnDisk detected: " + temp);
                    }
                    break;
                case 7:
                    //dateUploaded
                    temp = temp.substring(1, temp.lastIndexOf("'"));
                    if (!temp.equals("null")) {
                        photoObject.setDateUploaded(temp);
//                        System.out.println("-----dateUploaded detected: " + temp);

                    }
                    break;
                case 8:
                    //locationOnDevice
                    temp = temp.substring(1, temp.lastIndexOf("'"));
                    if (!temp.equals("null")) {
                        photoObject.setLocationOnDevice(temp);
//                        System.out.println("-----locationOnDevice detected: " + temp);
                    }
                    break;
            }
            i++;
        }
        logTool.logToFile("buildFromString Ends from: " + input + " to: " + photoObject.toString());
        return photoObject;
    }
}
