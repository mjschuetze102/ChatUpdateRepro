# TriggerRepro
Repro for issue with chat.update using Slack Bolt API

## Summary of Issue

The original message that I'm attempting to update comes from the `ack`
response to a command:

```java
private static void startCommand(App app) {
    app.command("/startChatUpdateRepro", (req, ctx) ->
        ctx.ack(res -> res
                .responseType("in_channel")
                .blocks(getBlocks(null))
        )
    );
}
```

Within the blocks of the message is an action button that when clicked
should cause the message to update:
    
```java
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
    });
}
```

However, the above code does not work, always resulting in a `cant_update_message`
error. The following does work, but the documentation gives the impression that
the above should work as well.

```java
private static void joinAction(App app) {
    app.blockAction("join_action", (req, ctx) -> {

        ctx.respond(res -> res
                .blocks(getBlocks(ctx.getRequestUserId()))
                .replaceOriginal(true)
        );

        return ctx.ack();
    });
}
```

After a bit more testing, I found that actually posting a message would allow
for the bot to update the message:

```java
private static void startCommand(App app) {
    app.command("/startChatUpdateRepro", (req, ctx) -> {
    
        ChatPostMessageResponse chatPostMessageRes = ctx.client().chatPostMessage(r -> r
                        .blocks(getBlocks(null))
                        .channel(req.getPayload().getChannelId())
                        .token(ctx.getBotToken())
        );

        if (chatPostMessageRes.isOk()) return ctx.ack();
        else return Response.builder().statusCode(500).body(chatPostMessageRes.getError()).build();

}
        
```

Which leads me to believe there are actually two contexts for the bot: an App
and the User. But they don't have visibility into each other's posts.
