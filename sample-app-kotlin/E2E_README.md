# CometChat Android (Kotlin / View) UIKit — E2E Test Suite

End-to-end (E2E) instrumented tests for the **CometChat Android UIKit** as consumed by the
**`sample-app-kotlin`** app (the classic Android **View** UI). The tests drive the real app on an
emulator as a user would — launch, log in, navigate, send/receive messages, react, thread, create
groups, manage members, place calls — and assert the UIKit renders and behaves correctly.

They run against a **live CometChat backend**, not mocks. There is only ever **one device**: a
second ("partner") user is driven from inside the test process over the **CometChat REST API**
(`RestApiHelper`) and, for typing/presence/call-initiation, over the **CometChat JS SDK** running in
an in-test WebView (`CometChatJsDriver`). Real WebSocket events therefore flow back into the app
under test. The suite is **~266 `@Test` methods across 45 test classes**.

> There is a parallel, identical-by-design suite for the Jetpack **Compose** app in
> [`sample-app-compose`](../sample-app-compose/E2E_README.md).

---

## 1. Configuration — **do this first**

All test configuration lives in a **single file**:

```
sample-app-kotlin/src/androidTest/java/com/cometchat/sampleapp/kotlin/e2e/helpers/E2ETestConfig.kt
```

To run the suite against **your own** CometChat app, open that file and replace the values. Nothing
else needs editing — there is no Gradle or `local.properties` wiring involved.

> The values below are **illustrative placeholders** — replace them with your own.

| Constant | What it is | Example (replace) |
|----------|------------|-------------------|
| `APP_ID` | Your CometChat App ID | `1a2b3c4d5e6f7g8h` |
| `REGION` | App region (`us`, `eu`, `in`) | `us` |
| `AUTH_KEY` | App **Auth Key** (auth-only scope) — initializes the UIKit SDK | `0000000000000000000000000000000000000000` |
| `REST_API_KEY` | **fullAccess REST API Key** (NOT the Auth Key) — the one real secret; lets `RestApiHelper` drive the partner user | `1111111111111111111111111111111111111111` |
| `LOGGED_IN_UID` | The UID the app logs in as ("User A") | `alice` |
| `ONE_TO_ONE_UID` | Primary partner — the 1:1 chat peer / main other group member | `bob` |
| `MULTI_MEMBER_UID` | Secondary partner for "multiple members" realtime scenarios | `carol` |
| `GROUP_MEMBER_1/2/3_UID` | Extra members used in member-management tests | `member-1` / `member-2` / `member-3` |
| `REALTIME_GROUP_GUID` | Public group owned by `LOGGED_IN_UID` for realtime group tests | `rt_group_demo` |
| `GROUP_RECEIVE_GUID` | Public group for realtime group-receive tests | `rt_receive_demo` |
| `GROUP_PASSWORD` | Password for the password-protected-group tests | `s3cret` |
| `MEDIA_FILE_URL` | A reachable hosted file used as a media attachment | a public PDF URL |

**Requirements for your app/users:**
- `LOGGED_IN_UID`, `ONE_TO_ONE_UID`, `MULTI_MEMBER_UID`, and the three `GROUP_MEMBER_*` UIDs must
  all exist in your app.
- The two group GUIDs must exist as **public** groups **owned by `LOGGED_IN_UID`** (so permission-gated
  tests can manage members).

**Display names are intentionally NOT stored.** Tests resolve a user's display name from its UID at
runtime via `E2ETestHelper.getUserName(...)`, so they don't break when dashboard data changes.

**Override without editing code (optional).** Any value can be overridden per-run via an
instrumentation runner argument — handy for CI or for keeping the REST key out of a commit:

```bash
./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.appId=YOUR_APP_ID \
  -Pandroid.testInstrumentationRunnerArguments.restApiKey=YOUR_FULLACCESS_REST_KEY
```

Runner-arg keys: `appId`, `region`, `authKey`, `restApiKey`, `testUid`, `partnerUid`, `partner2Uid`.

---

## 2. Quick start

From the repository root:

```bash
# Run the entire suite (boots an emulator if needed, builds, runs, reports)
./sample-app-kotlin/run_e2e_tests.sh

# Run a single test class
./sample-app-kotlin/run_e2e_tests.sh com.cometchat.sampleapp.kotlin.e2e.AuthenticationE2ETest

# Run a single test method
./sample-app-kotlin/run_e2e_tests.sh \
  com.cometchat.sampleapp.kotlin.e2e.realtime.RealTimeMessagingTest#test02_receiveText
```

**Prerequisites:** Android SDK installed (the script finds it via `local.properties` `sdk.dir` or
`$ANDROID_HOME`), at least one **AVD** created in Android Studio, `python3` on PATH (for the summary),
and the credentials set in §1.

---

## 3. What the runner script does

`run_e2e_tests.sh` is a single command for end devs. In order:

1. **Emulator** — if an emulator is already running it uses it; otherwise it boots the first AVD
   (`emulator -list-avds`) and waits for `sys.boot_completed`. The run is **pinned to that emulator**
   via `ANDROID_SERIAL`, so it never accidentally runs on a plugged-in phone.
2. **Config check** — verifies `E2ETestConfig.kt` exists and that `APP_ID`, `AUTH_KEY`,
   `REST_API_KEY`, `LOGGED_IN_UID`, `ONE_TO_ONE_UID` are real values (not placeholders). It stops
   early with a clear message if something looks unset.
3. **Build & run** — runs `:sample-app-kotlin:connectedDebugAndroidTest`, streaming **live per-test
   progress** (each test prints `▶ <test>(<Class>)` as it starts, via the runner's `TestRunner`
   logcat tag) on top of Gradle's own output, and tees everything to a timestamped log.
4. **Results** — parses the JUnit XML and prints `total / passed / failed / errors / skipped` plus
   the names of any failing tests, then prints the exact paths to the **HTML report**, the **XML
   results**, and the **run log**.

> Why the live progress matters: a bare `./gradlew connectedDebugAndroidTest` only prints failures
> and a final tally — passing tests are silent. The script surfaces each test as it runs so a long
> suite doesn't look frozen, while still giving the clean pass/fail summary at the end.

---

## 4. Directory layout

```
sample-app-kotlin/
├── run_e2e_tests.sh              # the one-command runner (this README's §2/§3)
├── e2e-results/                  # timestamped run logs (created by the script)
└── src/androidTest/java/com/cometchat/sampleapp/kotlin/e2e/
    ├── *E2ETest.kt               # 1:1 + group test classes (single-device UI flows)
    ├── realtime/                 # cross-user realtime tests (single emulator, REST/JS-driven)
    │   ├── RealtimeTestBase.kt   #   base: login, openPartnerChat, poll*/pollForMessageInChat
    │   ├── RealTime*Test.kt
    │   └── ...
    └── helpers/
        ├── E2ETestConfig.kt      # single source of truth for all config (see §1)
        ├── E2ETestHelper.kt      # navigation, finders, long-press, getUserName, scroll, etc.
        ├── RestApiHelper.kt      # drives the partner user over the CometChat REST API
        └── CometChatJsDriver.kt  # CometChat JS SDK in a WebView (typing/presence/call-initiate)
```

Reports are written by AGP under `sample-app-kotlin/build/`:
- HTML: `build/reports/androidTests/connected/debug/index.html`
- XML : `build/outputs/androidTest-results/connected/debug/`

---

## 5. How a test is structured

Tests follow the shape **launch + login → navigate → act → wait → assert**, powered by the helpers.

| Phase | Helper(s) | What it does |
|-------|-----------|--------------|
| Launch + login | `RealtimeTestBase` `@Before` / `E2ETestHelper` | Launches the app and logs in as `LOGGED_IN_UID`, lands on Chats. |
| Navigate | `E2ETestHelper.navigateToTab` / `openFirstConversation`, `RealtimeTestBase.openPartnerChat` | Moves to the Chats/Users/Groups tab, a conversation, or a group. |
| Act | UI gestures (`E2ETestHelper`) and/or the partner via `RestApiHelper.*` / `CometChatJsDriver.*` | Performs the action under test (send/edit/delete/react, or have the partner do so). |
| Wait | `RealtimeTestBase.poll` / `pollForMessageInChat` / `pollForTextGone` | Waits for the realtime event to land (polling, never a blind sleep where avoidable). |
| Assert | JUnit `assert*` over UIAutomator finders | Verifies the UI; realtime suites also assert server-side via `RestApiHelper.getMessage(...)` where the View renders something UIAutomator can't read. |

Run order within a class is fixed (`@FixMethodOrder(NAME_ASCENDING)`), so tests are named
`test01_…`, `test02_…`.

---

## 6. The single-emulator, dual-user model

There is one device. Realtime behavior is exercised by driving the **partner** user from the test
process:

```
partner action (RestApiHelper / CometChatJsDriver, onBehalfOf: partnerUid)
        │
        ▼
   CometChat server  ── WebSocket broadcast ──►  app's SDK listener ──► UIKit ──► UIAutomator asserts
```

- **`RestApiHelper`** is the REST client (base `https://{appId}.api-{region}.cometchat.io/v3`, headers
  `apikey` + `onBehalfOf`). It sends/edits/deletes messages, blocks/bans, changes member scope, fetches
  messages for server-side assertions, etc. It retries transient network blips (timeouts) and honors
  REST rate-limit (429) back-off.
- **`CometChatJsDriver`** runs the CometChat **JS** SDK inside a WebView to do the few things REST
  can't: start/stop typing, toggle presence, and **initiate** calls so the app shows its incoming-call
  UI.

---

## 7. Test catalog (45 classes)

**1-to-1 / shared (View)** — `AuthenticationE2ETest`, `ConversationsE2ETest`, `UsersE2ETest`,
`SearchE2ETest`, `OneToOneNavigationE2ETest`, `OneToOneHeaderE2ETest`,
`OneToOneActionsComposerE2ETest`, `OneToOneEdgeCasesE2ETest`, `MessagesE2ETest`,
`MediaMessagesE2ETest`, `ReactionsE2ETest`, `ThreadMessagesE2ETest`, `ReadReceiptsE2ETest`,
`BlockUserE2ETest`, `UserInfoE2ETest`, `CallButtonsE2ETest`, `SharedUIElementsE2ETest`,
`ConfigurationE2ETest`, `NetworkResilienceE2ETest`.

**Groups** — `GroupsE2ETest`, `GroupCreateE2ETest`, `GroupConversationE2ETest`,
`GroupOperationsE2ETest`, `GroupSendMessageE2ETest`, `GroupEditMessageE2ETest`,
`GroupDeleteMessageE2ETest`, `GroupMediaMessagesE2ETest`, `GroupReactionsE2ETest`,
`GroupThreadMessagesE2ETest`, `GroupMembersE2ETest`, `GroupMembersExtendedE2ETest`,
`GroupDetailsE2ETest`, `GroupHeaderE2ETest`, `GroupEdgeCasesE2ETest`.

**Realtime (cross-user, single emulator)** — `RealTimeMessagingTest`, `RealTimeOneToOneTest`,
`RealTimeExtendedE2ETest`, `RealTimeReceiveEdgeE2ETest`, `RealTimeReceiptE2ETest`,
`RealTimePresenceTest`, `RealTimeBlockGroupE2ETest`, `RealTimeGroupTest`, `GroupReceiveMessageE2ETest`,
`RealTimeCallE2ETest`, `RealTimeEdgeRobustnessE2ETest`, `OneToOneGapE2ETest` (+ `RealtimeTestBase`,
the shared base, not a test).

---

## 8. Reports

After a run, the script prints (and these always exist after a connected run):

| Output | Path |
|--------|------|
| HTML report (open in a browser) | `sample-app-kotlin/build/reports/androidTests/connected/debug/index.html` |
| JUnit XML (per-class) | `sample-app-kotlin/build/outputs/androidTest-results/connected/debug/` |
| Run log (full console output) | `sample-app-kotlin/e2e-results/run_<timestamp>.log` |

Open the HTML report with `open "<path>"` on macOS.

---

## 9. Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Tests run on a plugged-in phone too | `connectedDebugAndroidTest` targets **all** attached devices. | Use the script (it pins `ANDROID_SERIAL`), or prefix manual runs with `ANDROID_SERIAL=emulator-5554`. |
| A message is on screen but a finder can't match it | Message text contains a **pair of underscores** — the UIKit markdown formatter renders `_text_` as italic and strips the underscores. | Never put underscores in test message strings (use e.g. `Tag${ts}x$i`). |
| Install fails between runs | `INSTALL_FAILED_INSUFFICIENT_STORAGE`. | Free emulator space: `adb -s <serial> shell pm trim-caches 9999999999`, then re-run. |
| First (cold) test flakes on navigation/receipt | Cold app start races the first WebSocket connect / home render. | Already mitigated with polling + retries; re-running usually clears a one-off. |
| `SocketTimeoutException` mid-run | Transient emulator network blip. | `RestApiHelper` already retries transient timeouts; re-run if a whole run was killed. |
| Script says "No AVD found" | No emulator image created. | Create one in Android Studio → Device Manager, then re-run. |
| `restApiKey`/creds look wrong at startup | Placeholder left in `E2ETestConfig.kt`. | Set real values per §1 (or pass `-Pandroid.testInstrumentationRunnerArguments.restApiKey=…`). |
