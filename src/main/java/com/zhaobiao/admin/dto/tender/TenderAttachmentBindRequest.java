package com.zhaobiao.admin.dto.tender;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "招标附件绑定请求")
public class TenderAttachmentBindRequest {

    @NotEmpty(message = "至少选择一个附件")
    @Schema(description = "文件ID列表")
    private List<Long> fileIds;

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }
}
