package basecamp.zikgwan.ticketsale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerInfo {

    private Long userId;
    private String nickname;
    private String imageUrl;
}
