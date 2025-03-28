package com.swiftiq.logever.builder;

import com.swiftiq.logever.builder.entity.EmailDetails;

public interface EmailServiceRepository {
    String sendSimpleMail(EmailDetails details);
    String sendMailWithAttachment(EmailDetails details);
}
