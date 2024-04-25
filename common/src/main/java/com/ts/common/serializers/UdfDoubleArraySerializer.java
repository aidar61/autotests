package com.ts.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ts.common.entitites.commonEntities.udf.UdfDouble;

import java.io.IOException;

public class UdfDoubleArraySerializer extends JsonSerializer<UdfDouble[]> {
    @Override
    public void serialize(UdfDouble[] udfDoubles, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        for (int i = 0; i < udfDoubles.length; i++) {
            UdfDouble udfDouble = udfDoubles[i];
            if (udfDouble != null) {
                ObjectNode udfNode = JsonNodeFactory.instance.objectNode();
                jsonGenerator.writeStartObject();
                udfNode.put("udfid", udfDouble.getUdfId());
                udfNode.put("type", udfDouble.getType());
                if (udfDouble.getNumberValueDouble() != null) {
                    udfNode.put("numberValue", udfDouble.getNumberValueDouble());
                } else if (udfDouble.getNumberValue() != null) {
                    udfNode.put("numberValue", udfDouble.getNumberValue());
                }
                jsonGenerator.writeFieldName("udfdouble" + (i + 1));
                jsonGenerator.writeTree(udfNode);
                jsonGenerator.writeEndObject();
            }
        }
    }
}
