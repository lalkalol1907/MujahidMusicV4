import { getDb } from "../db/connection";

export async function writeAudit(action: string, target: string, ip: string | null): Promise<void> {
  await getDb().collection("admin_audit").insertOne({
    action,
    target,
    at: new Date(),
    ip,
  });
}

export async function listAudit(
  page: number,
  pageSize: number,
): Promise<
  [{ action: string; target: string; at: Date | null; ip: string | null }[], number]
> {
  const db = getDb();
  const total = await db.collection("admin_audit").countDocuments({});
  const skip = Math.max(page - 1, 0) * pageSize;
  const docs = await db
    .collection("admin_audit")
    .find({})
    .sort({ at: -1 })
    .skip(skip)
    .limit(pageSize)
    .toArray();

  const items = docs.map((doc) => ({
    action: doc.action as string,
    target: doc.target as string,
    at: (doc.at as Date | undefined) ?? null,
    ip: (doc.ip as string | undefined) ?? null,
  }));
  return [items, total];
}
