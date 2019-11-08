import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String filename;
    private int partNumber;
    private int partsCount;
    private byte[] data;

    public String getFilename() {
        return filename;
    }

    public int getPartNumber() {
        return partNumber;
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

    public FileMessage(Path path, int partNumber, int partsCount) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
        this.partNumber = partNumber;
        this.partsCount = partsCount;
    }
}
