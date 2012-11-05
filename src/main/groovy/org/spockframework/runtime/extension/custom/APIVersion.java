package org.spockframework.runtime.extension.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtensionAnnotation(APIVersionExtension.class)
public @interface APIVersion
{

	/**
	 * The minimum API version that is required to execute this test
	 * 
	 * @return the minimum API version that is required to execute this test
	 */
	String minimimApiVersion();
}
