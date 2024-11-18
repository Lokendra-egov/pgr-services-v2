package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
//import digit.web.models.HttpsrawGithubusercontentComegovernmentsmunicipalServicescommonContractdocscommonContractV200YmlcomponentsschemasResponseInfo;
//import digit.web.models.ServiceWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

import org.egov.common.contract.response.ResponseInfo;

/**
 * Response to the service request
 */
@Schema(description = "Response to the service request")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T11:13:37.148087853+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceResponse   {
        @JsonProperty("responseInfo")

          @Valid
                private ResponseInfo responseInfo = null;

        @JsonProperty("PGREntities")
          @Valid
                private List<ServiceWrapper> pgREntities = null;


        public ServiceResponse addPgREntitiesItem(ServiceWrapper pgREntitiesItem) {
            if (this.pgREntities == null) {
            this.pgREntities = new ArrayList<>();
            }
        this.pgREntities.add(pgREntitiesItem);
        return this;
        }

}
