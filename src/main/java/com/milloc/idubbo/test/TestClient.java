package com.milloc.idubbo.test;

import com.milloc.idubbo.client.Client;

@Client
public interface TestClient {
    String hello(String username);

    int hello(int a);

    void eeee(String fff, String[] aaa);

    String err();
}
