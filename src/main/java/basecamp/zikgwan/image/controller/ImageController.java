package basecamp.zikgwan.image.controller;


import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.config.security.CustomUserPrincipal;
import basecamp.zikgwan.image.Image;
import basecamp.zikgwan.image.enums.ImageType;
import basecamp.zikgwan.image.service.ImageService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지 업로드 (프로필 / 티켓 / 모임)
     *
     * @param type
     * @param file
     * @param refId
     * @param principal
     * @return
     * @throws Exception
     */
    @PostMapping("/{type}")
    public ResponseEntity<ApiResponse<Image>> uploadImage(
            @PathVariable String type,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long refId, // 모임/티켓 등 연관 ID
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) throws Exception {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("로그인 정보가 없습니다."));
        }

        ImageType imageType;
        try {
            imageType = ImageType.fromCode(type.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("잘못된 이미지 타입입니다."));
        }

        Image savedImage = imageService.uploadImage(imageType, refId, file, principal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(savedImage));
    }

    /**
     * 이미지 조회 (프로필 / 티켓 / 모임)
     *
     * @param type
     * @param refId
     * @return
     */
    @GetMapping("/{type}/{refId}")
    public ResponseEntity<ApiResponse<String>> getImage(
            @PathVariable String type,
            @PathVariable Long refId
    ) {
        ImageType imageType;
        try {
            imageType = ImageType.fromCode(type);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("잘못된 이미지 타입입니다."));
        }

        String path = imageService.getImage(imageType, refId);
        if (path == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("이미지를 찾을 수 없습니다."));
        }

        // 브라우저 접근 URL
        String imageUrl = "/images/" + path;
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(imageUrl));
    }

    /**
     * 이미지 삭제 (본인만 가능)
     *
     * @param imageId
     * @param principal
     * @return
     */
    @DeleteMapping("/delete/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("로그인 정보가 없습니다."));
        }

        Long userId = imageService.getOwnerIdByImageId(imageId);
        if (!userId.equals(principal.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
        }

        imageService.deleteImage(imageId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));

    }
}
