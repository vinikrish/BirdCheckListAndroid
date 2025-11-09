package com.vinikrish.birdchecklistandroid.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vinikrish.birdchecklistandroid.R;

public class CustomDialogUtils {

    public static void showSuccessDialog(Context context, String title, String message) {
        showCustomDialog(context, title, message);
    }

    public static void showErrorDialog(Context context, String title, String message) {
        showCustomDialog(context, title, message);
    }

    public static void showInfoDialog(Context context, String title, String message) {
        showCustomDialog(context, title, message);
    }

    private static void showCustomDialog(Context context, String title, String message) {
        Dialog dialog = new Dialog(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_message, null);
        dialog.setContentView(dialogView);
        
        // Configure dialog window
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.CENTER);
            
            // Set dialog size and position
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
        
        dialog.setCancelable(true);

        ImageView dialogIcon = dialogView.findViewById(R.id.dialogIcon);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button dialogOkButton = dialogView.findViewById(R.id.dialogOkButton);

        // Always use app logo for consistent branding
        dialogIcon.setImageResource(R.drawable.logo);
        dialogTitle.setText(title);
        dialogMessage.setText(message);

        dialogOkButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}