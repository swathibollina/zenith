package com.pavan.zenith.catalog.domain.pricing;

// The persisted rule type: the small marker stored with an event's data that
// records which pricing rule the organizer picked. Only the factory may turn
// this marker into a concrete PricingRule object.
public enum PricingRuleType {
  FLAT,
  TIERED,
  DYNAMIC_DEMAND
}
