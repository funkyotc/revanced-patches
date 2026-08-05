package app.revanced.extension.twitter.patches.links;

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class CustomizeSharingLinkPatch {
    private static final String LINK_FORMAT = "https://%s/%s/status/%s";

    /**
     * Method is modified during patching.  Do not change.
     */
    private static String getShareDomain() {
        return "";
    }

    private static boolean isReturnUsernameEnabled() {
        return false;
    }

    /**
     * Formats share sheet link for internal share such as sharing by dm.
     *
     * @param contextualPost The object containing tweet context.
     * @return A formatted link if successful; Unmodified otherwise.
     */
    public static String formatInternalShareSheetLink(Object contextualPost) {
        try {
            if (contextualPost == null) {
                return "https://x.com/i/status/";
            }
            String username = "i";

            if (isReturnUsernameEnabled()) {
                Object canonicalPost = ReflectionHelper.invoke(contextualPost, "getCanonicalPost");
                Object userResult = ReflectionHelper.invoke(canonicalPost, "getAuthor");
                String fetchedUsername = (String) ReflectionHelper.invoke(userResult, "getScreenName");

                if (fetchedUsername != null && !fetchedUsername.isEmpty()) {
                    username = fetchedUsername;
                }
            }

            return String.format(LINK_FORMAT, getShareDomain(), username, "");
        } catch (Exception e) {
            return "https://x.com/i/status/";
        }
    }

    /**
     * Formats share sheet link for external share such as {@code Copy link} or {@code Share via...} etc.
     *
     * @param object The root object containing contextual post data.
     * @return A formatted link if successful; Unmodified otherwise.
     */
    public static String formatExternalShareSheetLink(Object object) {
        Object contextualPost = ReflectionHelper.getFieldValueByType(object, "ContextualPost");

        return formatInternalShareSheetLink(contextualPost);
    }

    /**
     * Simplifies Reflection API usage by locating and invoking members based on their types.
     * Internally handles accessibility and reduces boilerplate code.
     */
    public static class ReflectionHelper {
        /**
         * Invokes a method by name, searching the entire class hierarchy including interfaces.
         *
         * @param object The target object to invoke on.
         * @param methodName The name of the method to be invoked.
         * @return The result of the invocation if successful; {@code null} otherwise.
         */
        public static Object invoke(Object object, String methodName) {
            if (object == null) return null;
            try {
                for (Method m : object.getClass().getMethods()) {
                    if (m.getName().equals(methodName)) {
                        m.setAccessible(true);
                        return m.invoke(object);
                    }
                }
            } catch (Exception e) { }
            return null;
        }

        /**
         * Retrieves a field's value whose type name contains the specified string.
         *
         * @param object The target object to inspect.
         * @param typeName The partial or full name of the class type to search for.
         * @return The field's value if found; {@code null} otherwise.
         */
        public static Object getFieldValueByType(Object object, String typeName) {
            if (object == null) return null;
            try {
                for (Field f : object.getClass().getDeclaredFields()) {
                    if (f.getType().getName().contains(typeName)) {
                        f.setAccessible(true);
                        return f.get(object);
                    }
                }
            } catch (Exception e) { }
            return null;
        }
    }
}