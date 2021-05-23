package Objects;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;

public class PhotoObject implements Serializable {

    private int id;
    private String fileName;
    private String dateTaken;
    private Double longitude;
    private Double latitude;
    private Double altitude;
    private String locationOnDisk;
    private String dateUploaded;
    private String locationOnDevice;

    public PhotoObject() {
    }

    public PhotoObject(String fileName, String locationOnDevice) {
        this.fileName = fileName;
        this.locationOnDevice = locationOnDevice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileNameAndRenameFile(String fileName){
        File oldFile = new File(getLocationOnDevice());
        String oldName = oldFile.getName();
        this.fileName = fileName;
        String newPath = getLocationOnDevice().substring(0, getLocationOnDevice().lastIndexOf("/") + 1);
        File newFile = new File(newPath + fileName);
        System.out.println("-----oldFile path: " + oldFile.getAbsolutePath() + " newFile path: " + newFile.getAbsolutePath());
        oldFile.renameTo(newFile);
        setLocationOnDevice(newFile.toString());
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public String getLocationOnDisk() {
        return locationOnDisk;
    }

    public void setLocationOnDisk(String locationOnDisk) {
        this.locationOnDisk = locationOnDisk;
    }

    public String getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(String dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getLocationOnDevice() {
        return locationOnDevice;
    }

    public void setLocationOnDevice(String locationOnDevice) {
        this.locationOnDevice = locationOnDevice;
    }

    public String getParentFolder() {
        String parentFolder = new String();

        File f = new File(getLocationOnDevice());
        parentFolder = f.getParentFile().getName();
        if (f.getParentFile().getName().equals(null) || f.getParentFile().getName().equals("")) {
            System.out.println("-----Parent is empty");
        }
        return parentFolder;
    }

    @Override
    public String toString() {
        return "PhotoObject{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", dateTaken='" + dateTaken + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", altitude=" + altitude +
                ", locationOnDisk='" + locationOnDisk + '\'' +
                ", dateUploaded='" + dateUploaded + '\'' +
                ", locationOnDevice='" + locationOnDevice + '\'' +
                '}';
    }

    public String toStringCompare() {
        return "PhotoObject{" + "fileName=" + fileName + ", dateTaken=" + dateTaken + ", longitude=" + longitude + ", latitude=" + latitude + ", altitude=" + altitude + '}';
    }

}
