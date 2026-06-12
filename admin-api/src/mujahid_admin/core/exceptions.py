from __future__ import annotations


class BotError(Exception):
    """Bot control plane request failed."""


class BotNotFoundError(BotError):
    """Requested bot resource was not found."""
