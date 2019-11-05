import java.util.List;

public class RefreshRequest extends AbstractMessage {
   private List<String> list;

    public List<String> getList() {
        return list;
    }

    public RefreshRequest(List<String> list) {
        this.list = list;
    }
}
