package com.ts.common.controllers.settings;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.settings.udf.NewValueUdfListRequestBody;
import com.ts.common.entitites.commonEntities.List;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.enums.Type;
import com.ts.common.utils.JsonUtils;
import io.restassured.response.Response;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;

public class UdfController extends BaseController {
    public UdfController(String url, AuthToken authToken) {
        super(url, authToken);
    }

    public List[] getListValuesOf(Udfs.UdfSd udfField) {
        this.get(getEndpoint(REST, UDF, udfField.udfId, LIST_VALUES));
        return JsonUtils.deserialize(this.response, List[].class);
    }

    public Response newListValueFor(Udfs.UdfSd udfField, List list) {
        if (!udfField.type.equals(Type.LIST)) throw new RuntimeException("Udf only support LIST");
        NewValueUdfListRequestBody requestBody = new NewValueUdfListRequestBody(list);
        this.post(getEndpoint(REST, UDF, udfField.udfId, LIST_VALUES), requestBody.removeFields());
        List responseBody = JsonUtils.deserialize(this.response, List.class);
        if (responseBody != null) {
            list.setId(responseBody.getId());
        }
        return this.response;
    }
}
