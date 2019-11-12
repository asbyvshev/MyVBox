import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String filename;
    private long fileTotalSize;
    private int partNumber;
    private int partsCount;
    private byte[] data;

    public String getFilename() {
        return filename;
    }

    public long getFileTotalSize() {
        return fileTotalSize;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public FileMessage(Path path) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }

    public FileMessage(String partFileName, long fileTotalSize, int partNumber, int partsCount, byte [] data) throws IOException {
        filename = partFileName;
        this.fileTotalSize = fileTotalSize;
        this.data = data;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
    }
}
