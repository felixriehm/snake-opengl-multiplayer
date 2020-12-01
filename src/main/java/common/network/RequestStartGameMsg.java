package common.network;

import java.util.UUID;

public class RequestStartGameMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    public RequestStartGameMsg(UUID sender) {
        super(sender);
    }
}
