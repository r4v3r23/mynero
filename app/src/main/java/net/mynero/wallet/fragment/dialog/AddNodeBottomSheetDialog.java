package net.mynero.wallet.fragment.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.mynero.wallet.R;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;
import net.mynero.wallet.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;

public class AddNodeBottomSheetDialog extends BottomSheetDialogFragment {
    public AddNodeListener listener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_node_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button addNodeButton = view.findViewById(R.id.add_node_button);
        EditText addressEditText = view.findViewById(R.id.address_edittext);
        EditText nodeNameEditText = view.findViewById(R.id.node_name_edittext);
        ImageButton pasteAddressImageButton = view.findViewById(R.id.paste_address_imagebutton);
        pasteAddressImageButton.setOnClickListener(view1 -> {
            Context ctx = getContext();
            if (ctx != null) {
                addressEditText.setText(Helper.getClipBoardText(ctx));
            }
        });
        addNodeButton.setOnClickListener(view1 -> {
            String node = addressEditText.getText().toString();
            String name = nodeNameEditText.getText().toString();
            try {

                if (node.contains(":") && !name.isEmpty()) {
                    String[] nodeParts = node.split(":");
                    if (nodeParts.length == 2) {
                        String address = nodeParts[0];
                        int port = Integer.parseInt(nodeParts[1]);
                        String newNodeString = address + ":" + port + "/mainnet/" + name;
                        boolean validAddress = Patterns.IP_ADDRESS.matcher(address).matches() || Patterns.DOMAIN_NAME.matcher(address).matches() || address.endsWith(".b32.i2p");
                        if (validAddress) {
                            String nodesArray = PrefService.getInstance().getString(Constants.PREF_CUSTOM_NODES, "[]");
                            JSONArray jsonArray = new JSONArray(nodesArray);
                            boolean exists = false;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String nodeString = jsonArray.getString(i);
                                if (nodeString.equals(newNodeString))
                                    exists = true;
                            }

                            if (!exists) {
                                jsonArray.put(newNodeString);
                            }

                            PrefService.getInstance().edit().putString(Constants.PREF_CUSTOM_NODES, jsonArray.toString()).apply();
                            if (listener != null) {
                                listener.onNodeAdded();
                            }
                            dismiss();
                        }

                    }
                } else if(node.endsWith(".b32.i2p")) {
                    String newNodeString = node + "/mainnet/" + name;
                    String nodesArray = PrefService.getInstance().getString(Constants.PREF_CUSTOM_NODES, "[]");
                    JSONArray jsonArray = new JSONArray(nodesArray);
                    boolean exists = false;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String nodeString = jsonArray.getString(i);
                        if (nodeString.equals(newNodeString))
                            exists = true;
                    }

                    if (!exists) {
                        jsonArray.put(newNodeString);
                    }

                    PrefService.getInstance().edit().putString(Constants.PREF_CUSTOM_NODES, jsonArray.toString()).apply();
                    if (listener != null) {
                        listener.onNodeAdded();
                    }
                    dismiss();
                }
            } catch (NumberFormatException | JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public interface AddNodeListener {
        void onNodeAdded();
    }
}