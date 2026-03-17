package sudark2.Sudark.mornsixQQBot.EmailRelated;

import java.util.ArrayList;
import java.util.List;

public class EmailMessage {
    private String id;
    private String subject;
    private String from;
    private String bodyPreview;
    private String receivedDateTime;
    private List<EmailAttachment> attachments;

    public EmailMessage() {
        this.attachments = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBodyPreview() {
        return bodyPreview;
    }

    public void setBodyPreview(String bodyPreview) {
        this.bodyPreview = bodyPreview;
    }

    public String getReceivedDateTime() {
        return receivedDateTime;
    }

    public void setReceivedDateTime(String receivedDateTime) {
        this.receivedDateTime = receivedDateTime;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    public static class EmailAttachment {
        private String id;
        private String name;
        private String contentType;
        private int size;
        private boolean isInline;
        private byte[] data;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public boolean isInline() {
            return isInline;
        }

        public void setInline(boolean inline) {
            isInline = inline;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public boolean isImage() {
            return contentType != null && contentType.toLowerCase().contains("image");
        }
    }
}
