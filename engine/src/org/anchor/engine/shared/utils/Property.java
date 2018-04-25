package org.anchor.engine.shared.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.FIELD, ElementType.METHOD
})
public @interface Property {

    public String value();

}
