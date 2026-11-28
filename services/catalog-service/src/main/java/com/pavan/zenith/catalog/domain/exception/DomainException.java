package com.pavan.zenith.catalog.domain.exception;

// Shared parent of every broken-business-rule exception in this service.
// Abstract on purpose: a bare DomainException names no rule, so it can never
// be thrown directly - only subclasses whose names state the exact rule.
// Names and messages speak business language, never technology.
public abstract class DomainException extends RuntimeException {

  protected DomainException(final String message) {
    super(message);
  }
}
