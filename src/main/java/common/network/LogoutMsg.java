package common.network;

import java.util.UUID;

public class LogoutMsg  extends BaseMsg{
    private static final long serialVersionUID = 1L;

    public LogoutMsg(UUID sender) {
        super(sender);
    }
}