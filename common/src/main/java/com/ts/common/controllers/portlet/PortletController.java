package com.ts.common.controllers.portlet;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.entitites.commonEntities.UserProjectAssign;
import com.ts.common.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;

public class PortletController extends BaseController {
    public PortletController(String url, AuthToken authToken) {
        super(url, authToken);
    }

    public void assignUserProject(UserProjectAssign userProjectAssign) {
        UserProjectAssignRequestBody userProjectAssignRequestBody = new UserProjectAssignRequestBody(userProjectAssign);
        this.response = super.post(getEndpoint(REST, PORTLET, CONTROL_PLAN, USER_PROJECT_ASSIGMENT), userProjectAssignRequestBody.removeFields());
    }

    public GetUserProjectResponseBody getAssignedUserProjects(UserProjectAssign userProjectAssign) {
        Map<String, String> params = new HashMap<>() {{
            put("userLogin", userProjectAssign.getUser().getLogin());
            put("from", userProjectAssign.getFromStr());
            put("to", userProjectAssign.getToStr());
        }};
        this.response = super.get(getEndpoint(REST, PORTLET, CONTROL_PLAN, CONTROL_PLAN_BEANS, formatParameters(params)));
        return JsonUtils.deserialize(this.response, GetUserProjectResponseBody.class);
    }
}
