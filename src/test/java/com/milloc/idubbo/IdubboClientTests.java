package com.milloc.idubbo;

import com.milloc.idubbo.test.TestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApp.class)
class IdubboClientTests {

    @Autowired
    private TestClient testClient;

    @Test
    void contextLoads() {
        testClient.err();
    }

}
