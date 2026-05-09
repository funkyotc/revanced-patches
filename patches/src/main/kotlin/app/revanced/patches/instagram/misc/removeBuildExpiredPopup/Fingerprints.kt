
package app.revanced.patches.instagram.misc.removeBuildExpiredPopup

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.appUpdateLockoutPresenterMethod by gettingFirstMethodDeclaratively(
    "lockout_active",
) {
    returnType("V")
    parameterTypes("Landroidx/fragment/app/FragmentActivity;", "LX/748;")
}
