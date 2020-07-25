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

import java.io.IOException;

public class Main extends Plugin {
    @Getter
    private static final Vertx vertx=Vertx.vertx();
    @Getter
    private static final Router mainRouter=Router.router(vertx);
    @Getter
    private static final ObjectMapper objectMapper=new ObjectMapper();
    @Getter
    private static final HttpServer httpServer = vertx.createHttpServer();

    @Subscribe
    public void onServerStart(ServerStartedEvent e) {
        Core.getLogger().info("RESTful Starting!");
        Config.setInst((Config) new Config(getDataFolder().toString()).saveDefaultOrLoad());
        loadRouter();
        httpServer.requestHandler(mainRouter::accept).listen(Config.getInst().getPort());
        Core.getLogger().info("Visit http://LISTEN_ADDR:{}/api/v1/ping for check status!", Config.getInst().getPort());
    }

    public static byte[] xorcrypt(byte[] data, byte[] key) {
        if (data == null || data.length == 0 || key == null || key.length == 0) {
            return data;
        }

        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length] ^ (i & 0xFF));
        }

        return result;
    }

    @SneakyThrows
    private void loadRouter() {
        mainRouter.route().handler(BodyHandler.create());
        mainRouter.route().handler(ctx -> {
            if (ctx.getBodyAsJson() == null || !ctx.getBodyAsJson().containsKey("sign")) {
                ctx.response().setStatusCode(403);
                ctx.response().end(Status.builder().code(Status.Code.UNAUTH).build().toString());
                return;
            }
            String sign = ctx.getBodyAsJson().getString("sign"); //todo
            if (Config.getInst().getBaseKey().equals(sign)) {
                ctx.next();
                return;
            }
            ctx.response().setStatusCode(403);
            ctx.response().end(Status.builder().code(Status.Code.UNAUTH).build().toString());
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
                    ctx.response().end(Status.builder().msg("Wrong args").code(Status.Code.ARGS_NOT_ENOUGH).build().toString());
                    return;
                }
                Core.get().bot(groupMessage.getBot()).ifPresent(bot->{
                    bot.getGroup(groupMessage.getGroupId()).ifPresent(group->{
                        group.sendMessage(groupMessage.getMessage());
                        ctx.response().end(Status.builder().code(Status.Code.SUCCEED).build().toString());
                        return;
                    });
                    ctx.response().end(Status.builder().code(Status.Code.GROUP_NOT_EXISTS).build().toString());
                    return;
                });
                ctx.response().end(Status.builder().code(Status.Code.BOT_NOT_EXISTS).msg("Bot not exists or args wrong.").build().toString());
            }catch(IOException e){
                ctx.response().end(Status.builder().msg("Wrong args").code(Status.Code.ARGS_NOT_ENOUGH).build().toString());
            }
        });
        groupRouters.mountSubRouter("/group",groupRouter);
        mainRouter.mountSubRouter("/api/v1",groupRouters);

    }
}
