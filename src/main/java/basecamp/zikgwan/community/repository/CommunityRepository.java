package basecamp.zikgwan.community.repository;

import basecamp.zikgwan.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    // JpaRepository의 기본 메서드들 사용
    // - save() : 등록/수정
    // - findById() : ID로 조회
    // - findAll() : 전체 조회
    // - deleteById() : 삭제
}
