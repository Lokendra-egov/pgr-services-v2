package digit.util;

import digit.web.models.AuditDetails;
import digit.web.models.Service;
import org.springframework.stereotype.Component;

@Component
public class PGRUtil {
    public AuditDetails getAuditDetails(String by, Service service, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().createdBy(service.getAuditDetails().getCreatedBy()).lastModifiedBy(by)
                    .createdTime(service.getAuditDetails().getCreatedTime()).lastModifiedTime(time).build();
    }
}
