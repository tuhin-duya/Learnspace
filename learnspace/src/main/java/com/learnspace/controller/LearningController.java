package com.learnspace.controller;

import com.learnspace.model.*;
import com.learnspace.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api")
public class LearningController {
 private final CourseRepository courses; private final BatchRepository batches; private final LessonRepository lessons; private final EnrollmentRepository enrollments; private final UserRepository users;
 LearningController(CourseRepository c,BatchRepository b,LessonRepository l,EnrollmentRepository e,UserRepository u){courses=c;batches=b;lessons=l;enrollments=e;users=u;}
 record CourseInput(String title,String instructor,String category,String level,String description,double price){} record BatchInput(Long courseId,String name,String schedule,LocalDate startDate,int seats){} record LessonInput(Long courseId,String title,String videoUrl,String duration,int position){}
 private AppUser user(HttpSession s){Long id=(Long)s.getAttribute("user");if(id==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in");return users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
 private void admin(HttpSession s){if(user(s).getRole()!=Role.ADMIN)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator access required");}
 private Course course(Long id){return courses.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));}
 private void copy(Course target,CourseInput source){target.setTitle(source.title());target.setInstructor(source.instructor());target.setCategory(source.category());target.setLevel(source.level());target.setDescription(source.description());target.setPrice(source.price());}
 private Batch batch(Long id){return batches.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Batch not found"));}
 private void copy(Batch target,BatchInput source){target.setCourse(course(source.courseId()));target.setName(source.name());target.setSchedule(source.schedule());target.setStartDate(source.startDate());target.setSeats(source.seats());}
 private Lesson lesson(Long id){return lessons.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Lesson not found"));}
 private void copy(Lesson target,LessonInput source){target.setCourse(course(source.courseId()));target.setTitle(source.title());target.setVideoUrl(source.videoUrl());target.setDuration(source.duration());target.setPosition(source.position());}
 @GetMapping("/courses") public List<Course> courses(){return courses.findAll();}
 @PostMapping("/courses") @ResponseStatus(HttpStatus.CREATED) public Course createCourse(@RequestBody CourseInput input,HttpSession s){admin(s);Course c=new Course();copy(c,input);return courses.save(c);}
 @PutMapping("/courses/{id}") public Course updateCourse(@PathVariable Long id,@RequestBody CourseInput input,HttpSession s){admin(s);Course c=course(id);copy(c,input);return courses.save(c);}
 @DeleteMapping("/courses/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteCourse(@PathVariable Long id,HttpSession s){admin(s);if(batches.countByCourseId(id)>0||lessons.countByCourseId(id)>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Delete this course's batches and lessons first");courses.delete(course(id));}
 @GetMapping("/courses/{id}/batches") public List<Batch> batches(@PathVariable Long id){return batches.findByCourseId(id);}
 @GetMapping("/batches") public List<Batch> allBatches(){return batches.findAll();}
 @PostMapping("/batches") @ResponseStatus(HttpStatus.CREATED) public Batch createBatch(@RequestBody BatchInput input,HttpSession s){admin(s);Batch b=new Batch();copy(b,input);return batches.save(b);}
 @PutMapping("/batches/{id}") public Batch updateBatch(@PathVariable Long id,@RequestBody BatchInput input,HttpSession s){admin(s);Batch b=batch(id);copy(b,input);return batches.save(b);}
 @DeleteMapping("/batches/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteBatch(@PathVariable Long id,HttpSession s){admin(s);if(enrollments.countByBatchId(id)>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Cannot delete a batch with enrollments");batches.delete(batch(id));}
 @GetMapping("/courses/{id}/lessons") public List<Lesson> lessons(@PathVariable Long id){return lessons.findByCourseIdOrderByPosition(id);}
 @GetMapping("/lessons") public List<Lesson> allLessons(){return lessons.findAll();}
 @PostMapping("/lessons") @ResponseStatus(HttpStatus.CREATED) public Lesson createLesson(@RequestBody LessonInput input,HttpSession s){admin(s);Lesson l=new Lesson();copy(l,input);return lessons.save(l);}
 @PutMapping("/lessons/{id}") public Lesson updateLesson(@PathVariable Long id,@RequestBody LessonInput input,HttpSession s){admin(s);Lesson l=lesson(id);copy(l,input);return lessons.save(l);}
 @DeleteMapping("/lessons/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteLesson(@PathVariable Long id,HttpSession s){admin(s);lessons.delete(lesson(id));}
 @GetMapping("/enrollments/mine") public List<Enrollment> mine(HttpSession s){return enrollments.findByStudentId(user(s).getId());}
 @PatchMapping("/enrollments/{id}/progress") public Enrollment progress(@PathVariable Long id,@RequestBody Map<String,Integer> body,HttpSession s){Enrollment e=enrollments.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(!e.getStudent().getId().equals(user(s).getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);e.setProgress(Math.max(0,Math.min(100,body.getOrDefault("progress",0))));return enrollments.save(e);}
 @DeleteMapping("/enrollments/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteEnrollment(@PathVariable Long id,HttpSession s){Enrollment e=enrollments.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(!e.getStudent().getId().equals(user(s).getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);enrollments.delete(e);}
 @GetMapping("/dashboard") public Map<String,Object> dashboard(){return Map.of("courses",courses.count(),"batches",batches.count(),"learners",users.count(),"enrollments",enrollments.count());}
}
