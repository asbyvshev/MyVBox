public class FileRequest extends AbstractMessage {

    public enum Command {
        DOWNLOAD,DELETE,RENAME,EMPTY
    }
    private Command command = Command.EMPTY;
    private String filename;
    private String fileRename;

    public String getFilename() {
        return filename;
    }

    public String getFileRename() {
        return fileRename;
    }

    public Command getCommand() {
        return command;
    }

    public FileRequest(Command command, String filename) {
        this.command = command;
        this.filename = filename;
    }

    public FileRequest(Command command, String filename, String fileRename) {
        this.command = command;
        this.filename = filename;
        this.fileRename = fileRename;
    }
}
