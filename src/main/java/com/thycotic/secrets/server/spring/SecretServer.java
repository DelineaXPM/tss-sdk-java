package com.thycotic.secrets.server.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

/**
 * A <a href="https://spring.io/projects/spring-framework">Spring Framework</a>
 * <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html">RestTemplate</a>
 * with convenience methods specific to the Delinea Secret Server REST API.
 *
 * <p>Use the {@link SecretServerFactoryBean} to create and initialize it.
 */
public class SecretServer extends RestTemplate {
	private static final String SECRET_ID_URI = "/secrets/{id}";
	private static final String SECRET_FILE_ATTACHMENT_URI = SECRET_ID_URI + "/fields/{slug}";

	/**
	 * Fetch and return a {@link Secret} from Delinea Secret Server.
	 *
	 * @param id - the integer ID of the secret to be fetched
	 * @param fetchFileAttachments - whether to fetch {@code fileAttachments} so
	 *        {@link Secret.Field#getValue()} returns the contents instead of the default placeholder
	 * @return the {@link Secret} object
	 */
	public Secret getSecret(final int id, final boolean fetchFileAttachments) {
		final Map<String, String> params = new HashMap<String, String>();

		params.put("id", String.valueOf(id));

		final Secret secret = getForObject(SECRET_ID_URI, Secret.class, params);

		if (fetchFileAttachments) {
			secret.getFields().forEach(field -> {
				if (field.getFileAttachmentId() > 0) {
					params.put("slug", field.getSlug());
					field.setValue(getForEntity(SECRET_FILE_ATTACHMENT_URI, String.class, params).getBody());
				}
			});
		}
		return secret;
	}

	/**
	 * Fetch and return a {@link Secret} from Delinea Secret Server, including {@code fileAttachments}
	 * @see #getSecret(int, boolean)
	 *
	 * @param id - the integer ID of the secret to be fetched
	 * @return the {@link Secret} object
	 */
	public Secret getSecret(final int id) {
		return getSecret(id, true);
	}
}
