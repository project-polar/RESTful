package cc.sfclub.restful;

import lombok.SneakyThrows;

public abstract class Packet {
    @SneakyThrows
    @Override
    public String toString() {
        return Main.getObjectMapper().writeValueAsString(this);
    }
}
