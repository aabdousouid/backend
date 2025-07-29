package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.Interview;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;


    public void sendCanceledInterview(String toEmail, Interview interview){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Mise à jour de votre candidature");


            String htmlContent = interviewCancelledHtml(interview);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }


    public void sendConfirmedInterview(String toEmail, Interview interview){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Mise à jour de votre candidature");


            String htmlContent = interviewConfirmedHtml(interview);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }

    public void sendRescheduledInterview(String toEmail, Interview interview){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Mise à jour de votre candidature");


            String htmlContent = interviewRescheduledHtml(interview);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }
    public void sendUpdateInterview(String toEmail, Interview interview){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Mise à jour de votre candidature");


            String htmlContent = interviewUpdated(interview);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }
    public void sendInterviewEmail(String toEmail, Interview interview){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Mise à jour de votre candidature");


            String htmlContent = InterviewScheduled(interview);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }

    public void sendCompletedInterview(String toEmail, Interview interview){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Mise à jour de votre candidature");


            String htmlContent = interviewCompletedHtml(interview);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }
    public void sendRejectionEmail(String toEmail, Application application){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Mise à jour de votre candidature");


            String htmlContent = ApplicationDenied(application.getUser().getUsername(),application.getJob().getTitle());
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }


    public void sendApprovalEmail(String toEmail, Application application){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Offre de poste ACTIA ES ======> "+application.getJob().getTitle()+" ( "+ application.getJob().getJobType() +" )");


            String htmlContent = ApplicationApproved(application.getUser().getUsername(),application.getJob().getTitle());
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }


    }

    public void sendHiredEmail(String toEmail, Application application){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Offre de poste ACTIA ES ======> "+application.getJob().getTitle()+" ( "+ application.getJob().getJobType() +" )");


            String htmlContent = ApplicationHired(application.getUser().getUsername(),application.getJob().getTitle());
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }


    }
    public void sendApplicationEmail(String toEmail){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Job application");


            String htmlContent = ApplicationEmail();
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email Address");

            String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;

            String htmlContent = buildVerificationEmailContent(verificationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    private String buildVerificationEmailContent(String verificationUrl) {
        return "<html>" +
                "<body>" +
                "<h2>Welcome! Please verify your email</h2>" +
                "<p>Thank you for registering. Please click the link below to verify your email address:</p>" +
                "<a href='" + verificationUrl + "' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Verify Email</a>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>If you didn't create an account, please ignore this email.</p>" +
                "</body>" +
                "</html>";
    }


    private String ApplicationEmail() {
        return "<html>" +
                "<body>" +
                "<h2>Application sent successfully</h2>" +
                "<p>Thank you for applying to job offer we will reach out soon inchalah:</p>" +
                "<p>If you didn't apply or don't have an account, please ignore this email.</p>" +
                "</body>" +
                "</html>";
    }

    private String ApplicationDenied(String username, String jobTitle) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".header { color: #2c5aa0; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc; font-size: 12px; color: #666; }" +
                ".logo { margin: 10px 0; }" +
                ".contact-info { margin: 10px 0; font-size: 13px; }" +
                ".disclaimer { margin-top: 15px; font-size: 11px; color: #888; font-style: italic; }" +
                ".content { margin: 20px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h2 class='header'>Mise à jour de votre demande d'emploi</h2>" +
                "<div class='content'>" +
                "<p>Bonjour " + username + ",</p>" +
                "<p>J'espère que vous allez bien.</p>" +
                "<p>Nous apprécions votre intérêt et les efforts que vous avez consacrés à votre candidature pour le poste de " + jobTitle + ". Le poste a suscité un intérêt exceptionnellement élevé et nous sommes ravis d'avoir reçu un tel nombre de candidats qualifiés, dont vous-même.</p>" +
                "<p>Après un examen approfondi et minutieux, nous avons décidé de poursuivre avec d'autres candidats pour ce poste spécifique.</p>" +
                "<p>Nous apprécions l'intérêt que vous portez à ACTIA Engineering Services et vous encourageons à rester en contact avec nous. N'hésitez pas à consulter notre site web régulièrement pour recevoir des mises à jour sur les opportunités futures au sein de notre entreprise.</p>" +
                "<p>Nous vous remercions encore une fois d'avoir envisagé de faire carrière chez nous. Nous vous souhaitons beaucoup de succès dans vos projets futurs et espérons que nos chemins se croiseront à nouveau.</p>" +
                "<p>Nous vous prions d'agréer, Madame, Monsieur, l'expression de nos salutations distinguées.</p>" +
                "</div>" +

                "<div class='footer'>" +
                "<div class='logo'>" +
                "<strong>ACTIA Engineering Services (siège social)</strong>" +
                "</div>" +
                "<div class='contact-info'>" +
                "Parc Technologique \"El-Ghazala\"<br>" +
                "1 Rue Newton, BP99 - 2088 ARIANA (Tunisie)<br>" +
                "Office: (+216) 70 68 79 17 (ext. 167)<br>" +
                "VoIP: (+33) 5 35 54 25 18 (ext. 167)<br>" +
                "Website: <a href='http://www.actia.com'>www.actia.com</a>" +
                "</div>" +
                "<div class='disclaimer'>" +
                "<hr>" +
                "<p>Ce courrier et toutes ses pièces jointes sont destinés exclusivement aux personnes ou institutions dont le nom figure ci-dessus et peuvent contenir des informations protégées par le secret professionnel, dont la divulgation est strictement prohibée.</p>" +
                "<p>Tout message électronique est susceptible d'altération. ACTIA Engineering Services décline toute responsabilité au titre de ce message s'il a été altéré, déformé ou falsifié. Si vous n'êtes pas destinataire, nous vous avisons que sa lecture, sa reproduction ou sa distribution sont strictement interdites. Nous vous prions en conséquence de nous aviser immédiatement par retour de ce courrier.</p>" +
                "<br>" +
                "<p>This mail and any attachments are intended solely for the use of the intended recipient(s) and may contain confidential and privileged information. Any unauthorized review, use, disclosure or distribution is prohibited. If you are not the intended recipient, please contact the sender by reply e-mail and destroy all copies of the original message. Thank you.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String ApplicationApproved(String username,String jobTitle) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".header { color: #2c5aa0; }" +
                ".success { color: #28a745; font-weight: bold; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc; font-size: 12px; color: #666; }" +
                ".logo { margin: 10px 0; }" +
                ".contact-info { margin: 10px 0; font-size: 13px; }" +
                ".disclaimer { margin-top: 15px; font-size: 11px; color: #888; font-style: italic; }" +
                ".next-steps { background-color: #f8f9fa; padding: 15px; border-left: 4px solid #28a745; margin: 15px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h2 class='header'>Congratulations "+ username+"! Your Application Has Been Approved</h2>" +
                "<p>Dear Candidate,</p>" +
                "<p class='success'>We are delighted to inform you that your application has been successfully approved!</p>" +
                "<p>After careful review of your qualifications and interview performance, we are pleased to inform that we decided to move forward with your application for the position of "+ jobTitle +". Your skills and experience make you an excellent fit for our team.</p>" +

                "<div class='next-steps'>" +
                "<h3>Next Steps:</h3>" +
                "<ul>" +
                "<li>You will receive a formal email invite for an interview within the next 2-3 business days</li>" +
                "<li>HR will contact you to discuss your disponibility</li>" +
                "<li>Please prepare any required documentation for onboarding</li>" +
                "</ul>" +
                "</div>" +

                "<p>We are excited to welcome you to the ACTIA Engineering Services family and look forward to working with you.</p>" +
                "<p>If you have any questions in the meantime, please don't hesitate to reach out.</p>" +
                "<p>Once again, congratulations on your success!</p>" +
                "<p>Best regards,</p>" +

                "<div class='footer'>" +
                "<div class='logo'>" +
                "<strong>ACTIA Engineering Services (siège social)</strong>" +
                "</div>" +
                "<div class='contact-info'>" +
                "Parc Technologique \"El-Ghazala\"<br>" +
                "1 Rue Newton, BP99 - 2088 ARIANA (Tunisie)<br>" +
                "Office: (+216) 70 68 79 17 (ext. 167)<br>" +
                "VoIP: (+33) 5 35 54 25 18 (ext. 167)<br>" +
                "Website: <a href='http://www.actia.com'>www.actia.com</a>" +
                "</div>" +
                "<div class='disclaimer'>" +
                "<hr>" +
                "<p>Ce courrier et toutes ses pièces jointes sont destinés exclusivement aux personnes ou institutions dont le nom figure ci-dessus et peuvent contenir des informations protégées par le secret professionnel, dont la divulgation est strictement prohibée.</p>" +
                "<p>Tout message électronique est susceptible d'altération. ACTIA Engineering Services décline toute responsabilité au titre de ce message s'il a été altéré, déformé ou falsifié. Si vous n'êtes pas destinataire, nous vous avisons que sa lecture, sa reproduction ou sa distribution sont strictement interdites. Nous vous prions en conséquence de nous aviser immédiatement par retour de ce courrier.</p>" +
                "<br>" +
                "<p>This mail and any attachments are intended solely for the use of the intended recipient(s) and may contain confidential and privileged information. Any unauthorized review, use, disclosure or distribution is prohibited. If you are not the intended recipient, please contact the sender by reply e-mail and destroy all copies of the original message. Thank you.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }



    private String ApplicationHired(String username,String jobTitle) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".header { color: #2c5aa0; }" +
                ".success { color: #28a745; font-weight: bold; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc; font-size: 12px; color: #666; }" +
                ".logo { margin: 10px 0; }" +
                ".contact-info { margin: 10px 0; font-size: 13px; }" +
                ".disclaimer { margin-top: 15px; font-size: 11px; color: #888; font-style: italic; }" +
                ".next-steps { background-color: #f8f9fa; padding: 15px; border-left: 4px solid #28a745; margin: 15px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h2 class='header'>Congratulations "+ username+"! Your Application Has Been Approved</h2>" +
                "<p>Dear Candidate,</p>" +
                "<p class='success'>We are delighted to inform you that your application has been successfully approved!</p>" +
                "<p>After careful review of your qualifications and interview performance, we are pleased to offer you the position of "+ jobTitle +". Your skills and experience make you an excellent fit for our team.</p>" +

                "<div class='next-steps'>" +
                "<h3>Next Steps:</h3>" +
                "<ul>" +
                "<li>You will receive a formal job offer letter within the next 2-3 business days</li>" +
                "<li>HR will contact you to discuss salary, benefits, and start date</li>" +
                "<li>Please prepare any required documentation for onboarding</li>" +
                "</ul>" +
                "</div>" +

                "<p>We are excited to welcome you to the ACTIA Engineering Services family and look forward to working with you.</p>" +
                "<p>If you have any questions in the meantime, please don't hesitate to reach out.</p>" +
                "<p>Once again, congratulations on your success!</p>" +
                "<p>Best regards,</p>" +

                "<div class='footer'>" +
                "<div class='logo'>" +
                "<strong>ACTIA Engineering Services (siège social)</strong>" +
                "</div>" +
                "<div class='contact-info'>" +
                "Parc Technologique \"El-Ghazala\"<br>" +
                "1 Rue Newton, BP99 - 2088 ARIANA (Tunisie)<br>" +
                "Office: (+216) 70 68 79 17 (ext. 167)<br>" +
                "VoIP: (+33) 5 35 54 25 18 (ext. 167)<br>" +
                "Website: <a href='http://www.actia.com'>www.actia.com</a>" +
                "</div>" +
                "<div class='disclaimer'>" +
                "<hr>" +
                "<p>Ce courrier et toutes ses pièces jointes sont destinés exclusivement aux personnes ou institutions dont le nom figure ci-dessus et peuvent contenir des informations protégées par le secret professionnel, dont la divulgation est strictement prohibée.</p>" +
                "<p>Tout message électronique est susceptible d'altération. ACTIA Engineering Services décline toute responsabilité au titre de ce message s'il a été altéré, déformé ou falsifié. Si vous n'êtes pas destinataire, nous vous avisons que sa lecture, sa reproduction ou sa distribution sont strictement interdites. Nous vous prions en conséquence de nous aviser immédiatement par retour de ce courrier.</p>" +
                "<br>" +
                "<p>This mail and any attachments are intended solely for the use of the intended recipient(s) and may contain confidential and privileged information. Any unauthorized review, use, disclosure or distribution is prohibited. If you are not the intended recipient, please contact the sender by reply e-mail and destroy all copies of the original message. Thank you.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }




    private String InterviewScheduled(Interview interview) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".header { color: #2c5aa0; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc; font-size: 12px; color: #666; }" +
                ".logo { margin: 10px 0; }" +
                ".contact-info { margin: 10px 0; font-size: 13px; }" +
                ".disclaimer { margin-top: 15px; font-size: 11px; color: #888; font-style: italic; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h2 class='header'>Interview Scheduled - Congratulations!</h2>" +
                "<p>Dear Candidate,</p>" +
                "<p>We are pleased to inform you that your application for the job "+interview.getApplication().getJob().getTitle()+" has been reviewed and you have been selected for an "+ interview.getInterviewTest() +" interview.</p>" +
                "<p><strong>Interview Details:</strong></p>" +
                "<ul>" +
                "<li>Date: "+ interview.getScheduledDate() +"</li>" +
                "<li>Time: "+interview.getScheduledHour()+"</li>" +
                "<li>Location: "+ interview.getLocation() +"</li>" +
                "</ul>" +
                "<p>Please confirm your attendance by replying to this email. If you have any questions or need to reschedule, please contact us as soon as possible.</p>" +
                "<p>We look forward to meeting you!</p>" +
                "<p>Best regards,</p>" +

                "<div class='footer'>" +
                "<div class='logo'>" +
                "<strong>ACTIA Engineering Services</strong>" +
                "</div>" +
                "<div class='contact-info'>" +
                "Parc Technologique \"El-Ghazala\"<br>" +
                "1 Rue Newton, BP99 - 2088 ARIANA (Tunisie)<br>" +
                "Office: (+216) 70 68 79 17 (ext. 167)<br>" +
                "VoIP: (+33) 5 35 54 25 18 (ext. 167)<br>" +
                "Website: <a href='http://www.actia.com'>www.actia.com</a>" +
                "</div>" +
                "<div class='disclaimer'>" +
                "<hr>" +
                "<p>Ce courrier et toutes ses pièces jointes sont destinés exclusivement aux personnes ou institutions dont le nom figure ci-dessus et peuvent contenir des informations protégées par le secret professionnel, dont la divulgation est strictement prohibée.</p>" +
                "<p>Tout message électronique est susceptible d'altération. ACTIA Engineering Services décline toute responsabilité au titre de ce message s'il a été altéré, déformé ou falsifié. Si vous n'êtes pas destinataire, nous vous avisons que sa lecture, sa reproduction ou sa distribution sont strictement interdites. Nous vous prions en conséquence de nous aviser immédiatement par retour de ce courrier.</p>" +
                "<br>" +
                "<p>This mail and any attachments are intended solely for the use of the intended recipient(s) and may contain confidential and privileged information. Any unauthorized review, use, disclosure or distribution is prohibited. If you are not the intended recipient, please contact the sender by reply e-mail and destroy all copies of the original message. Thank you.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


    private String interviewUpdated(Interview interview) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".header { color: #007bff; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc; font-size: 12px; color: #666; }" +
                ".logo { margin: 10px 0; }" +
                ".contact-info { margin: 10px 0; font-size: 13px; }" +
                ".disclaimer { margin-top: 15px; font-size: 11px; color: #888; font-style: italic; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h2 class='header'>Mise à jour de votre entretien</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Nous vous informons que les détails de votre entretien pour le poste <strong>" + interview.getApplication().getJob().getTitle() + "</strong> ont été mis à jour.</p>" +
                "<p><strong>Nouveaux détails de l'entretien :</strong></p>" +
                "<ul>" +
                "<li><strong>Type :</strong> " + interview.getInterviewTest() + "</li>" +
                "<li><strong>Date :</strong> " + interview.getScheduledDate() + "</li>" +
                "<li><strong>Heure :</strong> " + interview.getScheduledHour() + "</li>" +
                "<li><strong>Lieu :</strong> " + interview.getLocation() + "</li>" +
                "<li><strong>Intervenant :</strong> " + interview.getInterviewerName() + "</li>" +
                "</ul>" +
                "<p>Veuillez confirmer la réception de cette mise à jour. Si vous avez besoin de reporter l'entretien, n'hésitez pas à nous contacter.</p>" +
                "<p>Cordialement,</p>" +
                "<div class='footer'>" +
                "<div class='logo'>" +
                "<strong>ACTIA Engineering Services</strong>" +
                "</div>" +
                "<div class='contact-info'>" +
                "Parc Technologique \"El-Ghazala\"<br>" +
                "1 Rue Newton, BP99 - 2088 ARIANA (Tunisie)<br>" +
                "Office: (+216) 70 68 79 17 (ext. 167)<br>" +
                "VoIP: (+33) 5 35 54 25 18 (ext. 167)<br>" +
                "Website: <a href='http://www.actia.com'>www.actia.com</a>" +
                "</div>" +
                "<div class='disclaimer'>" +
                "<hr>" +
                "<p>Ce message est strictement confidentiel. Si vous n'en êtes pas le destinataire, merci de le supprimer immédiatement et de nous en informer.</p>" +
                "<p>This message is confidential. If you are not the intended recipient, please delete it and notify us immediately.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


    private String interviewCompletedHtml(Interview interview) {
        return "<html>" +
                "<head><style>body { font-family: Arial; color: #333; }</style></head>" +
                "<body>" +
                "<h2 style='color: #28a745;'>Entretien Terminé</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Votre entretien pour le poste de <strong>" + interview.getApplication().getJob().getTitle() + "</strong> a été complété avec succès.</p>" +
                "<p>Nous allons analyser votre performance et reviendrons vers vous prochainement concernant la suite du processus.</p>" +
                "<p>Merci pour votre temps et votre implication.</p>" +
                signatureFooter() +
                "</body></html>";
    }

    private String interviewCancelledHtml(Interview interview) {
        return "<html>" +
                "<head><style>body { font-family: Arial; color: #333; }</style></head>" +
                "<body>" +
                "<h2 style='color: #dc3545;'>Entretien Annulé</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Nous vous informons que votre entretien pour le poste de <strong>" + interview.getApplication().getJob().getTitle() + "</strong> a été annulé.</p>" +
                "<p>Si vous avez des questions ou besoin de précisions, n’hésitez pas à nous contacter.</p>" +
                "<p>Nous nous excusons pour ce désagrément.</p>" +
                signatureFooter() +
                "</body></html>";
    }
    private String interviewRescheduledHtml(Interview interview) {
        return "<html>" +
                "<head><style>body { font-family: Arial; color: #333; }</style></head>" +
                "<body>" +
                "<h2 style='color: #ffc107;'>Entretien Replanifié</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Votre entretien pour le poste de <strong>" + interview.getApplication().getJob().getTitle() + "</strong> a été replanifié. Voici les nouveaux détails :</p>" +
                "<ul>" +
                "<li>Date : " + interview.getScheduledDate() + "</li>" +
                "<li>Heure : " + interview.getScheduledHour() + "</li>" +
                "<li>Lieu : " + interview.getLocation() + "</li>" +
                "</ul>" +
                "<p>Merci de confirmer votre disponibilité dès que possible.</p>" +
                signatureFooter() +
                "</body></html>";
    }


    private String interviewConfirmedHtml(Interview interview) {
        return "<html>" +
                "<head><style>body { font-family: Arial; color: #333; }</style></head>" +
                "<body>" +
                "<h2 style='color: #007bff;'>Entretien Confirmé</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Nous confirmons que votre entretien de type <strong>" + interview.getInterviewTest() + "</strong> pour le poste de <strong>" + interview.getApplication().getJob().getTitle() + "</strong> est programmé comme suit :</p>" +
                "<ul>" +
                "<li>Date : " + interview.getScheduledDate() + "</li>" +
                "<li>Heure : " + interview.getScheduledHour() + "</li>" +
                "<li>Lieu : " + interview.getLocation() + "</li>" +
                "</ul>" +
                "<p>Nous vous remercions de votre intérêt et vous souhaitons bonne chance.</p>" +
                signatureFooter() +
                "</body></html>";
    }

    private String signatureFooter() {
        return "<div style='margin-top: 30px; font-size: 13px;'>" +
                "<strong>ACTIA Engineering Services</strong><br>" +
                "Parc Technologique \"El-Ghazala\"<br>" +
                "1 Rue Newton, BP99 - 2088 ARIANA (Tunisie)<br>" +
                "Office: (+216) 70 68 79 17 (ext. 167)<br>" +
                "VoIP: (+33) 5 35 54 25 18 (ext. 167)<br>" +
                "Website: <a href='http://www.actia.com'>www.actia.com</a><br>" +
                "<hr><p style='font-size: 11px; color: #666; font-style: italic;'>" +
                "Ce courrier et toutes ses pièces jointes sont destinés exclusivement aux personnes ou institutions dont le nom figure ci-dessus..." +
                "</p></div>";
    }


}
