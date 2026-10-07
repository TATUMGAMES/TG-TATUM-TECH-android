# Tatum Tech API Integration

How the Android app talks to the Tatum Tech backend, how to switch data sources and environments, and how to extend it.

```
Screens / AuthActivity
   │
   ├── TatumTechContentRepository   (events, speakers, partners → UI models)
   └── TatumTechSessionManager      (sign in/up, Google exchange, refresh, sign out)
            │
            ▼
   TatumTechApiProvider ──► TatumTechApiClient            (app: com.tatumgames.tatumtech.android.api)
                                 │  extends
                                 ▼
                           BaseApiClient                  (tatumgames-framework-android: ...framework.android.http)
                                 │  HttpRequestExecutor
                    ┌────────────┴─────────────┐
           OkHttpRequestExecutor      LocalJsonRequestExecutor
              (NETWORK mode)            (LOCAL_JSON mode, app assets)
```

Screens never use OkHttp or the HTTP framework directly; they call the repository or the session manager.

## Configuration

All settings are build-time values read from Gradle properties (`-P...`) or from the git-ignored `local.properties`, and exposed through `BuildConfig`. Never put credentials in tracked files.

| Property | Values | Default | Notes |
|---|---|---|---|
| `tatumTech.environment` | `PRODUCTION`, `STAGE` | `STAGE` | Debug builds only. Release builds always use `PRODUCTION`. |
| `tatumTech.dataSource` | `NETWORK`, `LOCAL_JSON` | `NETWORK` | Release builds always use `NETWORK`. Independent of debug/release. |
| `tatumTech.apiKey` | string | none | Sent as `x-api-key` when set. It is compiled into the APK, so treat it as a public client key. |
| `tatumTech.connectTimeoutMs` | positive number | framework default (15 s) | |

| Environment | Base URL |
|---|---|
| Production | `https://tg-api-new.uc.r.appspot.com` |
| Stage | `https://tg-api-new-stage.uc.r.appspot.com` |

Access token, refresh token, and token expiration are runtime values managed by `TatumTechSessionManager` (see [Authentication](#authentication)). `debugMode` follows `BuildConfig.DEBUG`.

Invalid values fail the build with a message naming the allowed values.

### Run with local JSON data

Uses the bundled `app/src/main/assets/upcoming_events.json` and `partners.json` instead of the network. Use it to compare the known-good content with what the API returns.

```properties
# local.properties
tatumTech.dataSource=LOCAL_JSON
```

or `./gradlew :app:installDebug -PtatumTech.dataSource=LOCAL_JSON`.

In this mode events, event details, speakers, partners, and partner details are served locally. Endpoints without local data (sign-in, sign-up, token refresh, forgot/reset password, sign-out, profile update) return `501 Not Implemented`, so email sign-in shows an error. Google sign-in still works through Firebase and enters the app without a Tatum Tech session.

### Run against the stage API

This is the default for debug builds: remove `tatumTech.environment` from `local.properties` (or set it to `STAGE`) and keep `tatumTech.dataSource=NETWORK`.

### Run against the production API

Release builds always use production. To point a debug build at production, opt in explicitly:

```properties
# local.properties
tatumTech.environment=PRODUCTION
```

or `./gradlew :app:installDebug -PtatumTech.environment=PRODUCTION`.

At startup debug builds log the selected environment, base URL, and why it was chosen (debug build default, set by `tatumTech.environment`, or release build), e.g. `Tatum Tech API: STAGE https://tg-api-new-stage.uc.r.appspot.com (debug build default), data source NETWORK`.

Rebuild after changing any of these values; they are compiled into `BuildConfig`.

## Initialization

`TatumTechApplication.onCreate()` (`app/src/main/java/com/tatumgames/tatumtech/android/application/TatumTechApplication.kt`):

```kotlin
TatumTechApiProvider.initialize(
    this,
    TatumTechAppConfiguration.fromBuildConfig(),
    FirebaseAnalyticsClient()
)
TatumTechApiProvider.getSessionManager().refreshInBackground()
```

`initialize` creates the single `TatumTechApiClient` with the executor for the configured data source, then restores any stored session into it. Afterwards use `TatumTechApiProvider.getInstance()` and `TatumTechApiProvider.getSessionManager()`; calling either before `initialize` throws an `IllegalStateException` explaining what to do.

### Analytics

The framework only knows its vendor-neutral `AnalyticsClient` interface. The app's `FirebaseAnalyticsClient` (`analytics/`) adapts it to Firebase: every failed API call is logged as the existing `api_error` event (sanitized endpoint, method, status code, duration, error type) through `AnalyticsService`. Successful calls are not logged. Pass `null` instead to disable API analytics.

### Threading

Client methods are `suspend` functions that are safe to call from the main thread. Request building, JSON parsing, error mapping, and analytics run on a background dispatcher (`Dispatchers.Default`), the HTTP exchange runs on OkHttp's threads (or `Dispatchers.IO` in `LOCAL_JSON` mode), and the result is returned on the caller's dispatcher, so a composable's coroutine scope receives it on the main thread.

### Debug HTTP logging

Debug builds log every request under the Logcat tag `RQ` and every response (or network failure) under `RS`, with pretty-printed JSON, headers, status, and duration. Filter Logcat with `tag:RQ | tag:RS`. It covers both `NETWORK` and `LOCAL_JSON` modes because `BaseApiClient` logs around whichever executor it uses. Passwords, tokens, secrets, and the `Authorization`/`x-api-key` headers are masked. Release builds never log: it requires both the framework's debug build type and `debugMode` in the client configuration. To customize it (tags, masking, body size), pass a `PrettyHttpTrafficLogger` or your own `HttpTrafficLogger` as `trafficLogger` to `BaseApiClient`; pass `null` to turn it off.

### Response envelope and errors

The API answers `{"status":{"statusCode":N,"statusMessage":"CODE"},"data":{...}}` and reports most failures inside an HTTP 200 response, for example `{"status":{"statusCode":406,"statusMessage":"PASSWORDS_DO_NOT_MATCH"},"data":{}}`. `TatumTechApiClient` checks `status.statusCode` on every response: anything other than 2xx becomes `ApiError.Http` with the API status as `statusCode`, the HTTP status as `responseStatusCode`, and the code as an `ErrorItem`. This also applies to endpoints without data (forgot/reset password, sign-out, profile update), so a rejected request is never reported as success. Observed codes include `USER_ALREADY_EXISTS` (400), `INVALID_EMAIL_FORMAT` (405), `INVALID_PASSWORD_FORMAT` and `PASSWORDS_DO_NOT_MATCH` (406), `WRONG_EMAIL_OR_PASSWORD` (414), `REFRESH_TOKEN_DOES_NOT_EXIST` (419), `UNAUTHORIZED` (401), and `EVENT_NOT_FOUND` (404). A refresh rejected with 400, 401, 403, or 419 signs the user out.

Every failure is logged once in debug builds under the app tag `TG_TatumTech` by `ApiErrorLogger`: environment, method, path (no query string), HTTP and API status, server code, error type, exception, duration, and a summarized response body (credential-like JSON values masked, HTML pages reduced to their title). Request bodies and headers are never included. The API does not return a request ID.

How errors reach the user is described in `app/src/main/docs/APP_DESIGN.md` under "API errors".

## Authentication

### Where state is stored

| What | Where |
|---|---|
| Access token, refresh token, expiry, user, sign-in method | `KeystoreSessionStore`: private SharedPreferences `tatum_tech_session`, encrypted with an AES-256-GCM key held in the Android Keystore (`tatum_tech_session_key`). |
| Installation `deviceId` (random UUID) | Same SharedPreferences, unencrypted (not a secret). |
| Tokens in use | The shared client's configuration (`jwtAccessToken`, `refreshToken`, `tokenExpiration`), kept in sync by `TatumTechSessionManager`. |
| Google identity | Credential Manager Google ID token plus Firebase Auth, managed by the framework's `GoogleAuthClient`. |

Keystore keys are never backed up, so a session restored onto another device cannot be decrypted; it is discarded and the user signs in again.

### Flows

- **Email sign in / sign up**: `SignInScreen` / `SignUpScreen` call `TatumTechSessionManager.signIn` / `signUp`. Success stores the session and opens the app; failure shows the server's message and stays on the screen.
- **Google**: after Firebase sign-in, `AuthScreen` sends the Google ID token to `tatum-tech/signin` (`signInWithGoogle`), waiting at most 10 seconds. If that fails, the user still enters the app, signed in with Google only.
- **Launch**: `AuthActivity` opens the main screen directly when a Tatum Tech session is stored or Firebase still has a Google user; otherwise it shows the auth flow.
- **Token refresh**: centralized in `TatumTechSessionManager.refreshIfNeeded()`. It runs in the background at launch, and before any call made through `authenticated { ... }`, which also retries once after a `401`. The access token (24 hours) is refreshed when it is within one hour of expiry, so active users stay signed in. If the server rejects the refresh token (400/401/403) the session is cleared and the next launch shows the auth flow; network failures never sign the user out.
- **Sign out**: `TatumTechSessionManager.signOut()` calls `tatum-tech/signout` (best effort, 10-second limit) and always clears the stored session. Account deletion calls it before removing the Google account and local data.

### Developer bypass

The email sign-in, sign-up, and forgot-password buttons each have a `TODO Developer bypass` comment. To skip the API while developing, uncomment the line under the TODO and comment out the `submit...()` call below it; reverse that to restore the real flow.

## Adding a Tatum Tech endpoint

1. **Models** – add request/response classes to `api/models/`. Give every response property a default value (so Gson applies defaults for missing fields) and use the `TatumTechResponse<T>` envelope for `{status, data}` responses.
2. **Path** – add the relative path to `TatumTechEndpoints` in `TatumTechApiClient.kt`.
3. **Client method** – add a `suspend` function to `TatumTechApiClient` using the inherited `get` / `post` / `put` / `patch` / `delete` helpers:

   ```kotlin
   suspend fun getEventAttendees(eventId: String): ApiResponse<List<TatumTechAttendee>> =
       get<TatumTechResponse<TatumTechAttendeesData>>(
           "${TatumTechEndpoints.EVENTS}/${encodePathSegment(eventId)}/attendees"
       ).unwrapData().map { it.attendees }
   ```

   Pass `authenticated = true` for endpoints that need the access token, and call them through `TatumTechApiProvider.getSessionManager().authenticated { getEventAttendees(id) }` so the token is refreshed first.
4. **Local JSON (optional)** – to serve it in `LOCAL_JSON` mode, add a route to `LocalJsonRequestExecutor` reading an asset. Without one, the endpoint returns `501` in local mode.
5. **UI** – expose it through a repository (like `TatumTechContentRepository`) that maps API models to UI models, rather than calling the client from composables.
6. **Tests** – add cases to `TatumTechApiClientTest` (path, method, body, headers, parsing) using its fake executor.

## Creating a client for another Tatum Games app

The shared Tatum Games framework module, `tatumgames-framework-android` (HTTP package `com.tatumgames.tatumtech.framework.android.http`), is app-agnostic. Another app depends on it with `implementation(project(":tatumgames-framework-android"))` and defines its own configuration and client:

```kotlin
class ArcadeClientConfiguration private constructor(values: Values) : CommonClientConfiguration(values) {
    class Builder : ConfigurationBuilder<Builder, ArcadeClientConfiguration>() {
        override fun self() = this
        override fun build() = ArcadeClientConfiguration(values())
    }
}

class ArcadeApiClient(
    configuration: ArcadeClientConfiguration,
    executor: HttpRequestExecutor = OkHttpRequestExecutor(configuration.connectTimeout)
) : BaseApiClient<ArcadeClientConfiguration>(configuration, executor) {

    suspend fun getLeaderboard(gameId: String): ApiResponse<Leaderboard> =
        get("leaderboards/${encodePathSegment(gameId)}")

    suspend fun submitScore(score: ScoreRequest): ApiResponse<EmptyStateInfo> =
        post("scores", body = score, authenticated = true)
}

val client = ArcadeApiClient(
    ArcadeClientConfiguration.Builder()
        .setBaseUrl("https://arcade.example.com")
        .setApiKey(BuildConfig.ARCADE_API_KEY)
        .build()
)
```

The framework provides JSON serialization, headers (`Accept`, `x-api-key`, `Authorization: Bearer`), typed `ApiResponse` / `ApiError` results, error-body parsing, cancellation, optional analytics hooks (`AnalyticsClient`), and request interceptors. Each app owns its endpoints, models, environments, session handling, analytics trackers (its own `AnalyticsClient` implementation), and any local fixtures; supply a custom `HttpRequestExecutor` for offline or test data, as `LocalJsonRequestExecutor` does here.
