package basecamp.zikgwan.ticketsale.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketSalePageResponse {

    private List<TicketSaleResponse> content;
    private int page;                        // 현재 페이지 번호
    private int size;                        // 페이지당 개수
    private long totalElements;              // 전체 게시글 수
    private int totalPages;                  // 전체 페이지 수
    private boolean last;                    // 마지막 페이지 여부
}
