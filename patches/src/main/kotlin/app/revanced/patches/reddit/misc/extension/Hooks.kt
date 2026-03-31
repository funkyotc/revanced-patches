package app.revanced.patches.reddit.misc.extension

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val mainActivityOnCreateHook = activityOnCreateExtensionHook(
    activityClassType = "Lcom/reddit/launch/main/MainActivity;",
    hookOnCreateBundleMethod = true,
)

internal val frontpageApplicationOnCreateHook = activityOnCreateExtensionHook(
    activityClassType = "Lcom/reddit/frontpage/FrontpageApplication;",
    hookOnCreateBundleMethod = false,
)