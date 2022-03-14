package com.sdm.core.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FileClassification {
    public String guild() default "";
    public boolean isHidden() default true;
    public boolean isPublic() default false;
}
