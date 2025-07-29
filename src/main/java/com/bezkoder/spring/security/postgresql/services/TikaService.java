package com.bezkoder.spring.security.postgresql.services;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class TikaService {

    private final Tika tika = new Tika();

    public String parse(File file) throws IOException, TikaException {
        return tika.parseToString(file);
    }
}
