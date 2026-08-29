package app.revanced.patches.shared.misc.gms

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.extensions.string
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.ResourcePatchBuilder
import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.packagename.changePackageNamePatch
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.gms.Constants.APP_AUTHORITIES
import app.revanced.patches.shared.misc.gms.Constants.APP_PERMISSIONS
import app.revanced.patches.shared.misc.gms.Constants.GMS_AUTHORITIES
import app.revanced.patches.shared.misc.gms.Constants.GMS_PERMISSIONS
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import java.net.URI

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/shared/GmsCoreSupport;"

private const val PACKAGE_NAME_REGEX_PATTERN = "^[a-z]\\w*(\\.[a-z]\\w*)+\$"

/**
 * A patch that allows patched Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param fromPackageName The package name of the original app.
 * @param toPackageName The package name to fall back to if no custom package name is specified in patch options.
 * @param getPrimeMethod The fingerprint of the "prime" method that needs to be patched.
 * @param getEarlyReturnMethods The methods that need to be returned early.
 * @param getMainActivityOnCreateMethodToGetInsertIndex The main activity onCreate method
 * and a function to get the index to insert the GmsCore check instruction at.
 * @param extensionPatch The patch responsible for the extension.
 * @param gmsCoreSupportResourcePatchFactory The factory for the corresponding resource patch
 * that is used to patch the resources.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */
fun gmsCoreSupportPatch(
    fromPackageName: String,
    toPackageName: String,
    getPrimeMethod: (BytecodePatchContext.() -> MutableMethod)? = null,
    getEarlyReturnMethods: Set<BytecodePatchContext.() -> MutableMethod> = setOf(),
    getMainActivityOnCreateMethodToGetInsertIndex: Pair<BytecodePatchContext.() -> MutableMethod, BytecodePatchContext.() -> Int>,
    extensionPatch: Patch,
    gmsCoreSupportResourcePatchFactory: (gmsCoreVendorGroupIdOption: Option<String>) -> Patch,
    executeBlock: BytecodePatchContext.() -> Unit = {},
    block: BytecodePatchBuilder.() -> Unit = {},
) = bytecodePatch(
    name = "GmsCore support",
    description = "Allows the app to work without root by using a different package name when patched " +
            "using a GmsCore instead of Google Play Services.",
) {
    val gmsCoreVendorGroupIdOption = stringOption(
        name = "GmsCore vendor group ID",
        default = "app.revanced",
        values = mapOf("ReVanced" to "app.revanced"),
        description = "The vendor's group ID for GmsCore.",
        required = true,
    ) { it!!.matches(Regex(PACKAGE_NAME_REGEX_PATTERN)) }

    dependsOn(
        changePackageNamePatch,
        gmsCoreSupportResourcePatchFactory(gmsCoreVendorGroupIdOption),
        extensionPatch,
    )

    apply {
        val gmsCoreVendorGroupId = gmsCoreVendorGroupIdOption.value!!

        fun transformPackages(string: String): String? = when (string) {
            "com.google",
            "com.google.android.gms",
            in GMS_PERMISSIONS,
            in GMS_AUTHORITIES,
                -> string.prefixOrReplace("com.google", gmsCoreVendorGroupId)

            in APP_PERMISSIONS,
            in APP_AUTHORITIES,
                -> string.prefixOrReplace(fromPackageName, toPackageName)

            else -> null
        }

        fun transformContentUrlAuthority(string: String) = if (!string.startsWith("content://")) {
            null
        } else {
            runCatching { URI.create(string) }.map {
                when (it.authority) {
                    in GMS_AUTHORITIES ->
                        if (it.authority.startsWith("com.google")) {
                            string.replace("com.google", gmsCoreVendorGroupId)
                        } else {
                            string.replace(
                                it.authority,
                                "$gmsCoreVendorGroupId.${it.authority}",
                            )
                        }

                    in APP_AUTHORITIES ->
                        if (it.authority.startsWith(fromPackageName)) {
                            string.replace(
                                it.authority,
                                it.authority.replace(fromPackageName, toPackageName)
                            )
                        } else {
                            string.replace(
                                it.authority,
                                "$toPackageName.${it.authority}",
                            )
                        }

                    else -> null
                }
            }.getOrNull()
        }

        val packageName = setOrGetFallbackPackageName(toPackageName)

        val transformations = arrayOf(
            ::transformPackages,
            ::transformContentUrlAuthority,
        )

        forEachInstructionAsSequence({ _, _, instruction, index ->
            val string = instruction.string ?: return@forEachInstructionAsSequence null

            val transformedString = transformations.firstNotNullOfOrNull { it(string) }
                ?: return@forEachInstructionAsSequence null

            index to transformedString
        }) { method, (index, transformedString) ->
            val register = method.getInstruction<OneRegisterInstruction>(index).registerA

            method.replaceInstruction(
                index,
                BuilderInstruction21c(
                    Opcode.CONST_STRING,
                    register,
                    ImmutableStringReference(transformedString),
                ),
            )
        }

        // Specific method that needs to be patched.
        if (getPrimeMethod != null) getPrimeMethod().apply {
            val index = indexOfFirstInstruction { string == fromPackageName }
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            replaceInstruction(
                index,
                "const-string v$register, \"$packageName\"",
            )
        }

        // GNP registration must use the original package name.
        gnpRegistrationTargetMethodMatch.let {
            val method = it.method
            val resultIndex = it[1]
            val register = method.getInstruction<OneRegisterInstruction>(resultIndex).registerA

            method.replaceInstruction(resultIndex, "const-string v$register, \"$fromPackageName\"")
        }

        // Return these methods early to prevent the app from crashing.
        getEarlyReturnMethods.forEach { it().returnEarly() }
        serviceCheckMethod.returnEarly()

        // Google Play Utility is not present in all apps, so we need to check if it's present.
        googlePlayUtilityMethod?.returnEarly(0)

        // Returns an int ConnectionResult status code (0 == SUCCESS), not a boolean, so returning 0
        // reports Play Services as available. Some bundled Maps SDKs (e.g. Google Photos) gate on it.
        isGooglePlayServicesAvailableMethod?.returnEarly()

        // Set original and patched package names for extension to use.
        originalPackageNameExtensionMethod.returnEarly(fromPackageName)

        // Run GmsCore presence, correct installation and update checks in the main activity.
        getMainActivityOnCreateMethodToGetInsertIndex.let { (getMethod, getInsertIndex) ->
            getMethod().addInstruction(
                getInsertIndex(),
                "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->" +
                        "checkGmsCore(Landroid/app/Activity;)V",
            )
        }

        // Change the vendor of GmsCore in the extension.
        getGmsCoreVendorGroupIdMethod.returnEarly(gmsCoreVendorGroupId)

        executeBlock()
    }

    block()
}

/**
 * Abstract resource patch that allows Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param fromPackageName The package name of the original app.
 * @param toPackageName The package name to fall back to if no custom package name is specified in patch options.
 * @param spoofedPackageSignature The signature of the package to spoof to.
 * @param gmsCoreVendorGroupIdOption The option to get the vendor group ID of GmsCore.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */
fun gmsCoreSupportResourcePatch(
    fromPackageName: String,
    toPackageName: String,
    spoofedPackageSignature: String,
    gmsCoreVendorGroupIdOption: Option<String>,
    executeBlock: ResourcePatchContext.() -> Unit = {},
    block: ResourcePatchBuilder.() -> Unit = {},
) = resourcePatch {
    dependsOn(
        changePackageNamePatch,
        addResourcesPatch,
    )

    val gmsCoreVendorGroupId = gmsCoreVendorGroupIdOption.value!!

    apply {
        addResources("shared", "misc.gms.gmsCoreSupportResourcePatch")

        val toPackageName = setOrGetFallbackPackageName(toPackageName)

        document("AndroidManifest.xml").use { document ->
            document.getElementsByTagName("permission").asSequence().forEach { node ->
                node.attributes.getNamedItem("android:name").apply {
                    APP_PERMISSIONS += textContent

                    textContent = textContent.prefixOrReplace(fromPackageName, toPackageName)
                }
            }

            document.getElementsByTagName("*").asSequence().forEach { node ->
                val permissionAttributeNames = when (node.nodeName) {
                    "uses-permission",
                    "uses-permission-sdk-23",
                    "uses-permission-sdk-m",
                        -> arrayOf("android:name")
                    else -> arrayOf("android:permission", "android:readPermission", "android:writePermission")
                }

                permissionAttributeNames.forEach { attributeName ->
                    node.attributes.getNamedItem(attributeName)?.apply {
                        textContent = when {
                            textContent in GMS_PERMISSIONS ->
                                textContent.replace("com.google", gmsCoreVendorGroupId)
                            attributeName == "android:name" && textContent in APP_PERMISSIONS ->
                                textContent.prefixOrReplace(fromPackageName, toPackageName)
                            else -> textContent
                        }
                    }
                }
            }

            document.getElementsByTagName("provider").asSequence().forEach { node ->
                node.attributes.getNamedItem("android:authorities").apply {
                    textContent = textContent.split(";")
                        .joinToString(";") { authority ->
                            APP_AUTHORITIES += authority

                            authority.prefixOrReplace(fromPackageName, toPackageName)
                        }
                }
            }

            document.getNode("manifest")
                .attributes.getNamedItem("package").textContent = toPackageName

            document.getNode("queries").appendChild(
                document.createElement("package").apply {
                    attributes.setNamedItem(
                        document.createAttribute("android:name").apply {
                            textContent = "$gmsCoreVendorGroupId.android.gms"
                        },
                    )
                },
            )

            val applicationNode = document.getNode("application")

            // Spoof package name and signature.
            applicationNode.appendChild(
                document.createElement("meta-data").apply {
                    setAttribute(
                        "android:name",
                        "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_NAME",
                    )
                    setAttribute("android:value", fromPackageName)
                },
            )

            applicationNode.appendChild(
                document.createElement("meta-data").apply {
                    setAttribute(
                        "android:name",
                        "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_SIGNATURE",
                    )
                    setAttribute("android:value", spoofedPackageSignature)
                },
            )

            // GmsCore presence detection in extension.
            applicationNode.appendChild(
                document.createElement("meta-data").apply {
                    // TODO: The name of this metadata should be dynamic.
                    setAttribute("android:name", "app.revanced.MICROG_PACKAGE_NAME")
                    setAttribute("android:value", "$gmsCoreVendorGroupId.android.gms")
                },
            )
        }

        executeBlock()
    }

    block()
}

private object Constants {
    // Permissions declared by stock Google Play services in these versions:
    // 25.04.32, 25.14.62, 25.26.35, 25.35.62, 25.40.31, 25.45.35, 25.49.32,
    // 26.02.33, 26.04.35, 26.08.33, 26.12.32, 26.15.61, 26.19.34, 26.24.34, 26.25.32, 26.26.34, 26.30.32.
    private val STOCK_GMS_PERMISSIONS = setOf(
        "com.google.android.c2dm.permission.RECEIVE",
        "com.google.android.c2dm.permission.SEND",
        "com.google.android.gms.appevents.permission.SEND_APP_IMPORTANCE_UPDATES",
        "com.google.android.gms.auth.api.phone.permission.SEND",
        "com.google.android.gms.auth.api.signin.permission.REVOCATION_NOTIFICATION",
        "com.google.android.gms.auth.authzen.permission.DEVICE_SYNC_FINISHED",
        "com.google.android.gms.auth.authzen.permission.GCM_DEVICE_PROXIMITY",
        "com.google.android.gms.auth.authzen.permission.KEY_REGISTRATION_FINISHED",
        "com.google.android.gms.auth.cryptauth.permission.CABLEV2_SERVER_LINK",
        "com.google.android.gms.auth.cryptauth.permission.KEY_CHANGE",
        "com.google.android.gms.auth.permission.FACE_UNLOCK",
        "com.google.android.gms.auth.permission.GOOGLE_ACCOUNT_CHANGE",
        "com.google.android.gms.auth.permission.POST_SIGN_IN_ACCOUNT",
        "com.google.android.gms.auth.proximity.permission.SMS_CONNECT_SETUP_REQUESTED",
        "com.google.android.gms.carsetup.DRIVING_MODE_MANAGER",
        "com.google.android.gms.chimera.permission.CONFIG_CHANGE",
        "com.google.android.gms.chimera.permission.QUERY_MODULES",
        "com.google.android.gms.chromesync.permission.CONTENT_PROVIDER_ACCESS",
        "com.google.android.gms.chromesync.permission.METADATA_UPDATED",
        "com.google.android.gms.cloudsave.BIND_EVENT_BROADCAST",
        "com.google.android.gms.common.internal.SHARED_PREFERENCES_PERMISSION",
        "com.google.android.gms.contextmanager.CONTEXT_MANAGER_RESTARTED_BROADCAST",
        "com.google.android.gms.dck.permission.DIGITAL_KEY_IN_USE",
        "com.google.android.gms.dck.permission.DIGITAL_KEY_PRIVILEGED",
        "com.google.android.gms.dck.permission.DIGITAL_KEY_READ",
        "com.google.android.gms.dck.permission.DIGITAL_KEY_WRITE",
        "com.google.android.gms.dck.permission.SE_APPLET_NOTIFICATION",
        "com.google.android.gms.DRIVE",
        "com.google.android.gms.dtdi.permission.START_COMPONENTS",
        "com.google.android.gms.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
        "com.google.android.gms.findmydevice.spot.permission.DEVICE_CHANGES",
        "com.google.android.gms.fraudprotect.permission.CALL_VALIDATION_SERVICE",
        "com.google.android.gms.games.permission.NOTIFY_GAME_EVENT",
        "com.google.android.gms.googlehelp.LAUNCH_SUPPORT_SCREENSHARE",
        "com.google.android.gms.home.matter.BIND_MATTER_COMMISSIONING_SERVICE",
        "com.google.android.gms.learning.permission.LAUNCH_IN_APP_PROXY",
        "com.google.android.gms.locationsharingreporter.periodic.STATUS_UPDATE",
        "com.google.android.gms.magictether.permission.CLIENT_TETHERING_PREFERENCE_CHANGED",
        "com.google.android.gms.magictether.permission.CONNECTED_HOST_CHANGED",
        "com.google.android.gms.magictether.permission.DISABLE_SOFT_AP",
        "com.google.android.gms.magictether.permission.SCANNED_DEVICE",
        "com.google.android.gms.matchstick.permission.BROADCAST_LIGHTER_WEB_INFO",
        "com.google.android.gms.nearby.exposurenotification.EXPOSURE_CALLBACK",
        "com.google.android.gms.people.permission.contactssync.BACKUP_SYNC_STATE_UPDATE_BROADCAST",
        "com.google.android.gms.permission.ACCESS_GESTUREEXCHANGE",
        "com.google.android.gms.permission.ACCESS_MULTIPACKAGE_COMPONENT",
        "com.google.android.gms.permission.ACCESS_NEARBY_SHARE_API",
        "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
        "com.google.android.gms.permission.AD_ID",
        "com.google.android.gms.permission.AD_ID_NOTIFICATION",
        "com.google.android.gms.permission.APPINDEXING",
        "com.google.android.gms.permission.BIND_NETWORK_TASK_SERVICE",
        "com.google.android.gms.permission.BIND_PAYMENTS_CALLBACK_SERVICE",
        "com.google.android.gms.permission.BIOAUTH_CONSENT",
        "com.google.android.gms.permission.BROADCAST_TO_GOOGLEHELP",
        "com.google.android.gms.permission.C2D_MESSAGE",
        "com.google.android.gms.permission.CAR",
        "com.google.android.gms.permission.CAR_FUEL",
        "com.google.android.gms.permission.CAR_MILEAGE",
        "com.google.android.gms.permission.CAR_SPEED",
        "com.google.android.gms.permission.CAR_VENDOR_EXTENSION",
        "com.google.android.gms.permission.CHECKIN_NOW",
        "com.google.android.gms.permission.CONTACTS_SYNC_DELEGATION",
        "com.google.android.gms.permission.GOOGLE_PAY",
        "com.google.android.gms.permission.GRANT_WALLPAPER_PERMISSIONS",
        "com.google.android.gms.permission.GROWTH",
        "com.google.android.gms.permission.INJECT_GESTURE_EVENT",
        "com.google.android.gms.permission.INTERNAL_BROADCAST",
        "com.google.android.gms.permission.NEARBY_START_DISCOVERER",
        "com.google.android.gms.permission.PHENOTYPE_OVERRIDE_FLAGS",
        "com.google.android.gms.permission.PHENOTYPE_UPDATE_BROADCAST",
        "com.google.android.gms.permission.READ_VALUABLES_IMAGES",
        "com.google.android.gms.permission.REPORT_TAP",
        "com.google.android.gms.permission.REQUEST_SCREEN_LOCK_COMPLEXITY",
        "com.google.android.gms.permission.SAFETY_NET",
        "com.google.android.gms.permission.SEND_ANDROID_PAY_DATA",
        "com.google.android.gms.permission.SHOW_PAYMENT_CARD_DETAILS",
        "com.google.android.gms.permission.SHOW_TRANSACTION_RECEIPT",
        "com.google.android.gms.permission.SHOW_WARM_WELCOME_TAPANDPAY_APP",
        "com.google.android.gms.permission.wearable.BUGREPORT_USER_CONSENT",
        "com.google.android.gms.presencemanager.permission.PRESENCE_MANAGER_UPDATE_BROADCAST",
        "com.google.android.gms.security.permission.BANK_SCAM_WARNING",
        "com.google.android.gms.smartdevice.permission.NOTIFY_QUICK_START_STATUS",
        "com.google.android.gms.time.permission.SEND_TRUSTED_TIME_SIGNAL",
        "com.google.android.gms.trustagent.framework.model.DATA_ACCESS",
        "com.google.android.gms.trustagent.framework.model.DATA_CHANGE_NOTIFICATION",
        "com.google.android.gms.trustagent.permission.TRUSTAGENT_STATE",
        "com.google.android.gms.vehicle.permission.SHARED_AUTO_SENSOR_DATA",
        "com.google.android.gms.WRITE_VERIFY_APPS_CONSENT",
        "com.google.android.gtalkservice.permission.GTALK_SERVICE",
        "com.google.android.providers.gsf.permission.READ_GSERVICES",
        "com.google.android.providers.gsf.permission.WRITE_GSERVICES",
        "com.google.android.providers.settings.permission.WRITE_GSETTINGS",
        "com.google.firebase.auth.api.gms.permission.LAUNCH_FEDERATED_SIGN_IN",
    )

    val GMS_PERMISSIONS = STOCK_GMS_PERMISSIONS + setOf(
        "com.google.android.gms.permission.CAR_INFORMATION",
        "com.google.android.googleapps.permission.GOOGLE_AUTH",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.cp",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.local",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.mail",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.writely",
    )

    val GMS_AUTHORITIES = setOf(
        "com.google.android.gms.fileprovider",
        "com.google.android.gms.auth.accounts",
        "com.google.android.gms.chimera",
        "com.google.android.gms.fonts",
        "com.google.android.gms.phenotype",
        "com.google.android.gsf.gservices",
        "com.google.settings",
        "subscribedfeeds",
    )

    val APP_PERMISSIONS = mutableSetOf(
        "org.microg.gms.STATUS_BROADCAST",
        "org.microg.gms.EXTENDED_ACCESS",
        "org.microg.gms.PROVISION"
    )

    val APP_AUTHORITIES = mutableSetOf<String>()
}

fun String.prefixOrReplace(from: String, to: String) = if (startsWith(from)) {
    replace(from, to)
} else {
    "$to.$this"
}
