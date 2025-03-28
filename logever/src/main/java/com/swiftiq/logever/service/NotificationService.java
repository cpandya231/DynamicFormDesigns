package com.swiftiq.logever.service;

import com.swiftiq.logever.kraftos.NotificationRepository;
import com.swiftiq.logever.kraftos.SettingsRepository;
import com.swiftiq.logever.kraftos.entity.Notification;
import com.swiftiq.logever.kraftos.entity.Settings;
import com.swiftiq.logever.kraftos.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    AppService appService;

    public List<Notification> findAllRelevant(User user) {
        return notificationRepository.findByUserOrRoleInAndDepartment(user.getUsername(), user.getRoles().stream().map(r->r.getId()).collect(Collectors.toList()), user.getDepartment().getId());
    }

    public List<Notification> findAllRelevantForApp(User user, String app) {
        if(app.equalsIgnoreCase("Logever")) {
            List appList = appService.getAll().stream().filter(a->a.getType().equalsIgnoreCase("Logever")).map(a->a.getName()).collect(Collectors.toList());
            appList.add("Logever");
            return notificationRepository
                    .findAllRelevantforLogever(user.getUsername(),
                            user.getRoles().stream().map(r->r.getId()).collect(Collectors.toList()),
                            user.getDepartment().getId(),
                            appList);
        }
        else
            return notificationRepository
                    .findAllRelevant(user.getUsername(),
                            user.getRoles().stream().map(r->r.getId()).collect(Collectors.toList()),
                            user.getDepartment().getId(),
                            app);
    }

    public List<Notification> findAllFor(User user) {
        return notificationRepository.findByUserOrRoleInAndDepartment(user.getUsername(),
                user.getRoles().stream().map(r->r.getId()).collect(Collectors.toList()),
                user.getDepartment().getId());
    }

    public void deleteFormEntryNotification(String appName, String formId, String entryId) {
        notificationRepository.deleteAllByAppAndPrimaryIdAndSubPrimaryId(appName, formId, entryId);
    }
    public void deleteAll(List<Notification> notifications) {
        notificationRepository.deleteAll(notifications);
    }

    public void delete(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public void deleteAllByAppAndId(String app, String primaryId) {
        notificationRepository.deleteAllByAppAndPrimaryId(app, primaryId);
    }

    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }
}
