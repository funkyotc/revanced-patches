package app.revanced.patches.reddit.layout.dialogs

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val removeSubredditDialogsPatch = bytecodePatch(
    name = "Remove subreddit dialogs",
    description = "Removes the NSFW community warning and notifications suggestion dialogs by dismissing them automatically."
) {
    compatibleWith("com.reddit.frontpage")

    dependsOn(sharedExtensionPatch)

    apply {
        mapOf(
            frequentUpdatesHandlerMethodMatch to 0,
            nsfwAlertEmitMethodMatch to 1
        ).forEach { (match, boolean) ->
            match.let {
                it.method.apply {
                    val index = it[2]
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    addInstructions(index, "const/4 v$register, $boolean")
                }
            }
        }

        showNsfwAlertDialogMethodMatch.let {
            it.method.apply {
                // Skip building any NSFW dialogs. Continue as if the positive action was taken.
                addInstructions(
                    0,
                    """
                        invoke-interface { p1 }, Lkotlin/jvm/functions/Function0;->invoke()Ljava/lang/Object;
                        return-void
                    """
                )
            }
        }
    }

}