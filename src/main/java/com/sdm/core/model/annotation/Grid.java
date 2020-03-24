package com.sdm.core.model.annotation;

import com.sdm.core.model.ModelInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Grid {
    /**
     * Grid Label
     */
    String value() default "";

    /**
     * Alignment Setting of Table Cell.
     * Default Setting is String => left, number => right, other => center
     * @return
     */
    ModelInfo.Alignment alignment() default ModelInfo.Alignment.left;

    /**
     * Table Cell Type
     * @return
     */
    ModelInfo.GridType type() default ModelInfo.GridType.text;


    /**
     * Column width
     */
    int minWidth() default 100;

    /**
     * Column is sortable;
     */
    boolean sortable() default false;

    /**
     * Hide in Grid
     */
    boolean hide() default  false;

    /**
     * Javascript onLoad Function
     * item => return string;
     */
    String onLoad() default "";
}
