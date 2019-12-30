/**
 * 
 */
package com.yyn.base.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

/**
 * @author dandan
 *
 */
public class YYNJsonUtils {

	public static final ObjectMapper MAPPER_COMMON = new ObjectMapper();
	public static final ObjectMapper MAPPER_LOOSE_DESERIALIZATION = new ObjectMapper();
	static {

		MAPPER_LOOSE_DESERIALIZATION.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER_LOOSE_DESERIALIZATION.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

		MAPPER_COMMON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
		MAPPER_COMMON.registerModule(javaTimeModule);

	}

	public static String beanToString(Object bean) {
		try {
			return MAPPER_COMMON.writeValueAsString(bean);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T jsonNodeToBean(ObjectMapper mapper, JsonNode jsonNode, Class<T> c) {
		Object obj = null;
		try {
			obj = mapper.convertValue(jsonNode, c);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return (T) obj;
	}

	public static <T> T jsonNodeToBean(JsonNode jsonNode, Class<T> c) {
		return jsonNodeToBean(MAPPER_COMMON, jsonNode, c);
	}

	public static <T> T stringToBean(ObjectMapper mapper, String json, Class<T> c) {
		Object obj = null;
		try {
			obj = mapper.readValue(json, c);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (T) obj;
	}

	public static <T> T stringToBean(String json, Class<T> c) {
		return stringToBean(MAPPER_COMMON, json, c);
	}

	public static JsonNode stringToJsonNode(String json) {
		if (json == null) {
			return null;
		}
		JsonNode jsonNode = null;
		try {
			jsonNode = YYNJsonUtils.MAPPER_COMMON.readTree(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonNode;
	}

	public static ObjectNode stringToObjectNode(String json) {
		if (json == null) {
			return null;
		}
		ObjectNode jsonNode = null;
		try {
			jsonNode = YYNJsonUtils.MAPPER_COMMON.readValue(json, ObjectNode.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonNode;
	}

	public static ObjectNode beanToObjectNode(Object bean) {
		ObjectNode jsonNode = null;
		jsonNode = YYNJsonUtils.MAPPER_COMMON.convertValue(bean, ObjectNode.class);
		return jsonNode;
	}

	public static ObjectNode buildJsonObject() {
		ObjectNode json = YYNJsonUtils.MAPPER_COMMON.createObjectNode();
		return json;
	}

	public static ObjectNode addProperty(ObjectNode json, String name, Object value) {
		if (json != null) {
			json.set(name, YYNJsonUtils.MAPPER_COMMON.convertValue(value, JsonNode.class));
		}
		return json;
	}

	public static ObjectNode addProperty(ObjectNode json, String name, String value) {
		if (json != null) {
			json.set(name, TextNode.valueOf(value));
		}
		return json;
	}

	public static ObjectNode addProperty(ObjectNode json, String name, boolean value) {
		if (json != null) {
			json.set(name, BooleanNode.valueOf(value));
		}
		return json;
	}

	public static String getStringProperty(ObjectNode objectNode, String fieldName) {
		if (objectNode == null) {
			return null;
		}
		JsonNode jsonNode = objectNode.get(fieldName);
		if (jsonNode == null) {
			return null;
		}
		return jsonNode.asText();
	}
}
