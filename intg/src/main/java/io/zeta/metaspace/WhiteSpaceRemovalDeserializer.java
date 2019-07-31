package io.zeta.metaspace;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * json反序列化时去掉string的前后的空格
 * @author zhuxt
 * @date 2019-07-30
 */
public class WhiteSpaceRemovalDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode treeNode = jsonParser.getCodec().readTree(jsonParser);
        if (treeNode == null) {
            return null;
        }
        return treeNode.asText().trim();
    }
}
