package com.sdm.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sdm.core.DefaultReadTest;
import com.sdm.core.DefaultTest;
import com.sdm.core.model.response.ListResponse;
import com.sdm.file.model.File;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public class FileControllerTest extends DefaultReadTest {

    private static File data;

    @AfterAll
    public synchronized static void clearFile() {
        data = null;
        DefaultTest.close();
    }

    @Override
    protected String getUrl() {
        return "/files";
    }

    @Override
    protected Serializable getId() {
        return data.getId();
    }

    private MockMultipartFile createImage() throws IOException {
        String text = faker.ancient().god();
        int width = 512;
        int height = 512;
        int fontSize = 48;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphic = image.createGraphics();
        graphic.setColor(Color.GRAY);
        graphic.fillRect(0, 0, width, height);

        graphic.setColor(Color.DARK_GRAY);
        Font font = new Font("Cambria", Font.BOLD, fontSize);
        graphic.setFont(font);

        FontMetrics metrics = graphic.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        int y = (width - metrics.getHeight()) / 2 + metrics.getAscent();
        graphic.drawString(text, x, y);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);

        return new MockMultipartFile("uploadedFile", text + ".png", MediaType.IMAGE_PNG_VALUE, outputStream.toByteArray());
    }

    @Test
    @Order(1)
    public void uploadFile() throws Exception {
        MockMultipartFile multipartFile = this.createImage();
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders
                .multipart(getUrl() + "/upload")
                .file(multipartFile)
                .param("isPublic", "true")
                .header("user-agent", USER_AGENT)
                .header("x-forwarded-for", CLIENT_IP));
        String json = result.andReturn().getResponse().getContentAsString();
        ListResponse<File> response = objectMapper.readValue(json, new TypeReference<ListResponse<File>>() {
        });
        Assertions.assertTrue(response.getCount() > 0);
        data = (File) response.getData().toArray()[0];

        result.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @Order(10)
    public void downloadFile() throws Exception {
        String url = getUrl() + "/download/" + getId() + "/" + data.getName();
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_PNG))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(11)
    public void publicDownloadFile() throws Exception {
        String url = "/public/files/" + getId() + "/" + data.getName();
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_PNG))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(100)
    public void remove() throws Exception {
        String url = getUrl() + "/" + getId();
        this.test(url, HttpMethod.DELETE, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(101)
    public void multiRemove() throws Exception {
        String url = getUrl();
        this.test(url, HttpMethod.DELETE, Set.of())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
