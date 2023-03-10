package net.mynero.wallet.fragment.onboarding;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.mynero.wallet.MainActivity;
import net.mynero.wallet.MoneroApplication;
import net.mynero.wallet.R;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;
import net.mynero.wallet.util.RestoreHeight;

import java.io.File;
import java.util.Calendar;

public class OnboardingFragment extends Fragment {
    private boolean useOffset = true;
    private OnboardingViewModel mViewModel;
    TextWatcher proxyAddressListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (mViewModel != null) {
                mViewModel.setProxyAddress(editable.toString());
                mViewModel.updateProxy(((MoneroApplication)getActivity().getApplication()));
            }
        }
    };
    TextWatcher proxyPortListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (mViewModel != null) {
                mViewModel.setProxyPort(editable.toString());
                mViewModel.updateProxy(((MoneroApplication)getActivity().getApplication()));
            }
        }
    };
    private EditText walletProxyAddressEditText;
    private EditText walletProxyPortEditText;
    private EditText walletPasswordEditText;
    private EditText walletPasswordConfirmEditText;
    private EditText walletSeedEditText;
    private EditText walletRestoreHeightEditText;
    private Button createWalletButton;
    private TextView moreOptionsDropdownTextView;
    private SwitchCompat torSwitch;
    private ConstraintLayout proxySettingsLayout;
    private ImageView moreOptionsChevronImageView;
    private CheckBox seedOffsetCheckbox;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        walletPasswordEditText = view.findViewById(R.id.wallet_password_edittext);
        walletPasswordConfirmEditText = view.findViewById(R.id.wallet_password_confirm_edittext);
        walletSeedEditText = view.findViewById(R.id.wallet_seed_edittext);
        walletRestoreHeightEditText = view.findViewById(R.id.wallet_restore_height_edittext);
        createWalletButton = view.findViewById(R.id.create_wallet_button);
        moreOptionsDropdownTextView = view.findViewById(R.id.advanced_settings_dropdown_textview);
        moreOptionsChevronImageView = view.findViewById(R.id.advanced_settings_chevron_imageview);
        torSwitch = view.findViewById(R.id.tor_onboarding_switch);
        proxySettingsLayout = view.findViewById(R.id.wallet_proxy_settings_layout);
        seedOffsetCheckbox = view.findViewById(R.id.seed_offset_checkbox);
        walletProxyAddressEditText = view.findViewById(R.id.wallet_proxy_address_edittext);
        walletProxyPortEditText = view.findViewById(R.id.wallet_proxy_port_edittext);
        seedOffsetCheckbox.setChecked(useOffset);

        bindListeners();
        bindObservers();
    }

    private void bindObservers() {
        mViewModel.showMoreOptions.observe(getViewLifecycleOwner(), show -> {
            if (show) {
                moreOptionsChevronImageView.setImageResource(R.drawable.ic_keyboard_arrow_up);
                walletSeedEditText.setVisibility(View.VISIBLE);
                walletRestoreHeightEditText.setVisibility(View.VISIBLE);
            } else {
                moreOptionsChevronImageView.setImageResource(R.drawable.ic_keyboard_arrow_down);
                walletSeedEditText.setVisibility(View.GONE);
                walletRestoreHeightEditText.setVisibility(View.GONE);
            }
        });
    }

    private void bindListeners() {
        moreOptionsDropdownTextView.setOnClickListener(view12 -> mViewModel.onMoreOptionsClicked());
        moreOptionsChevronImageView.setOnClickListener(view12 -> mViewModel.onMoreOptionsClicked());
        seedOffsetCheckbox.setOnCheckedChangeListener((compoundButton, b) -> useOffset = b);

        createWalletButton.setOnClickListener(view1 -> {
            prepareDefaultNode();
            ((MoneroApplication)getActivity().getApplication()).getExecutor().execute(() -> {
                createOrImportWallet(
                        walletPasswordEditText.getText().toString(),
                        walletPasswordConfirmEditText.getText().toString(),
                        walletSeedEditText.getText().toString().trim(),
                        walletRestoreHeightEditText.getText().toString().trim()
                );
            });
        });
        walletPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) {
                    walletPasswordConfirmEditText.setText(null);
                    walletPasswordConfirmEditText.setVisibility(View.GONE);
                } else {
                    walletPasswordConfirmEditText.setVisibility(View.VISIBLE);
                }
            }
        });
        walletSeedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) {
                    createWalletButton.setText(R.string.create_wallet);
                } else {
                    createWalletButton.setText(R.string.menu_restore);
                }
            }
        });
        torSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            PrefService.getInstance().edit().putBoolean(Constants.PREF_USES_TOR, b).apply();
            if (b) {
                removeProxyTextListeners();

                if (PrefService.getInstance().hasProxySet()) {
                    String proxyAddress = PrefService.getInstance().getProxyAddress();
                    String proxyPort = PrefService.getInstance().getProxyPort();
                    initProxyStuff(proxyAddress, proxyPort);
                } else {
                    initProxyStuff("127.0.0.1", "9050");
                }
                addProxyTextListeners();

                proxySettingsLayout.setVisibility(View.VISIBLE);
            } else {
                proxySettingsLayout.setVisibility(View.GONE);
            }

            mViewModel.updateProxy(((MoneroApplication)getActivity().getApplication()));
        });
    }

    private void prepareDefaultNode() {
        PrefService.getInstance().getNode();
    }

    private void createOrImportWallet(String walletPassword, String confirmedPassword, String walletSeed, String restoreHeightText) {
        String offset = useOffset ? walletPassword : "";
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            if (!walletPassword.isEmpty()) {
                if(!walletPassword.equals(confirmedPassword)) {
                    mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, getString(R.string.invalid_confirmed_password), Toast.LENGTH_SHORT).show());
                    return;
                }
                PrefService.getInstance().edit().putBoolean(Constants.PREF_USES_PASSWORD, true).apply();
            }
            long restoreHeight = getNewRestoreHeight();
            File walletFile = new File(mainActivity.getApplicationInfo().dataDir, Constants.WALLET_NAME);
            Wallet wallet = null;
            if(!offset.isEmpty()) {
                PrefService.getInstance().edit().putBoolean(Constants.PREF_USES_OFFSET, true).apply();
            }

            if (walletSeed.isEmpty()) {
                Wallet tmpWallet = createTempWallet(mainActivity.getApplicationInfo().dataDir); //we do this to get seed, then recover wallet so we can use seed offset
                wallet = WalletManager.getInstance().recoveryWallet(walletFile, walletPassword, tmpWallet.getSeed(""), offset, restoreHeight);
            } else {
                if (!checkMnemonic(walletSeed)) {
                    mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, getString(R.string.invalid_mnemonic_code), Toast.LENGTH_SHORT).show());
                    return;
                }
                if (!restoreHeightText.isEmpty()) {
                    restoreHeight = Long.parseLong(restoreHeightText);
                }
                wallet = WalletManager.getInstance().recoveryWallet(walletFile, walletPassword, walletSeed, offset, restoreHeight);
            }
            Wallet.Status walletStatus = wallet.getStatus();
            wallet.close();
            boolean ok = walletStatus.isOk();
            walletFile.delete(); // cache is broken for some reason when recovering wallets. delete the file here. this happens in monerujo too.

            if (ok) {
                mainActivity.init(walletFile, walletPassword);
                mainActivity.runOnUiThread(mainActivity::onBackPressed);
            } else {
                mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, getString(R.string.create_wallet_failed, walletStatus.getErrorString()), Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void removeProxyTextListeners() {
        walletProxyAddressEditText.removeTextChangedListener(proxyAddressListener);
        walletProxyPortEditText.removeTextChangedListener(proxyPortListener);
    }

    private void addProxyTextListeners() {
        walletProxyAddressEditText.addTextChangedListener(proxyAddressListener);
        walletProxyPortEditText.addTextChangedListener(proxyPortListener);
    }

    private void initProxyStuff(String proxyAddress, String proxyPort) {
        boolean validIpAddress = Patterns.IP_ADDRESS.matcher(proxyAddress).matches();
        if (validIpAddress) {
            mViewModel.setProxyAddress(proxyAddress);
            mViewModel.setProxyPort(proxyPort);
            walletProxyAddressEditText.setText(proxyAddress);
            walletProxyPortEditText.setText(proxyPort);
        }
    }

    private long getNewRestoreHeight() {
        Calendar restoreDate = Calendar.getInstance();
        restoreDate.add(Calendar.DAY_OF_MONTH, 0);
        return RestoreHeight.getInstance().getHeight(restoreDate.getTime());
    }

    private Wallet createTempWallet(String dir) {
        File tmpWalletFile = new File(dir, Constants.WALLET_NAME + "_tmp");
        return WalletManager.getInstance().createWallet(tmpWalletFile, "", Constants.MNEMONIC_LANGUAGE, 0);
    }

    private boolean checkMnemonic(String seed) {
        return (seed.split("\\s").length == 25);
    }
}