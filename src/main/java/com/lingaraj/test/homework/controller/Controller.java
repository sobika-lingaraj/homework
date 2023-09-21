package com.lingaraj.test.homework.controller;

import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.lingaraj.test.homework.dto.DeleteResponse;
import com.lingaraj.test.homework.dto.GetUserResponse;
import com.lingaraj.test.homework.dto.PatchRequest;
import com.lingaraj.test.homework.dto.PatchResponse;
import com.lingaraj.test.homework.dto.SignUpRequest;
import com.lingaraj.test.homework.dto.SignUpResponse;
import com.lingaraj.test.homework.dto.UserDto;
import com.lingaraj.test.homework.entity.User;
import com.lingaraj.test.homework.repository.UserRepository;

@RestController
public class Controller {

    @Autowired
    UserRepository userRepository;

    @PostMapping(path = "/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        SignUpResponse response = new SignUpResponse();
        
        if (request.getUserId() == null || request.getPassword() == null) {
            response.setMessage("Account creation failed");
            response.setCause("required user_id and password");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.getUserId().length() < 6 || request.getPassword().length() < 8) {
            response.setMessage("Account creation failed");
            response.setCause("Cannot create an account without user_id and password length");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.findFirstByUserId(request.getUserId()).isPresent()) {
            response.setMessage("Account creation failed");
            response.setCause("already same user_id is used");
            return ResponseEntity.badRequest().body(response);
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(request.getPassword());
        userRepository.save(user);

        response.setMessage("Account successfully created");
        response.setUser(new UserDto());
        response.getUser().setUserId(user.getUserId());
        response.getUser().setNickname(user.getNickname() == null ? user.getUserId() : user.getNickname());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "/users/{id}")
    public ResponseEntity<GetUserResponse> getUser(
            @RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable String id) {
        String[] authPair = getUserIdFromHeader(authorization);
        GetUserResponse response = new GetUserResponse();

        if (authPair == null) {
            response.setMessage("Authentication Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        System.out.println(authPair[0]);
        System.out.println(userRepository.findAll().size());
        Optional<User> userOptionalAuth = userRepository.findFirstByUserId(authPair[0]);
        if (userOptionalAuth.isEmpty()) {
            response.setMessage("Authentication Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<User> userOptional = userRepository.findFirstByUserId(id);

        if (userOptional.isEmpty()) {
            response.setMessage("No User found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setMessage("User details by user_id");
        response.setUser(new UserDto());
        response.getUser().setUserId(userOptional.get().getUserId());
        response.getUser().setNickname(userOptional.get().getNickname() == null ? userOptional.get().getUserId()
                : userOptional.get().getNickname());
        response.getUser().setComment(userOptional.get().getComment());
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping(path = "/users/{id}")
    public ResponseEntity<PatchResponse> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable String id,
            @RequestBody PatchRequest request) {
        String[] authPair = getUserIdFromHeader(authorization);
        PatchResponse response = new PatchResponse();

        if (authPair == null) {
            response.setMessage("Authentication Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<User> userOptionalAuth = userRepository.findFirstByUserId(authPair[0]);
        if (userOptionalAuth.isEmpty()) {
            response.setMessage("Authentication Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (request.getNickname() == null && request.getComment() == null) {
            response.setMessage("User updation failed");
            response.setCause("required nickname or comment");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.getUserId() != null || request.getPassword() != null) {
            response.setMessage("User updation failed");
            response.setCause("not updatable user_id and password");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOptional = userRepository.findFirstByUserId(id);

        if (userOptional.isEmpty()) {
            response.setMessage("No User found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!userOptional.get().getUserId().equals(userOptionalAuth.get().getUserId())) {
            response.setMessage("No Permission for Update");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (request.getNickname() != null) {
            userOptional.get().setNickname(request.getNickname());
        }
        if (request.getComment() != null) {
            userOptional.get().setComment(request.getComment());
        }
        userRepository.save(userOptional.get());

        response.setMessage("User successfully updated");
        response.setRecipe(new UserDto());
        response.getRecipe().setNickname(userOptional.get().getNickname() == null ? userOptional.get().getUserId()
                : userOptional.get().getNickname());
        response.getRecipe().setComment(userOptional.get().getComment());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(path = "/close")
    public ResponseEntity<DeleteResponse> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String[] authPair = getUserIdFromHeader(authorization);
        DeleteResponse response = new DeleteResponse();

        if (authPair == null) {
            response.setMessage("Authentication Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<User> userOptional = userRepository.findFirstByUserId(authPair[0]);
        if (userOptional.isEmpty()) {
            response.setMessage("Authentication Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        userRepository.delete(userOptional.get());
        response.setMessage("Account and user successfully removed");
        return ResponseEntity.ok(response);
    }

    String[] getUserIdFromHeader(String authorization) {
        if (null == authorization) {
            return null;
        }

        if (!authorization.startsWith("Basic ")) {
            return null;
        }

        System.out.println(authorization.substring(6));
        try {
            byte[] decodedAuthBytes = Base64.getDecoder().decode(authorization.substring(6));
            String decodedAuth = new String(decodedAuthBytes);

            if (!decodedAuth.contains(":")) {
                return null;
            }
            return decodedAuth.split(":", 2);
        } catch (Exception exception) {
            return null;
        }
    }
}
