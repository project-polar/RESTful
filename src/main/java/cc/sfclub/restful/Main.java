package cc.sfclub.restful;

import cc.sfclub.events.server.ServerStartedEvent;
import cc.sfclub.plugin.Plugin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.Getter;
import org.greenrobot.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Plugin {
    @Getter
    private static final Vertx vertx = Vertx.vertx();
    private final Logger logger = LoggerFactory.getLogger("RESTful");
    @Getter
    private static final Router mainRouter = Router.router(vertx);
    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private static final HttpServer httpServer = vertx.createHttpServer();

    @Subscribe
    public void onServerStart(ServerStartedEvent e) {
        logger.info("RESTful Starting!");
        Config.setInst((Config) new Config(getDataFolder().toString()).saveDefaultOrLoad());
        httpServer.requestHandler(mainRouter::accept).listen(Config.getInst().getPort());
        logger.info("Visit http://LISTEN_ADDR:{}/api/v1/ping for check status!", Config.getInst().getPort());
    }
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
