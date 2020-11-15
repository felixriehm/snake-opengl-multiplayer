package common.network;

import java.util.UUID;

public class JoinGameMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    public JoinGameMsg(UUID sender) {
        super(sender);
    }
}
