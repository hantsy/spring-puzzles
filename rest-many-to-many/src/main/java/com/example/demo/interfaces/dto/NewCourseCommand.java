package com.example.demo.interfaces.dto;

import javax.validation.constraints.NotEmpty;

public record NewCourseCommand(@NotEmpty String name, String description) {
}
