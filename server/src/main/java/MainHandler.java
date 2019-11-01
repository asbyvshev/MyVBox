

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
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
