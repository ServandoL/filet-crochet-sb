// Gives every pattern without a householdId the household it belongs to, before the
// household-aware API is deployed. Progress needs nothing: it belongs to a household through its
// pattern (same _id). PATTERNS_BCK and PROGRESS_BCK are manual backups and are left alone.
//
// Dry run by default; it prints what it would change. To write:
//
//   mongosh "$MONGO_APP_URL" --eval 'const HOUSEHOLD = "<id>"; const APPLY = true' scripts/backfill-household.js
//
// Safe to run again: it only touches patterns with no householdId.

if (typeof HOUSEHOLD === 'undefined') throw new Error('Set HOUSEHOLD to the household id.');
const apply = typeof APPLY !== 'undefined' && APPLY === true;
const householdId = ObjectId(HOUSEHOLD);
const crochet = db.getSiblingDB(typeof DATABASE !== 'undefined' ? DATABASE : 'CROCHET');

// The household must exist in household-api's database, or no one could reach the patterns.
const household = db.getSiblingDB('HOUSEHOLD').households.findOne({ _id: householdId, deletedAt: null });
if (!household) throw new Error(`No live household ${HOUSEHOLD} in HOUSEHOLD.households.`);

print(`${apply ? 'APPLYING' : 'DRY RUN'}: patterns without a household go to "${household.name}" (${HOUSEHOLD}),`);
print(`members ${household.members.map((m) => m.email).join(', ')}`);

const missing = { householdId: { $exists: false } };
const count = crochet.PATTERNS.countDocuments(missing);
if (apply && count > 0) crochet.PATTERNS.updateMany(missing, { $set: { householdId } });
const ids = crochet.PATTERNS.find({}, { _id: 1 }).toArray().map((p) => p._id);
print(`  patterns ${count} of ${crochet.PATTERNS.countDocuments()} ${apply ? 'updated' : 'to update'}`);
print(`  progress ${crochet.PROGRESS.countDocuments()}, ${crochet.PROGRESS.countDocuments({ _id: { $nin: ids } })} without a pattern`);
