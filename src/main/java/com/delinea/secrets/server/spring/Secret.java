package com.delinea.secrets.server.spring;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Java representation of a <i>Secret</i> retrieved from Secret Server.
 */
public class Secret {
    /**
     * Java representation of an <i>Item</i> of a <i>Secret</i>.
     */
    public static class Field {
        private int id;
        private int fieldId;
        private int fileAttachmentId;
        private String fieldDescription;
        private String fieldName;
        private String filename;
        private String value;
        private String slug;

        @JsonProperty("itemId")
        public int getId() {
            return id;
        }

        public int getFieldId() {
            return fieldId;
        }

        public int getFileAttachmentId() {
            return fileAttachmentId;
        }

        public String getFieldDescription() {
            return fieldDescription;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFilename() {
            return filename;
        }

        @JsonProperty("itemValue")
        public String getValue() {
            return value;
        }

        public String getSlug() {
            return slug;
        }

        void setValue(final String value) {
            this.value = value;
        }
    }

    private String name;
    private String secretTemplateName;
    private String lastHeartBeatStatus;
    private Date lastHeartBeatCheck;
    private Date lastPasswordChangeAttempt;
    private int id;
    private int folderId;
    private int secretTemplateId;
    private int siteId;
    private boolean active;
    private boolean checkedOut;
    private boolean checkoutEnabled;
    
    // List of Field objects
    private List<Field> fields = new ArrayList<>();

    // Getters for the fields
    public String getName() {
        return name;
    }

    public String getSecretTemplateName() {
        return secretTemplateName;
    }

    public String getLastHeartBeatStatus() {
        return lastHeartBeatStatus;
    }

    public Date getLastHeartBeatCheck() {
        return new Date(lastHeartBeatCheck.getTime());
    }

    public Date getLastPasswordChangeAttempt() {
        return new Date(lastPasswordChangeAttempt.getTime());
    }

    public int getId() {
        return id;
    }

    public int getFolderId() {
        return folderId;
    }

    public int getSecretTemplateId() {
        return secretTemplateId;
    }

    public int getSiteId() {
        return siteId;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCheckedOut() {
        return checkedOut;
    }

    public boolean isCheckoutEnabled() {
        return checkoutEnabled;
    }

    @JsonProperty("items")
    public List<Field> getFields() {
        return Collections.unmodifiableList(fields); // Return an unmodifiable view
    }

    @Override
    public String toString() {
        return String.format("Secret { id: %d, folderId: %d, name: %s, templateName: %s }", 
                this.id, this.folderId, this.name, this.secretTemplateName);
    }
}
