package app.revanced.patches.strava.route.export

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.strava.misc.extension.sharedExtensionPatch

private const val ROUTE_EXPORT_CLASS_DESCRIPTOR = "Lapp/revanced/extension/strava/AddRouteExportPatch;"

@Suppress("unused")
val addRouteExportPatch = bytecodePatch(
    name = "Add route export",
    description = "Extends the route menu with items to export them in GPX or TCX formats.",
) {
    compatibleWith("com.strava")

    dependsOn(sharedExtensionPatch)

    apply {
        // Intercept onCreate to display the export dialog over.
        shareSheetActivityOnCreateMethod.addInstructions(
            0,
            "invoke-static {p0}, $ROUTE_EXPORT_CLASS_DESCRIPTOR->showExportDialog(Landroid/app/Activity;)V"
        )
    }
}
