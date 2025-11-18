import pool from '../db.js';

export async function findByIdentifier(identifier) {
  const sql = `
    SELECT *
    FROM users
    WHERE email = :identifier OR username = :identifier
    LIMIT 1
  `;
  const [rows] = await pool.execute(sql, { identifier });
  return rows[0] || null;
}

export async function createUser({ username, email, passwordHash, name, lastName }) {
  const sql = `
    INSERT INTO users (username, email, password, bio, failed_login_attempts)
    VALUES (:username, :email, :passwordHash, :bio, 0)
  `;
  await pool.execute(sql, {
    username,
    email,
    passwordHash,
    bio: buildBio(name, lastName)
  });
}

export async function updateLoginState(userId, { failedLoginAttempts, lockoutEndTime }) {
  const sql = `
    UPDATE users
    SET failed_login_attempts = :failedLoginAttempts,
        lockout_end_time = :lockoutEndTime
    WHERE id = :userId
  `;
  await pool.execute(sql, {
    failedLoginAttempts,
    lockoutEndTime,
    userId
  });
}

export async function setResetCode(userId, code, expiresAt) {
  const sql = `
    UPDATE users
    SET reset_code = :code,
        reset_code_expires_at = :expiresAt
    WHERE id = :userId
  `;
  await pool.execute(sql, { code, expiresAt, userId });
}

export async function updatePassword(userId, passwordHash) {
  const sql = `
    UPDATE users
    SET password = :passwordHash,
        reset_code = NULL,
        reset_code_expires_at = NULL
    WHERE id = :userId
  `;
  await pool.execute(sql, { passwordHash, userId });
}

export async function insertLoginHistory(entry) {
  const sql = `
    INSERT INTO login_history (username, ip_address, status, user_agent)
    VALUES (:username, :ipAddress, :status, :userAgent)
  `;
  await pool.execute(sql, entry);
}

function buildBio(name, lastName) {
  if (!name && !lastName) {
    return 'New user';
  }
  return `Xin chào, tôi là ${[name, lastName].filter(Boolean).join(' ')}`.trim();
}

