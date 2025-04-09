/*
 * @ {#} RegisterRequest.java   1.0     15/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   15/03/2025
 * @version:    1.0
 */

@Builder
public class SignUpRequest {
    @JsonProperty("display_name")
    @NotBlank(message = "Display name is required")
    private String displayName;
    @NotBlank(message = "Phone number is required")
    private String phone;

    @Size(min=8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate dob;
    List<String> roles;

    public @NotBlank(message = "Display name is required") String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NotBlank(message = "Display name is required") String displayName) {
        this.displayName = displayName;
    }

    public @NotBlank(message = "Phone number is required") String getPhone() {
        return phone;
    }

    public void setPhone(@NotBlank(message = "Phone number is required") String phone) {
        this.phone = phone;
    }

    public @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") String getPassword() {
        return password;
    }

    public void setPassword(@Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") String password) {
        this.password = password;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
