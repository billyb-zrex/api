package com.sharefable.api.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.annotations.ESDocument;
import com.sharefable.api.entity.AssetContent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

public class EntitySchemaTest {
    @Test
    void entityAssetContentShouldHaveAllTheRequiredFields() {
        boolean annotationPresent = AssetContent.class.isAnnotationPresent(ESDocument.class);
        if (!annotationPresent) {
            throw new AssertionError("Class must be annotated with @ESDocument");
        }

        ESDocument docAnnot = AssetContent.class.getAnnotation(ESDocument.class);
        String schemaFile = docAnnot.schema();
        try(InputStream jsonSchema = this.getClass().getClassLoader().getResourceAsStream(schemaFile)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(jsonSchema, JsonNode.class);
            JsonNode propertiesNode = node.get("mappings").get("properties");
            Iterator<String> keysInSchemaItr = propertiesNode.fieldNames();
            Map<String, Integer> keysInSchema = new HashMap<>();
            keysInSchemaItr.forEachRemaining(key -> keysInSchema.put(key, 1));

            Field[] fields = AssetContent.class.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();
                if (keysInSchema.containsKey(name)) {
                    keysInSchema.remove(name);
                    continue;
                }
                throw new AssertionError("Key " + name +" is present in Entity class but not found in schema");
            }

            if (keysInSchema.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (String key : keysInSchema.keySet()) {
                    sb.append(key).append(" ");
                }
                sb.append("keys are present in schema but not in Entity class");
                throw new AssertionError(sb);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
