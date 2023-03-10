/*
 * Copyright (c) 2020 m2049r
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

package net.mynero.wallet.data;

// Nodes stolen from https://moneroworld.com/#nodes

public enum DefaultNodes {
    SAMOURAI("163.172.56.213:18089/mainnet/SamouraiWallet"),
    MONERUJO("nodex.monerujo.io:18081/mainnet/monerujo"),
    SUPPORTXMR("node.supportxmr.com:18081/mainnet/SupportXMR"),
    HASHVAULT("nodes.hashvault.pro:18081/mainnet/Hashvault"),
    MONEROWORLD("node.moneroworld.com:18089/mainnet/MoneroWorld"),
    XMRTW("opennode.xmr-tw.org:18089/mainnet/XMRTW"),
    MYNERO_I2P("ynk3hrwte23asonojqeskoulek2g2cd6tqg4neghnenfyljrvhga.b32.i2p:0/mainnet/node.mynero.i2p"),
    SAMOURAI_ONION("446unwib5vc7pfbzflosy6m6vtyuhddnalr3hutyavwe4esfuu5g6ryd.onion:18089/mainnet/SamouraiWallet.onion"),
    MONERUJO_ONION("monerujods7mbghwe6cobdr6ujih6c22zu5rl7zshmizz2udf7v7fsad.onion:18081/mainnet/monerujo.onion"),
    Criminales78("56wl7y2ebhamkkiza4b7il4mrzwtyvpdym7bm2bkg3jrei2je646k3qd.onion:18089/mainnet/Criminales78.onion"),
    xmrfail("mxcd4577fldb3ppzy7obmmhnu3tf57gbcbd4qhwr2kxyjj2qi3dnbfqd.onion:18081/mainnet/xmrfail.onion"),
    boldsuck("6dsdenp6vjkvqzy4wzsnzn6wixkdzihx3khiumyzieauxuxslmcaeiad.onion:18081/mainnet/boldsuck.onion");

    private final String uri;

    DefaultNodes(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String getAddress() {
        return uri.split("/")[0];
    }

    public String getName() {
        return uri.split("/")[2];
    }
}
