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

        /** Returns the item ID */
        @JsonProperty("itemId")
        public int getId() {
            return id;
        }

        /** Returns the field ID */
        public int getFieldId() {
            return fieldId;
        }

        /** Returns file attachment ID */
        public int getFileAttachmentId() {
            return fileAttachmentId;
        }

        /** Returns field description */
        public String getFieldDescription() {
            return fieldDescription;
        }

        /** Returns field name */
        public String getFieldName() {
            return fieldName;
        }
        
        /** Returns filename */
        public String getFilename() {
            return filename;
        }

        /** Returns the field value */
        @JsonProperty("itemValue")
        public String getValue() {
            return value;
        }

        /** Returns the slug */
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

    /** Returns the secret name */
    public String getName() {
        return name;
    }

    /** Returns the template name */
    public String getSecretTemplateName() {
        return secretTemplateName;
    }

    /** Returns last heartbeat status */
    public String getLastHeartBeatStatus() {
        return lastHeartBeatStatus;
    }

    /** Returns last heartbeat check date */
    public Date getLastHeartBeatCheck() {
        return new Date(lastHeartBeatCheck.getTime());
    }

    /** Returns last password change attempt */
    public Date getLastPasswordChangeAttempt() {
        return new Date(lastPasswordChangeAttempt.getTime());
    }

    /** Returns secret ID */
    public int getId() {
        return id;
    }

    /** Returns folder ID */
    public int getFolderId() {
        return folderId;
    }

    /** Returns template ID */
    public int getSecretTemplateId() {
        return secretTemplateId;
    }

    /** Returns site ID */
    public int getSiteId() {
        return siteId;
    }

    /** Returns whether the secret is active */
    public boolean isActive() {
        return active;
    }

    /** Returns whether the secret is checked out */
    public boolean isCheckedOut() {
        return checkedOut;
    }

    /** Returns whether checkout is enabled */
    public boolean isCheckoutEnabled() {
        return checkoutEnabled;
    }

    /** Returns an unmodifiable list of fields */
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
