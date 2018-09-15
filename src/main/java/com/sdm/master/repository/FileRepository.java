package com.sdm.master.repository;

import com.sdm.master.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    /*
     @Override
    public <T extends Serializable> void delete(T entity, boolean autoCommit) {
        FileEntity fileEntity = (FileEntity) entity;
        File savedFile = new File(Setting.getInstance().get(Setting.UPLOAD_DIRECTORY) + fileEntity.getStoragePath());
        if (savedFile.exists() && savedFile.delete()) {
            LOG.info("Deleted File by ID => " + fileEntity.getId());
        }
        super.delete(fileEntity, autoCommit);
    }

    private File generateFile(long userId, String ext) {
        String uploadPath = "/" + Constants.USER_PREFIX + userId + Globalizer.getDateString("/yyyy/MMMM/", new Date());
        String fileName = Globalizer.generateToken(8) + "." + ext;

        File baseDir = new File(Setting.getInstance().get(Setting.UPLOAD_DIRECTORY) + uploadPath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        return new File(baseDir, fileName);
    }

    public FileEntity createFile(InputStream fileStream, FileEntity entity, boolean autoCommit) {
        File saveFile = this.generateFile(entity.getOwnerId(), entity.getExtension());

        // Get File Type from saved file.
        if (entity.getType() == null || entity.getType().length() <= 0) {
            final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
            String type = fileTypeMap.getContentType(saveFile);
            entity.setType(type);
        }

        try (OutputStream out = new FileOutputStream(saveFile)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = fileStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            fileStream.close();
        } catch (IOException e) {
            LOG.error(e);
        }

        // Set File Info
        entity.setFileSize(saveFile.length());
        entity.setStoragePath(
                saveFile.getPath().substring(Setting.getInstance().get(Setting.UPLOAD_DIRECTORY).length()));
        return super.insert(entity, autoCommit);
    }

    @Override
    public <T extends Serializable> T insert(T requestEntity, boolean autoCommit) {
        FileEntity entity = (FileEntity) requestEntity;
        if (entity.getExternalUrl()== null || entity.getExternalUrl().isEmpty()) {
            throw new InvalidRequestException("external_url",
                "External URL can't be blank. Otherwise, try to use file upload path.",
                "");
        }

        entity.setStatus(FileEntity.EXTERNAL);
        Client client = ClientBuilder.newClient();
        Response response = client.target(entity.getExternalUrl()).request().get();
        return (T) this.createFile(response.readEntity(InputStream.class), entity, autoCommit);
    }
     */
}
