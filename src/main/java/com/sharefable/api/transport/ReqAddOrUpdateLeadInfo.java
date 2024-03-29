package com.sharefable.api.transport;

import com.sharefable.api.common.LeadInfoKey;

public record ReqAddOrUpdateLeadInfo(
  Long tourId,
  String emailId,
  String value,
  LeadInfoKey key
) {
}
