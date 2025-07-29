package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.Interview;
import com.bezkoder.spring.security.postgresql.models.InterviewStatus;

import java.util.List;

public interface InterviewService {
Interview addInterview(Interview interview,Long applicationId);

Interview findById(Long interviewId);

void deleteById(Long interviewId);
List<Interview> findApplicationInterview(Long applicationId);

Interview updateInterview (Long interviewId,Interview interview);

Interview updateStatus (Long interviewId, String status);

}
