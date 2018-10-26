package com.github.euonmyoji.newhonor.mung.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author MungSoup
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {

    String command();

    String args() default "";

    String description();

    String permission() default "";

    String hover() default "";

    String click() default "";

    boolean console() default false;

    String consoleExecuteMsg() default "§c不支持控制台!";

    String noPermissionMsg() default "§c您没有权限使用这个命令!";
}
