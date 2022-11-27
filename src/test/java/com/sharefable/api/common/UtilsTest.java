package com.sharefable.api.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

class UtilsTest {
    @Test
    void testFromEntityToTransportObjectForSameMapping() {
        TestEntity1 entity = new TestEntity1(1L, Timestamp.from(Instant.now()), "Test");

        class DelegateImpl implements EntityTransportConversionDelegate<TestEntity1, TestTransport1> {
            int i = -1;

            @Override
            public void apply(TestEntity1 entityObj, TestTransport1 transportObj, List<Field> failedToConvertFields) {
                i = failedToConvertFields.size();
            }
        }
        DelegateImpl delegate = new DelegateImpl();
        try {
            TestTransport1 testTransport1 = Utils.fromEntityToTransportObject(entity, TestTransport1.class, delegate);
            Assertions.assertEquals(0, delegate.i);
            Assertions.assertEquals(entity.getId(), testTransport1.getId());
            Assertions.assertEquals(entity.getFirstName(), testTransport1.getFirstName());
            Assertions.assertEquals(entity.getCreatedAt(), testTransport1.getCreatedAt());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testFromEntityToTransportObjectForDifferentMapping() {
        TestEntity1 entity = new TestEntity1(1L, Timestamp.from(Instant.now()), "Test");

        class DelegateImpl implements EntityTransportConversionDelegate<TestEntity1, TestTransport2> {
            int i = -1;

            @Override
            public void apply(TestEntity1 entityObj, TestTransport2 transportObj, List<Field> failedToConvertFields) {
                i = failedToConvertFields.size();

                transportObj.setName(entityObj.getFirstName());
                transportObj.setTime(entityObj.getCreatedAt().toString());
            }
        }
        DelegateImpl delegate = new DelegateImpl();
        try {
            TestTransport2 testTransport2 = Utils.fromEntityToTransportObject(entity, TestTransport2.class, delegate);
            Assertions.assertEquals(2, delegate.i);
            Assertions.assertEquals(entity.getId(), testTransport2.getId());
            Assertions.assertEquals(entity.getFirstName(), testTransport2.getName());
            Assertions.assertEquals(entity.getCreatedAt().toString(), testTransport2.getTime());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestEntity1 {
        Long id;
        Timestamp createdAt;
        String firstName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestTransport1 {
        Long id;
        Timestamp createdAt;
        String firstName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestTransport2 {
        Long id;
        String name;
        String time;
    }
}
