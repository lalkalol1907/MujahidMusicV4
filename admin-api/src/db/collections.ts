import type { Collection } from "mongodb";
import type { AuditDocument, PlaylistDocument } from "@/db/documents";
import { getDb } from "@/db/connection";

export function playlistsCollection(): Collection<PlaylistDocument> {
  return getDb().collection<PlaylistDocument>("playlists");
}

export function adminAuditCollection(): Collection<AuditDocument> {
  return getDb().collection<AuditDocument>("admin_audit");
}
