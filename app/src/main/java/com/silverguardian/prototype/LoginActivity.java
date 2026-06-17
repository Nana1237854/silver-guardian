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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.User;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private EditText pinInput;
    private Button loginButton;
    private TextView selectedUserName;
    private UserAdapter adapter;
    private User selectedUser;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);
        setContentView(R.layout.activity_login);

        users = MockData.getUsers();
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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                loginButton.setEnabled(s.length() == 4 && selectedUser != null);
            }
        });

        loginButton.setOnClickListener(v -> login());
        if (!users.isEmpty()) selectUser(users.get(0));
    }

    private void login() {
        if (selectedUser == null) {
            Toast.makeText(this, "请先选择老人档案", Toast.LENGTH_SHORT).show();
            return;
        }
        String pin = pinInput.getText().toString().trim();
        if (pin.equals(selectedUser.pin)) {
            MockData.setActiveUser(selectedUser.id);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_name", selectedUser.name);
            intent.putExtra("user_id", selectedUser.id);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "PIN 码错误，请重试", Toast.LENGTH_SHORT).show();
            pinInput.setText("");
        }
    }

    private void showAddUserDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(36, 12, 36, 0);

        EditText nameInput = input("姓名，例如：颜爷爷");
        EditText pin = input("4 位 PIN，例如：2468");
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        EditText ageInput = input("年龄，例如：72");
        ageInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText conditionInput = input("健康状况，例如：高血压");

        form.addView(nameInput);
        form.addView(pin);
        form.addView(ageInput);
        form.addView(conditionInput);

        new AlertDialog.Builder(this)
            .setTitle("添加老人档案")
            .setView(form)
            .setPositiveButton("保存", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String pinText = pin.getText().toString().trim();
                if (name.isEmpty() || pinText.length() != 4) {
                    Toast.makeText(this, "请填写姓名和 4 位 PIN", Toast.LENGTH_LONG).show();
                    return;
                }
                int age = ageInput.getText().toString().trim().isEmpty() ? 60 : Integer.parseInt(ageInput.getText().toString().trim());
                User user = MockData.addUser(name, pinText, age, conditionInput.getText().toString().trim());
                selectUser(user);
                adapter.notifyDataSetChanged();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private EditText input(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setTextSize(18);
        editText.setSingleLine(true);
        editText.setPadding(0, 10, 0, 10);
        return editText;
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
                    Toast.makeText(LoginActivity.this, "至少保留一个老人档案", Toast.LENGTH_SHORT).show();
                    return true;
                }
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("删除老人档案")
                    .setMessage("确定删除「" + user.name + "」吗？相关本地数据也会删除。")
                    .setPositiveButton("删除", (d, w) -> {
                        MockData.deleteUser(user);
                        selectedUser = users.isEmpty() ? null : users.get(0);
                        selectedUserName.setText(selectedUser == null ? "" : selectedUser.name);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("取消", null)
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
