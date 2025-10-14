package basecamp.zikgwan.image.service;


import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.image.Image;
import basecamp.zikgwan.image.enums.ImageType;
import basecamp.zikgwan.image.repository.ImageRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${file.upload-dir}")
    private String uploadDir; // application.properties 값 사용

    /**
     * 이미지 업로드/교체
     *
     * @param type
     * @param refId
     * @param file
     * @param ownerId
     * @return
     * @throws IOException
     */
    @Transactional
    public Image uploadImage(ImageType type, Long refId, MultipartFile file, Long ownerId) throws IOException {
        log.info("type : {}, refId : {}, ownerId : {}", type, refId, ownerId);

        // 기존 이미지 삭제처리
        if (type.equals(ImageType.U)) {
            imageRepository.findByImageTypeAndRefIdAndSaveState(type, ownerId, SaveState.Y)
                    .ifPresent(img -> img.setSaveState(SaveState.N));
        } else {
            imageRepository.findByImageTypeAndRefIdAndSaveState(type, refId, SaveState.Y)
                    .ifPresent(img -> img.setSaveState(SaveState.N));
        }

        // 타입별 폴더 생성
        Path folder = Paths.get(uploadDir, type.getPath());
        Files.createDirectories(folder);

        // 파일 저장
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = folder.resolve(filename);
        file.transferTo(filePath);

        // 엔티티 저장 (브라우저 접근용 상대 경로 저장)
        Image newImage = Image.builder()
                .imageType(type)
                .refId(refId != null ? refId : ownerId)
                .imagePath(type.getPath() + "/" + filename)
                .saveState(SaveState.Y)
                .build();

        return imageRepository.save(newImage);
    }

    /**
     * 이미지 조회
     *
     * @param type
     * @param refId
     * @return
     */
    public String getImage(ImageType type, Long refId) {
        return imageRepository.findByImageTypeAndRefIdAndSaveState(type, refId, SaveState.Y)
                .map(Image::getImagePath)
                .orElse(null);
    }

    /**
     * 이미지 삭제
     *
     * @param imageId
     */
    @Transactional
    public void deleteImage(Long imageId) {
        imageRepository.findById(imageId)
                .ifPresent(img -> img.setSaveState(SaveState.N));
        /*
        imageRepository.findById(imageId)
                .ifPresent(img -> {
                    Image delImg = Image.builder()
                            .imageId(img.getImageId())
                            .imageType(img.getImageType())
                            .refId(img.getRefId())
                            .imagePath(img.getImagePath())
                            .saveState(SaveState.N)
                            .build();
                    imageRepository.save(delImg);
                });

         */
    }

    /**
     * 이미지 소유자 조회 (본인여부확인)
     *
     * @param imageId
     * @return
     */
    public Long getOwnerIdByImageId(Long imageId) {
        return imageRepository.findById(imageId)
                .map(Image::getRefId)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));
    }
}
