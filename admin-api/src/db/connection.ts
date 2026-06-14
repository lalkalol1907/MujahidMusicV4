import { MongoClient, type Db } from "mongodb";
import { settings } from "../core/config";

let client: MongoClient | null = null;

export function getClient(): MongoClient {
  if (!client) {
    client = new MongoClient(settings.mongoUri);
  }
  return client;
}

export function getDb(): Db {
  return getClient().db(settings.mongoDb);
}

export async function pingMongo(): Promise<boolean> {
  try {
    await getClient().db("admin").command({ ping: 1 });
    return true;
  } catch {
    return false;
  }
}

export async function closeMongo(): Promise<void> {
  if (client) {
    await client.close();
    client = null;
  }
}
