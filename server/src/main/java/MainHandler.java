

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final int LIMITER = 5 * 1024 * 1024;

    private List<String> serverFilesList;
    private String root = "server/server_storage/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FileRequest) {
                System.out.println("запрос на действий с файлом");
                FileRequest fr = (FileRequest) msg;
                Path path = Paths.get(root + fr.getFilename());
                if (Files.exists(path)) {
                    switch (fr.getCommand()){
                        case DOWNLOAD:
                            if (Files.size(path) > LIMITER){
                                sendBigFile(ctx, fr, path);
                                break;
                            }
                            FileMessage fm = new FileMessage(path);
                            ctx.writeAndFlush(fm);
                            break;
                        case DELETE:
                            Files.delete(path);
                            refreshFileList(ctx);
                            break;
                        case RENAME:
                            Path pathTarget = Paths.get(root + fr.getFileRename());
                            Files.move(path,pathTarget);
                            refreshFileList(ctx);
                            break;
                        case EMPTY:
                            break;
                    }

                }
            }
            if (msg instanceof FileMessage) {
                System.out.println("прислан файл на запись");
                FileMessage fm = (FileMessage) msg;
                if (fm.getPartsCount()!= 0){
                    saveBigFile(fm);
                    refreshFileList(ctx);
                } else {
                    Files.write(Paths.get(root + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                    refreshFileList(ctx);
                }
            }
            if (msg instanceof RefreshRequest) {
                System.out.println("запрос на обновление списка файлов");
                refreshFileList(ctx);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendBigFile(ChannelHandlerContext ctx, FileRequest fr, Path path) throws IOException {
        long fileTotalSize = Files.size(path);
        int partsCount = new Long(fileTotalSize / LIMITER).intValue();
        if (fileTotalSize % LIMITER != 0) {
            partsCount++;
        }
        FileMessage fm = new FileMessage(fr.getFilename() + ".part",fileTotalSize,-1,
                partsCount, new byte[LIMITER]);
        FileInputStream in = new FileInputStream(String.valueOf(path));
        for (int i = 0; i < partsCount; i++) {
            int readBytes = in.read(fm.getData());
            fm.setPartNumber(i + 1);
            if (readBytes < LIMITER) {
                fm.setData(Arrays.copyOfRange(fm.getData(), 0, readBytes));
            }
            ChannelFuture channelFuture = ctx.writeAndFlush(fm);
            System.out.println("Отправлена часть #" + (i + 1));
        }
        in.close();
    }

    private void refreshFileList(ChannelHandlerContext ctx) throws IOException {
        serverFilesList = Files.list(Paths.get(root))
                .map(p -> p.getFileName().toString()).collect(Collectors.toList());
        ctx.writeAndFlush(new RefreshRequest(serverFilesList));
    }

    private void saveBigFile(FileMessage fm) throws IOException {
        boolean append = true;
        if (fm.getPartNumber() == 1) {
            append = false;
        }
        System.out.println(fm.getPartNumber() + " / " + fm.getPartsCount());
        FileOutputStream fos = new FileOutputStream(root + fm.getFilename(), append);
        fos.write(fm.getData());
        fos.close();
        if (fm.getPartNumber() == fm.getPartsCount()
                && fm.getFileTotalSize() == Files.size(Paths.get(root + fm.getFilename()))) {
            String fileName = root + fm.getFilename().substring(0,fm.getFilename().length() - 5);
            Files.move(Paths.get(root + fm.getFilename()),Paths.get(fileName));
        }
    }
}
