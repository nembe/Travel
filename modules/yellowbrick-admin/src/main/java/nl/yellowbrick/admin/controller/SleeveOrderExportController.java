package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.service.SleeveOrderCsvExporter;
import nl.yellowbrick.admin.service.SleeveOrderExportScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/provisioning/sleeve_exports")
public class SleeveOrderExportController {

    private static final DateTimeFormatter SCHEDULE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired private SleeveOrderCsvExporter sleeveOrderCsvExporter;
    @Autowired private SleeveOrderExportScheduler exportScheduler;

    @RequestMapping(method = RequestMethod.GET)
    public String exports(ModelMap model) {
        List<ExportsListItem> exports = sleeveOrderCsvExporter.listExports()
                .map(ExportsListItem::new)
                .collect(Collectors.toList());

        model.put("exports", exports);
        model.put("nextExportTime", exportScheduler.nextScheduledExport().format(SCHEDULE_DISPLAY_FORMAT));

        return "provisioning/sleeve_exports/index";
    }

    @RequestMapping(method = RequestMethod.GET, params = { "fileName" })
    public ResponseEntity<FileSystemResource> exportedFile(@RequestParam Map<String, String> requestParams) {
        File export = sleeveOrderCsvExporter.listExports()
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

        private ExportsListItem(Path export) {
            this.fileName = export.getFileName().toString();
            this.downloadUrl = String.format("/provisioning/sleeve_exports/?fileName=%s", this.fileName);
        }
    }
}
