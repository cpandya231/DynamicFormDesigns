package ai.smartfac.logever.repository;

import ai.smartfac.logever.entity.EmailDetails;

public interface EmailServiceRepository {
    String sendSimpleMail(EmailDetails details);
    String sendMailWithAttachment(EmailDetails details);
}
