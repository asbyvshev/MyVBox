public class Authorization {

    public static void tryToAuth(String login, String password) {
        Network.start();
        AuthRequest authRequest = new AuthRequest(login,password);
        Network.sendMsg(authRequest);
    }
}
