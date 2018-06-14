package com.rbkmoney.wallets_hooker;

/**
 * Created by inalarsanukaev on 11.05.17.
 */

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * @since 12.04.17
 **/
@Ignore
public class MockServer {

    Logger log = LoggerFactory.getLogger(this.getClass());

    int PORT = 8089;

    @Test
    public void test() throws Exception {

        final Dispatcher dispatcher = new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    request.getBody().writeTo(byteArrayOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                log.info("\nRequest: " + request.getRequestLine() + "\nBody: " + byteArrayOutputStream.toString());
                return new MockResponse().setBody("xyi").setResponseCode(200);
            }
        };
        // Create a MockWebServer. These are lean enough that you can createByMessageId a new
        // instance for every unit test.
        MockWebServer server = new MockWebServer();
        server.setDispatcher(dispatcher);

        // Start the server.

        server.start(PORT);
        log.info("Server started on port: " + server.getPort());
        log.info("To run it : \n ngrok http " + PORT);

        while (true) {
            server.takeRequest();
        }
    }
}
