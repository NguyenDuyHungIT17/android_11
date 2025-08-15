package com.example.backendTeam12.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backendTeam12.model.TokenInfo;
import com.example.backendTeam12.model.User;
import com.example.backendTeam12.repository.UserRepository;
import com.example.backendTeam12.service.UserService;
import com.example.backendTeam12.utils.EmailUtil;
import com.example.backendTeam12.utils.Generate;
import com.example.backendTeam12.utils.Validate;

@Service
public class UserServiceImpl implements UserService {
    private final Map<String, TokenInfo> resetTokens = new ConcurrentHashMap<>();
    
    @Autowired
    private UserRepository userRepository;

    
    private final EmailUtil emailUtil;
    public UserServiceImpl(EmailUtil emailUtil) {
        this.emailUtil = emailUtil;
    }

    @Override
    public User createUser(User user) {
        String hasspassword = Generate.hashPassword(user.getPassword());

        if (!Validate.isValidPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Định dạng số điện thoại không hợp lệ");
        }

        if (!Validate.isValidEmail(user.getEmail())) {
            throw new RuntimeException("Định dạng email không hợp lệ");
        }
        user.setPassword(hasspassword);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        //update
        existingUser.setUserName(user.getUserName());
        existingUser.setPassword(Generate.hashPassword(user.getPassword()));
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setGender(user.getGender());
        existingUser.setFullName(user.getFullName());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setUpdatedAt(LocalDateTime.now());

        //save
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUserName(String username) {
        return Optional.ofNullable(userRepository.findByUserName(username));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsByUserName(String username) {
        return userRepository.existsByUserName(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void loginUser(String username, String password){
         Optional<User> optionalUser = getUserByUserName(username);
        User user = optionalUser.orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        if (!Generate.checkPassword(password, user.getPassword())) {
            throw new RuntimeException("Password is incorrect");
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    @Override
    public List<User> searchUsersByUsername(String search) {
        return userRepository.findByUserNameContainingIgnoreCase(search);
    }

    @Override
    public int percentNewUser(){
        LocalDate  now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        long totalUsers = userRepository.countAllUser();
        if (totalUsers == 0){
            return 0;
        }

        long newUsers = userRepository.countAllUserCreatedAtMonth(month, year);
        if(newUsers == 0){
            return 0;
        }

        int percent = (int) (((double)newUsers / totalUsers) * 100);
       
        return  percent;
    }

    @Override
    public String forgotPassword(String email) throws Exception {
        User user = getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15); // hết hạn sau 15 phút
        resetTokens.put(token, new TokenInfo(email, expiry));

        String userName = user.getFullName();
        emailUtil.sendResetPasswordEmail(email, token, userName);
        return token;
    } 

    @Override
    public boolean validateResetToken(String token) {
        TokenInfo info = resetTokens.get(token);
        if (info == null) {
            return false; // token không tồn tại
        }

        if (LocalDateTime.now().isAfter(info.getExpiry())) {
            resetTokens.remove(token); // xóa token đã hết hạn
            return false;
        }

        return true;
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        TokenInfo info = resetTokens.get(token);
        if (info == null || LocalDateTime.now().isAfter(info.getExpiry())) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }

        if (info.getExpiry().isBefore(LocalDateTime.now())) {
            resetTokens.remove(token);
            throw new RuntimeException("Token đã hết hạn");
        }
        
        User user = getUserByEmail(info.getEmail())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Hash password mới
        user.setPassword(Generate.hashPassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Xóa token sau khi dùng
        resetTokens.remove(token);
    }

    @Override
    public void changePassword(String password, long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(Generate.hashPassword(password));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
} 