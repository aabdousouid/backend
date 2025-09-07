package com.bezkoder.spring.security.postgresql.dto;

import java.util.List;

public record PlatformActivityResponse (List<String> labels,
                                        List<Long> jobPostings,
                                        List<Long> applications,
                                        List<Long> hires){
}
