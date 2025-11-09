package com.vinikrish.birdchecklistandroid.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Utility class for managing progress dialogs during Firebase operations
 * Provides consistent progress bar implementation across the app
 */
public class ProgressDialogUtils {
    
    private static ProgressDialog currentProgressDialog;
    
    /**
     * Show a progress dialog with a custom message
     * @param context The context (Activity or Fragment)
     * @param message The message to display
     * @return ProgressDialog instance for further customization if needed
     */
    public static ProgressDialog showProgressDialog(Context context, String message) {
        // Dismiss any existing dialog first
        dismissProgressDialog();
        
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                createAndShowDialog(context, message);
            });
        } else {
            createAndShowDialog(context, message);
        }
        
        return currentProgressDialog;
    }
    
    /**
     * Show a progress dialog for loading data
     * @param context The context (Activity or Fragment)
     * @return ProgressDialog instance
     */
    public static ProgressDialog showLoadingDialog(Context context) {
        return showProgressDialog(context, "Loading data...");
    }
    
    /**
     * Show a progress dialog for saving data
     * @param context The context (Activity or Fragment)
     * @return ProgressDialog instance
     */
    public static ProgressDialog showSavingDialog(Context context) {
        return showProgressDialog(context, "Saving data...");
    }
    
    /**
     * Show a progress dialog for loading life list
     * @param context The context (Activity or Fragment)
     * @return ProgressDialog instance
     */
    public static ProgressDialog showLoadingLifeListDialog(Context context) {
        return showProgressDialog(context, "Loading your life list...");
    }
    
    /**
     * Show a progress dialog for loading bird list
     * @param context The context (Activity or Fragment)
     * @return ProgressDialog instance
     */
    public static ProgressDialog showLoadingBirdListDialog(Context context) {
        return showProgressDialog(context, "Loading bird list...");
    }
    
    /**
     * Show a progress dialog for saving birds
     * @param context The context (Activity or Fragment)
     * @return ProgressDialog instance
     */
    public static ProgressDialog showSavingBirdsDialog(Context context) {
        return showProgressDialog(context, "Saving birds to your list...");
    }
    
    /**
     * Show a progress dialog for loading profile data
     * @param context The context (Activity or Fragment)
     * @return ProgressDialog instance
     */
    public static ProgressDialog showLoadingProfileDialog(Context context) {
        return showProgressDialog(context, "Loading profile data...");
    }
    
    /**
     * Dismiss the current progress dialog
     */
    public static void dismissProgressDialog() {
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                dismissCurrentDialog();
            });
        } else {
            dismissCurrentDialog();
        }
    }
    
    /**
     * Create and show the progress dialog
     * @param context The context
     * @param message The message to display
     */
    private static void createAndShowDialog(Context context, String message) {
        try {
            currentProgressDialog = new ProgressDialog(context);
            currentProgressDialog.setMessage(message);
            currentProgressDialog.setCancelable(false); // Prevent user from dismissing during operation
            currentProgressDialog.setIndeterminate(true); // Show spinning progress
            currentProgressDialog.show();
        } catch (Exception e) {
            // Handle any exceptions (e.g., if context is no longer valid)
            e.printStackTrace();
        }
    }
    
    /**
     * Dismiss the current dialog safely
     */
    private static void dismissCurrentDialog() {
        try {
            if (currentProgressDialog != null && currentProgressDialog.isShowing()) {
                currentProgressDialog.dismiss();
            }
        } catch (Exception e) {
            // Handle any exceptions (e.g., if dialog was already dismissed)
            e.printStackTrace();
        } finally {
            currentProgressDialog = null;
        }
    }
    
    /**
     * Check if a progress dialog is currently showing
     * @return true if a dialog is showing, false otherwise
     */
    public static boolean isProgressDialogShowing() {
        return currentProgressDialog != null && currentProgressDialog.isShowing();
    }
}