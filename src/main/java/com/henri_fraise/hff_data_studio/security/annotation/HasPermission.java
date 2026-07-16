package com.henri_fraise.hff_data_studio.security.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

	String value();

	String entity() default "";

	String operation() default "READ";
}