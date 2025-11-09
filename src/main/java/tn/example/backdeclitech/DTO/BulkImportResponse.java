package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkImportResponse<T> {
    private int successCount;
    private int failureCount;
    private List<T> successes;
}
