/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mynero.wallet.service;

import static net.mynero.wallet.model.Wallet.SWEEP_ALL;

import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.data.Node;
import net.mynero.wallet.data.TxData;
import net.mynero.wallet.model.PendingTransaction;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletListener;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.util.Constants;


/**
 * Handy class for starting a new thread that has a looper. The looper can then be
 * used to create handler classes. Note that start() must still be called.
 * The started Thread has a stck size of STACK_SIZE (=5MB)
 */
public class MoneroHandlerThread extends Thread implements WalletListener {
    // from src/cryptonote_config.h
    static public final long THREAD_STACK_SIZE = 5 * 1024 * 1024;
    int triesLeft = 5;
    private Listener listener = null;
    private final Wallet wallet;

    public MoneroHandlerThread(String name, Listener listener, Wallet wallet) {
        super(null, null, name, THREAD_STACK_SIZE);
        this.listener = listener;
        this.wallet = wallet;
    }

    @Override
    public synchronized void start() {
        super.start();
        this.listener.onRefresh();
    }

    @Override
    public void run() {
        String currentNodeString = PrefService.getInstance().getString(Constants.PREF_NODE, DefaultNodes.XMRTW.getAddress());
        Node selectedNode = Node.fromString(currentNodeString);
        boolean usesTor = PrefService.getInstance().getBoolean(Constants.PREF_USES_TOR, false);
        boolean isLocalIp = currentNodeString.startsWith("10.") || currentNodeString.startsWith("192.168.") || currentNodeString.equals("localhost") || currentNodeString.equals("127.0.0.1");
        if (usesTor && !isLocalIp) {
            String proxy = PrefService.getInstance().getString(Constants.PREF_PROXY, "");
            WalletManager.getInstance().setProxy(proxy);
            wallet.setProxy(proxy);
        }
        WalletManager.getInstance().setDaemon(selectedNode);
        wallet.init(0);
        wallet.setListener(this);
        wallet.startRefresh();
    }

    @Override
    public void moneySpent(String txId, long amount) {
    }

    @Override
    public void moneyReceived(String txId, long amount) {
    }

    @Override
    public void unconfirmedMoneyReceived(String txId, long amount) {
    }

    @Override
    public void newBlock(long height) {
        refresh();
        BlockchainService.getInstance().setDaemonHeight(wallet.isSynchronized() ? height : 0);
    }

    @Override
    public void updated() {
        refresh();
    }

    @Override
    public void refreshed() {
        Wallet.ConnectionStatus status = wallet.getFullStatus().getConnectionStatus();
        if (status == Wallet.ConnectionStatus.ConnectionStatus_Disconnected || status == null) {
            if (triesLeft > 0) {
                wallet.startRefresh();
                triesLeft--;
            } else {
                listener.onConnectionFail();
            }
        } else {
            BlockchainService.getInstance().setDaemonHeight(wallet.getDaemonBlockChainHeight());
            wallet.setSynchronized();
            wallet.store();
            refresh();
        }

        BlockchainService.getInstance().setConnectionStatus(status);
    }

    private void refresh() {
        wallet.refreshHistory();
        listener.onRefresh();
    }

    public PendingTransaction createTx(String address, String amountStr, boolean sendAll, PendingTransaction.Priority feePriority) {
        long amount = sendAll ? SWEEP_ALL : Wallet.getAmountFromString(amountStr);
        return wallet.createTransaction(new TxData(address, amount, 0, feePriority));
    }

    public boolean sendTx(PendingTransaction pendingTx) {
        return pendingTx.commit("", true);
    }

    public interface Listener {
        void onRefresh();

        void onConnectionFail();
    }
}