package com.silverguardian.prototype.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.silverguardian.prototype.R;

/** Shared, persistent-label form fields for programmatically built dialogs. */
public final class FormFieldFactory {
    private FormFieldFactory() { }

    public static EditText addTextField(Context context, LinearLayout parent,
                                        String label, String hint, boolean multiline) {
        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextSize(15);
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        labelView.setPadding(0, dp(context, 14), 0, dp(context, 6));
        parent.addView(labelView);

        EditText input = new EditText(context);
        input.setHint(hint);
        input.setContentDescription(label);
        input.setTextSize(17);
        input.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        input.setHintTextColor(ContextCompat.getColor(context, R.color.text_tertiary));
        input.setBackgroundResource(R.drawable.bg_pin_input);
        input.setPadding(dp(context, 14), dp(context, 12), dp(context, 14), dp(context, 12));
        input.setMinHeight(dp(context, multiline ? 96 : 56));
        input.setSingleLine(!multiline);
        if (multiline) {
            input.setMinLines(3);
            input.setGravity(Gravity.TOP | Gravity.START);
        }
        parent.addView(input, new LinearLayout.LayoutParams(-1, -2));
        return input;
    }

    public static TextView addLabel(Context context, LinearLayout parent, String label) {
        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextSize(15);
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        labelView.setPadding(0, dp(context, 14), 0, dp(context, 6));
        parent.addView(labelView);
        return labelView;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
