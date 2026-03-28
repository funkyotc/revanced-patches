package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getAuthorizationUrlMethodMatch by composingFirstMethod {
    instructions("client_id=zACVn0dSFGdWqQ"())
}

internal val BytecodePatchContext.requestTokenMethodMatch by composingFirstMethod {
    instructions(
        "zACVn0dSFGdWqQ"(),
        "kDm2tYpu9DqyWFFyPlNcXGEni4k"(String::contains),
    )
}

internal val BytecodePatchContext.getRestClientUserAgentMethod by gettingFirstMethodDeclaratively {
    definingClass("/RestClient;")
    name("getUserAgent")
}

internal val BytecodePatchContext.getRedditUserAgentMethod by gettingFirstMethodDeclaratively {
    definingClass("RedditRetrofitClientModule;")
    name("getUserAgent")
}

internal val BytecodePatchContext.getAuthorizeUrlMethodMatch by composingFirstMethod {
    instructions("redirect_uri=http://baconreader.com/auth"())
}

internal val BytecodePatchContext.isRedirectUrlMethodMatch by composingFirstMethod {
    name("isRedirectUrl")
    instructions("http://baconreader.com/auth"())
}

internal val BytecodePatchContext.runTaskMethodMatch by composingFirstMethod {
    name("runTask")
    instructions("http://baconreader.com/auth"())
}
