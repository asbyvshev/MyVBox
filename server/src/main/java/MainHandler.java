

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.FileInputStream;
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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
//            System.out.println("Client connect");
            if (msg instanceof FileRequest) {
                System.out.println("запрос на действий с файлом");
                FileRequest fr = (FileRequest) msg;
                Path path = Paths.get("server/server_storage/" + fr.getFilename());
                if (Files.exists(path)) {
                    switch (fr.getCommand()){
                        case DOWNLOAD:
                            if (Files.size(path) > LIMITER){
                                int partCount = new Long(Files.size(path) / LIMITER).intValue();
                                if (Files.size(path) % LIMITER != 0) {
                                    partCount++;
                                }
                                FileMessage fm = new FileMessage(fr.getFilename() + ".part",-1,
                                        partCount, new byte[LIMITER]);
                                FileInputStream in = new FileInputStream(String.valueOf(path));
                                for (int i = 0; i < partCount; i++) {
                                    int readedBytes = in.read(fm.getData());
                                    fm.setPartNumber(i + 1);
                                    if (readedBytes < LIMITER) {
                                        fm.setData(Arrays.copyOfRange(fm.getData(), 0, readedBytes));
                                    }
                                    ChannelFuture channelFuture = ctx.writeAndFlush(fm);
                                    System.out.println("Отправлена часть #" + (i + 1));
                                }
                                in.close();
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
                            Path pathTarget = Paths.get("server/server_storage/" + fr.getFileRename());
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
                Files.write(Paths.get("server/server_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                refreshFileList(ctx);
            }
            if (msg instanceof RefreshRequest) {
                System.out.println("запрос на обновление списка файлов");
                refreshFileList(ctx);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void refreshFileList(ChannelHandlerContext ctx) throws IOException {
        serverFilesList = Files.list(Paths.get("server/server_storage"))
                .map(p -> p.getFileName().toString()).collect(Collectors.toList());
        ctx.writeAndFlush(new RefreshRequest(serverFilesList));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
