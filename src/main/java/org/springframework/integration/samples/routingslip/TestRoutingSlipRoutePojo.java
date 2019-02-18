package org.springframework.integration.samples.routingslip;

import org.springframework.messaging.Message;

public class TestRoutingSlipRoutePojo {

		final String[] channels = { "lowercase", "capitalize" };

		private int i = 0;

		public String get(Message<?> requestMessage, Object reply) {
			try {
				return this.channels[i++];
			}
			catch (Exception e) {
				return null;
			}
		}

	}