package com.silverguardian.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.models.User;
import com.silverguardian.prototype.utils.FormFieldFactory;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BaseActivity {
    private EditText pinInput;
    private Button loginButton;
    private TextView selectedUserName;
    private UserAdapter adapter;
    private User selectedUser;
    private final List<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        users.clear();
        users.addAll(userSession().getUsers());
        pinInput = findViewById(R.id.pin_input);
        loginButton = findViewById(R.id.login_button);
        selectedUserName = findViewById(R.id.selected_user_name);

        RecyclerView userGrid = findViewById(R.id.user_grid);
        userGrid.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new UserAdapter();
        userGrid.setAdapter(adapter);

        TextView addUser = findViewById(R.id.add_user_button);
        if (addUser != null) addUser.setOnClickListener(v -> showAddUserDialog());

        pinInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { loginButton.setEnabled(s.length() == 4 && selectedUser != null); }
        });

        loginButton.setOnClickListener(v -> login());
        if (!users.isEmpty()) selectUser(users.get(0));
    }

    private void login() {
        if (selectedUser == null) {
            Toast.makeText(this, R.string.login_select_user_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        String pin = pinInput.getText().toString().trim();
        if (pin.equals(selectedUser.pin)) {
            userSession().setActiveUser(selectedUser.id);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_name", selectedUser.name);
            intent.putExtra("user_id", selectedUser.id);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, R.string.login_invalid_pin_toast, Toast.LENGTH_SHORT).show();
            pinInput.setText("");
        }
    }

    private void showAddUserDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(8), dp(24), 0);

        EditText nameInput = FormFieldFactory.addTextField(this, form, getString(R.string.login_field_name), getString(R.string.login_field_name_hint), false);
        EditText pin = FormFieldFactory.addTextField(this, form, getString(R.string.login_field_pin), getString(R.string.login_field_pin_hint), false);
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        EditText ageInput = FormFieldFactory.addTextField(this, form, getString(R.string.login_field_age), getString(R.string.login_field_age_hint), false);
        ageInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText conditionInput = FormFieldFactory.addTextField(this, form, getString(R.string.login_field_condition), getString(R.string.login_field_condition_hint), false);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.login_add_user_title)
            .setView(form)
            .setPositiveButton(R.string.common_save, null)
            .setNegativeButton(R.string.common_cancel, null)
            .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String pinText = pin.getText().toString().trim();
            if (name.isEmpty()) {
                nameInput.setError(getString(R.string.login_name_required));
                nameInput.requestFocus();
                return;
            }
            if (!pinText.matches("\\d{4}")) {
                pin.setError(getString(R.string.login_pin_required));
                pin.requestFocus();
                return;
            }
            int age = ageInput.getText().toString().trim().isEmpty() ? 60 : Integer.parseInt(ageInput.getText().toString().trim());
            if (age < 1 || age > 150) {
                ageInput.setError(getString(R.string.login_age_invalid));
                ageInput.requestFocus();
                return;
            }
            User user = userSession().addUser(name, pinText, age, conditionInput.getText().toString().trim());
            users.clear();
            users.addAll(userSession().getUsers());
            selectUser(user);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void selectUser(User user) {
        selectedUser = user;
        selectedUserName.setText(user.name);
        loginButton.setEnabled(pinInput.getText().toString().trim().length() == 4);
    }

    private class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {
        @Override public UserViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_user_avatar, parent, false);
            return new UserViewHolder(view);
        }

        @Override public void onBindViewHolder(UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.nameText.setText(user.name);
            holder.avatarText.setText(user.name.substring(0, 1));
            holder.itemView.setSelected(user == selectedUser);
            holder.itemView.setOnClickListener(v -> {
                selectUser(user);
                notifyDataSetChanged();
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (users.size() <= 1) {
                    Toast.makeText(LoginActivity.this, R.string.login_keep_one_user, Toast.LENGTH_SHORT).show();
                    return true;
                }
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle(R.string.login_delete_user_title)
                    .setMessage(getString(R.string.login_delete_user_message, user.name))
                    .setPositiveButton(R.string.common_delete, (d, w) -> {
                        userSession().deleteUser(user);
                        users.clear();
                        users.addAll(userSession().getUsers());
                        selectedUser = users.isEmpty() ? null : users.get(0);
                        selectedUserName.setText(selectedUser == null ? "" : selectedUser.name);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton(R.string.common_cancel, null)
                    .show();
                return true;
            });
        }

        @Override public int getItemCount() { return users.size(); }
    }

    private static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, avatarText;
        UserViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.user_name);
            avatarText = itemView.findViewById(R.id.user_avatar_text);
        }
    }
}
