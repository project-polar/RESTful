package cc.sfclub.restful.packets;

import lombok.Getter;

public abstract class GroupPacket extends BotPacket{
    @Getter
    private long groupId;
}
