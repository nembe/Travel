package nl.yellowbrick.admin.util;

import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.data.domain.ProductGroup;

import java.util.List;
import java.util.Map;

public class CommonRequestParams {

    public static final String PRODUCT_GROUP_KEY = "productGroup";

    private final Map<String, String> params;

    private CommonRequestParams(Map<String, String> params) {
        this.params = params;
    }

    public ProductGroup productGroupOrDefault(List<ProductGroup> allProductGroups) {
        if(!params.containsKey(PRODUCT_GROUP_KEY))
            return allProductGroups.stream()
                    .findFirst()
                    .orElseThrow(() -> new InconsistentDataException("no product groups"));

        String requestedProductGroup = params.get(PRODUCT_GROUP_KEY);

        return allProductGroups.stream()
                .filter(pg -> pg.getId().toString().equals(requestedProductGroup))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("unknown product group id " + requestedProductGroup));
    }

    public static CommonRequestParams from(Map<String, String> params) {
        return new CommonRequestParams(params);
    }
}
