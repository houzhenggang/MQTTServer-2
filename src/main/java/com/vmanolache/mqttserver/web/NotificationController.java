package com.vmanolache.mqttserver.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vmanolache.mqttserver.entity.Notification;
import com.vmanolache.mqttserver.entity.NotificationRepository;

/**
 * Controller for handling notification submissions and listing of notifications.
 * 
 * @author zakyalvan
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
	
	@Autowired
	private NotificationRepository notificationRepository;
	
	@RequestMapping(method=RequestMethod.POST)
	public HttpEntity<Notification> submitNotification(@Validated @RequestBody NotificationForm form, BindingResult bindingResult) {
		LOGGER.debug("Handle new notifications submissions");
		if(bindingResult.hasErrors()) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		
		/**
		 * FIXME
		 * 
		 * Use dozer here for binding new object! See http://dozer.sourceforge.net/
		 */
		Notification notification = new Notification();
		notification.setSubject(form.getSubject());
		notification.setContent(form.getContent());
		if(form.targets.size() > 0) {
			notification.setTargeted(true);
			notification.setTargets(form.getTargets());
		}
		else {
			notification.setTargeted(false);
		}
		notification.setPublished(false);
		Notification persistedNotification = notificationRepository.save(notification);
		return new ResponseEntity<>(persistedNotification, HttpStatus.CREATED);
	} 
}
