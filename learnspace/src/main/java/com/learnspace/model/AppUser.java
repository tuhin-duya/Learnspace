package com.learnspace.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
@Entity public class AppUser { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private String displayName; @Column(unique=true) private String email; private String password; @Enumerated(EnumType.STRING) private Role role;
 public Long getId(){return id;} public String getDisplayName(){return displayName;} public void setDisplayName(String v){displayName=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} @JsonIgnore public String getPassword(){return password;} public void setPassword(String v){password=v;} public Role getRole(){return role;} public void setRole(Role v){role=v;} }
