package com.example.demo;

import com.example.demo.interfaces.dto.NewCourseCommand;
import com.example.demo.interfaces.dto.NewStudentCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Slf4j
class IntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        RestAssured.port = this.port;
    }

    @Test
    void getAllCourse() {
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .get("/courses")

           .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    void getNoneExistingCourse() {
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .get("/courses/"+ Long.MAX_VALUE)

            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    /**
     * This test will go through the following flow:
     * 1. Create a new course
     * 2. Extract the location and verify it is created.
     * 3. Get the sub resources `/students` under this newly created course.
     * 4. Create a new Student
     * 5. Add the new Student to the above Course
     * 6. Verify it is added successfully
     * 7. Remove the student from the Course.
     * 8. Remove the Course.
     * 9. Verify the Course is removed successfully.
     */
    @SneakyThrows
    @Test
    void testCrudFlow() {

        // create a course.
        //@formatter:off
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(new NewCourseCommand("test course", "description of test course")))

            .when()
               .post("/courses")


            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", containsString("/courses"));
        //@formatter:on

        //extract the Location from http response header.
        var location = response.extract().headers().get("Location").getValue();
        log.info("Location:: {}", location);

        // verify the course is added successfully by the Location.
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .get(location)

            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("title", is("test course"))
                .body("description", is("description of test course"));
        //@formatter:on

        //Get the students sub resources.
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .get(location+"/students")

            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        //@formatter:on

        // add a new student
        //@formatter:off
        ValidatableResponse studentResponse = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(new NewStudentCommand("test student")))

            .when()
                .post("/students")


            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", containsString("/students"));
        //@formatter:on

        //extract the id from the Location header.
        var studentLocation = studentResponse.extract().headers().get("Location").getValue();
        log.info("Location:: {}", studentLocation);
        var locArray = studentLocation.split("/");

        assertThat(locArray.length).isGreaterThan(0);
        var id = Long.parseLong(locArray[locArray.length - 1]);

        // add the student to the course.
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .post(location+"/students/"+ id)

            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        //@formatter:on

        // Get all students sub resources.
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .get(location+"/students")

            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", hasItem("test student"));
        //@formatter:on

        // remove the student from the course.
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .delete(location+"/students/"+ id)

           .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        //@formatter:on

        // remove the course.
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .delete(location)

            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        //@formatter:on

        // verify the course is deleted successfully.
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .get(location)

            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    @Test
    void testReport() {
        //@formatter:off
        given()
                .accept(ContentType.JSON)

            .when()
                .get("/report")

            .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("title", hasItem("Jakarta EE course"));
        //@formatter:on
    }

}
