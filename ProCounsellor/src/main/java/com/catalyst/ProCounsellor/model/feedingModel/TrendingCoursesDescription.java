package com.catalyst.ProCounsellor.model.feedingModel;

import java.util.List;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Data;

@Data
public class TrendingCoursesDescription {
	@DocumentId
	private String descriptionId;
    private String overview;
    private List<String> top_institutes;
    private List<String> skills_covered;
    private List<String> job_roles;
    private String salary_range;
    private String industry_demand;
    private String average_course_duration;
    private List<String> certifications;
    private List<String> popular_online_courses;
    private String future_scope;
    private String market_growth_projection;
    private String global_demand;
    private String investment_required;
    private String eligibility_criteria;
    private List<String> top_companies_hiring;
    private TrendingCoursesDescriptionUsefulResources useful_resources;
    private List<String> industry_domains;
    private String course_type;
}
