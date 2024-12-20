package com.rsmanager.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;

public class InstantAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString()); // 格式化为 ISO-8601 字符串
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return LocalDate.parse(json.getAsString()); // 从字符串解析回 LocalDate
    }
}
