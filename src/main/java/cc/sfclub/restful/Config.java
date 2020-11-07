package cc.sfclub.restful;

import cc.sfclub.util.common.JsonConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Config extends JsonConfig {
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private static Config inst;
    private int port = 8080;

    public Config(String rootDir) {
        super(rootDir);
    }
}
