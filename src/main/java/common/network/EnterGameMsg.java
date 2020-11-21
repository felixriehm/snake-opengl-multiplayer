package common.network;

import java.util.UUID;

public class EnterGameMsg  extends BaseMsg{
    private static final long serialVersionUID = 1L;

    public EnterGameMsg(UUID sender) {
        super(sender);
    }
}