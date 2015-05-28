package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.service.WelcomeLetterExportService;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.yellowbrick.admin.util.CommonRequestParams.PRODUCT_GROUP_KEY;
import static nl.yellowbrick.admin.util.CommonRequestParams.from;

@Controller
@RequestMapping("/provisioning/welcome_letters")
public class WelcomeLetterController {

    @Autowired private WelcomeLetterExportService exportService;

    @RequestMapping(method = RequestMethod.GET)
    public String get(ModelMap model,
                      @RequestParam Map<String, String> requestParams,
                      @ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups) {

        ProductGroup productGroup = from(requestParams).productGroupOrDefault(allProductGroups);

        List<ExportsListItem> exports = exportService.listExports(productGroup)
                .map(resource -> new ExportsListItem(resource, productGroup))
                .collect(Collectors.toList());

        model.put(PRODUCT_GROUP_KEY, productGroup);
        model.addAttribute("exports", exports);

        return "provisioning/welcome_letters/index";
    }

    @RequestMapping(method = RequestMethod.GET, params = { "productGroup", "fileName" })
    public ResponseEntity<FileSystemResource> exportedFile(
            @RequestParam Map<String, String> requestParams,
            @ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups) {

        ProductGroup productGroup = from(requestParams).productGroupOrDefault(allProductGroups);

        FileSystemResource resource = exportService.listExports(productGroup)
                .filter(r -> r.getFilename().equals(requestParams.get("fileName")))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentLength(resource.getFile().length());
        headers.setContentDispositionFormData("attachment", resource.getFilename());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private class ExportsListItem {

        public final String fileName;
        public final String downloadUrl;

        private ExportsListItem(FileSystemResource resource, ProductGroup pg) {
            this.fileName = resource.getFilename();
            this.downloadUrl = String.format("/provisioning/welcome_letters/?productGroup=%s&fileName=%s",
                    pg.getId(), this.fileName);
        }
    }
}
