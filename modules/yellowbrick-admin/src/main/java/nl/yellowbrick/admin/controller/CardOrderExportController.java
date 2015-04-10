package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.service.CardOrderCsvExporter;
import nl.yellowbrick.admin.service.CardOrderExportScheduler;
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

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.yellowbrick.admin.util.CommonRequestParams.PRODUCT_GROUP_KEY;
import static nl.yellowbrick.admin.util.CommonRequestParams.from;

@Controller
@RequestMapping("/provisioning/exports")
public class CardOrderExportController {

    private static final DateTimeFormatter SCHEDULE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired private CardOrderCsvExporter cardOrderExporter;
    @Autowired private CardOrderExportScheduler exportScheduler;

    @RequestMapping(method = RequestMethod.GET)
    public String exports(@ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups) {
        ProductGroup defaultProductGroup = allProductGroups.get(0);

        return "redirect:/provisioning/exports?productGroup=" + defaultProductGroup.getId();
    }

    @RequestMapping(method = RequestMethod.GET, params = { "productGroup" })
    public String exportsByProductGroup(ModelMap model,
                                        @RequestParam Map<String, String> requestParams,
                                        @ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups) {

        ProductGroup productGroup = from(requestParams).productGroupOrDefault(allProductGroups);

        List<ExportsListItem> exports = cardOrderExporter.listExports(productGroup)
                .stream()
                .map(file -> new ExportsListItem(file, productGroup))
                .collect(Collectors.toList());

        Optional<LocalDateTime> nextExportTime = exportScheduler.nextScheduledExport(productGroup);

        model.put(PRODUCT_GROUP_KEY, productGroup);
        model.put("exports", exports);
        nextExportTime.ifPresent(t -> model.put("nextExportTime", t.format(SCHEDULE_DISPLAY_FORMAT)));

        return "/provisioning/exports/index";
    }

    @RequestMapping(method = RequestMethod.GET, params = { "productGroup", "fileName" })
    public ResponseEntity<FileSystemResource> exportedFile(@RequestParam Map<String, String> requestParams,
                             @ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups) {
        ProductGroup productGroup = from(requestParams).productGroupOrDefault(allProductGroups);

        File export = cardOrderExporter.listExports(productGroup)
                .stream()
                .filter(e -> e.getFileName().toString().equals(requestParams.get("fileName")))
                .map(Path::toFile)
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);

        FileSystemResource resource = new FileSystemResource(export);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentLength(export.length());
        headers.setContentDispositionFormData("attachment", resource.getFilename());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private class ExportsListItem {

        public final String fileName;
        public final String downloadUrl;

        private ExportsListItem(Path export, ProductGroup pg) {
            this.fileName = export.getFileName().toString();
            this.downloadUrl = String.format("/provisioning/exports/?productGroup=%s&fileName=%s", pg.getId(), this.fileName);
        }
    }
}
