package com.milloc.idubbo.test;

import com.milloc.idubbo.provider.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Provider
@Slf4j
public class TestClientImpl implements TestClient {
    @Override
    public String hello(String username) {
        return "hello " + username;
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
