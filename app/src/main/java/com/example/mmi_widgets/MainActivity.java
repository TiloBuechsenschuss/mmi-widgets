package com.example.mmi_widgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mmi_widgets.data.MmiActions;
import com.example.mmi_widgets.data.SettingsStore;
import com.example.mmi_widgets.databinding.ActivityMainBinding;
import com.example.mmi_widgets.model.MmiAction;

/**
 * Configuration screen: the user saves the number to forward to and can toggle each MMI function
 * directly (the same code path the widgets use).
 *
 * <p>Action rows are generated from {@link MmiActions#all()} so adding a new function to the
 * registry automatically shows up here &mdash; no layout changes needed.</p>
 */
public final class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SettingsStore settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settings = new SettingsStore(this);

        binding.numberInput.setText(settings.getForwardNumber());
        binding.saveNumberButton.setOnClickListener(v -> saveNumber());

        buildActionRows();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // State may have changed from a widget tap while this screen was in the background.
        refreshActionStates();
    }

    private void saveNumber() {
        String number = binding.numberInput.getText().toString().trim();
        settings.setForwardNumber(number);
        Toast.makeText(this, R.string.number_saved, Toast.LENGTH_SHORT).show();
    }

    private void buildActionRows() {
        LayoutInflater inflater = getLayoutInflater();
        binding.actionsContainer.removeAllViews();

        for (MmiAction action : MmiActions.all()) {
            View row = inflater.inflate(R.layout.item_action, binding.actionsContainer, false);
            row.setTag(action.getId());

            TextView label = row.findViewById(R.id.action_label);
            Button toggle = row.findViewById(R.id.action_toggle);

            label.setText(action.getLabel());
            toggle.setOnClickListener(v -> {
                // Delegate to the shared dispatcher (handles permission + state + widget refresh).
                startActivity(MmiDispatchActivity.createIntent(
                        this, action.getId(), MmiDispatchActivity.MODE_TOGGLE));
            });

            binding.actionsContainer.addView(row);
        }
        refreshActionStates();
    }

    private void refreshActionStates() {
        for (int i = 0; i < binding.actionsContainer.getChildCount(); i++) {
            View row = binding.actionsContainer.getChildAt(i);
            Object tag = row.getTag();
            if (!(tag instanceof String)) {
                continue;
            }
            String actionId = (String) tag;
            TextView state = row.findViewById(R.id.action_state);
            boolean enabled = settings.isEnabled(actionId);
            state.setText(getString(enabled ? R.string.state_on : R.string.state_off));
        }
    }
}
