package basecamp.zikgwan.image;

import basecamp.zikgwan.common.domain.CreatedEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.image.enums.ImageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "images")
public class Image extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "ref_type", nullable = false)
    private ImageType imageType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "image_path", length = 100, nullable = false)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Y'")
    @Column(name = "save_state", nullable = false)
    private SaveState saveState;

}
