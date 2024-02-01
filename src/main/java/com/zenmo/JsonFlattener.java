package com.zenmo;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonFlattener {

    public static Map<String, String> flattenJson(JsonObject jsonObject) {
        Map<String, String> flatMap = new LinkedHashMap<>();
        flattenJsonToMap(jsonObject, flatMap, "");
        return flatMap;
    }

    private static void flattenJsonToMap(JsonValue jsonValue, Map<String, String> map, String parentKey) {
        switch (jsonValue.getValueType()) {
            case OBJECT:
                JsonObject object = (JsonObject) jsonValue;
                object.forEach((key, value) -> flattenJsonToMap(value, map, addKeyToParent(parentKey, key)));
                break;
            case ARRAY:
                JsonArray array = (JsonArray) jsonValue;
                for (int i = 0; i < array.size(); i++) {
                    flattenJsonToMap(array.get(i), map, addKeyToParent(parentKey, String.valueOf(i)));
                }
                break;
            case STRING:
                map.put(parentKey, ((JsonString) jsonValue).getString());
                break;
            default:
                map.put(parentKey, jsonValue.toString());
        }
    }

    private static String addKeyToParent(String parentKey, String key) {
        if (parentKey == null || parentKey.isEmpty()) {
            return key;
        }
        return parentKey + "." + key;
    }
}
