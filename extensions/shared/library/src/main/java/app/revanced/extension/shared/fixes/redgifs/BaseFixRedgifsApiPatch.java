package app.revanced.extension.shared.fixes.redgifs;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;

import app.revanced.extension.shared.Logger;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class BaseFixRedgifsApiPatch implements Interceptor {
    protected static BaseFixRedgifsApiPatch INSTANCE;
    public abstract String getDefaultUserAgent();

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        if (!request.url().host().equals("api.redgifs.com")) {
            return chain.proceed(request);
        }

        // RedGifs removed /info. Sync only uses it to learn its own public IP,
        // which it then passes as ?user-addr= — no longer needed.
        if (request.url().encodedPath().equals("/info")) {
            return new Response.Builder()
                .message("OK")
                .code(HttpURLConnection.HTTP_OK)
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .header("Content-Type", "application/json")
                .body(ResponseBody.create("{\"remote-addr\":\"\"}", MediaType.get("application/json")))
                .build();
        }

        String userAgent = getDefaultUserAgent();
        boolean forceTokenRefresh = false;
        if (request.header("Authorization") != null) {
            Response response = chain.proceed(request.newBuilder().header("User-Agent", userAgent).build());
            if (response.isSuccessful()) {
                return response;
            }
            // It's possible that the user agent is being overwritten later down in the interceptor
            // chain, so make sure we grab the new user agent from the request headers.
            userAgent = response.request().header("User-Agent");
            response.close();

            // Tokens might become invalid even if they are not expired (IP change, redgifs backend error)
            forceTokenRefresh = true;
        }

        try {
            RedgifsTokenManager.RedgifsToken token = RedgifsTokenManager.refreshToken(userAgent, forceTokenRefresh);

            // Emulate response for old OAuth endpoint
            if (request.url().encodedPath().equals("/v2/oauth/client")) {
                String responseBody = RedgifsTokenManager.getEmulatedOAuthResponseBody(token);
                return new Response.Builder()
                        .message("OK")
                        .code(HttpURLConnection.HTTP_OK)
                        .protocol(Protocol.HTTP_1_1)
                        .request(request)
                        .header("Content-Type", "application/json")
                        .body(ResponseBody.create(
                                responseBody, MediaType.get("application/json")))
                        .build();
            }

            Request modifiedRequest = request.newBuilder()
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .header("User-Agent", userAgent)
                    .build();
            return chain.proceed(modifiedRequest);
        } catch (JSONException ex) {
            Logger.printException(() -> "Could not parse Redgifs response", ex);
            throw new IOException(ex);
        }
    }
}
