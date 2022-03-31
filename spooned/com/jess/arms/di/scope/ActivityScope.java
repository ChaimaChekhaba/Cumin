package com.jess.arms.di.scope;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Scope
@Documented
@Retention(RUNTIME)
public @interface ActivityScope {}

