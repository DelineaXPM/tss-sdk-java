package com.delinea.secrets.server.spring;

import lombok.Data;

@Data
public class ServerResponseModel {
	private boolean healthy;
	private boolean databaseHealthy;
	private boolean serviceBusHealthy;
	private boolean storageAccountHealthy;
	private boolean scheduledForDeletion;
}