package com.nat3z.qoluxe.vicious;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface Configurable {
    String name();
    String description();
    String category();
    String subCategory();
    String[] sliderChoices() default {""};
    ConfigType type();
    boolean requiresElementToggled() default true;
    String requiredElementToggled() default "";
    boolean hidden();
}
