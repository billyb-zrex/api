package com.sharefable.api.integration;

import com.sharefable.api.Main;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = Main.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestWithRunnerAndSetup extends AbstractTest {
    @Override
    @BeforeAll
    public void setUp() {
        super.setUp();
    }
}
