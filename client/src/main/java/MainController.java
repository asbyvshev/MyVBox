import io.netty.channel.ChannelFuture;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final int LIMITER = 5*1024*1024;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();

//        Network.sendMsg(new RefreshRequest(new ArrayList<String>(serverFilesList.getItems()))); - запихнуть в авторизацию

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();

                    if (am instanceof FileMessage) {

                        FileMessage fm = (FileMessage) am;

                        if (fm.getPartsCount()!= 0){
                            boolean append = true;
                            if (fm.getPartNumber() == 1) {
                                append = false;
                            }
                            System.out.println(fm.getPartNumber() + " / " + fm.getPartsCount());
                            FileOutputStream fos = new FileOutputStream("client/client_storage/" + fm.getFilename(), append);
                            fos.write(fm.getData());
                            fos.close();
//                            if (fm.getPartNumber() == fm.getPartsCount()) {
//                                  добавить переименование в нормально название без ".part"
//                            }
                        }

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
            String filePath = "client/client_storage/" + tfFileName.getText();
            if (Files.exists(Paths.get(filePath))) {

                try {
                    long fileSize = Files.size(Paths.get(filePath));

                    if (fileSize > LIMITER) {
//                        sendBigFile(filePath,fileSize);
                    }

                    Network.sendMsg(new FileMessage(Paths.get(filePath)));


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("file not exist");
            }
            tfFileName.clear();
        }
    }

    private void sendBigFile(String filePath, long fileSize) throws IOException {

            int partsCount = new Long(fileSize / LIMITER).intValue();
            if (fileSize % LIMITER != 0) {
                partsCount++;
            }
            FileMessage fm = new FileMessage(tfFileName.getText() + ".part",-1,
                    partsCount, new byte[LIMITER]);
            FileInputStream in = new FileInputStream(filePath);
            for (int i = 0; i < partsCount; i++) {
                int readBytes = in.read(fm.getData());
                fm.setPartNumber(i + 1);
                if (readBytes < LIMITER) {
                    fm.setData(Arrays.copyOfRange(fm.getData(), 0, readBytes));
                }
                Network.sendMsg(fm);
//                ChannelFuture channelFuture = ctx.writeAndFlush(fm);
                System.out.println("Отправлена часть #" + (i + 1));
            }
            in.close();

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
