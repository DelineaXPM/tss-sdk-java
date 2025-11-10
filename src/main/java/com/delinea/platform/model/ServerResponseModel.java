package com.delinea.platform.model;

import lombok.Data;

/**
* Represents the health and operational status of a Delinea platform server.
* <p>
* This model is typically used to communicate the results of a health check
* or status endpoint, providing insights into the condition of key system components.
* </p>
* <p>Uses Lombok's {@link Data} annotation to automatically generate
* getters, setters, {@code toString()}, {@code equals()}, and {@code hashCode()} methods.</p>
*/
@Data
public class ServerResponseModel {
	private boolean healthy;
	private boolean databaseHealthy;
	private boolean serviceBusHealthy;
	private boolean storageAccountHealthy;
	private boolean scheduledForDeletion;
}