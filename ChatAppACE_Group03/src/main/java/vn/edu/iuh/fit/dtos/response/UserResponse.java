/*
 * @ {#} UserResponse.java   1.0     15/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   15/03/2025
 * @version:    1.0
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private ObjectId id;
    @JsonProperty("display_name")
    private String displayName;
    private String password;
    private String gender;
    private String phone;
    private String avatar;
    private LocalDate dob;
    private boolean enabled;
    List<String> roles;
}
