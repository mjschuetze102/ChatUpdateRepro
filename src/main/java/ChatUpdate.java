import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.model.block.LayoutBlock;

import java.util.List;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;

public class ChatUpdate {
    private static void startCommand(App app) {
        app.command("/startChatUpdateRepro", (req, ctx) -> {

//            ChatPostMessageResponse chatPostMessageRes = ctx.client().chatPostMessage(r -> r
//                    .blocks(getBlocks(null))
//                    .channel(req.getPayload().getChannelId())
//                    .token(ctx.getBotToken())
//            );

//            if (chatPostMessageRes.isOk()) return ctx.ack();
//            else return Response.builder().statusCode(500).body(chatPostMessageRes.getError()).build();

            return ctx.ack(res -> res
                    .responseType("in_channel")
                    .blocks(getBlocks(null))
            );
        });
    }

    private static void joinAction(App app) {
        app.blockAction("join_action", (req, ctx) -> {

            // The following does not work, resulting in a cant_update_message error
            ChatUpdateResponse chatUpdateRes = ctx.client().chatUpdate(r -> r
                    .blocks(getBlocks(ctx.getRequestUserId()))
                    .channel(req.getPayload().getChannel().getId())
                    .ts(req.getPayload().getMessage().getTs())
                    .token(ctx.getBotToken())
            );

            if (chatUpdateRes.isOk()) return ctx.ack();
            else return Response.builder().statusCode(500).body(chatUpdateRes.getError()).build();

//            // The following does work, but documentation leads to believe the above should work as well
//            ctx.respond(res -> res
//                    .blocks(getBlocks(ctx.getRequestUserId()))
//                    .replaceOriginal(true)
//            );
//            return ctx.ack();
        });
    }

    private static List<LayoutBlock> getBlocks(String userId) {
        return asBlocks(
                context(context -> context
                        .elements(asContextElements(
                                markdownText(mt -> mt
                                    .text(String.format(
                                          "Hello, %s", userId == null ? "be the first to join" : userId
                                    ))
                                )
                        ))
                ),
                divider(),
                actions(action -> action
                        .elements(asElements(
                                button(b -> b.actionId("join_action")
                                        .text(plainText(pt -> pt.text("Join")))
                                        .value("temp")
                                )
                        ))
                )
        );
    }

    public static void main(String[] args) throws Exception {
        App app = new App();

        startCommand(app);
        joinAction(app);

        SlackAppServer server = new SlackAppServer(app);
        server.start();
    }
}
