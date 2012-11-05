package com.movideo.runtime.extension.custom;

import org.spockframework.runtime.SpockAssertionError

public class APIVersionError extends SpockAssertionError {

	public APIVersionError(String formatString, Object... args) {
		super(String.format(formatString, args));
	}
}
