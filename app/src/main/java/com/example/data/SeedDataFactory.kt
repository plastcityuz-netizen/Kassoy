package com.example.data

object SeedDataFactory {

    data class SeedBundle(
        val companies: List<Pair<String, Company>>,
        val transactions: List<BusinessTransaction>,
        val inventory: List<InventoryItem>,
        val production: List<ProductionOrder>,
        val employees: List<EmployeeRecord>,
        val debts: List<DebtRecord>
    )

    fun createUzbekDemoBundle(primaryCompanyId: String, primaryCompany: Company): SeedBundle {
        val secondaryCompanyId = "comp_baraka_savdo"
        val secondaryCompany = Company(
            userId = primaryCompany.userId,
            name = "Baraka Savdo Markazi MCHJ",
            stir = "309817442",
            taxRegime = TaxRegime.AYLANMA.key,
            industry = IndustryType.SAVDO.key,
            city = "Toshkent",
            qqsRate = 0.0,
            inpsRate = 1.0,
            incomeTaxRate = 4.0,
            cashBalanceUzs = 94_000_000L,
            bankBalanceUzs = 215_000_000L
        )

        val transactions = listOf(
            // Primary Company
            BusinessTransaction(
                companyId = primaryCompanyId,
                type = TransactionType.SALE.key,
                category = "Ulgurji savdo",
                counterparty = "Samarqand Savdo Uyi MCHJ",
                amountUzs = 248_000_000L,
                status = TxStatus.PAID.key,
                dateDisplay = "21.11.2026",
                note = "Eksport partiyasi #104"
            ),
            BusinessTransaction(
                companyId = primaryCompanyId,
                type = TransactionType.SALE.key,
                category = "Chakana va B2B",
                counterparty = "Chorsu Tekstil Savdo",
                amountUzs = 142_500_000L,
                status = TxStatus.PENDING.key,
                dateDisplay = "19.11.2026",
                note = "Shartnoma #88"
            ),
            BusinessTransaction(
                companyId = primaryCompanyId,
                type = TransactionType.SALE.key,
                category = "Ulgurji savdo",
                counterparty = "Andijon Market Network",
                amountUzs = 195_000_000L,
                status = TxStatus.PAID.key,
                dateDisplay = "15.11.2026",
                note = "Qishki kolleksiya"
            ),
            BusinessTransaction(
                companyId = primaryCompanyId,
                type = TransactionType.PURCHASE.key,
                category = "Xomashyo xaridi",
                counterparty = "Farg‘ona Paxta Tolasi AJ",
                amountUzs = 164_000_000L,
                status = TxStatus.PAID.key,
                dateDisplay = "18.11.2026",
                note = "Ip va xomashyo"
            ),
            BusinessTransaction(
                companyId = primaryCompanyId,
                type = TransactionType.EXPENSE.key,
                category = "Logistika va yetkazish",
                counterparty = "Vodiy Trans Ekspress",
                amountUzs = 42_000_000L,
                status = TxStatus.PAID.key,
                dateDisplay = "16.11.2026",
                note = "Toshkent-Farg‘ona reyslari"
            ),
            BusinessTransaction(
                companyId = primaryCompanyId,
                type = TransactionType.PAYROLL.key,
                category = "Ish haqi fondi",
                counterparty = "Xodimlar oyligi (Noyabr)",
                amountUzs = 78_500_000L,
                status = TxStatus.PAID.key,
                dateDisplay = "10.11.2026",
                note = "Avans va asosiy qism"
            ),
            BusinessTransaction(
                companyId = primaryCompanyId,
                type = TransactionType.EXPENSE.key,
                category = "Energiya va kommunal",
                counterparty = "Hududiy Elektr Tarmoqlari",
                amountUzs = 28_400_000L,
                status = TxStatus.PAID.key,
                dateDisplay = "12.11.2026",
                note = "Sex elektr energiyasi"
            ),
            BusinessTransaction(
                companyId = primaryCompanyId,
                type = TransactionType.EXPENSE.key,
                category = "Marketing va reklama",
                counterparty = "Yangi Digital Agency",
                amountUzs = 19_600_000L,
                status = TxStatus.OVERDUE.key,
                dateDisplay = "08.11.2026",
                note = "B2B katalog targ‘iboti"
            ),
            // Secondary Company
            BusinessTransaction(
                companyId = secondaryCompanyId,
                type = TransactionType.SALE.key,
                category = "Chakana savdo",
                counterparty = "Toshkent xaridorlari",
                amountUzs = 310_000_000L,
                status = TxStatus.PAID.key,
                dateDisplay = "20.11.2026",
                note = "Terminal va Payme tushumlari"
            ),
            BusinessTransaction(
                companyId = secondaryCompanyId,
                type = TransactionType.PURCHASE.key,
                category = "Tovar xaridi",
                counterparty = "Navoiy Ulgurji Ombor",
                amountUzs = 185_000_000L,
                status = TxStatus.PAID.key,
                dateDisplay = "17.11.2026",
                note = "Yangi partiya"
            )
        )

        val inventory = listOf(
            InventoryItem(
                companyId = primaryCompanyId,
                sku = "RAW-IP-301",
                name = "Paxta ipi 30/1 (Birinchi nav)",
                category = "Xomashyo",
                quantity = 4_200L,
                unit = "kg",
                unitCostUzs = 36_500L,
                minThreshold = 1_500L
            ),
            InventoryItem(
                companyId = primaryCompanyId,
                sku = "RAW-BOYOQ-04",
                name = "Reaktiv bo‘yoq (Zumrad)",
                category = "Kimyo",
                quantity = 180L,
                unit = "litr",
                unitCostUzs = 145_000L,
                minThreshold = 250L
            ),
            InventoryItem(
                companyId = primaryCompanyId,
                sku = "FG-MATO-240",
                name = "Suprem trikotaj mato 240g",
                category = "Tayyor mahsulot",
                quantity = 2_850L,
                unit = "metr",
                unitCostUzs = 48_000L,
                minThreshold = 800L
            ),
            InventoryItem(
                companyId = primaryCompanyId,
                sku = "PKG-QUTI-12",
                name = "Eksport gofra qutisi",
                category = "Qadoq",
                quantity = 320L,
                unit = "dona",
                unitCostUzs = 11_500L,
                minThreshold = 500L
            ),
            InventoryItem(
                companyId = secondaryCompanyId,
                sku = "TRD-SKU-90",
                name = "Maishiy texnika to‘plami A+",
                category = "Savdo tovari",
                quantity = 95L,
                unit = "dona",
                unitCostUzs = 1_450_000L,
                minThreshold = 25L
            )
        )

        val production = listOf(
            ProductionOrder(
                companyId = primaryCompanyId,
                productName = "Suprem mato 240g (Qora)",
                bomSummary = "Paxta ipi 30/1 (0.28 kg) + Bo‘yoq (0.04 L)",
                targetQty = 2_000L,
                completedQty = 1_480L,
                unitCostUzs = 38_400L,
                defectPercent = 1.4,
                status = ProductionStatus.IN_PROGRESS.key
            ),
            ProductionOrder(
                companyId = primaryCompanyId,
                productName = "Futer 3-ipli (Kulrang)",
                bomSummary = "Paxta ipi 20/1 (0.42 kg) + Elastan (0.03 kg)",
                targetQty = 1_200L,
                completedQty = 1_200L,
                unitCostUzs = 54_200L,
                defectPercent = 0.9,
                status = ProductionStatus.COMPLETED.key
            ),
            ProductionOrder(
                companyId = primaryCompanyId,
                productName = "Bolalar futbolka to‘plami",
                bomSummary = "Trikotaj mato (0.65 m) + Furnitura",
                targetQty = 3_500L,
                completedQty = 420L,
                unitCostUzs = 29_500L,
                defectPercent = 1.8,
                status = ProductionStatus.PLANNED.key
            )
        )

        val employees = listOf(
            EmployeeRecord(
                companyId = primaryCompanyId,
                fullName = "Dilshod Tursunov",
                roleKey = "CHIEF_ACCOUNTANT",
                department = "Buxgalteriya",
                phone = "+998 90 124 45 10",
                attendancePercent = 100,
                salaryUzs = 14_500_000L,
                status = EmployeeStatus.ACTIVE.key
            ),
            EmployeeRecord(
                companyId = primaryCompanyId,
                fullName = "Madina Alimova",
                roleKey = "SALES_MANAGER",
                department = "Sotuv bo‘limi",
                phone = "+998 91 550 88 21",
                attendancePercent = 96,
                salaryUzs = 11_200_000L,
                status = EmployeeStatus.ACTIVE.key
            ),
            EmployeeRecord(
                companyId = primaryCompanyId,
                fullName = "Javohir Qodirov",
                roleKey = "PRODUCTION_HEAD",
                department = "Ishlab chiqarish",
                phone = "+998 93 778 19 04",
                attendancePercent = 98,
                salaryUzs = 13_800_000L,
                status = EmployeeStatus.ACTIVE.key
            ),
            EmployeeRecord(
                companyId = primaryCompanyId,
                fullName = "Sardorbek Rustamov",
                roleKey = "WAREHOUSE",
                department = "Omborxona",
                phone = "+998 99 412 33 77",
                attendancePercent = 94,
                salaryUzs = 8_400_000L,
                status = EmployeeStatus.ACTIVE.key
            ),
            EmployeeRecord(
                companyId = primaryCompanyId,
                fullName = "Nilufar Xasanova",
                roleKey = "HR",
                department = "Kadrlar (HR)",
                phone = "+998 97 310 65 90",
                attendancePercent = 100,
                salaryUzs = 9_600_000L,
                status = EmployeeStatus.ACTIVE.key
            )
        )

        val debts = listOf(
            DebtRecord(
                companyId = primaryCompanyId,
                counterparty = "Chorsu Tekstil Savdo",
                direction = DebtDirection.RECEIVABLE.key,
                amountUzs = 28_400_000L,
                dueDateDisplay = "14.11.2026",
                overdueDays = 8,
                reminderSent = false
            ),
            DebtRecord(
                companyId = primaryCompanyId,
                counterparty = "Buxoro Mega Market",
                direction = DebtDirection.RECEIVABLE.key,
                amountUzs = 19_800_000L,
                dueDateDisplay = "17.11.2026",
                overdueDays = 5,
                reminderSent = false
            ),
            DebtRecord(
                companyId = primaryCompanyId,
                counterparty = "Namangan Ip-Kalava Zavodi",
                direction = DebtDirection.PAYABLE.key,
                amountUzs = 34_000_000L,
                dueDateDisplay = "28.11.2026",
                overdueDays = 0,
                reminderSent = false
            )
        )

        return SeedBundle(
            companies = listOf(
                primaryCompanyId to primaryCompany,
                secondaryCompanyId to secondaryCompany
            ),
            transactions = transactions,
            inventory = inventory,
            production = production,
            employees = employees,
            debts = debts
        )
    }
}
