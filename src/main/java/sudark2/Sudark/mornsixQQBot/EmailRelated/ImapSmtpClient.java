package sudark2.Sudark.mornsixQQBot.EmailRelated;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import com.sun.mail.imap.IMAPStore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static sudark2.Sudark.mornsixQQBot.MornsixQQBot.logger;

public class ImapSmtpClient {

    private static final int MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB

    public static List<EmailMessage> getUnreadEmails() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", EmailConfig.getImapHost());
        props.put("mail.imap.port", String.valueOf(EmailConfig.getImapPort()));
        props.put("mail.imap.connectiontimeout", "10000");
        props.put("mail.imap.timeout", "15000");

        Session session = Session.getInstance(props);
        IMAPStore store = null;
        Folder folder = null;

        try {
            store = (IMAPStore) session.getStore("imap");
            store.connect(EmailConfig.getImapHost(), EmailConfig.getEmail(), EmailConfig.getPassword());

            // 163邮箱要求发送IMAP ID信息
            HashMap<String, String> iamMap = new HashMap<>();
            iamMap.put("name", "MornsixQQBot");
            iamMap.put("version", "2.0");
            iamMap.put("vendor", "Sudark");
            iamMap.put("support-email", "support@mornsix.com");
            store.id(iamMap);

            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);

            Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            List<EmailMessage> emails = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            for (Message message : messages) {
                EmailMessage em = new EmailMessage();
                em.setId(String.valueOf(message.getMessageNumber()));
                em.setSubject(message.getSubject() != null ? message.getSubject() : "(无主题)");

                Address[] fromAddrs = message.getFrom();
                if (fromAddrs != null && fromAddrs.length > 0) {
                    em.setFrom(((InternetAddress) fromAddrs[0]).getAddress());
                } else {
                    em.setFrom("未知发件人");
                }

                if (message.getReceivedDate() != null) {
                    em.setReceivedDateTime(sdf.format(message.getReceivedDate()));
                } else {
                    em.setReceivedDateTime("");
                }

                // 解析正文和附件
                em.setBodyPreview(extractText(message, 500));
                em.setAttachments(extractAttachments(message));

                // 标记为已读
                message.setFlag(Flags.Flag.SEEN, true);
                emails.add(em);
            }
            return emails;
        } finally {
            if (folder != null && folder.isOpen()) folder.close(false);
            if (store != null) store.close();
        }
    }

    public static boolean testConnection() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", EmailConfig.getImapHost());
        props.put("mail.imap.port", String.valueOf(EmailConfig.getImapPort()));
        props.put("mail.imap.connectiontimeout", "10000");
        props.put("mail.imap.timeout", "15000");

        IMAPStore store = null;
        Folder folder = null;
        try {
            Session session = Session.getInstance(props);
            store = (IMAPStore) session.getStore("imap");
            store.connect(EmailConfig.getImapHost(), EmailConfig.getEmail(), EmailConfig.getPassword());

            // 163邮箱要求发送IMAP ID信息
            HashMap<String, String> iamMap = new HashMap<>();
            iamMap.put("name", "MornsixQQBot");
            iamMap.put("version", "2.0");
            iamMap.put("vendor", "Sudark");
            iamMap.put("support-email", "support@mornsix.com");
            store.id(iamMap);

            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try { if (folder != null && folder.isOpen()) folder.close(false); } catch (Exception ignored) {}
            try { if (store != null) store.close(); } catch (Exception ignored) {}
        }
    }

    public static boolean sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", EmailConfig.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(EmailConfig.getSmtpPort()));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.port", String.valueOf(EmailConfig.getSmtpPort()));
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "15000");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EmailConfig.getEmail(), EmailConfig.getPassword());
                }
            });
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EmailConfig.getEmail()));
            message.setRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            return true;
        } catch (Exception e) {
            warn("§7发送邮件失败: " + e.getMessage());
            return false;
        }
    }

    private static String extractText(Part part, int maxLen) throws Exception {
        if (part.isMimeType("text/plain")) {
            String text = (String) part.getContent();
            return text.length() > maxLen ? text.substring(0, maxLen) : text;
        }
        if (part.isMimeType("text/html")) {
            String html = (String) part.getContent();
            String text = html.replaceAll("<[^>]+>", "").trim();
            return text.length() > maxLen ? text.substring(0, maxLen) : text;
        }
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            // 优先取text/plain
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    return extractText(bp, maxLen);
                }
            }
            // 没有text/plain则递归
            for (int i = 0; i < mp.getCount(); i++) {
                String result = extractText(mp.getBodyPart(i), maxLen);
                if (!result.isEmpty()) return result;
            }
        }
        return "";
    }

    private static List<EmailMessage.EmailAttachment> extractAttachments(Part part) throws Exception {
        List<EmailMessage.EmailAttachment> attachments = new ArrayList<>();
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String disposition = bp.getDisposition();
                if (Part.ATTACHMENT.equalsIgnoreCase(disposition) || Part.INLINE.equalsIgnoreCase(disposition)) {
                    String ct = bp.getContentType() != null ? bp.getContentType().toLowerCase() : "";
                    if (ct.contains("image") && bp.getSize() <= MAX_ATTACHMENT_SIZE) {
                        EmailMessage.EmailAttachment att = new EmailMessage.EmailAttachment();
                        att.setName(bp.getFileName() != null ? bp.getFileName() : "image");
                        att.setContentType(bp.getContentType());
                        att.setSize(bp.getSize());
                        att.setInline(Part.INLINE.equalsIgnoreCase(disposition));
                        att.setData(readBytes(bp.getInputStream()));
                        attachments.add(att);
                    }
                }
                // 递归嵌套multipart
                if (bp.isMimeType("multipart/*")) {
                    attachments.addAll(extractAttachments(bp));
                }
            }
        }
        return attachments;
    }

    private static byte[] readBytes(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1)
            baos.write(buf, 0, n);
        return baos.toByteArray();
    }

    private static void warn(String msg) {
        logger.warning(msg);
    }
}
