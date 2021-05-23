package Objects;

import java.io.Serializable;
import java.util.ArrayList;

public class UploadedList implements Serializable {
    private ArrayList<PhotoObject> uploadedPictureList;

    public UploadedList() {
        uploadedPictureList = new ArrayList<>();
    }

    public void addToList(PhotoObject photoObject) {
        uploadedPictureList.add(photoObject);
    }

    public int getSize() {
        return uploadedPictureList.size();
    }

    public PhotoObject getPhotoObject(int index) {
        PhotoObject po = uploadedPictureList.get(index);
        return po;
    }

    public boolean contains(PhotoObject po) {
        for (int i = 0; i < uploadedPictureList.size(); i++) {
            if (po.toString().equals(uploadedPictureList.get(i).toString())) {
                return true;
            }
        }
        return false;
    }
}
