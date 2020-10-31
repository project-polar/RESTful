package cc.sfclub.restful.packets;

import cc.sfclub.restful.Packet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Status extends Packet {
    @Builder.Default
    private String details = "";
    private Code msg;

    public enum Code {
        ARGS_NOT_ENOUGH,
        WRONG_ARGS,
        BOT_NOT_EXISTS,
        GROUP_NOT_EXISTS,
        SUCCEED,
        UNAUTH
    }
}
