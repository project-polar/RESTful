package cc.sfclub.restful.packets;

import cc.sfclub.restful.Packet;
import lombok.Getter;

public abstract class BotPacket extends Packet{
    @Getter
    private String bot;
    @Getter
    private String sign;
}
