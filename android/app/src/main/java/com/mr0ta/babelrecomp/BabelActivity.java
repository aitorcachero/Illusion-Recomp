package com.mr0ta.babelrecomp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.libsdl.app.SDLActivity;

/*
 * The game reads its data "beside the executable": on Android that is the
 * app's external files folder (Android/data/com.mr0ta.babelrecomp/files/)
 * or internal files folder (/data/data/com.mr0ta.babelrecomp/files/).
 * The files bundled in the APK (launcher fonts and images, i18n/, the mod catalog,
 * and the bundled ROM) are copied there.
 */
public class BabelActivity extends SDLActivity {
    private static final String TAG = "babel-recomp";

    private static final String[] SINGLE_FILES = {
        "rom.sfc", "Illusion of Time (ESP).sfc", "rom.cfg", "config.ini", "keybinds.ini"
    };

    private static final String[] DIRECTORIES = {
        "assets", "mods"
    };

    @Override
    protected String[] getLibraries() {
        return new String[] { "main" };   /* SDL3 is linked into libmain.so */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Crash reporting handler
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    Log.e(TAG, "FATAL CRASH in thread " + t.getName(), e);
                    File internalDir = getFilesDir();
                    if (internalDir != null) {
                        File crashFile = new File(internalDir, "crash.txt");
                        try (PrintWriter pw = new PrintWriter(new FileWriter(crashFile, true))) {
                            pw.println("=== CRASH AT " + System.currentTimeMillis() + " IN THREAD " + t.getName() + " ===");
                            e.printStackTrace(pw);
                            pw.println();
                        }
                    }
                } catch (Throwable ignored) {
                }
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(t, e);
                }
            }
        });

        super.onCreate(savedInstanceState);

        try {
            installBundledFiles();
        } catch (Throwable t) {
            Log.e(TAG, "Error installing bundled files", t);
        }

        try {
            addVirtualGamepad();
        } catch (Throwable t) {
            Log.e(TAG, "Error adding virtual gamepad", t);
        }
    }

    // When false, isTelevisionDevice() automatically hides touch controls on TV
    private static final boolean FORCE_NO_TOUCH_CONTROLS = true;

    private boolean isTelevisionDevice() {
        try {
            android.app.UiModeManager uiModeManager = (android.app.UiModeManager) getSystemService(UI_MODE_SERVICE);
            if (uiModeManager != null && uiModeManager.getCurrentModeType() == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION) {
                return true;
            }
            if (getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK)) {
                return true;
            }
            if (!getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_TOUCHSCREEN)) {
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private void addVirtualGamepad() {
        if (FORCE_NO_TOUCH_CONTROLS || isTelevisionDevice()) {
            Log.i(TAG, "Android TV: Virtual touch gamepad overlay disabled");
            return;
        }
        if (mLayout != null) {
            VirtualGamepadView gamepad = new VirtualGamepadView(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            );
            mLayout.addView(gamepad, params);
            Log.i(TAG, "Virtual touch gamepad overlay attached to layout");
        }
    }

    private void installBundledFiles() {
        // Install to both external and internal files directories for resilience
        File externalRoot = getExternalFilesDir(null);
        File internalRoot = getFilesDir();

        if (externalRoot != null) {
            installToDir(externalRoot);
        }
        if (internalRoot != null) {
            installToDir(internalRoot);
        }
    }

    private void installToDir(File root) {
        try {
            if (!root.exists()) {
                root.mkdirs();
            }

            String version;
            try {
                version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName
                        + "-" + getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
            } catch (Exception e) {
                version = "1.2";
            }

            File marker = new File(root, ".bundled-version");
            boolean needCopy = !marker.exists() || !version.equals(readFile(marker));

            // Self-healing: if neither ROM exists, force copy
            if (!new File(root, "rom.sfc").exists() && !new File(root, "Illusion of Time (ESP).sfc").exists()) {
                needCopy = true;
            }

            if (!needCopy) {
                Log.i(TAG, "Bundled files up-to-date in " + root.getAbsolutePath());
                return;
            }

            Log.i(TAG, "Installing bundled files to: " + root.getAbsolutePath());
            AssetManager am = getAssets();

            // 1. Copy explicit single files directly (no am.list() needed)
            for (String item : SINGLE_FILES) {
                File dst = new File(root, item);
                if (dst.exists() && dst.isDirectory()) {
                    deleteRecursive(dst);
                }
                copyFile(am, item, dst);
            }

            // 2. Copy directories recursively
            for (String dir : DIRECTORIES) {
                copyTree(am, dir, new File(root, dir));
            }

            writeFile(marker, version);
            Log.i(TAG, "Installation complete for " + root.getAbsolutePath());
        } catch (Throwable t) {
            Log.e(TAG, "installToDir failed for " + root.getAbsolutePath(), t);
        }
    }

    private static void copyTree(AssetManager am, String path, File dst) {
        try {
            String[] children = am.list(path);
            if (children == null || children.length == 0) {
                // If it's a file, copy it directly
                copyFile(am, path, dst);
                return;
            }
            if (!dst.exists()) {
                dst.mkdirs();
            }
            for (String child : children) {
                copyTree(am, path + "/" + child, new File(dst, child));
            }
        } catch (IOException e) {
            Log.w(TAG, "copyTree " + path + ": " + e);
        }
    }

    private static void copyFile(AssetManager am, String path, File dst) {
        /* The player's own mod selection is kept across updates. */
        if (dst.getName().equals("state.toml") && dst.exists()) return;

        File parent = dst.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (InputStream in = am.open(path)) {
            if (dst.exists() && dst.isDirectory()) {
                deleteRecursive(dst);
            }
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                }
                Log.i(TAG, "Copied asset: " + path + " -> " + dst.getName() + " (" + dst.length() + " bytes)");
            }
        } catch (IOException e) {
            // Optional asset or not present in APK root
            Log.d(TAG, "Asset not found or skipped: " + path);
        }
    }

    private static void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        f.delete();
    }

    private static String readFile(File f) {
        try (InputStream in = new java.io.FileInputStream(f)) {
            byte[] b = new byte[256];
            int n = in.read(b);
            return n > 0 ? new String(b, 0, n) : "";
        } catch (IOException e) {
            return "";
        }
    }

    private static void writeFile(File f, String s) {
        try (OutputStream out = new FileOutputStream(f)) {
            out.write(s.getBytes());
        } catch (IOException e) {
            Log.w(TAG, "write " + f + ": " + e);
        }
    }
}
