package Server;

public class FileInfo {
    private String Filename;
    private String FileType;
    private int FileID;

    public FileInfo(String filename,String fileType)
    {
        Filename=filename;
        FileType=fileType;
    }
    public void setFilename(String filename) {
        Filename = filename;
    }

    public void setFileType(String fileType) {
        FileType = fileType;
    }

    public String getFilename() {
        return Filename;
    }

    public void setFileID(int fileID) {
        FileID = fileID;
    }

    public int getFileID() {
        return FileID;
    }

    public String getFileType() {
        return FileType;
    }

}
