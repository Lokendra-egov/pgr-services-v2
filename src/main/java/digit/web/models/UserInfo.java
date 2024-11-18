package digit.web.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Role;
import digit.web.models.TenantRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.
 */
@Schema(description = "This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T11:13:37.148087853+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
    @JsonProperty("tenantId")
    @NotNull

    private String tenantId = null;

    @JsonProperty("uuid")

    private String uuid = null;

    @JsonProperty("userName")
    @NotNull

    private String userName = null;

    @JsonProperty("password")

    private String password = null;

    @JsonProperty("idToken")

    private String idToken = null;

    @JsonProperty("mobile")

    private String mobile = null;

    @JsonProperty("email")

    private String email = null;

    @JsonProperty("primaryrole")
    @NotNull
    @Valid
    private List<Role> primaryrole = new ArrayList<>();

    @JsonProperty("additionalroles")
    @Valid
    private List<TenantRole> additionalroles = null;


    public UserInfo addPrimaryroleItem(Role primaryroleItem) {
        this.primaryrole.add(primaryroleItem);
        return this;
    }

    public UserInfo addAdditionalrolesItem(TenantRole additionalrolesItem) {
        if (this.additionalroles == null) {
            this.additionalroles = new ArrayList<>();
        }
        this.additionalroles.add(additionalrolesItem);
        return this;
    }

}
