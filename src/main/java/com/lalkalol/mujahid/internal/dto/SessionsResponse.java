package com.lalkalol.mujahid.internal.dto;

import java.util.List;

public record SessionsResponse(List<NodeDto> nodes, List<SessionDto> sessions) {
}
