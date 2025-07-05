package com.catalyst.ProCounsellor.processor.dto;

import java.util.List;

import lombok.Data;

@Data
public class CollegeResponse {
    private String nirf_rank;
    private String avg_placement;
    private String highest_placement;
    private List<String> top_recruiters;
    private String fee_per_year;
    private String total_fee;
    private List<Course> courses_offered;
    private Location location;
    private String infrastructure;
    private String hostel_facility;
    private String scholarships;
    private Accreditation accreditation;
    private String college_type;
    private String established_year;
    private String description;
}