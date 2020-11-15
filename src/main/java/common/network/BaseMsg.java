package common.network;

import java.io.Serializable;
import java.util.UUID;

public class BaseMsg implements Serializable {
    private final UUID sender;

    protected BaseMsg(UUID uuid) {
        this.sender = uuid;
    }

    public UUID getSender(){ return this.sender; }
}
