package net.mynero.wallet.service;

import android.content.Context;
import android.content.SharedPreferences;

import net.mynero.wallet.MoneroApplication;
import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.data.Node;
import net.mynero.wallet.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;

public class PrefService extends ServiceBase {
    private static SharedPreferences preferences = null;
    private static PrefService instance = null;

    public PrefService(MoneroApplication application) {
        super(null);
        preferences = application.getSharedPreferences(application.getApplicationInfo().packageName, Context.MODE_PRIVATE);
        instance = this;
    }

    public SharedPreferences.Editor edit() {
        return preferences.edit();
    }

    public Node getNode() {
        boolean usesProxy = getBoolean(Constants.PREF_USES_TOR, false);
        DefaultNodes defaultNode = DefaultNodes.SAMOURAI;
        if(usesProxy) {
            String proxyPort = getProxyPort();
            if (!proxyPort.isEmpty()) {
                int port = Integer.parseInt(proxyPort);
                if(port == 4447) {
                    defaultNode = DefaultNodes.MYNERO_I2P;
                } else {
                    defaultNode = DefaultNodes.SAMOURAI_ONION;
                }
            }
        }
        String nodeString = getString(Constants.PREF_NODE_2, defaultNode.getUri());
        if(!nodeString.isEmpty()) {
            return Node.fromString(nodeString);
        } else {
            return null;
        }
    }

    public void upgradeNodePrefs() {
        String oldNodeString = getString("pref_node", "");
        if(!oldNodeString.isEmpty()) {
            //upgrade old node pref to new node pref
            try {
                Node oldNode = getNode(oldNodeString);
                if(oldNode != null) {
                    SharedPreferences.Editor editor = edit();
                    editor.putString(Constants.PREF_NODE_2, oldNode.toNodeString());
                    editor.putString("pref_node", "");
                    editor.apply();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Node getNode(String oldNodeString) throws JSONException {
        String nodeString = "";
        String nodesArray = PrefService.getInstance().getString(Constants.PREF_CUSTOM_NODES, "[]");
        JSONArray jsonArray = new JSONArray(nodesArray);
        for (int i = 0; i < jsonArray.length(); i++) {
            //check custom nodes
            String jsonNodeString = jsonArray.getString(i);
            Node savedNode = Node.fromString(jsonNodeString);
            if(savedNode != null) {
                if (savedNode.getAddress().equals(oldNodeString)) {
                    nodeString = savedNode.toNodeString();
                    break;
                }
            }
        }
        if(nodeString.isEmpty()) {
            //not in custom nodes, maybe in default nodes?
            for (DefaultNodes defaultNode : DefaultNodes.values()) {
                Node node = Node.fromString(defaultNode.getUri());
                if(node != null) {
                    if(node.getAddress().equals(oldNodeString)) {
                        nodeString = node.toNodeString();
                        break;
                    }
                }
            }
        }
        if(nodeString.isEmpty()) {
            return null;
        } else {
            return Node.fromString(nodeString);
        }
    }

    public String getProxy() {
        return PrefService.getInstance().getString(Constants.PREF_PROXY, "");
    }

    public boolean hasProxySet() {
        String proxyString = getProxy();
        return proxyString.contains(":");
    }

    public String getProxyAddress() {
        if (hasProxySet()) {
            String proxyString = getProxy();
            return proxyString.split(":")[0];
        }
        return "";
    }

    public String getProxyPort() {
        if (hasProxySet()) {
            String proxyString = getProxy();
            return proxyString.split(":")[1];
        }
        return "";
    }

    public String getString(String key, String defaultValue) {
        String value = preferences.getString(key, "");
        if(value.isEmpty() && !defaultValue.isEmpty()) {
            edit().putString(key, defaultValue).apply();
            return defaultValue;
        }
        return value;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        boolean containsKey = preferences.contains(key);
        boolean value = preferences.getBoolean(key, false);
        if(!value && defaultValue && !containsKey) {
            edit().putBoolean(key, true).apply();
            return true;
        }
        return value;
    }

    public static PrefService getInstance() {
        return instance;
    }
}
