package com.literalura.challenge.back;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransformaDatos implements ITransformaDatos{
    private ObjectMapper mapper = new ObjectMapper();
    @Override
    public <T> T catchData(String json, Class<T> clase) {
        try {
            return mapper.readValue(json.toString(), clase);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
