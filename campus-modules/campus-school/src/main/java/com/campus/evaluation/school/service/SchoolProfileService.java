package com.campus.evaluation.school.service;

import com.campus.evaluation.school.domain.dto.SchoolProfileUpdateDTO;
import com.campus.evaluation.school.domain.vo.SchoolProfileVO;

public interface SchoolProfileService {

    SchoolProfileVO getCurrentProfile();

    SchoolProfileVO updateCurrentProfile(SchoolProfileUpdateDTO dto);
}
