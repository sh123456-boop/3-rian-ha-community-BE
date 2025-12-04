package com.ktb.community.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class JoinRequestDto {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(min = 6, max = 254)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]|;:'\\\",.<>?/]).+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 최소 1자 이상 포함해야 합니다.")
    private String password;

    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @NotBlank(message = "비밀번호를 다시 입력해주세요. ")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]|;:'\\\",.<>?/]).+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 최소 1자 이상 포함해야 합니다.")
    private String rePassword;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?!.*[\\u3131-\\u318E])[A-Za-z0-9가-힣]+$",
            message = "닉네임은 영문, 숫자, 한글만 사용할 수 있으며 자모 단일 문자는 허용되지 않습니다.")
    private String nickname;

    public JoinRequestDto(String email, String password, String rePassword, String nickname) {
        this.email = email;
        this.password = password;
        this.rePassword = rePassword;
        this.nickname = nickname;
    }
}
