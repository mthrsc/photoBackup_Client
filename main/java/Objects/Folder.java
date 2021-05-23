package Objects;

public class Folder {
    String path;
    boolean backup;

    public Folder() {
    }

    public Folder(String path, boolean backup) {
        this.path = path;
        this.backup = backup;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }

    public String getFolderName(){
        String folderName = new String();

        folderName = path.substring(path.lastIndexOf("/") + 1);

        return folderName;
    }
}
