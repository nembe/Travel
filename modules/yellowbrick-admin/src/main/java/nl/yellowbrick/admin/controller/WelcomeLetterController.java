package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.service.WelcomeLetterExportService;
import nl.yellowbrick.admin.util.MessageHelper;
import nl.yellowbrick.data.dao.WelcomeLetterSettingsDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.yellowbrick.admin.util.CommonRequestParams.PRODUCT_GROUP_KEY;
import static nl.yellowbrick.admin.util.CommonRequestParams.from;

@Controller
@RequestMapping("/provisioning/welcome_letters")
public class WelcomeLetterController {

    private static final int MAX_LIST_SIZE = 30;

    @Autowired private WelcomeLetterExportService exportService;
    @Autowired private WelcomeLetterSettingsDao welcomeLetterSettingsDao;

    @RequestMapping(method = RequestMethod.GET)
    public String get(ModelMap model,
                      @RequestParam Map<String, String> requestParams,
                      @ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups) {

        ProductGroup productGroup = from(requestParams).productGroupOrDefault(allProductGroups);

        List<ExportsListItem> exports = exportService.listExports(productGroup)
                .map(resource -> new ExportsListItem(resource, productGroup))
                .sorted()
                .limit(MAX_LIST_SIZE)
                .collect(Collectors.toList());

        model.put(PRODUCT_GROUP_KEY, productGroup);
        model.addAttribute("exports", exports);

        if(!model.containsAttribute("nextBatchForm"))
            model.addAttribute("nextBatchForm", nextBatchForm(productGroup));

        if(!model.containsAttribute("betweenDatesForm"))
            model.addAttribute("betweenDatesForm", betweenDatesForm(productGroup));

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

    @RequestMapping(method = RequestMethod.POST, params = "action=exportBatch")
    public String triggerNextBatch(ModelMap model,
                                   @RequestParam Map<String, String> requestParams,
                                   @ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups,
                                   @ModelAttribute("nextBatchForm") @Valid NextBatchForm form,
                                   BindingResult bindingResult,
                                   RedirectAttributes ra) {

        if(bindingResult.hasErrors())
            return get(model, requestParams, allProductGroups);
        else
            model.clear();

        ProductGroup productGroup = from(requestParams).productGroupOrDefault(allProductGroups);

        exportService.exportForProductGroup(productGroup, form.getCustomer()).apply(
                exception -> MessageHelper.flashWarning(ra, "welcomeLetter.exportError", exception.getMessage()),
                path -> MessageHelper.flash(ra, "welcomeLetter.exported")
        );

        return "redirect:/provisioning/welcome_letters";
    }

    @RequestMapping(method = RequestMethod.POST, params = "action=exportBetweenDates")
    public String triggerNextBatch(ModelMap model,
                                   @RequestParam Map<String, String> requestParams,
                                   @ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups,
                                   @ModelAttribute("betweenDatesForm") @Valid BetweenDatesForm form,
                                   BindingResult bindingResult,
                                   RedirectAttributes ra) {

        if(bindingResult.hasErrors())
            return get(model, requestParams, allProductGroups);
        else
            model.clear();

        ProductGroup productGroup = from(requestParams).productGroupOrDefault(allProductGroups);

        exportService.exportForProductGroup(productGroup, form.getStartDate(), form.getEndDate()).apply(
                exception -> MessageHelper.flashWarning(ra, "welcomeLetter.exportError", exception.getMessage()),
                path -> MessageHelper.flash(ra, "welcomeLetter.exported")
        );

        return "redirect:/provisioning/welcome_letters";
    }

    private NextBatchForm nextBatchForm(ProductGroup productGroup) {
        NextBatchForm form = new NextBatchForm();
        form.setProductGroup(productGroup.getId());

        welcomeLetterSettingsDao.latestExportedCustomer().ifPresent(form::setCustomer);

        return form;
    }

    private BetweenDatesForm betweenDatesForm(ProductGroup productGroup) {
        BetweenDatesForm form = new BetweenDatesForm();
        form.setProductGroup(productGroup.getId());

        return form;
    }

    private class ExportsListItem implements Comparable<ExportsListItem> {

        public final String fileName;
        public final String downloadUrl;

        private ExportsListItem(FileSystemResource resource, ProductGroup pg) {
            this.fileName = resource.getFilename();
            this.downloadUrl = String.format("/provisioning/welcome_letters/?productGroup=%s&fileName=%s",
                    pg.getId(), this.fileName);
        }

        @Override
        public int compareTo(ExportsListItem other) {
            return fileName.compareTo(other.fileName) * -1;
        }
    }

    static class NextBatchForm {

        @NotNull
        private Long customer;
        @NotNull
        private Long productGroup;

        public NextBatchForm() {}

        public Long getCustomer() {
            return customer;
        }

        public void setCustomer(long customerId) {
            this.customer = customerId;
        }

        public Long getProductGroup() {
            return productGroup;
        }

        public void setProductGroup(long productGroup) {
            this.productGroup = productGroup;
        }
    }

    static class BetweenDatesForm {

        @DateTimeFormat(pattern = "dd-MM-yyyy")
        @NotNull
        private Date startDate;
        @DateTimeFormat(pattern = "dd-MM-yyyy")
        @NotNull
        private Date endDate;
        @NotNull
        private Long productGroup;

        public BetweenDatesForm() {}

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public Long getProductGroup() {
            return productGroup;
        }

        public void setProductGroup(long productGroup) {
            this.productGroup = productGroup;
        }
    }
}
