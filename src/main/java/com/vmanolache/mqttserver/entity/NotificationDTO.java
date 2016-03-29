
package com.vmanolache.mqttserver.entity;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author vmanolache
 */ 
public class NotificationDTO implements Serializable {

    private final String subject;
    private final String content;
    private final Date publishDate;

    public NotificationDTO(String subject, String content) {
        this.subject = subject;
        this.content = content;
        this.publishDate = new Date();
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public Date getPublishDate() {
        return publishDate;
    }
}
