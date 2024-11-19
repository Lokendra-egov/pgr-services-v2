package digit.service;

import digit.config.Configuration;
import digit.web.models.RequestSearchCriteria;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.UserDetailResponse;
import org.egov.common.contract.user.UserSearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import digit.util.UserUtil;

import static digit.config.ServiceConstants.USERTYPE_CITIZEN;

@Component
public class UserService {

    private UserUtil userUtils;

    private Configuration config;

    public UserService(UserUtil userUtils, Configuration config) {
        this.userUtils = userUtils;
        this.config = config;
    }

    /**
     * Enriches the list of userUuids associated with the mobileNumber in the search criteria
     * @param tenantId
     * @param criteria
     */
    public void enrichUserIds(String tenantId, RequestSearchCriteria criteria){

        String mobileNumber = criteria.getMobileNumber();

        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_CITIZEN);
        userSearchRequest.setTenantId(tenantId);
        userSearchRequest.setMobileNumber(mobileNumber);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest,uri);
        List<User> users = userDetailResponse.getUser();

        Set<String> userIds = users.stream().map(User::getUuid).collect(Collectors.toSet());
        criteria.setUserIds(userIds);
    }
}
