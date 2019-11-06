import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    TextField tfFileRename;

    @FXML
    ListView<String>clientFilesList;

    @FXML
    ListView<String>serverFilesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
//        Network.sendMsg(new RefreshRequest(new ArrayList<String>(serverFilesList.getItems())));

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();

                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client/client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if (am instanceof RefreshRequest){
                        RefreshRequest rr = (RefreshRequest)am;
                        refreshServerFilesList(rr.getList());
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();

    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get("client/client_storage")).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshServerFilesList(List <String> list){
        updateUI(() -> {
                serverFilesList.getItems().clear();
                list.stream().forEach(o -> serverFilesList.getItems().add(o));
        });
    }

    public void pressOnRefreshBtn(ActionEvent actionEvent) {
        Network.sendMsg(new RefreshRequest(new ArrayList<String>(serverFilesList.getItems())));
        refreshLocalFilesList();
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(FileRequest.Command.DOWNLOAD,tfFileName.getText()));
            tfFileName.clear();
        }
    }

    public void pressOnSendBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            if (Files.exists(Paths.get("client/client_storage/" + tfFileName.getText()))) {
                try {
                    Network.sendMsg(new FileMessage(Paths.get("client/client_storage/" + tfFileName.getText())));
                    System.out.println("send msg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("file not exist");
            }
            tfFileName.clear();
        }
    }

    public void pressOnDeleteBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(FileRequest.Command.DELETE,tfFileName.getText()));
            tfFileName.clear();
        }
    }

    public void pressOnRenameBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(FileRequest.Command.RENAME,tfFileName.getText(),tfFileRename.getText()));
            tfFileName.clear();
        }
    }
}
