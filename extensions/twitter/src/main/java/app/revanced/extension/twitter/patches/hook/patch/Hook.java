package app.revanced.extension.twitter.patches.hook.patch;

import org.jetbrains.annotations.NotNull;

public interface Hook<T> {
    /**
     * Hook implementation.
     *
     * @param data The data to hook.
     * @return The hooked data.
     */
    T hook(@NotNull T data);
}