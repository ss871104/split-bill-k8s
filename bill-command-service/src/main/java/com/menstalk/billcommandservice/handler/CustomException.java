package com.menstalk.billcommandservice.handler;

import org.hibernate.service.spi.ServiceException;

public class CustomException extends ServiceException {
    public CustomException(String message) {
        super(message);
    }
}
