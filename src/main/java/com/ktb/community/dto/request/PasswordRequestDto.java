package com.ktb.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordRequestDto {

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]|;:'\\\",.<>?/]).+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 최소 1자 이상 포함해야 합니다.")
    private String password;

    @NotBlank(message = "새 비밀번호를 다시 입력해주세요. ")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]|;:'\\\",.<>?/]).+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 최소 1자 이상 포함해야 합니다.")
    private String rePassword;
}
