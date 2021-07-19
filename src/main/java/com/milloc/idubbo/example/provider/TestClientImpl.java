package com.milloc.idubbo.example.provider;

import com.milloc.idubbo.example.client.TestClient;
import com.milloc.idubbo.provider.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * TestClientImpl
 *
 * @author gongdeming
 * @date 2021-07-19
 */
@Provider
@Slf4j
public class TestClientImpl implements TestClient {
    @Override
    public String hello(String username) {
        return "[provider] " + username;
    }

    @Override
    public int hello(int a) {
        return 222 + a;
    }

    @Override
    public void eeee(String fff, String[] aaa) {
        log.info("{} {}", fff, Arrays.deepToString(aaa));
    }

    @Override
    public String err() {
        throw new NullPointerException("this is a err");
    }
}
