package com.ktb.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class NicknameRequestDto {

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?!.*[\\u3131-\\u318E])[A-Za-z0-9가-힣]+$",
            message = "닉네임은 영문, 숫자, 한글만 사용할 수 있으며 자모 단일 문자는 허용되지 않습니다.")
    private String nickname;
}
