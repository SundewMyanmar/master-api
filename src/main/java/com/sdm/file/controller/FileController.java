package com.sdm.file.controller;

import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.file.model.File;
import com.sdm.file.repository.FileRepository;
import com.sdm.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/files")
public class FileController extends DefaultReadController<File, String> {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Override
    protected DefaultRepository<File, String> getRepository() {
        return this.fileRepository;
    }

    @GetMapping("/{id}/folder")
    public ResponseEntity<PaginationResponse<File>> getPagingByFolder(
            @RequestParam(value = "page", defaultValue = "0") int pageId,
            @RequestParam(value = "size", defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", defaultValue = "id:DESC") String sortString,
            @RequestParam(value = "filter", defaultValue = "") String filter,
            @RequestParam(value="public", defaultValue = "false") Boolean isPublic,
            @RequestParam(value="hidden", defaultValue = "false") Boolean isHidden,
            @PathVariable("id") Integer id){
        Page<File> result;
        if(id==null || id<=0){
            result=fileRepository.findByFolderIsNull(this.buildPagination(pageId,pageSize,sortString),filter,
                    isPublic?null:false,
                    isHidden?null:File.Status.STORAGE);
        }else{
            result=fileRepository.findByFolder(this.buildPagination(pageId,pageSize,sortString),filter,id,
                    isPublic?null:false,
                    isHidden?null:File.Status.STORAGE);
        }
        return new ResponseEntity<>(new PaginationResponse<>(result), HttpStatus.PARTIAL_CONTENT);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<File> update(@Valid @RequestBody File body, @PathVariable("id") @Size(min = 36, max = 36) String id) {
        File file=this.getRepository().findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "There is no any data by : " + id.toString()));

        if (!id.equals(body.getId())) {
            throw new GeneralException(HttpStatus.CONFLICT,
                    "Path ID and body ID aren't match.");
        }

        file.setPublicAccess(body.isPublicAccess());
        file.setStatus(body.getStatus());
        file.setFolder(body.getFolder());
        file = getRepository().save(file);
        return ResponseEntity.ok(file);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> remove(@PathVariable("id") @Size(min = 36, max = 36) String id,
                                                  @RequestParam(value = "isTrash", required = false, defaultValue = "false") boolean isTrash) {
        fileService.remove(id, isTrash);
        MessageResponse message = new MessageResponse(HttpStatus.OK, "Successfully deleted.",
                "Deleted data on your request by : " + id, null);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("")
    @Transactional
    public ResponseEntity<MessageResponse> multiRemove(@RequestBody Set<String> ids,
                                                       @RequestParam(value = "isTrash", required = false, defaultValue = "false") final boolean isTrash) {
        ids.forEach(id -> fileService.remove(id, isTrash));
        MessageResponse message = new MessageResponse(HttpStatus.OK, "Successfully deleted.",
                "Deleted data on your request.", null);
        return ResponseEntity.ok(message);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<ListResponse<File>> uploadFile(@RequestParam("uploadedFile") List<MultipartFile> files,
                                                         @RequestParam(value = "isPublic", required = false, defaultValue = "false") boolean isPublic,
                                                         @RequestParam(value="isHidden",required = false,defaultValue = "false")boolean isHidden,
                                                         @RequestParam(value="folder", required = false,defaultValue = "")Integer folder) {
        ListResponse<File> uploadedFiles = new ListResponse<>();
        files.forEach(file -> {
            File fileEntity = fileService.create(file, isPublic,isHidden,folder);
            uploadedFiles.addData(fileEntity);
        });
        return new ResponseEntity<ListResponse<File>>(uploadedFiles, HttpStatus.CREATED);
    }

    @GetMapping("/download/{id}/{name}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") @Size(max = 36, min = 36) String id,
                                          @PathVariable("name") String filename,
                                          @RequestParam("size") Optional<File.ImageSize> imageSize) {

        return fileService.downloadFile(id, filename, imageSize.orElse(File.ImageSize.medium), false);
    }
}
