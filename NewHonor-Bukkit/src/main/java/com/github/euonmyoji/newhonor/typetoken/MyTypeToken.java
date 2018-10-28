package com.github.euonmyoji.newhonor.typetoken;

import com.google.common.reflect.TypeToken;

public abstract class MyTypeToken<T> extends TypeToken<T> {

    public TypeToken<T> getMyType() {
        return new TypeToken<T>(getClass()) {
        };
    }
}
