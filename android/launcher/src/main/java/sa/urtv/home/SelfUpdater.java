package sa.urtv.home;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Updates the home app itself from the project's GitHub release.
 *
 * A newer build is downloaded by itself (every six hours, and once at start); the top bar then shows
 * a gold «تحديث جاهز» pill and one press installs it — Android shows its own "install this update?"
 * screen on top.
 * The APK is only accepted when it is the same package, newer, and signed with the same key as the
 * copy that is running, so a download can never replace the app with something else.
 */
final class SelfUpdater {
    enum State { IDLE, CHECKING, DOWNLOADING, READY, INSTALLING, ERROR }

    static final String ACTION_RESULT = "sa.urtv.home.UPDATE_RESULT";
    private static final long CHECK_EVERY = 6 * 60 * 60 * 1000L;

    private static volatile State state = State.IDLE;
    private static volatile int progress;
    private static volatile String message;
    private static volatile long lastCheck;
    private static Runnable listener;   // runs on the main thread
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private SelfUpdater() {}

    static State state() { return state; }
    static int progress() { return progress; }
    static String message() { return message; }

    static void listen(Runnable r) { listener = r; }

    private static void set(State s, int p, String m) {
        state = s;
        progress = p;
        message = m;
        MAIN.post(() -> {
            Runnable r = listener;
            if (r != null) r.run();
        });
    }

    static void failed(int status, String detail) {
        set(State.READY, 100, detail == null ? "install " + status : detail);
    }

    /** True when the APK is already on the TV, waiting for the press. */
    static boolean ready() { return state == State.READY; }

    /** Looks for a newer build; quiet when offline, and at most once every six hours. */
    static void check(final Context context) {
        long now = System.currentTimeMillis();
        if (now - lastCheck < CHECK_EVERY) return;
        if (state == State.CHECKING || state == State.DOWNLOADING || state == State.INSTALLING
                || state == State.READY) return;
        lastCheck = now;
        final Context app = context.getApplicationContext();
        new Thread(() -> {
            set(State.CHECKING, 0, null);
            try {
                String base = app.getString(R.string.update_base);
                JSONObject json = new JSONObject(fetch(base + "latest.json?t=" + System.currentTimeMillis()));
                int run = json.getInt("run");
                String apk = json.optString("apk", "urtv-home.apk");
                if (run <= runningVersion(app) || apk.isEmpty()) {
                    deleteLeftover(app);   // the APK of an update that has already been installed
                    set(State.IDLE, 0, null);
                    return;
                }
                download(app, base + apk, run);
            } catch (Exception e) {
                android.util.Log.w("SelfUpdater", "check failed: " + e);
                set(State.IDLE, 0, null);   // offline or GitHub is down: the next check tries again
            }
        }, "self-update").start();
    }

    /** No update is needed: drop an APK left behind by an earlier check or an install that finished. */
    private static void deleteLeftover(Context c) {
        File old = new File(new File(c.getFilesDir(), "update"), "home.apk");
        if (old.exists() && !old.delete()) android.util.Log.w("SelfUpdater", "could not delete " + old);
    }

    private static void download(Context c, String url, int run) {
        File dir = new File(c.getFilesDir(), "update");
        if (!dir.exists() && !dir.mkdirs()) { set(State.ERROR, 0, "no cache"); return; }
        File file = new File(dir, "home.apk");
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(10_000);
            con.setReadTimeout(30_000);
            con.setRequestProperty("User-Agent", "urtv-home");
            int total = con.getContentLength();
            set(State.DOWNLOADING, 0, null);
            try (InputStream in = con.getInputStream(); OutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[64 * 1024];
                int n;
                long done = 0;
                while ((n = in.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                    done += n;
                    if (total > 0) set(State.DOWNLOADING, (int) (done * 100 / total), null);
                }
            }
            String problem = verify(c, file, run);
            if (problem != null) {
                android.util.Log.w("SelfUpdater", "rejected the downloaded APK: " + problem);
                file.delete();
                set(State.ERROR, 0, problem);
                return;
            }
            set(State.READY, 100, null);
        } catch (Exception e) {
            android.util.Log.w("SelfUpdater", "download failed: " + e);
            file.delete();
            set(State.ERROR, 0, null);
        } finally {
            if (con != null) con.disconnect();
        }
    }

    /** null when the file is the same app, newer, and signed with the key that is running. */
    private static String verify(Context c, File file, int run) {
        PackageManager pm = c.getPackageManager();
        // Android 9 only fills `signatures` for archives with GET_SIGNATURES; 28+ fills `signingInfo` with
        // GET_SIGNING_CERTIFICATES. Ask for both and take whichever is there.
        int flags = Build.VERSION.SDK_INT >= 28
                ? PackageManager.GET_SIGNING_CERTIFICATES | PackageManager.GET_SIGNATURES
                : PackageManager.GET_SIGNATURES;
        PackageInfo archive = pm.getPackageArchiveInfo(file.getAbsolutePath(), flags);
        if (archive == null || archive.packageName == null) return "bad apk";
        if (!c.getPackageName().equals(archive.packageName)) return "other app";
        try {
            PackageInfo running = pm.getPackageInfo(c.getPackageName(), flags);
            long mine = Build.VERSION.SDK_INT >= 28 ? running.getLongVersionCode() : running.versionCode;
            long theirs = Build.VERSION.SDK_INT >= 28 ? archive.getLongVersionCode() : archive.versionCode;
            if (theirs <= mine || theirs < run) return "older";
            Signature[] mineSigs = signatures(running), theirSigs = signatures(archive);
            if (mineSigs.length == 0 || theirSigs.length == 0) {
                android.util.Log.w("SelfUpdater", "no signatures: running=" + mineSigs.length
                        + " (signingInfo " + (running.signingInfo != null) + "), apk=" + theirSigs.length
                        + " (signingInfo " + (archive.signingInfo != null) + ")");
                return "unsigned";
            }
            if (!Arrays.equals(fingerprint(mineSigs), fingerprint(theirSigs))) {
                return "other key";
            }
            return null;
        } catch (Exception e) {
            android.util.Log.w("SelfUpdater", "verify failed: " + e + " (archive " + file.length() + " bytes, package "
                    + archive.packageName + ", signingInfo " + (archive.signingInfo != null) + ")", e);
            return "unreadable";
        }
    }

    private static Signature[] signatures(PackageInfo info) {
        if (Build.VERSION.SDK_INT >= 28 && info.signingInfo != null) {
            Signature[] apk = info.signingInfo.getApkContentsSigners();
            if (apk != null && apk.length > 0) return apk;
        }
        if (info.signatures != null && info.signatures.length > 0) return info.signatures;
        return new Signature[0];
    }

    private static byte[] fingerprint(Signature[] signatures) throws Exception {
        if (signatures == null || signatures.length == 0) throw new IllegalStateException("unsigned");
        return MessageDigest.getInstance("SHA-256").digest(signatures[0].toByteArray());
    }

    private static long runningVersion(Context c) {
        try {
            PackageInfo info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            return Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    /** One press: hand the downloaded APK to Android's installer (it asks to confirm). */
    static void install(Context context) {
        if (state != State.READY) return;
        File file = new File(new File(context.getFilesDir(), "update"), "home.apk");
        if (!file.exists()) { set(State.IDLE, 0, null); return; }
        set(State.INSTALLING, 100, null);
        try {
            PackageInstaller installer = context.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName(context.getPackageName());
            int id = installer.createSession(params);
            try (PackageInstaller.Session session = installer.openSession(id)) {
                try (OutputStream out = session.openWrite("base.apk", 0, file.length());
                     InputStream in = new FileInputStream(file)) {
                    byte[] buffer = new byte[64 * 1024];
                    int n;
                    while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                    session.fsync(out);
                }
                Intent result = new Intent(context, UpdateReceiver.class).setAction(ACTION_RESULT);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT
                        | (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
                session.commit(PendingIntent.getBroadcast(context, id, result, flags).getIntentSender());
            }
        } catch (Exception e) {
            android.util.Log.w("SelfUpdater", "install failed: " + e);
            set(State.READY, 100, e.getClass().getSimpleName());
        }
    }

    private static String fetch(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(8000);
        con.setReadTimeout(8000);
        con.setRequestProperty("User-Agent", "urtv-home");
        con.setRequestProperty("Cache-Control", "no-cache");
        try {
            StringBuilder body = new StringBuilder();
            try (InputStream in = con.getInputStream()) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer)) > 0) body.append(new String(buffer, 0, n, "UTF-8"));
            }
            return body.toString();
        } finally {
            con.disconnect();
        }
    }
}
