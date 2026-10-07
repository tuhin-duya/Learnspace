package com.learnspace.model;
import jakarta.persistence.*;
@Entity public class Lesson { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(optional=false) private Course course; private String title, videoUrl, duration; private int position;
 public Long getId(){return id;} public Course getCourse(){return course;} public void setCourse(Course v){course=v;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getVideoUrl(){return videoUrl;} public void setVideoUrl(String v){videoUrl=v;} public String getDuration(){return duration;} public void setDuration(String v){duration=v;} public int getPosition(){return position;} public void setPosition(int v){position=v;} }
