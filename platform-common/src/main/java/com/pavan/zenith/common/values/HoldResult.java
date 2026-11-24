package com.pavan.zenith.common.values;

public sealed interface HoldResult permits Granted, Rejected, PartiallyGranted {}
