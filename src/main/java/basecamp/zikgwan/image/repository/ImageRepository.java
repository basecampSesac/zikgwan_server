package basecamp.zikgwan.image.repository;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.image.Image;
import basecamp.zikgwan.image.enums.ImageType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    //특정 타입, 참조 ID, 저장 상태로 이미지 조회 (활성화된 이미지 1개)
    Optional<Image> findByImageTypeAndRefIdAndSaveState(ImageType imageType, Long refId, SaveState saveState);

    //특정 엔티티에 연결된 모든 이미지 조회 (활성/비활성 구분 없이)
    List<Image> findAllByImageTypeAndRefId(ImageType imageType, Long refId);
}
