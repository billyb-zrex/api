package com.sharefable.api.common;

import com.sharefable.api.entity.EntityBase;
import com.sharefable.api.entity.TransportObjRef;
import com.sharefable.api.transport.ResponseBase;
import lombok.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

class UtilsTest {
    @SneakyThrows
    @Test
    void testEntityTransportConversion() {
        TestEntity100 testEntity100 = new TestEntity100("id", "rid", "dn", "th",
            new TestEntity99("id2", "rid2", "john"));
        testEntity100.setUpdatedAt(Timestamp.from(Instant.now()));
        testEntity100.setCreatedAt(Timestamp.from(Instant.now()));

        TestResp100 responseBase = (TestResp100) Utils.fromEntityToTransportObject(testEntity100);

        Assertions.assertEquals("rid", responseBase.getRid());
        Assertions.assertInstanceOf(TestResp99.class, responseBase.getT());
        Assertions.assertEquals("rid2", responseBase.getT().getRid());
    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @TransportObjRef(cls = TestResp100.class)
    public static class TestEntity100 extends EntityBase {
        private String id;
        private String rid;
        private String displayName;
        private String thumbnail;
        private TestEntity99 t;
    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResp100 extends ResponseBase {
        private String rid;
        private String displayName;
        private String thumbnail;
        private TestResp99 t;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @TransportObjRef(cls = TestResp99.class)
    public static class TestEntity99 extends EntityBase {
        private String id;
        private String rid;
        private String firstName;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResp99 extends ResponseBase {
        private String rid;
        private String firstName;
    }
}
