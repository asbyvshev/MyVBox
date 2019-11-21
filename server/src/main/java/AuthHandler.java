import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private boolean authorization;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("New unauthorized client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("AuthGatewayHandler: new mesage recieved " + msg.getClass().getName());
        if (!authorization){
            if (msg instanceof AuthRequest) {
                AuthRequest ar = (AuthRequest) msg;
                if (ar.getLogin().equals("login") && ar.getPassword().equals("password")) {
                    authorization = true;
                   AuthRequest authRequest = new AuthRequest(authorization,ar.getLogin(),ar.getPassword(),"client");
                    ctx.writeAndFlush(authRequest);
                    RefreshRequest rr = new RefreshRequest(null);
                    ctx.fireChannelRead(rr);
                } else {
                    System.out.println("Неверные логин/пароль");
                    AuthRequest authRequest = new AuthRequest(authorization);
                    ctx.writeAndFlush(authRequest);
                }
            } else {
                ReferenceCountUtil.release(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
