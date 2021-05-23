package Objects;

import java.io.Serializable;
import java.util.ArrayList;

public class FolderList implements Serializable {
    private ArrayList<Folder> folderList;
    private boolean generateNewGallery;

    public FolderList() {
        folderList = new ArrayList<>();
    }

    public ArrayList<Folder> getFolderList() {
        return folderList;
    }

    public void setFolderList(ArrayList<Folder> folderList) {
        this.folderList = folderList;
    }

    public void addToList(Folder folder) {
        folderList.add(folder);
    }

    public int getSize() {
        return folderList.size();
    }

    public String getPath(int i) {
        return folderList.get(i).getPath();
    }

    public Folder getFolder(int i) {
        return folderList.get(i);
    }

    public Folder getFolder(String folderName) {
        for (int i = 0; i < folderList.size(); i++) {
            if (folderList.get(i).getFolderName().equals(folderName)) {
                return folderList.get(i);
            }
        }
        return null;
    }

    public boolean containsPath(String path) {
        boolean b = false;
        for (int i = 0; i < folderList.size(); i++) {
            if (folderList.get(i).getPath().equals(path)) {
                b = true;
                break;
            }
        }
        return b;
    }

    public void remove(int index) {
        folderList.remove(index);
    }

//    public boolean noFolderBackup() {
//        boolean b = false;
//        int k = 0;
//        for (int i = 0; i < folderList.size(); i++) {
//            if (!folderList.get(i).isBackup()) {
//                k++;
//            }
//        }
//
//        if (k == folderList.size()) {
//            b = true;
//        }
//
//        return b;
//    }

    public boolean isGenerateNewGallery() {
        return generateNewGallery;
    }

    public void setGenerateNewGallery(boolean generateNewGallery) {
        this.generateNewGallery = generateNewGallery;
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < folderList.size(); i++) {
            s.append(folderList.get(i).getFolderName() + ", " + folderList.get(i).isBackup() + " - ");
        }

        return "FolderList{" +
                "folderList=" + s.toString() +
                '}';
    }
}
