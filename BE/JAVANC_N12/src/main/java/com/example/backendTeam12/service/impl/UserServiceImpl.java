package com.example.backendTeam12.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backendTeam12.model.CodeInfo;
import com.example.backendTeam12.model.User;
import com.example.backendTeam12.repository.UserRepository;
import com.example.backendTeam12.service.UserService;
import com.example.backendTeam12.utils.EmailUtil;
import com.example.backendTeam12.utils.Generate;
import com.example.backendTeam12.utils.Validate;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    
    private final EmailUtil emailUtil;
    public UserServiceImpl(EmailUtil emailUtil) {
        this.emailUtil = emailUtil;
    }
    private final Map<String, CodeInfo> resetCodes = new ConcurrentHashMap<>();

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

        
        String code = Generate.generateCode();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        resetCodes.put(email, new CodeInfo(code, expiry));

        String username = user.getUserName();
        emailUtil.sendResetPasswordEmail(email, code, username);

        return code;
    } 

    @Override
    public boolean verifyResetCode(String email, String code) {
        CodeInfo info = resetCodes.get(email);
        if (info == null || !info.getCode().equals(code)) {
            return false;
        }

        if (LocalDateTime.now().isAfter(info.getExpiry())) {
            resetCodes.remove(email); 
            return false; 
        }
        return true;
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        CodeInfo codeInfo = resetCodes.get(email);
        if (codeInfo == null || LocalDateTime.now().isAfter(codeInfo.getExpiry())) {
            throw new RuntimeException("Code không hợp lệ hoặc đã hết hạn");
        }
        
        if (!codeInfo.getCode().equals(code)) {
            throw new RuntimeException("Mã xác nhận không đúng");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        
        user.setPassword(Generate.hashPassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Xóa token sau khi dùng
        resetCodes.remove(email);
    }

    @Override
    public void changePassword(String password, long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(Generate.hashPassword(password));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
} 