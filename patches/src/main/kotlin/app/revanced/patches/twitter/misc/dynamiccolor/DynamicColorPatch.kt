package app.revanced.patches.twitter.misc.dynamiccolor

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch
import java.io.FileWriter
import java.nio.file.Files

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/twitter/patches/misc/DynamicColorPatch;"

@Suppress("unused")
private val dynamicColorBytecodePatch = bytecodePatch {
    compatibleWith(
        "com.twitter.android"(
            "12.8.0-release.0",
            "12.10.0-release.0",
        ),
    )

    dependsOn(sharedExtensionPatch)

    apply {
        // Replace the default X (Formerly Twitter) Blue with the user's Material You palette.
        designTokenMethodMatch.method.addInstructions(
            designTokenMethodMatch[0] + 1,
            """
                invoke-static { v0, v1 }, $EXTENSION_CLASS_DESCRIPTOR->getDynamicColor(J)J
                move-result-wide v0
            """
        )
    }
}

@Suppress("unused")
val dynamicColorPatch = resourcePatch(
    name = "Dynamic color",
    description = "Replaces the default X (Formerly Twitter) Blue with the user's Material You palette.",
) {
    compatibleWith(
        "com.twitter.android"(
            "11.80.0-release.0",
            "12.8.0-release.0",
            "12.10.0-release.0",
        ),
    )

    dependsOn(dynamicColorBytecodePatch)

    apply {
        val resDirectory = get("res")
        if (!resDirectory.isDirectory) throw PatchException("The res folder can not be found.")

        val valuesV31Directory = resDirectory.resolve("values-v31")
        if (!valuesV31Directory.isDirectory) Files.createDirectories(valuesV31Directory.toPath())

        val valuesNightV31Directory = resDirectory.resolve("values-night-v31")
        if (!valuesNightV31Directory.isDirectory) Files.createDirectories(valuesNightV31Directory.toPath())

        listOf(valuesV31Directory, valuesNightV31Directory).forEach { it ->
            val colorsXml = it.resolve("colors.xml")

            if (!colorsXml.exists()) {
                FileWriter(colorsXml).use {
                    it.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><resources></resources>")
                }
            }
        }

        document("res/values-v31/colors.xml").use { document ->
            arrayOf(
                "ps__twitter_blue" to "@color/twitter_blue",
                "ps__twitter_blue_pressed" to "@color/twitter_blue_fill_pressed",
                "twitter_blue" to "@android:color/system_accent1_400",
                "twitter_blue_fill_pressed" to "@android:color/system_accent1_300",
                "twitter_blue_opacity_30" to "@android:color/system_accent1_100",
                "twitter_blue_opacity_50" to "@android:color/system_accent1_200",
                "twitter_blue_opacity_58" to "@android:color/system_accent1_300",
                "deep_transparent_twitter_blue" to "@android:color/system_accent1_200",
            ).forEach { (k, v) ->
                val colorElement = document.createElement("color")

                colorElement.setAttribute("name", k)
                colorElement.textContent = v

                document.getElementsByTagName("resources").item(0).appendChild(colorElement)
            }
        }

        document("res/values-night-v31/colors.xml").use { document ->
            arrayOf(
                "twitter_blue" to "@android:color/system_accent1_200",
                "twitter_blue_fill_pressed" to "@android:color/system_accent1_300",
                "twitter_blue_opacity_30" to "@android:color/system_accent1_50",
                "twitter_blue_opacity_50" to "@android:color/system_accent1_100",
                "twitter_blue_opacity_58" to "@android:color/system_accent1_200",
                "deep_transparent_twitter_blue" to "@android:color/system_accent1_200",
            ).forEach { (k, v) ->
                val colorElement = document.createElement("color")

                colorElement.setAttribute("name", k)
                colorElement.textContent = v

                document.getElementsByTagName("resources").item(0).appendChild(colorElement)
            }
        }
    }
}
