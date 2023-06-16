package com.menstalk.authservice.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NoWhitespaceValidator implements ConstraintValidator<NoWhitespace, String> {
    public void initialize(NoWhitespace constraint) {
    }

    public boolean isValid(String str, ConstraintValidatorContext context) {
        return str != null && !str.contains(" ");
    }
}
