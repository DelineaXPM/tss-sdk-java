package com.delinea.server.spring;

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

        /**
         * Returns the item ID.
         * 
         * @return the unique ID of this field item
         */
        @JsonProperty("itemId")
        public int getId() {
            return id;
        }

        /**
         * Returns the field ID.
         * 
         * @return the unique ID of the field definition
         */
        public int getFieldId() {
            return fieldId;
        }

        /**
         * Returns the file attachment ID, if the field stores a file.
         * 
         * @return the file attachment ID
         */
        public int getFileAttachmentId() {
            return fileAttachmentId;
        }

        /**
         * Returns the field description.
         * 
         * @return the field description text
         */
        public String getFieldDescription() {
            return fieldDescription;
        }

        /**
         * Returns the field name.
         * 
         * @return the name of the field
         */
        public String getFieldName() {
            return fieldName;
        }

        /**
         * Returns the filename if this field represents a file.
         * 
         * @return the filename associated with this field
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Returns the value of the field.
         * 
         * @return the field value as a string
         */
        @JsonProperty("itemValue")
        public String getValue() {
            return value;
        }

        /**
         * Returns the slug identifier of this field.
         * 
         * @return the field slug
         */
        public String getSlug() {
            return slug;
        }

        /**
         * Sets the value of this field.
         * 
         * @param value the new value to assign
         */
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

    /**
     * Returns the name of the secret.
     * 
     * @return the secret name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the template name used for this secret.
     * 
     * @return the secret template name
     */
    public String getSecretTemplateName() {
        return secretTemplateName;
    }

    /**
     * Returns the most recent heartbeat status.
     * 
     * @return the last heartbeat status string
     */
    public String getLastHeartBeatStatus() {
        return lastHeartBeatStatus;
    }

    /**
     * Returns the timestamp of the last heartbeat check.
     * 
     * @return a {@link Date} of the last heartbeat check
     */
    public Date getLastHeartBeatCheck() {
        return new Date(lastHeartBeatCheck.getTime());
    }

    /**
     * Returns the date of the last password change attempt.
     * 
     * @return a {@link Date} of the last password change attempt
     */
    public Date getLastPasswordChangeAttempt() {
        return new Date(lastPasswordChangeAttempt.getTime());
    }

    /**
     * Returns the unique ID of the secret.
     * 
     * @return the secret ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the folder ID where this secret is stored.
     * 
     * @return the folder ID
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Returns the template ID associated with this secret.
     * 
     * @return the secret template ID
     */
    public int getSecretTemplateId() {
        return secretTemplateId;
    }

    /**
     * Returns the site ID related to this secret.
     * 
     * @return the site ID
     */
    public int getSiteId() {
        return siteId;
    }
    
    /**
     * Returns whether this secret is currently active.
     * 
     * @return true if active; false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns whether this secret is checked out.
     * 
     * @return true if checked out; false otherwise
     */
    public boolean isCheckedOut() {
        return checkedOut;
    }

    /**
     * Returns whether the secret supports checkout.
     * 
     * @return true if checkout is enabled; false otherwise
     */
    public boolean isCheckoutEnabled() {
        return checkoutEnabled;
    }

    /**
     * Returns an unmodifiable list of all field items for this secret.
     * 
     * @return an unmodifiable {@link List} of {@link Field} objects
     */
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
