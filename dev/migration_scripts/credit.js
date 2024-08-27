const mysql = require('mysql');

const dbConfig = {
    host: '<>',
    user: '<>',
    password: '<>',
    database: '<>',
    port: '<>'
};

async function connectToDatabase() {
    const connection = mysql.createConnection(dbConfig);

    return new Promise((resolve, reject) => {
        connection.connect((err) => {
            if (err) {
                console.error('Error connecting to MySQL database:', err);
                reject(err);
                return;
            }
            console.log('Connected to MySQL database!');
            resolve(connection);
        });
    });
}

const planCredit = new Map();

planCredit.set("solo-3-USD-Yearly", 7);
planCredit.set("startup-1-USD-Monthly", 6);
planCredit.set("startup-1-USD-Yearly", 5);
planCredit.set("business-3-USD-Monthly", 4);
planCredit.set("business-3-USD-Yearly", 3);
planCredit.set("tier-1-USD-lifetime", 2);
planCredit.set("tier-2-USD-lifetime", 1);
planCredit.set("tier-3-USD-lifetime", 9);
planCredit.set("tier-4-USD-lifetime", 10);
planCredit.set("tier-5-USD-lifetime", 11);

async function processAllData() {
    const conn = await connectToDatabase();
    try {
        const orgs = await new Promise((resolve, reject) => {
            conn.query(`SELECT *
                        FROM org`, (queryErr, results) => {
                if (queryErr) reject(queryErr);
                resolve(results);
            });
        });

        let currentOrg = 1;
        const totalOrgs = orgs.length;
        for (const org of orgs) {

            console.log(`${currentOrg++}/${totalOrgs}`);
            const subs = await new Promise((resolve, reject) => {
                conn.query(`SELECT *
                            FROM subscriptions
                            WHERE org_id = ${org.id}`, (queryErr, results) => {
                    if (queryErr) reject(queryErr);
                    resolve(results);
                });
            });

            const entityConfigs = await new Promise((resolve, reject) => {
                conn.query(`SELECT *
                            FROM entity_config_kv
                            WHERE entity_id = ${org.id}`, (queryErr, results) => {
                    if (queryErr) reject(queryErr);
                    resolve(results);
                });
            });

            let isCreditConfigExists = false;
            for (const entityConfig of entityConfigs) {
                if (entityConfig.config_key === 'FABLE_GIVE_CREDIT' || entityConfig.config_key === 'TOPUP_CREDIT') {
                    isCreditConfigExists = true;
                }
            }

            if (!isCreditConfigExists) {
                const creditValue = planCredit.get(subs.get_payment_plan_id)
                await new Promise((resolve, reject) => {
                    conn.query(`INSERT INTO entity_config_kv (created_at, updated_at, entity_id, entity_type,
                                                              config_type,
                                                              config_key,
                                                              config_val)
                                VALUES (NOW(), NOW(), ${org.id}, 'Org', 3, 'FABLE_GIVEN_CREDIT',
                                        '${JSON.stringify({
                                            value: creditValue,
                                            updatedAt: +new Date()
                                        })}'),
                                       (NOW(), NOW(), ${org.id}, 'Org', 3, 'TOPUP_CREDIT',
                                        '${JSON.stringify({
                                            value: 0,
                                            updatedAt: +new Date()
                                        })}
                                        ')`, (queryErr, results) => {
                        if (queryErr) reject(queryErr);
                        resolve(results);
                    });
                });
            }
        }
    } finally {
        conn.end();
    }
}

processAllData().catch(err => console.error('Error processing data:', err));