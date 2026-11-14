package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.repository.Userrepository;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class Usercontroller {

    private final Userrepository userRepository;

    public Usercontroller(Userrepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {

        return userRepository.save(user);
    }

    @GetMapping
    public List<User> getUsers() {

        return userRepository.findAll();
    }
}