package com.example.demo.interfaces.dto;

import javax.validation.constraints.NotEmpty;

public record NewStudentCommand(@NotEmpty String name) {
}
