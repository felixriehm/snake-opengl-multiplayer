package common.network;

import java.util.UUID;

public class ExitGameMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    public ExitGameMsg(UUID sender) {
        super(sender);
    }
}