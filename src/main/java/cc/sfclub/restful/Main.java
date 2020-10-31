package cc.sfclub.restful;

import cc.sfclub.core.Core;
import cc.sfclub.events.server.ServerStartedEvent;
import cc.sfclub.plugin.Plugin;
import cc.sfclub.restful.packets.GroupMessage;
import cc.sfclub.restful.packets.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.greenrobot.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
        loadRouter();
        httpServer.requestHandler(mainRouter::accept).listen(Config.getInst().getPort());
        logger.info("Visit http://LISTEN_ADDR:{}/api/v1/ping for check status!", Config.getInst().getPort());
    }

    @SneakyThrows
    private void loadRouter() {
        mainRouter.route().handler(BodyHandler.create());
        mainRouter.route("/api/v1/*").handler(ctx -> {
            if (ctx.request().getHeader("X-Sign") == null) {
                ctx.response().setStatusCode(403);
                ctx.response().end(Status.builder().msg(Status.Code.UNAUTH).build().toString());
                return;
            }
            String sign = ctx.request().getHeader("X-Sign"); //todo
            if (Config.getInst().getBaseKey().equals(sign)) {
                ctx.next();
                return;
            }
            ctx.response().setStatusCode(403);
            ctx.response().end(Status.builder().msg(Status.Code.UNAUTH).build().toString());
        });
        Router groupRouters = Router.router(vertx);
        groupRouters.route("/ping").handler(ctx -> {
            ctx.response().end("RESTful Running!");
        });
        Router groupRouter = Router.router(vertx);
        groupRouter.post("/message").blockingHandler(ctx -> {
            GroupMessage groupMessage;
            try {
                groupMessage = objectMapper.readValue(ctx.getBodyAsString(), GroupMessage.class);
                if (groupMessage == null || groupMessage.getGroupId() == -1 || groupMessage.getMessage() == null || groupMessage.getBot() == null) {
                    ctx.response().end(Status.builder().details("Wrong args").msg(Status.Code.ARGS_NOT_ENOUGH).build().toString());
                    return;
                }
                Core.get().bot(groupMessage.getBot()).ifPresent(bot -> {
                    if (!bot.getGroup(groupMessage.getGroupId()).isPresent()) {
                        ctx.response().end(Status.builder().msg(Status.Code.GROUP_NOT_EXISTS).build().toString());
                    }
                    bot.getGroup(groupMessage.getGroupId()).ifPresent(group -> {
                        group.sendMessage(groupMessage.getMessage());
                        ctx.response().end(Status.builder().msg(Status.Code.SUCCEED).build().toString());
                        return;
                    });
                    return;
                });
                ctx.response().end(Status.builder().msg(Status.Code.BOT_NOT_EXISTS).details("Bot not exists or args wrong.").build().toString());
            } catch (IOException e) {
                ctx.response().end(Status.builder().details("Wrong args").msg(Status.Code.ARGS_NOT_ENOUGH).build().toString());
            }
        });
        groupRouters.mountSubRouter("/group", groupRouter);
        mainRouter.mountSubRouter("/api/v1", groupRouters);

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
