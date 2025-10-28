package com.ktb.community.controller;

import com.ktb.community.dto.ApiResponseDto;
import com.ktb.community.dto.request.PreSignedUrlRequestDto;
import com.ktb.community.dto.response.PreSignedUrlResponseDto;
import com.ktb.community.service.S3ServiceImpl;
import com.ktb.community.util.SessionRequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "S3 API", description = "s3 도메인 API")
@RestController
@RequiredArgsConstructor
public class S3urlController {

    private final S3ServiceImpl s3Service;


    @Operation(
            summary = "Presigned URL 생성",
            description = "게시물 이미지 업로드를 위한 Presigned URL을 생성하여 반환합니다. 클라이언트는 이 URL을 사용해 직접 S3와 같은 스토리지에 파일을 업로드할 수 있습니다. 요청 헤더에 Access Token이 반드시 필요합니다.",
            security = { @SecurityRequirement(name = "accessTokenAuth") },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Presigned URL을 생성할 파일의 정보(예: 파일명)를 담은 JSON Body 데이터",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PreSignedUrlRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Presigned URL 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PreSignedUrlResponseDto.class)
                            )
                    )
            }
    )
    @PostMapping("/v1/posts/presignedUrl")
    public ApiResponseDto<PreSignedUrlResponseDto> getUrl(
            @RequestBody @Valid PreSignedUrlRequestDto dto,
            HttpServletRequest request
    ) {
        Long userId = SessionRequestUtils.getRequiredUserId(request);

        PreSignedUrlResponseDto urlResponseDto = s3Service.getPostPresignedPutUrl(userId, dto.getFileName());
        return ApiResponseDto.success(urlResponseDto);
    }





    @Operation(
            summary = "Presigned URL 생성",
            description = "유저 프로필 이미지 업로드를 위한 Presigned URL을 생성하여 반환합니다. 클라이언트는 이 URL을 사용해 직접 S3와 같은 스토리지에 파일을 업로드할 수 있습니다. 요청 헤더에 Access Token이 반드시 필요합니다.",
            security = { @SecurityRequirement(name = "accessTokenAuth") },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Presigned URL을 생성할 파일의 정보(예: 파일명)를 담은 JSON Body 데이터",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PreSignedUrlRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Presigned URL 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PreSignedUrlResponseDto.class)
                            )
                    )
            }
    )
    @PostMapping("/v1/users/presignedUrl")
    public ApiResponseDto<PreSignedUrlResponseDto> getProfileUrl (
            HttpServletRequest request,
            @RequestBody PreSignedUrlRequestDto requestDto
    ){
        Long userId = SessionRequestUtils.getRequiredUserId(request);
        PreSignedUrlResponseDto urlResponseDto = s3Service.getProfileImagePresignedUrl(userId, requestDto.getFileName());
        return ApiResponseDto.success(urlResponseDto);
    }

}
