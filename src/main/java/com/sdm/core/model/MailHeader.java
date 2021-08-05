/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Htoonlin
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailHeader {

    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;

    public MailHeader(String to, String subject) {
        this.to = to;
        this.subject = subject;
    }
}
