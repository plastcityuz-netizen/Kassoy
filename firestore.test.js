const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uz_owner";
const BOB_UID = "bob_other_owner";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any BALANS AI collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("companies").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      companyId: "comp_1",
      type: "SALE",
      category: "Ulgurji savdo",
      counterparty: "Baraka Market MCHJ",
      amountUzs: 12500000,
      status: "PAID",
      dateDisplay: "22.11.2026",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Owner can create & partially update Workspace, Company, and Transactions (Kotlin-mirrored)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Azizbek Karimov",
      email: "azizbek@balans.uz",
      activeCompanyId: "comp_1",
      activeRole: "OWNER",
      planId: "premium",
      billingCycle: "monthly",
      subscriptionStatus: "trial",
      trialDaysRemaining: 23,
      aiQuestionsUsed: 4,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("companies").doc("comp_1").set({
      userId: ALICE_UID,
      name: "Oltin Vodiy Tekstil MCHJ",
      stir: "305412890",
      taxRegime: "QQS",
      industry: "ISHLAB_CHIQARISH",
      city: "Farg'ona",
      qqsRate: 12.0,
      inpsRate: 1.0,
      incomeTaxRate: 12.0,
      cashBalanceUzs: 185000000,
      bankBalanceUzs: 420000000,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      companyId: "comp_1",
      type: "SALE",
      category: "Eksport savdo",
      counterparty: "Samarqand Savdo Uy",
      amountUzs: 84000000,
      status: "PENDING",
      dateDisplay: "22.11.2026",
      note: "Shartnoma #42",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").update({
      status: "PAID",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Ghost field & @DocumentId 'id' injection rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_2").set({
      id: "tx_2",
      userId: ALICE_UID,
      companyId: "comp_1",
      type: "SALE",
      category: "Savdo",
      counterparty: "Test",
      amountUzs: 100000,
      status: "PAID",
      dateDisplay: "22.11.2026",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Invalid STIR rejected (must be 9 digits)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("companies").doc("comp_bad").set({
      userId: ALICE_UID,
      name: "Bad STIR MCHJ",
      stir: "12345",
      taxRegime: "QQS",
      industry: "SAVDO",
      city: "Toshkent",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Cross-user isolation blocks Bob from reading or modifying Alice's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const past = new Date(Date.now() - 10000);
    await db.collection("users").doc(ALICE_UID).collection("debts").doc("debt_1").set({
      userId: ALICE_UID,
      companyId: "comp_1",
      counterparty: "Nur Savdo",
      direction: "RECEIVABLE",
      amountUzs: 16200000,
      dueDateDisplay: "18.11.2026",
      overdueDays: 4,
      reminderSent: false,
      createdAt: past,
      updatedAt: past,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("debts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("debts").doc("debt_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("debts").doc("debt_1").update({
      reminderSent: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Terminal state locking prevents modifying COMPLETED production orders & audit logs are append-only", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("production").doc("prod_1").set({
      userId: ALICE_UID,
      companyId: "comp_1",
      productName: "Paxta mato 240g",
      bomSummary: "Ip 30/1 (85%), Bo'yoq (10%)",
      targetQty: 500,
      completedQty: 500,
      unitCostUzs: 42000,
      defectPercent: 1.2,
      status: "COMPLETED",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Completed production order cannot be mutated
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("production").doc("prod_1").update({
      completedQty: 400,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Audit logs can be created, never updated or deleted
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("auditLogs").doc("log_1").set({
      userId: ALICE_UID,
      companyId: "comp_1",
      actorRole: "OWNER",
      actionTitle: "Yangi sotuv kiritildi",
      moduleKey: "SALES",
      amountUzs: 12000000,
      createdAt: past,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("auditLogs").doc("log_1").delete()
  );
});
