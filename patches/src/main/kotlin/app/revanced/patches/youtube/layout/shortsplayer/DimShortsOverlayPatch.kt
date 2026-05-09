package app.revanced.patches.youtube.layout.shortsplayer

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.layout.toolbar.hookToolbar
import app.revanced.patches.youtube.layout.toolbar.toolbarHookPatch
import app.revanced.patches.youtube.misc.navigation.hookNavigationButtonCreated
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.playertype.reelWatchPagerMethodMatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DimShortsOverlayPatch;"

@Suppress("unused")
val dimShortsOverlayPatch = bytecodePatch(
    name = "Dim Shorts overlay",
    description = "Adds an option to reduce the brightness of the Shorts overlay to prevent OLED burn-in.",
) {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
        toolbarHookPatch,
        navigationBarHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "layout.shortsplayer.dimShortsOverlayPatch")

        PreferenceScreen.SHORTS.addPreferences(
            TextPreference("revanced_shorts_overlay_opacity", inputType = InputType.NUMBER),
            SwitchPreference("revanced_shorts_immersive_mode"),
        )

        // Dim toolbar buttons (search, 3-dots) dynamically based on player state.
        hookToolbar("$EXTENSION_CLASS_DESCRIPTOR->dimShortsToolbarButton")

        // Dim bottom navigation bar tabs when Shorts is active.
        hookNavigationButtonCreated(EXTENSION_CLASS_DESCRIPTOR)

        reelWatchPagerMethodMatch.let {
            it.method.apply {
                val viewRegisterIndex = it[-1]
                val viewRegister =
                    getInstruction<OneRegisterInstruction>(viewRegisterIndex).registerA

                addInstruction(
                    viewRegisterIndex + 1,
                    "invoke-static { v$viewRegister }, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->dimShortsPlayerOverlay(Landroid/view/View;)V",
                )
            }
        }
    }
}
