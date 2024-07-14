package com.literalura.challenge.back;

public interface ITransformaDatos {
    <T> T catchData(String json, Class<T> clase);
}
