import { adminAuditCollection } from "@/db/collections";
import type { AuditDocument } from "@/db/documents";

export interface AuditListRow {
  action: string;
  target: string;
  at: Date | null;
  ip: string | null;
}

export async function writeAudit(action: string, target: string, ip: string | null): Promise<void> {
  const entry: AuditDocument = {
    action,
    target,
    at: new Date(),
    ip,
  };
  await adminAuditCollection().insertOne(entry);
}

export async function listAudit(
  page: number,
  pageSize: number,
): Promise<[AuditListRow[], number]> {
  const collection = adminAuditCollection();
  const total = await collection.countDocuments({});
  const skip = Math.max(page - 1, 0) * pageSize;
  const docs = await collection
    .find({})
    .sort({ at: -1 })
    .skip(skip)
    .limit(pageSize)
    .toArray();

  const items: AuditListRow[] = docs.map((doc) => ({
    action: doc.action,
    target: doc.target,
    at: doc.at ?? null,
    ip: doc.ip,
  }));
  return [items, total];
}
