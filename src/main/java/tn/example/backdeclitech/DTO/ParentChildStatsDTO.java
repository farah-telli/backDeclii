package tn.example.backdeclitech.DTO;

public class ParentChildStatsDTO {
    private Long parentId;
    private Long childCount;

    public ParentChildStatsDTO() {}

    public ParentChildStatsDTO(Long parentId, Long childCount) {
        this.parentId = parentId;
        this.childCount = childCount;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getChildCount() {
        return childCount;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setChildCount(Long childCount) {
        this.childCount = childCount;
    }
}
