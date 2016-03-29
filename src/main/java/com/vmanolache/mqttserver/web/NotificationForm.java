/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vmanolache.mqttserver.web;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Notification form backing object.
 *
 * @author vmanolache
 */ 
public class NotificationForm implements Serializable {

    /**
     * Subject of notification.
     */
    @NotBlank
    private String subject;

    /**
     * Content of notification.
     */
    @NotBlank
    private String content;

    /**
     * Username as targets of notification.
     */
    Set<String> targets = new HashSet<>();

    /**
     * Scheduled publish date.
     */
    private Date publishDate;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<String> getTargets() {
        return new HashSet<>(targets);
    }

    public void setTargets(Set<String> targets) {
        this.targets.addAll(targets);
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }
}
