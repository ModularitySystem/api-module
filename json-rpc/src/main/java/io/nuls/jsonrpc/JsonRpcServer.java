/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.jsonrpc;

import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.Charsets;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;

import java.io.IOException;
import java.util.Map;


/**
 * @author Niels
 */
public class JsonRpcServer {

    private HttpServer httpServer;

    public void startServer(String ip, int port) {

        this.httpServer = new HttpServer();
        NetworkListener listener = new NetworkListener("NULS-RPC", ip, port);
        TCPNIOTransport transport = listener.getTransport();
        ThreadPoolConfig workerPool = ThreadPoolConfig.defaultConfig()
                .setCorePoolSize(4)
                .setMaxPoolSize(4)
                .setQueueLimit(1000)
                .setThreadFactory((new ThreadFactoryBuilder()).setNameFormat("grizzly-http-server-%d").build());
        transport.configureBlocking(false);
        transport.setSelectorRunnersCount(2);
        transport.setWorkerThreadPoolConfig(workerPool);
        transport.setIOStrategy(WorkerThreadIOStrategy.getInstance());
        transport.setTcpNoDelay(true);
        listener.setSecure(false);
        httpServer.addListener(listener);

        ServerConfiguration config = httpServer.getServerConfiguration();
        config.addHttpHandler(new JsonRpcHandler());
        config.setDefaultQueryEncoding(Charsets.UTF8_CHARSET);

        try {
            ClassLoader loader = this.getClass().getClassLoader();

            httpServer.start();
        } catch (IOException e) {
//            Log.error(e);
            httpServer.shutdownNow();
        }
    }

    public void shutdown() {
        Map<HttpHandler, HttpHandlerRegistration[]> mapping = httpServer.getServerConfiguration().getHttpHandlersWithMapping();
        for (HttpHandler handler : mapping.keySet()) {
            handler.destroy();
        }
        httpServer.shutdown();
    }

}
