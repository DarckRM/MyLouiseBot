package com.darcklh.louise.Utils;

import com.darcklh.louise.Config.LouiseConfig;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class LouiseProxy {
    private static Proxy proxy;

    LouiseProxy() {
        if (LouiseConfig.LOUISE_PROXY_PORT > 0)
            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(LouiseConfig.LOUISE_PROXY, LouiseConfig.LOUISE_PROXY_PORT));
        else
            proxy = null;
    }

    public static Proxy get() {
        return proxy;
    }
}
