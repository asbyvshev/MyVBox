

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
//            System.out.println("Client connect");
            if (msg instanceof FileRequest) {
                System.out.println("запрос на скачивание");
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get("server/server_storage/" + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get("server/server_storage/" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }
            if (msg instanceof FileMessage) {
                System.out.println("прислан файл на запись");
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("server/server_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
            }
            if (msg instanceof RefreshRequest) {
                System.out.println("запрос на обновление списка файлов");
                RefreshRequest rr = (RefreshRequest) msg;
                List <String> serverFilesList = rr.getList();

                if (serverFilesList.isEmpty()){
                    Files.list(Paths.get("server/server_storage")).map(p -> p.getFileName().toString()).forEach(o -> serverFilesList.add(o));
                    ctx.writeAndFlush(new RefreshRequest(serverFilesList));
                }
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
}
