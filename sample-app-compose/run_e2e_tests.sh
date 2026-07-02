#!/usr/bin/env bash
#
# run_e2e_tests.sh — one-command E2E runner for sample-app-compose
#
#   ./sample-app-compose/run_e2e_tests.sh                 # run the whole suite
#   ./sample-app-compose/run_e2e_tests.sh <FQ test class> # run one class
#   ./sample-app-compose/run_e2e_tests.sh <class#method>  # run one test
#
# Examples:
#   ./sample-app-compose/run_e2e_tests.sh com.cometchat.sampleapp.compose.e2e.AuthenticationE2ETest
#   ./sample-app-compose/run_e2e_tests.sh com.cometchat.sampleapp.compose.e2e.realtime.RealTimeMessagingTest#test02_receiveText
#
# What it does:
#   1. Ensures exactly ONE Android emulator is up (boots the first AVD if none is running),
#      and pins the run to that emulator only — never a plugged-in phone.
#   2. Sanity-checks E2ETestConfig.kt has real credentials (not placeholders).
#   3. Builds the APKs and runs the instrumented tests, streaming live per-test progress.
#   4. Prints a pass/fail summary and the exact paths to the HTML + XML reports.
#
# ─────────────────────────────────────────────────────────────────────────────
set -uo pipefail

MODULE="sample-app-compose"
PKG="com.cometchat.sampleapp.compose"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(dirname "$SCRIPT_DIR")"
GRADLEW="$ROOT/gradlew"
CONFIG="$SCRIPT_DIR/src/androidTest/java/${PKG//.//}/e2e/helpers/E2ETestConfig.kt"
TEST_FILTER="${1:-}"

bold() { printf '\033[1m%s\033[0m\n' "$1"; }
ok()   { printf '  \033[32m✓\033[0m %s\n' "$1"; }
warn() { printf '  \033[33m⚠\033[0m %s\n' "$1"; }
err()  { printf '  \033[31m✗\033[0m %s\n' "$1"; }

# ─── Resolve the SDK / adb / emulator ────────────────────────────────────────
SDK_DIR="$(sed -nE 's/^sdk\.dir=(.*)/\1/p' "$ROOT/local.properties" 2>/dev/null | head -1)"
[ -z "${SDK_DIR:-}" ] && SDK_DIR="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"
ADB="$SDK_DIR/platform-tools/adb"; [ -x "$ADB" ] || ADB="$(command -v adb || true)"
EMU="$SDK_DIR/emulator/emulator";  [ -x "$EMU" ] || EMU="$(command -v emulator || true)"
[ -x "$ADB" ] || { err "adb not found (set ANDROID_HOME or sdk.dir in local.properties)"; exit 1; }

first_emulator()     { "$ADB" devices | awk '$1 ~ /^emulator-/ && $2=="device" {print $1; exit}'; }
any_emulator_serial(){ "$ADB" devices | awk '$1 ~ /^emulator-/ {print $1; exit}'; }

# ─── 1. Ensure an emulator is running ────────────────────────────────────────
bold "1. Emulator"
SERIAL="$(first_emulator)"
if [ -n "$SERIAL" ]; then
  ok "Using running emulator: $SERIAL"
else
  [ -x "$EMU" ] || { err "No emulator running and the 'emulator' binary was not found."; exit 1; }
  AVD="$("$EMU" -list-avds | head -1)"
  [ -z "$AVD" ] && { err "No AVD found. Create one in Android Studio > Device Manager."; exit 1; }
  warn "No emulator running — booting AVD: $AVD"
  nohup "$EMU" -avd "$AVD" -no-snapshot-save -netdelay none -netspeed full >/dev/null 2>&1 &
  for _ in $(seq 1 60); do SERIAL="$(any_emulator_serial)"; [ -n "$SERIAL" ] && break; sleep 2; done
  [ -z "${SERIAL:-}" ] && { err "Emulator did not appear after ~2 min."; exit 1; }
  "$ADB" -s "$SERIAL" wait-for-device
  printf '  waiting for boot to complete'
  for _ in $(seq 1 90); do
    [ "$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break
    printf '.'; sleep 2
  done
  printf '\n'
  "$ADB" -s "$SERIAL" shell input keyevent 82 >/dev/null 2>&1 || true   # dismiss lock screen
  ok "Emulator ready: $SERIAL"
fi

# ─── 2. Validate config ──────────────────────────────────────────────────────
bold "2. Config ($CONFIG)"
[ -f "$CONFIG" ] || { err "E2ETestConfig.kt not found."; exit 1; }
CONFIG_BAD=0
check_cred() {
  local name="$1" line val
  line="$(grep -E "val ${name}\b" "$CONFIG" | head -1)"
  val="$(printf '%s' "$line" | sed -nE 's/.*\?:[[:space:]]*"([^"]*)".*/\1/p')"
  if [ -z "$val" ]; then warn "$name: no in-code default (must be passed via runner arg)"; return; fi
  case "$val" in
    *PUT_*|*YOUR_*|*REPLACE*|*CHANGE*|*xxxx*|*"<"*) err "$name looks like a placeholder: \"$val\""; CONFIG_BAD=1;;
    *) ok "$name set";;
  esac
}
for c in APP_ID AUTH_KEY REST_API_KEY LOGGED_IN_UID ONE_TO_ONE_UID; do check_cred "$c"; done
if [ "$CONFIG_BAD" -ne 0 ]; then
  err "Fix the credentials in E2ETestConfig.kt (or pass them via -Pandroid.testInstrumentationRunnerArguments.<key>) and re-run."
  exit 1
fi

# ─── 3. Build + run ──────────────────────────────────────────────────────────
bold "3. Build & run tests on $SERIAL"
[ -n "$TEST_FILTER" ] && echo "   filter: $TEST_FILTER" || echo "   running the full suite (~266 tests)"
LOG_DIR="$SCRIPT_DIR/e2e-results"; mkdir -p "$LOG_DIR"
STAMP="$(date +%Y-%m-%d_%H-%M-%S)"
LOG="$LOG_DIR/run_${STAMP}.log"

# Live per-test progress from the instrumentation runner's logcat tag (best-effort).
"$ADB" -s "$SERIAL" logcat -c >/dev/null 2>&1 || true
"$ADB" -s "$SERIAL" logcat -v brief TestRunner:I '*:S' 2>/dev/null \
    | awk '/started:/{sub(/.*started: /,""); print "  ▶ " $0; fflush()}' &
PROGRESS_PID=$!
# Kill BOTH the awk pipe end and the lingering `adb logcat` (which runs forever otherwise and
# would hold the terminal open). Also runs on Ctrl-C / unexpected exit.
stop_progress() {
  kill "$PROGRESS_PID" >/dev/null 2>&1 || true
  pkill -f "logcat -v brief TestRunner:I" >/dev/null 2>&1 || true
}
trap stop_progress EXIT INT TERM

GRADLE_ARGS=(":$MODULE:connectedDebugAndroidTest" --console=plain)
[ -n "$TEST_FILTER" ] && GRADLE_ARGS+=("-Pandroid.testInstrumentationRunnerArguments.class=$TEST_FILTER")

ANDROID_SERIAL="$SERIAL" "$GRADLEW" "${GRADLE_ARGS[@]}" 2>&1 | tee "$LOG"
GRADLE_RC=${PIPESTATUS[0]}
stop_progress

# ─── 4. Summary + report locations ───────────────────────────────────────────
RESULTS_DIR="$SCRIPT_DIR/build/outputs/androidTest-results/connected/debug"
HTML="$SCRIPT_DIR/build/reports/androidTests/connected/debug/index.html"
bold "4. Results"
python3 - "$RESULTS_DIR" <<'PY'
import glob, os, re, sys
d = sys.argv[1]
files = sorted(glob.glob(os.path.join(d, "*.xml")), key=os.path.getmtime, reverse=True)
if not files:
    print("  (no XML results found — the run may have failed before tests executed)"); sys.exit(0)
tot=fail=err=skip=0; failed=[]
for fp in files:
    xml=open(fp, encoding="utf-8", errors="ignore").read()
    for m in re.finditer(r'<testcase\b([^>]*?)(/>|>(.*?)</testcase>)', xml, re.S):
        attrs=m.group(1); body=m.group(3) or ""
        tot+=1
        name=re.search(r'name="([^"]+)"', attrs); cls=re.search(r'classname="([^"]+)"', attrs)
        nm=(cls.group(1)+"#" if cls else "")+(name.group(1) if name else "?")
        if "<failure" in body: fail+=1; failed.append(nm)
        elif "<error" in body: err+=1; failed.append(nm)
        elif "<skipped" in body: skip+=1
passed=tot-fail-err-skip
print(f"  total={tot}  passed={passed}  failed={fail}  errors={err}  skipped={skip}")
if failed:
    print("  failing:")
    for n in failed[:50]: print("    - "+n)
    if len(failed)>50: print(f"    … and {len(failed)-50} more")
PY
echo
bold "Reports"
echo "  HTML  : $HTML"
echo "  XML   : $RESULTS_DIR"
echo "  Log   : $LOG"
[ -f "$HTML" ] && echo "  Open  : open \"$HTML\""

exit "$GRADLE_RC"
